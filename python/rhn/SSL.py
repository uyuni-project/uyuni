#  pylint: disable=invalid-name
#
# Higher-level SSL objects used by rpclib
#
# Copyright (c) 2002--2017 Red Hat, Inc.
#
# Author: Mihai Ibanescu <misa@redhat.com>
#
# In addition, as a special exception, the copyright holders give
# permission to link the code of portions of this program with the
# OpenSSL library under certain conditions as described in each
# individual source file, and distribute linked combinations
# including the two.
# You must obey the GNU General Public License in all respects
# for all of the code used other than OpenSSL.  If you modify
# file(s) with this exception, you may extend this exception to your
# version of the file(s), but you are not obligated to do so.  If you
# do not wish to do so, delete this exception statement from your
# version.  If you delete this exception statement from all source
# files in the program, then also delete it here.


"""
rhn.SSL builds an abstraction on top of the objects provided by pyOpenSSL
"""

# SSL.crypto is provided to other modules
# pylint: disable-next=unused-import
from OpenSSL import crypto
import ssl as SSL
import os

import socket
import select
from rhn.stringutils import bstr
import sys

DEFAULT_TIMEOUT = 120

if hasattr(socket, "sslerror"):
    socket_error = socket.sslerror
else:
    # pylint: disable-next=unused-import
    from ssl import socket_error

try:
    # pylint: disable-next=unused-import
    from ssl import CertificateError
except ImportError:
    # python 2.6
    # pylint: disable-next=unused-import
    from backports.ssl_match_hostname import match_hostname, CertificateError


class SSLSocket:
    """
    Class that wraps a pyOpenSSL Connection object, adding more methods
    """

    # pylint: disable-next=redefined-outer-name
    def __init__(self, socket, trusted_certs=None):
        # SSL.Context object
        self._ctx = None
        # SSL.Connection object
        self._connection = None
        self._sock = socket
        self._trusted_certs = []
        # convert None to empty list
        trusted_certs = trusted_certs or []
        for f in trusted_certs:
            self.add_trusted_cert(f)

        # Buffer size for reads
        self._buffer_size = 8192

        # Position, for tell()
        self._pos = 0
        # Buffer
        self._buffer = bstr("")

        # Flag to show if makefile() was called
        self._makefile_called = 0

        self._closed = None

    def add_trusted_cert(self, file):
        """
        Adds a trusted certificate to the certificate store of the SSL context
        object.
        """
        if not os.access(file, os.R_OK):
            # pylint: disable-next=consider-using-f-string
            raise ValueError("Unable to read certificate file %s" % file)
        self._trusted_certs.append(file.encode("utf-8"))

    def init_ssl(self, server_name=None):
        """
        Initializes the SSL connection.
        """
        self._check_closed()
        if hasattr(SSL, "PROTOCOL_TLS_CLIENT"):
            self._ctx = SSL.SSLContext(SSL.PROTOCOL_TLS_CLIENT)
            self._ctx.options |= SSL.OP_NO_TLSv1
            self._ctx.options |= SSL.OP_NO_TLSv1_1
            self._ctx.verify_mode = SSL.CERT_REQUIRED
            self._ctx.check_hostname = True
            self._ctx.load_default_certs(SSL.Purpose.SERVER_AUTH)
            if self._trusted_certs:
                # We have been supplied with trusted CA certs
                for f in self._trusted_certs:
                    self._ctx.load_verify_locations(f)
            self._connection = self._ctx.wrap_socket(
                self._sock, server_hostname=server_name
            )
        else:
            # This needs to be kept for old traditional clients
            # SSL method to use
            if hasattr(SSL, "PROTOCOL_TLS"):
                self._ssl_method = SSL.PROTOCOL_TLS
            else:
                self._ssl_method = SSL.PROTOCOL_SSLv23

            if hasattr(SSL, "SSLContext"):
                self._ctx = SSL.SSLContext(self._ssl_method)
                self._ctx.verify_mode = SSL.CERT_REQUIRED
                self._ctx.check_hostname = True
                self._ctx.load_default_certs(SSL.Purpose.SERVER_AUTH)
                if self._trusted_certs:
                    # We have been supplied with trusted CA certs
                    for f in self._trusted_certs:
                        self._ctx.load_verify_locations(f)

                # pylint: disable-next=deprecated-method
                self._connection = self._ctx.wrap_socket(
                    self._sock, server_hostname=server_name
                )
            else:
                # Python 2.6-2.7.8
                cacert = None
                if self._trusted_certs:
                    # seems python2.6 supports only 1
                    cacert = self._trusted_certs[0]
                # pylint: disable-next=deprecated-method
                self._connection = SSL.wrap_socket(
                    self._sock,
                    ssl_version=self._ssl_method,
                    cert_reqs=SSL.CERT_REQUIRED,
                    ca_certs=cacert,
                )
                # pylint: disable-next=used-before-assignment
                match_hostname(self._connection.getpeercert(), server_name)

    # pylint: disable-next=unused-argument
    def makefile(self, mode, bufsize=None):
        """
        Returns self, since we are a file-like object already
        """
        if bufsize:
            self._buffer_size = bufsize

        # Increment the counter with the number of times we've called makefile
        # - we don't want close to actually close things until all the objects
        # that originally called makefile() are gone
        self._makefile_called = self._makefile_called + 1
        return self

    def close(self):
        """
        Closes the SSL connection
        """
        # XXX Normally sock.makefile does a dup() on the socket file
        # descriptor; httplib relies on this, but there is no dup for an ssl
        # connection; so we have to count how may times makefile() was called
        if self._closed:
            # Nothing to do
            return
        if not self._makefile_called:
            self._really_close()
            return
        self._makefile_called = self._makefile_called - 1

    # BZ 1464157 - Python 3 http attempts to call this method during close,
    # at least add it empty
    def flush(self):
        pass

    def _really_close(self):
        # No connection was established
        if self._connection is None:
            return
        self._connection.close()
        self._closed = 1

    def _check_closed(self):
        if self._closed:
            raise ValueError("I/O operation on closed file")

    def __getattr__(self, name):
        if hasattr(self._connection, name):
            return getattr(self._connection, name)
        raise AttributeError(name)

    # File methods
    def isatty(self):
        """
        Returns false always.
        """
        return 0

    def tell(self):
        return self._pos

    def seek(self, pos, mode=0):
        raise NotImplementedError("seek")

    def read(self, amt=None):
        """
        Reads up to amt bytes from the SSL connection.
        """
        self._check_closed()
        # Initially, the buffer size is the default buffer size.
        # Unfortunately, pending() does not return meaningful data until
        # recv() is called, so we only adjust the buffer size after the
        # first read
        buffer_size = self._buffer_size

        buffer_length = len(self._buffer)
        # Read only the specified amount of data
        while buffer_length < amt or amt is None:
            # if amt is None (read till the end), fills in self._buffer
            if amt is not None:
                buffer_size = min(amt - buffer_length, buffer_size)

            try:
                data = self._connection.recv(buffer_size)

                self._buffer = self._buffer + data
                buffer_length = len(self._buffer)

                # More bytes to read?
                pending = self._connection.pending()
                if pending == 0 and buffer_length == amt:
                    # we're done here
                    break
            except SSL.SSLError as err:
                if err.args[0] == SSL.SSL_ERROR_ZERO_RETURN:
                    # Nothing more to be read
                    break
                elif err.args[0] == SSL.SSL_ERROR_SYSCALL:
                    e = sys.exc_info()[1]
                    print("SSL exception", e.args)
                    break
                elif err.args[0] == SSL.SSL_ERROR_WANT_WRITE:
                    self._poll(select.POLLOUT, "read")
                elif err.args[0] == SSL.SSL_ERROR_WANT_READ:
                    self._poll(select.POLLIN, "read")

        if amt:
            ret = self._buffer[:amt]
            self._buffer = self._buffer[amt:]
        else:
            ret = self._buffer
            self._buffer = bstr("")

        self._pos = self._pos + len(ret)
        return ret

    def readinto(self, buf):
        buf[:] = self.read(len(buf))
        return len(buf)

    def _poll(self, filter_type, caller_name):
        poller = select.poll()
        poller.register(self._sock, filter_type)
        res = poller.poll(self._sock.gettimeout() * 1000)
        if res == []:
            # pylint: disable-next=consider-using-f-string
            raise TimeoutException("Connection timed out on %s" % caller_name)

    def write(self, data):
        """
        Writes to the SSL connection.
        """
        self._check_closed()

        # XXX Should use sendall
        # sent = self._connection.sendall(data)
        origlen = len(data)
        while True:
            try:
                sent = self._connection.send(data)
                if sent == len(data):
                    break
                data = data[sent:]
            except SSL.SSLError as err:
                if err.args[0] == SSL.SSL_ERROR_WANT_WRITE:
                    self._poll(select.POLLOUT, "write")
                elif err.args[0] == SSL.SSL_ERROR_WANT_READ:
                    self._poll(select.POLLIN, "write")

        return origlen

    def recv(self, amt):
        return self.read(amt)

    send = write

    sendall = write

    def readline(self, length=None):
        """
        Reads a single line (up to `length' characters long) from the SSL
        connection.
        """
        self._check_closed()
        while True:
            # charcount contains the number of chars to be outputted (or None
            # if none to be outputted at this time)
            charcount = None
            i = self._buffer.find(bstr("\n"))
            if i >= 0:
                # Go one char past newline
                charcount = i + 1
            elif length and len(self._buffer) >= length:
                charcount = length

            if charcount is not None:
                ret = self._buffer[:charcount]
                self._buffer = self._buffer[charcount:]
                self._pos = self._pos + len(ret)
                return ret

            # Determine the number of chars to be read next
            bufsize = self._buffer_size
            if length:
                # we know length > len(self._buffer)
                bufsize = min(self._buffer_size, length - len(self._buffer))

            try:
                data = self._connection.recv(bufsize)
                if len(data) == 0:
                    break
                self._buffer = self._buffer + data
            except SSL.SSLError as err:
                if err.args[0] == SSL.SSL_ERROR_ZERO_RETURN:
                    # Nothing more to be read
                    break
                elif err.args[0] == SSL.SSL_ERROR_WANT_WRITE:
                    self._poll(select.POLLOUT, "readline")
                elif err.args[0] == SSL.SSL_ERROR_WANT_READ:
                    self._poll(select.POLLIN, "readline")

        # We got here if we're done reading, so return everything
        ret = self._buffer
        self._buffer = ""
        self._pos = self._pos + len(ret)
        return ret


class TimeoutException(SSL.SSLError, socket.timeout):
    def __init__(self, *args):
        self.args = args

    def __str__(self):
        return "Timeout Exception"
