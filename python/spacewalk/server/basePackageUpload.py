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

from rhn.UserDictCase import UserDictCase

from spacewalk.common import apache
from spacewalk.common.rhnLog import log_debug
from spacewalk.common.rhnException import rhnFault


# pylint: disable-next=missing-class-docstring
class BasePackageUpload:
    # pylint: disable-next=unused-argument
    def __init__(self, req):
        self.header_prefix = "X-RHN-Upload"
        self.error_header_prefix = "X-RHN-Upload-Error"
        self.prefix = "rhn/repository"
        self.is_source = 0
        self.rel_package_path = None
        self.package_path = None
        self.required_fields = [
            "Package-Name",
            "Package-Version",
            "Package-Release",
            "Package-Arch",
            "File-Checksum",
            "File-Checksum-Type",
        ]
        self.field_data = UserDictCase()
        self.org_id = None

    def headerParserHandler(self, req):
        # Initialize the logging
        log_debug(3, "Method", req.method)

        # legacy rhnpush sends File-MD5sum; translate it into File-Checksum
        md5sum_header = f"{self.header_prefix}-File-MD5sum"
        if md5sum_header in req.headers_in:
            req.headers_in[f"{self.header_prefix}-File-Checksum-Type"] = "md5"
            req.headers_in[f"{self.header_prefix}-File-Checksum"] = req.headers_in[
                md5sum_header
            ]

        for f in self.required_fields:
            hf = f"{self.header_prefix}-{f}"
            if hf not in req.headers_in:
                log_debug(4, f"Required field {f} missing")
                raise rhnFault(500, f)

            self.field_data[f] = req.headers_in[hf]

        self.package_name = self.field_data["Package-Name"]
        self.package_version = self.field_data["Package-Version"]
        self.package_release = self.field_data["Package-Release"]
        self.package_arch = self.field_data["Package-Arch"]
        self.file_checksum_type = self.field_data["File-Checksum-Type"]
        self.file_checksum = self.field_data["File-Checksum"]
        # 4/18/05 wregglej. if 1051 is in the header's keys, then it's a nosrc package.
        self.is_source = self.package_arch == "src" or self.package_arch == "nosrc"
        return apache.OK

    def handler(self, req):
        log_debug(3, "Method", req.method)
        return apache.OK

    # pylint: disable-next=unused-argument
    def cleanupHandler(self, req):
        return apache.OK

    # pylint: disable-next=unused-argument
    def logHandler(self, req):
        return apache.OK
