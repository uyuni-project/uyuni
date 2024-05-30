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


def map_attribute(attribute: str):
    """
    Map the given attribute name form the metadata file to the corresponding
    name in the importLib Package class.
    If no mapping found, return None.
    """
    attributes = {
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
        "group": "package_group"
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
        self.packages: List[Package] = []  # A group of packages to insert together into the db
        self.text = None
        self.currentElement = None
        self.attributes_stack = []  # used for nested attributes that has a list of objects
        self.currentParent = None  # Used to identify an attribute that has a list of objects. Eg: 'provides'

    def startElementNS(self, name, qname, attrs):
        searched_attrs = {
            # "location": ["href"],  # TODO Check: is it source_rpm ?
            "time": ["build"],
            "version": ["epoch", "ver", "rel"],  # TODO Check: we have 'version' and 'rpm_version', which one is it ?
            "checksum": ["type"],
            "size": ["package", "installed"],
            "header-range": ["start", "end"]
        }

        if name == (COMMON_NS, "package"):
            self.package = {}
        elif self.package is not None and name[0] == COMMON_NS and name[1] in searched_attrs:
            if name[1] == "checksum":
                self.currentElement = Checksum()
                self.currentElement['value'] = ""
                if 'type' in attrs.getQNames():
                    self.currentElement['type'] = attrs.getValueByQName('type')
                    self.text = ""
                else:
                    logging.error("missing %s %s attribute, ignoring package", name[1], 'type')
                    self.package = None
            else:
                for attr_name in searched_attrs[name[1]]:
                    if attr_name not in attrs.getQNames():
                        logging.error("missing %s %s attribute, ignoring package", name[1], attr_name)
                        self.package = None
                    else:
                        value = attrs.getValueByQName(attr_name)
                        extended_name = "/".join([name[1], attr_name])  # Eg: version/ver
                        if extended_name in attribute_map:
                            actual_name = attribute_map[extended_name]
                            self.package[actual_name] = value
                        else:
                            logging.warning(
                                "Couldn't map the attribute: %s to any importLib attribute, ignoring package",
                                extended_name)
                            continue
        elif self.package is not None and (name[0] == COMMON_NS or name[0] == RPM_NS) and name[1] in SEARCHED_CHARS:
            if name[1] in nested_attributes:
                # Dealing with list/nested attributes : ["provides", "requires", "enhances", "obsoletes"]
                self.currentParent = name[1]
            else:
                self.text = ""
        elif self.package is not None and (name[0] == COMMON_NS or name[0] == RPM_NS) and name[1] == "entry":
            # Grouping the attribute of list/nested attributes. Eg: for the 'provides' attribute
            dependency = Dependency()
            for attr_name in dependency.attributeTypes.keys():  # ['name', 'version', 'flags']
                if attr_name not in attrs.getQNames():
                    # logging.warning("Attribute %s not found. Skipping..", attr_name)
                    continue
                else:
                    dependency[attr_name] = attrs.getValueByQName(
                        "ver" if attr_name == "version" else attr_name
                    )
            self.attributes_stack.append(dependency)

    def characters(self, content):
        if self.text is not None:
            self.text += content

    def endElementNS(self, name, qname):
        if name == (COMMON_NS, "package"):
            self.packages.append(self.package)
        elif self.package is not None and (name[0] == COMMON_NS or name[0] == RPM_NS) and name[1] in SEARCHED_CHARS:
            if name[1] == "checksum":
                self.currentElement['value'] = self.text
                self.package["checksum_list"] = [self.currentElement]  # TODO can we have multitple ?
            elif name[1] in nested_attributes:
                self.package[name[1]] = self.attributes_stack  # eg: [Dependency] of 'provides' attribute
                self.currentParent = None
                self.attributes_stack = []
            else:
                self.package[name[1]] = self.text
            self.text = None