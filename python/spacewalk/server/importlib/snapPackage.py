#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2025 SUSE, LLC
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
# Converts SNAP headers to the intermediate format
#

from . import headerSource
import time
from contextlib import suppress
from spacewalk.server.importlib.importLib import Channel
from spacewalk.server.importlib.backendLib import gmtime, localtime


# pylint: disable-next=missing-class-docstring
class snapBinaryPackage(headerSource.rpmBinaryPackage):
    # pylint: disable-next=dangerous-default-value
    def __init__(
        self, header, size, checksum_type, checksum, path=None, org_id=None, channels=[]
    ):
        print(f"DEBUG: snapBinaryPackage.__init__ called")
        print(f"DEBUG: header type: {type(header)}")

        # Call parent constructor first
        headerSource.rpmBinaryPackage.__init__(self)
        self.tagMap = headerSource.rpmBinaryPackage.tagMap.copy()


        # Remove already-mapped tags
        self._already_mapped = [
            "rpm_version",
            "payload_size",
            "payload_format",
            "package_group",
            "build_time",
            "build_host",
        ]
        for tag in self._already_mapped:
            with suppress(KeyError):
                del self.tagMap[tag]

        # XXX is seems to me that this is the place that 'source_rpm' is getting
        # set
        for key in self.keys():
            field = self.tagMap.get(key, key)
            if not field:  # unsupported
                continue

            value = header[field]
            if key == "build_time" and isinstance(value, int):
                value = gmtime(value)  # unix timestamp
            elif value == []:
                value = None
            elif value:
                value = str(value)

            self[key] = value

        self["package_size"] = size
        self["checksum_type"] = checksum_type
        self["checksum"] = checksum
        self["path"] = path
        self["org_id"] = org_id
        self["header_start"] = None
        self["header_end"] = None
        self["last_modified"] = localtime(time.time())
        if self["sigmd5"]:
            self["sigchecksum_type"] = "md5"
            self["sigchecksum"] = self["sigmd5"]
        del self["sigmd5"]

        vendor = self["vendor"]
        if vendor is None:
            self["vendor"] = "Debian"
        payloadFormat = self["payload_format"]
        if payloadFormat is None:
            self["payload_format"] = "ar"
        if self["payload_size"] is None:
            self["payload_size"] = 0


        # Ensure header is not None and has required fields
        if header is None:
            print(f"DEBUG: header is None, creating default header")
            header = self._create_default_header()

        # Log all header fields for debugging
        if hasattr(header, 'hdr'):
            print(f"DEBUG: header.hdr keys: {list(header.hdr.keys()) if header.hdr else []}")
            for key, value in (header.hdr.items() if header.hdr else []):
                print(f"DEBUG: header['{key}'] = {value}")
        else:
            print(f"DEBUG: header has no 'hdr' attribute")

        # Populate file information
        self._populateFiles(header)
        # Populate dependency information
        self._populateDependencyInformation(header)
        # Populate changelogs
        self._populateChangeLog(header)
        # Channels
        self._populateChannels(channels)
        # populate extraTags from headers not in already mapped fields
        self._populateExtraTags(header)

        # Call populate method to set up the package data
        self["source_rpm"] = None

        group = self.get("package_group", "")
        if group == "" or group is None:
            self["package_group"] = "NoGroup"

    def _populateFiles(self, header):
        files = []
        # for f in header.get('files', []):
        #    fc = headerSource.rpmFile()
        #    fc.populate(f)
        #    files.append(fc)
        self["files"] = files

    def _populateDependencyInformation(self, header):
        mapping = {
            "provides": headerSource.rpmProvides,
            "requires": headerSource.rpmRequires,
            "conflicts": headerSource.rpmConflicts,
            "obsoletes": headerSource.rpmObsoletes,
            "suggests": headerSource.rpmSuggests,
            "recommends": headerSource.rpmRecommends,
            "breaks": headerSource.rpmBreaks,
            "predepends": headerSource.rpmPredepends,
        }
        for k, dclass in list(mapping.items()):
            l = []
            values = header.get(k, [])
            if isinstance(values, str):
                val = [elem.strip() for elem in values.split(",")]
            elif isinstance(values, list):
                val = values
            else:
                val = []
            for v in val:
                relation = 0
                version = ""
                if "|" in v:
                    # TODO: store alternative-package-names semantically someday
                    name = v
                else:
                    nv = v.split("(")
                    name = nv[0]
                    if len(nv) > 1:
                        version = nv[1].rstrip(")")
                        if version:
                            while version.startswith(("<", ">", "=")):
                                if version.startswith("<"):
                                    relation |= 2
                                if version.startswith(">"):
                                    relation |= 4
                                if version.startswith("="):
                                    relation |= 8
                                version = version[1:]
                hash = {"name": name, "version": version, "flags": relation}
                finst = dclass()
                finst.populate(hash)
                l.append(finst)
            self[k] = l

    def _populateChangeLog(self, header):
        l = []
        # for cinfo in header.get('changelog', []):
        #    cinst = headerSource.rpmChangeLog()
        #    cinst.populate(cinfo)
        #    l.append(cinst)
        self["changelog"] = l


    def _populateExtraTags(self, header):
        already_processed = list(set([
            "arch", "name", "summary", "epoch", "version", "release",
            "payload_size", "vendor", "package_group", "requires",
            "obsoletes", "predepends", "package", "architecture",
            "description", "maintainer", "section", "depends",
            "provides", "conflicts", "replaces", "recommends",
            "suggests", "breaks", "pre-depends", "installed-size",
        ]))
        l = []
        for k, v in header.items():
            if k.lower() not in already_processed and v:
                l.append({"name": k, "value": v})

        self["extra_tags"] = l


    def _create_default_header(self):
        return {
            'name': 'unknown_snap_package',
            'version': '1.0',
            'release': '1',
            'epoch': '',
            'arch': 'amd64-snap',
            'summary': 'SNAP package',
            'description': 'SNAP package',
            'package_group': 'NoGroup',
        }

    def _safe_get_header_value(self, header, key, default):
        try:
            return str(header.get(key, default))
        except AttributeError:
            return default


# Source package class (SNAP packages are typically not source packages)
class snapSourcePackage(snapBinaryPackage):
    def __init__(
        self, header, size, checksum_type, checksum, path=None, org_id=None, channels=[]
    ):
        snapBinaryPackage.__init__(
            self, header, size, checksum_type, checksum, path, org_id, channels
        )
        # Override arch for source packages
        self["arch"] = "src"