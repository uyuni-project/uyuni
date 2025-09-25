#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2010--2016 Red Hat, Inc.
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
# Meta-package manager
#
# Author: Lukas Durfina <lukas.durfina@gmail.com>

import sys
import tempfile

from snapcraft import snapfile
from snapcraft.snap822 import Snap822

from uyuni.common.usix import raise_with_tb
from uyuni.common import checksum
from uyuni.common.rhn_pkg import A_Package, InvalidPackageError

# bare-except and broad-except
# pylint: disable=W0702,W0703

SNAP_CHECKSUM_TYPE = "sha256"  # FIXME: this should be a configuration option


# pylint: disable-next=missing-class-docstring,invalid-name
class snap_Header:
    # this is a workaround for issue in python-snapcraft
    # https://www.mail-archive.com/pkg-python-snapcraft-maint@alioth-lists.snapcraft.net/msg00598.html
    # after the issue is fixed, remove this function
    def get_file(self, control, fname):
        if fname.startswith("./"):
            fname = fname[2:]
        elif fname.startswith("/"):
            fname = fname[1:]

        try:
            fobj = control.tgz().extractfile(fname)
        except KeyError:
            # pylint: disable-next=raise-missing-from
            raise snapfile.SnapError("control.tar.* not found inside package")

        if fobj is None:
            raise snapfile.SnapError("control.tar.* not found inside package")

        return fobj

    # pylint: disable-next=pointless-string-statement
    "Wrapper class for a snap header - we need to store a flag is_source"

    def __init__(self, stream):
        self.packaging = "snap"
        self.signatures = []
        self.is_source = 0
        self.snap = None

        try:
            self.snap = snapfile.SnapFile(stream.name)
        except Exception:
            e = sys.exc_info()[1]
            raise_with_tb(InvalidPackageError(e), sys.exc_info()[2])

        try:
            # Fill info about package
            try:
                snapcontrol = self.snap.snapcontrol()
            except snapfile.SnapError:
                # this is a workaround for issue in python-snapcraft
                # https://www.mail-archive.com/pkg-python-snapcraft-maint@alioth-lists.snapcraft.net/msg00598.html
                snapcontrol = Snap822(self.get_file(self.snap.control, "control"))

            self.hdr = {
                "name": snapcontrol.get_as_string("Package"),
                "arch": snapcontrol.get_as_string("Architecture") + "-snap",
                "summary": snapcontrol.get_as_string("Description").splitlines()[0],
                "epoch": "",
                "version": 0,
                "release": 0,
                "description": snapcontrol.get_as_string("Description"),
            }
            for hdr_k, snap_k in [
                ("requires", "Depends"),
                ("provides", "Provides"),
                ("conflicts", "Conflicts"),
                ("obsoletes", "Replaces"),
                ("recommends", "Recommends"),
                ("suggests", "Suggests"),
                ("breaks", "Breaks"),
                ("predepends", "Pre-Depends"),
                ("payload_size", "Installed-Size"),
                ("maintainer", "Maintainer"),
            ]:
                if snap_k in snapcontrol:
                    self.hdr[hdr_k] = snapcontrol.get_as_string(snap_k)
            for k in list(snapcontrol.keys()):
                if k not in self.hdr:
                    self.hdr[k] = snapcontrol.get_as_string(k)

            version = snapcontrol.get_as_string("Version")
            if version.find(":") != -1:
                self.hdr["epoch"], version = version.split(":")
                self.hdr["version"] = version
            if version.find("-") != -1:
                # pylint: disable-next=invalid-name
                version_tmpArr = version.split("-")
                self.hdr["version"] = "-".join(version_tmpArr[:-1])
                self.hdr["release"] = version_tmpArr[-1]
            else:
                self.hdr["version"] = version
                self.hdr["release"] = "X"
        except Exception:
            e = sys.exc_info()[1]
            raise_with_tb(InvalidPackageError(e), sys.exc_info()[2])

    @staticmethod
    def checksum_type():
        return SNAP_CHECKSUM_TYPE

    @staticmethod
    def is_signed():
        return 0

    def __getitem__(self, name):
        return self.hdr.get(str(name))

    def __setitem__(self, name, item):
        self.hdr[name] = item

    def __delitem__(self, name):
        del self.hdr[name]

    def __getattr__(self, name):
        return getattr(self.hdr, name)

    def __len__(self):
        return len(self.hdr)


# pylint: disable-next=missing-class-docstring,invalid-name
class SNAP_Package(A_Package):
    def __init__(self, input_stream=None):
        A_Package.__init__(self, input_stream)
        self.header_data = tempfile.NamedTemporaryFile()
        self.checksum_type = SNAP_CHECKSUM_TYPE

    def read_header(self):
        self._stream_copy(self.input_stream, self.header_data)
        self.header_end = self.header_data.tell()
        try:
            self.header_data.seek(0, 0)
            self.header = snap_Header(self.header_data)
        except:
            e = sys.exc_info()[1]
            raise_with_tb(InvalidPackageError(e), sys.exc_info()[2])

    def save_payload(self, output_stream):
        c_hash = checksum.getHashlibInstance(self.checksum_type, False)
        if output_stream:
            output_start = output_stream.tell()
        self._stream_copy(self.header_data, output_stream, c_hash)
        self.checksum = c_hash.hexdigest()
        if output_stream:
            self.payload_stream = output_stream
            self.payload_size = output_stream.tell() - output_start
