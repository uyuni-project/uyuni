import logging
import xml.sax
from typing import List

from lzreposync.importUtils import import_package_batch
from lzreposync.rpm_repo import RPMHeader
from spacewalk.server.importlib.importLib import Package, Checksum, Dependency

COMMON_NS = "http://linux.duke.edu/metadata/common"
RPM_NS = "http://linux.duke.edu/metadata/rpm"


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

    if attribute in attributes:
        return attributes[attribute]
    return None


def is_complex(attribute: str):
    """
    Return True if the attribute is represented by a list of objects in importLib
    Eg: provides: [Dependency]
    """
    return attribute in ["provides", "requires", "enhances", "obsoletes"]


class Handler(xml.sax.ContentHandler):
    """
    SAX parser handler for repository primary.xml files.
    """

    def __init__(self, batch_size=20):
        super().__init__()
        self.batch_size = batch_size
        self.batch_index = 0  # Number of already imported batches
        self.count = 0  # Counting the num of packages of current batch
        self.package = None
        self.batch: List[Package] = []  # A group of packages to insert together into the db
        self.text = None
        self.currentElement = None
        self.attributes_stack = []  # used for nested attributes that has a list of objects
        self.currentParent = None  # Used to identify an attribute that has a list of objects. Eg: 'provides'
        self.searched_chars = ["arch",
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
                               "checksum",
                               "header-range"
                               ]
        self.searched_attrs = {
            # "location": ["href"],  # TODO Check: is it source_rpm ?
            "time": ["build"],
            "version": ["epoch", "ver", "rel"],  # TODO Check: we have 'version' and 'rpm_version', which one is it ?
            "checksum": ["type"],
            "size": ["package", "installed"],
            "header-range": ["start", "end"]
        }

    def set_checksum(self, attrs):
        """
        Add the checksum (object) to the current package's metadata
        """
        self.currentElement = Checksum()
        self.currentElement['value'] = ""
        if 'type' in attrs.getQNames():
            self.currentElement['type'] = attrs.getValueByQName('type')
            self.text = ""
        else:
            logging.error("missing 'checksum' attribute, ignoring package")
            self.package = None

    def set_element_attribute(self, attr_name, element, attrs):
        """
        Add the given attribute to the current package's metadata.
        For example the attribute can be 'epoch' of an element : <version epoch="0" ver="1.22.0" rel="lp155.3.4.1"/>
        - attr_name: attribute within the xml element. Eg: 'epoch', 'ver', 'rel', etc... that we want to set
        - element: the current element we're parsing. Eg: <version...>
        - attrs: list of element's attributes returned by the sax event handler. Eg: ['epoch', 'ver', 'rel']
        """
        if attr_name not in attrs.getQNames():
            logging.error("missing %s %s attribute, ignoring attribute", element, attr_name)
            # self.package = None  # TODO If we find some missing attributes, should we continue parsing? what attributes can we tolerate missing.
        else:
            extended_name = "/".join([element, attr_name])  # Eg: version/ver
            actual_name = map_attribute(extended_name)
            if actual_name is not None:
                value = attrs.getValueByQName(attr_name)
                self.package[actual_name] = value
            else:
                logging.warning(
                    "Couldn't map the attribute: %s to any importLib attribute, ignoring attribute",
                    extended_name)
                # self.package = None  # TODO If we find some missing attributes, should we continue parsing? what attributes can we tolerate missing.

    def add_dependency(self, attrs):
        """
        Adding a new dependency to the current attributes_stack.
        This will be generally done by parsing the content of <rpm:entry .../>.

        Note: The current 'attributes_stack' contains attributes (dependencies) relative to (or children of) the
        attribute inside the 'currentParent'.
        """
        dependency = Dependency()
        for attr_name in dependency.attributeTypes.keys():  # ['name', 'version', 'flags']
            if attr_name not in attrs.getQNames():
                # Skipp.. Eg: 'epoch'
                continue
            else:
                dependency[attr_name] = attrs.getValueByQName(
                    "ver" if attr_name == "version" else attr_name
                )
        self.attributes_stack.append(dependency)

    def startElementNS(self, name, qname, attrs):
        if name == (COMMON_NS, "package"):
            #  self.package = {}
            self.package = Package()
            self.package['header'] = RPMHeader()  # TODO should we use the header class defined in rhn_mpm.py: class MPM_Header ?
        elif self.package is not None and name[0] == COMMON_NS and name[1] in self.searched_attrs:
            if name[1] == "checksum":
                self.set_checksum(attrs)
            else:
                # Dealing with elements with attributes. Eg: <version epoch="0" ver="1.22.0" rel="lp155.3.4.1"/>
                for attr_name in self.searched_attrs[name[1]]:
                    self.set_element_attribute(attr_name, name[1], attrs)
        elif self.package is not None and (name[0] == COMMON_NS or name[0] == RPM_NS) and name[1] in self.searched_chars:
            if is_complex(name[1]):
                # Dealing with list/nested attributes. Eg: ["provides", "requires", "enhances", "obsoletes"]
                self.currentParent = name[1]
            elif len(attrs) > 0:
                # Rpm element with attributes. Eg: <rpm:header-range start="6200" end="149568"/>
                for attr_name in self.searched_attrs[name[1]]:
                    self.set_element_attribute(attr_name, name[1], attrs)
            else:
                self.text = ""
        elif self.package is not None and name[0] == RPM_NS and name[1] == "entry":
            self.add_dependency(attrs)

    def characters(self, content):
        if self.text is not None:
            self.text += content

    def endElementNS(self, name, qname):
        if name == (COMMON_NS, "package"):
            self.count += 1
            self.batch.append(self.package)
            if self.count >= self.batch_size:
                print("----> TESTING: Importing {} packages...".format(self.count))
                # Import current batch
                self.batch_index += 1
                import_package_batch(self.batch, self.batch_index, self.batch_size)
                self.count = 0
        elif self.package is not None and (name[0] == COMMON_NS or name[0] == RPM_NS) and name[
            1] in self.searched_chars:
            if name[1] == "arch":
                # Tagging 'binary' packages with {isSource:True}, and 'source' ones with {isSource:False}
                if self.text == "src":
                    self.package['header'].is_source = True
            if name[1] == "checksum":
                self.currentElement['value'] = self.text
                self.package["checksum"] = self.currentElement
            elif is_complex(name[1]):
                self.package[name[1]] = self.attributes_stack  # eg: [Dependency] of 'provides' attribute
                self.currentParent = None
                self.attributes_stack = []
            else:
                self.package[name[1]] = self.text
            self.text = None