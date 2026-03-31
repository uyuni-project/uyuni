#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2009--2016 Red Hat, Inc.
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

import os

import hashlib
import inspect

hashlib_has_usedforsecurity = (
    "usedforsecurity" in inspect.getfullargspec(hashlib.new)[0]
)


# pylint: disable-next=invalid-name
def getHashlibInstance(hash_type, used_for_security):
    """Get an instance of a hashlib object."""
    if hashlib_has_usedforsecurity:
        return hashlib.new(hash_type, usedforsecurity=used_for_security)
    else:
        return hashlib.new(hash_type)


# pylint: disable-next=invalid-name
def getFileChecksum(
    hashtype,
    filename=None,
    fd=None,
    file_obj=None,
    buffer_size=None,
    used_for_security=False,
):
    """Compute a file's checksum
    Used by rotateFile()
    """

    # python's md5 lib sucks
    # there's no way to directly import a file.
    if buffer_size is None:
        buffer_size = 65536

    if hashtype == "sha":
        hashtype = "sha1"

    if filename is None and fd is None and file_obj is None:
        raise ValueError("no file specified")
    if file_obj:
        f = file_obj
    elif fd is not None:
        f = os.fdopen(os.dup(fd), "rb")
    else:
        f = open(filename, "rb")
    # Rewind it
    f.seek(0, 0)
    m = getHashlibInstance(hashtype, used_for_security)
    while 1:
        try:
            buf = f.read(buffer_size)
        except:  # pylint: disable=W0702,W0703
            # No need to know exact root cause of the exception.
            # Will produce checksum other than expected for such case.
            break
        if not buf:
            break
        m.update(buf)

    # cleanup time
    if file_obj is not None:
        file_obj.seek(0, 0)
    else:
        f.close()
    return m.hexdigest()


# pylint: disable-next=invalid-name
def getStringChecksum(hashtype, s):
    """compute checksum of an arbitrary string"""
    h = getHashlibInstance(hashtype, False)
    h.update(s)
    return h.hexdigest()
