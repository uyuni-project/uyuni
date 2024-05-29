import logging
import xml.sax
from typing import List

from spacewalk.server.importlib.importLib import Package, Checksum, Dependency

COMMON_NS = "http://linux.duke.edu/metadata/common"
RPM_NS = "http://linux.duke.edu/metadata/rpm"

SEARCHED_CHARS = ["arch",
                  "name",
                  "summary",
                  "description",
                  "packager",
                  "url",
                  "license",
                  "vendor",
                  "group",
                  "buildhost",
                  "sourcerpm",
                  "provides",
                  "requires",
                  "obsoletes",
                  "enhances",
                  "checksum"
                  ]

# Mapping the attributes from the xml tags to the importLib's Package class's attributes
# Note: if an attribute is not in this map, then: if it is in the searched_attrs then it is ignored else it is left as
# it is, as it has the same name as the importLib
attribute_map = {
    "version/ver": "version",
    "version/rel": "release",
    "version/epoch": "epoch",
    "time/build": "build_time",
    "size/package": "package_size",
    "size/installed": "installed_size",
    "header-range/start": "header_start",
    "header-range/end": "header_end",
    "buildhost": "build_host",
    "sourcerpm": "source_rpm",
    "group": "package_group",
}

# Attributes that are represented by list of objects in importLib
nested_attributes = ["provides", "requires", "enhances", "obsoletes"]


class Handler(xml.sax.ContentHandler):
    """
    SAX parser handler for repository primary.xml files.
    """

    def __init__(self):
        super().__init__()
        self.package = None
        self.rpms = {}
        self.text = None

    def startElementNS(self, name, qname, attrs):
        searched_attrs = {
            "location": ["href"],
            "time": ["file"],
            "version": ["epoch", "ver", "rel"]
        }  # TODO update this with the package's metadata we need

        if name == (COMMON_NS, "package"):
            self.package = {}
        elif self.package is not None and name[0] == COMMON_NS and name[1] in searched_attrs:
            for attr_name in searched_attrs[name[1]]:
                if attr_name not in attrs.getQNames():
                    logging.error("missing %s %s attribute, ignoring package", name[1], attr_name)
                    self.package = None
                else:
                    value = attrs.getValueByQName(attr_name)
                    self.package["/".join([name[1], attr_name])] = value
        elif self.package is not None and name[0] == COMMON_NS and name[1] in SEARCHED_CHARS:
            self.text = ""

    def characters(self, content):
        if self.text is not None:
            self.text += content

    def endElementNS(self, name, qname):
        if name == (COMMON_NS, "package"):
            if self.package is not None and self.package["arch"] in ["x86_64", "noarch"]:
                pkg_name = self.package["name"]

                rpm = RPM(
                    self.package["location/href"],
                    int(self.package["time/file"]),
                    pkg_name,
                    self.package["version/epoch"],
                    self.package["version/ver"],
                    self.package["version/rel"],
                )
                latest_rpm = self.rpms.get(pkg_name)
                if latest_rpm is None or latest_rpm.compare(rpm):
                    self.rpms[pkg_name] = rpm
        elif self.package is not None and name[0] == COMMON_NS and name[1] in SEARCHED_CHARS:
            self.package[name[1]] = self.text
            self.text = None
