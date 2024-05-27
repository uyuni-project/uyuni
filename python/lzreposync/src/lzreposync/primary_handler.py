import logging
import xml.sax
from lzreposync.rpm import RPM


COMMON_NS = "http://linux.duke.edu/metadata/common"
SEARCHED_CHARS = ["arch", "name"]


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
