#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2010--2016 Red Hat, Inc.
# Copyright (c) 2022 SUSE, LLC
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
# Converts headers to the intermediate format
#

from . import headerSource
import time
from contextlib import suppress
from spacewalk.server.importlib.importLib import Channel
from spacewalk.server.importlib.backendLib import gmtime, localtime


# pylint: disable-next=missing-class-docstring
class debBinaryPackage(headerSource.rpmBinaryPackage):
    # pylint: disable-next=dangerous-default-value
    def __init__(
        self, header, size, checksum_type, checksum, path=None, org_id=None, channels=[]
    ):
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

        # Fix some of the information up
        vendor = self["vendor"]
        if vendor is None:
            self["vendor"] = "Debian"
        payloadFormat = self["payload_format"]
        if payloadFormat is None:
            self["payload_format"] = "ar"
        if self["payload_size"] is None:
            self["payload_size"] = 0

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
            values = header[k]
            if values is not None:
                val = [elem.strip() for elem in values.split(",")]  # split packages
                i = 0
                for v in val:
                    relation = 0
                    version = ""
                    if "|" in v:
                        # TODO: store alternative-package-names semantically someday
                        name = v + "_" + str(i)
                    else:
                        nv = v.split("(")
                        name = nv[0] + "_" + str(i)
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
                    # pylint: disable-next=redefined-builtin
                    hash = {"name": name, "version": version, "flags": relation}
                    finst = dclass()
                    finst.populate(hash)
                    l.append(finst)
                    i += 1
            self[k] = l

    def _populateChangeLog(self, header):
        l = []
        # for cinfo in header.get('changelog', []):
        #    cinst = headerSource.rpmChangeLog()
        #    cinst.populate(cinfo)
        #    l.append(cinst)
        self["changelog"] = l

    def _populateChannels(self, channels):
        l = []
        for channel in channels:
            # pylint: disable-next=redefined-builtin
            dict = {"label": channel}
            obj = Channel()
            obj.populate(dict)
            l.append(obj)
        self["channels"] = l

    def _populateExtraTags(self, header):
        already_processed = [
            "arch",
            "name",
            "summary",
            "epoch",
            "version",
            "release",
            "payload_size",
            "vendor",
            "package_group",
            "requires",
            "obsoletes",
            "predepends",
            "package",
            "architecture",
            "description",
            "maintainer",
            "section",
            "version",
            "depends",
            "provides",
            "conflicts",
            "replaces",
            "recommends",
            "suggests",
            "breaks",
            "pre-depends",
            "installed-size",
        ]
        l = []
        for k, v in header.items():
            if k.lower() not in already_processed and v:
                l.append({"name": k, "value": v})

        self["extra_tags"] = l
