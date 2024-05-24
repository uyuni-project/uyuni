#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2008--2016 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
#

import os
import sys
import time
import traceback
import socket

try:
    #  python 2
    from StringIO import StringIO
except ImportError:
    #  python3
    from io import StringIO
from rhn.connections import idn_puny_to_unicode

from spacewalk.common.rhnConfig import CFG, PRODUCT_NAME
from spacewalk.common.rhnLog import log_error
from spacewalk.common.rhnTranslate import _
from spacewalk.common import rhnMail
from spacewalk.common import rhnFlags

# Get the hostname for traceback use
hostname = socket.gethostname()

# Keep QUIET_MAIL in a global variable that is initialized from CFG
# when it is first needed. It controls the maximum number of
# tracebacks we're willing to send out of this process in order to
# avoid a huge flood of mail requests.
QUIET_MAIL = None


def print_env(fd=sys.stderr):
    """Dump the environment."""
    fd.write(f"\nEnvironment for PID={os.getpid()} on exception:\n")
    for key, value in sorted(os.environ.items()):
        fd.write(f"{key} = {value}\n")


def print_locals(fd=sys.stderr, tb=None):
    """Dump a listing of all local variables and their value for better debugging
    chance.
    """
    if tb is None:
        tb = sys.exc_info()[2]
    stack = []
    # walk the traceback to the end
    while 1:
        if not tb.tb_next:
            break
        tb = tb.tb_next
    # and now start extracting the stack frames
    f = tb.tb_frame
    while f:
        stack.append(f)
        f = f.f_back
    fd.write("\nLocal variables by frame\n")
    for frame in stack:
        fd.write(
            f"Frame {frame.f_code.co_name} in {frame.f_code.co_filename} in line {frame.f_lineno}\n"
        )
        for key, value in frame.f_locals.items():
            fd.write(f"\t{key:>20} = ")
            # We have to be careful not to cause a new error in our error
            # printer! Calling str() on an unknown object could cause an
            # error we don't want.
            try:
                s = str(value)
            # pylint: disable-next=broad-exception-caught
            except Exception:
                s = "<ERROR WHILE PRINTING VALUE>"
            if len(s) > 100 * 1024:
                s = "<ERROR WHILE PRINTING VALUE: string representation too large>"
            fd.write(f"{type(value)} s\n")
        fd.write("\n")


def print_req(req, fd=sys.stderr):
    """get some debugging information about the current exception for sending
    out when we raise an exception
    """

    fd.write("Request object information:\n")
    fd.write(f"URI: {req.unparsed_uri}\n")
    fd.write(
        # pylint: disable-next=consider-using-f-string
        "Remote Host: {remote_host}\nServer Name: {server_hostname}:{server_port}\n".format(
            remote_host=req.get_remote_host(),
            server_hostname=req.server.server_hostname,
            server_port=req.server.port,
        )
    )
    fd.write("Headers passed in:\n")
    for key, value in sorted(req.headers_in.items()):
        fd.write(f"\t{key}: {value}\n")
    return 0


def Traceback(
    method=None,
    req=None,
    mail=1,
    ostream=sys.stderr,
    extra=None,
    severity="notification",
    with_locals=0,
):
    """Reports an traceback error and optionally sends mail about it.
    NOTE: extra = extra text information.
    """
    # pylint: disable=C0103

    global QUIET_MAIL

    if mail:
        # safeguard
        if QUIET_MAIL is None:
            QUIET_MAIL = CFG.QUIET_MAIL

        if QUIET_MAIL < 0:
            QUIET_MAIL = 0
        if QUIET_MAIL == 0:  # make sure we don't mail
            mail = 0

    e_type = sys.exc_info()[:2][0]
    t = time.ctime(time.time())
    exc = StringIO()

    unicode_hostname = idn_puny_to_unicode(hostname)
    exc.write(f"Exception reported from {unicode_hostname}\nTime: {t}\n")
    exc.write(f"Exception type {e_type}\n")
    if method:
        exc.write(f"Exception while handling function {e_type}\n")

    # print information about the request being served
    if req:
        print_req(req, exc)
    if extra:
        exc.write(f"Extra information about this error:\n{extra}\n")

    # Print the traceback
    exc.write("\nException Handler Information\n")
    traceback.print_exc(None, exc)

    if with_locals and not mail:
        # The mail case will call print_locals by itself
        print_locals(exc)

    # we always log it somewhere
    if ostream:
        ostream.write(exc.getvalue())
        ostream.write("\n")

    if mail:
        # print the stack frames for the mail we send out
        print_locals(exc)
        # dump the environment
        print_env(exc)
        # and send the mail
        # build the headers
        to = CFG.TRACEBACK_MAIL
        from_ = to
        if isinstance(to, type([])):
            from_ = to[0].strip()
            to = ", ".join([x.strip() for x in to])
        headers = {
            "Subject": f"{PRODUCT_NAME} TRACEBACK from {unicode_hostname}",
            "From": f"{hostname} <{from_}>",
            "To": to,
            "X-RHN-Traceback-Severity": severity,
            "Content-Type": 'text/plain; charset="utf-8"',
        }
        QUIET_MAIL = QUIET_MAIL - 1  # count it no matter what

        outstring = exc.getvalue()

        # 5/18/05 wregglej - 151158 Go through every string in the security list
        # and censor it out of the debug information.
        outstring = censor_string(outstring)

        rhnMail.send(headers, outstring)

    exc.close()
    return


def fetchTraceback(method=None, req=None, extra=None, with_locals=0):
    """a cheat for snagging just the string value of a Traceback"""
    exc = StringIO()
    Traceback(
        method=method,
        req=req,
        mail=0,
        ostream=exc,
        extra=extra,
        severity=None,
        with_locals=with_locals,
    )
    return exc.getvalue()


def exitWithTraceback(e, msg, exitnum, mail=0):
    tbOut = StringIO()
    Traceback(mail, ostream=tbOut, with_locals=1)
    log_error(-1, _("ERROR: %s %s: %s") % (e.__class__.__name__, msg, e))
    log_error(-1, _("TRACEBACK: %s") % tbOut.getvalue())
    sys.exit(exitnum)


class SecurityList:
    """The SecurityList is a list of strings that are censored out of a debug email.
    Right now it's only used for censoring traceback emails.
    """

    _flag_string = "security-list"

    def __init__(self):
        # We store the security list in the global flags. This way, we don't
        # have to worry about clearing it up.
        if rhnFlags.test(self._flag_string):
            self.sec = rhnFlags.get(self._flag_string)
        else:
            self.sec = []
            rhnFlags.set(self._flag_string, self.sec)

    def add(self, obj):
        self.sec.append(obj)

    def check(self, obj):
        return obj in self.sec


def get_seclist():
    """Returns the list of strings to be censored."""
    return SecurityList().sec


def censor_string(strval):
    """Remove all instances of the strings in seclist.sec from strval"""
    censorlist = get_seclist()
    for c in censorlist:
        # Censor it with a fixed length string. This way the length of the hidden string isn't revealed.
        strval = strval.replace(c, "<CENSORED!>")
    return strval


def add_to_seclist(obj):
    """Adds a string to seclist.sec, but only if it's not already there."""
    seclist = SecurityList()
    if not seclist.check(obj):
        seclist.add(obj)
