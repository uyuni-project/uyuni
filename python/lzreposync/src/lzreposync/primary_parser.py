import gzip
from xml.dom import pulldom

from lzreposync.rpm_utils import RPMHeader
from spacewalk.server.importlib.importLib import Package, Checksum, Dependency

COMMON_NS = "http://linux.duke.edu/metadata/common"
RPM_NS = "http://linux.duke.edu/metadata/rpm"

# Data to be included in the package object
package_data = ["package_size", "checksum", "checksum_type", "header_start", "header_end"]

ignored_attributes = ["provideflags", "requirename", "requireversion", "requireflags", "conflictname",
                      "conflictversion", "conflictflags", "obsoletename", "obsoleteversion", "obsoleteflags", 1159,
                      1160, 1161, 1156, 1157, 1158, 5052, 5053, 5054, 5055, 5056, 5057, 5049, 5050, 5051, 5046, 5047,
                      5048, 'filenames', 'filedevices', 'fileinodes', 'filemodes', 'fileusername', 'filegroupname',
                      'filerdevs', 'filesizes', 'longfilesizes', 'filemtimes', 'filemd5s', 'filelinktos', 'fileflags',
                      'fileverifyflags', 'filelangs']


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
        "time/build": "buildtime",
        "size/package": "package_size",
        "size/installed": "installed_size",
        "size/archive": "archivesize",
        "header-range/start": "header_start",
        "header-range/end": "header_end"
    }

    if attribute in attributes:
        return attributes[attribute]
    return None

def map_dependency_attribute(dependency_name, attribute):
    """
    Map the given dependency's attribute to the corresponding name
    Eg: dependency="requires", attribute="name" => return "requirename"
    We'll be using the mapping used in headerSource.py
    """

    if dependency_name == "provides":
        return rpmProvides.tagMap.get(attribute)
    if dependency_name == "requires":
        return rpmRequires.tagMap.get(attribute)
    if dependency_name == "enhances":
        return rpmEnhances.tagMap.get(attribute)  # TODO Note: there's also 'rpmOldEnhances'..what to choose ?
    if dependency_name == "obsoletes":
        return rpmObsoletes.tagMap.get(attribute)


def is_complex(attribute: str):
    """
    Return True if the attribute is represented by a list of objects in importLib
    Eg: provides: [Dependency]
    """
    return attribute in ["provides", "requires", "enhances", "obsoletes"]


def get_text(node):
    """
    Building the text content of the node as it can be formed by multiple lines
    """
    text_content = []
    for child in node.childNodes:
        if child.nodeType == node.TEXT_NODE:
            text_content.append(child.nodeValue)

    return ''.join(text_content).strip()


class PrimaryParser:
    def __init__(self, primary_file):
        """
        primary_file: In gzip format
        """
        self.primaryFile = primary_file
        self.currentPackage = None
        self.searchedChars = ["arch",
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
                              ]  # Elements that has text content or have child elements (Not self-closing elements)
        self.searchedAttrs = {
            # "location": ["href"],  # TODO complete
            "time": ["build"],
            "version": ["epoch", "ver", "rel"],
            "checksum": ["type"],
            "size": ["package", "installed"],
            "header-range": ["start", "end"]
        }  # Self-closing elements: relevant values are their attributes'

    def set_checksum_node(self, node):
        """
        Parse the given "checksum" node and the result Checksum object to the current package
        """
        # TODO: fix later
        # if not self.currentPackage:
        #     print("Error: No package being parsed!")
        #     raise ValueError("No package being parsed")

        checksum = Checksum()
        # TODO: Surround with try except
        checksum["type"] = node.attributes["type"].value
        checksum["value"] = node.firstChild.nodeValue
        self.currentPackage["checksum"] = checksum

    def set_attribute_element_node(self, node):
        """
        Parse the given attribute element node and add its information to the currentPackage.
        node: self-closing element. Eg: <version epoch="0" ver="1.22.0" rel="lp155.3.4.1"/>
        """
        # TODO: fix later
        # if not self.currentPackage:
        #     print("Error: No package being parsed!")
        #     raise ValueError("No package being parsed")

        elt_name = node.localName
        for attr in node.attributes.keys():
            extended_name = "/".join([elt_name, attr])  # Eg: version/ver
            actual_name = map_attribute(extended_name)
            if actual_name:
                value = node.getAttributeNode(attr).value
                if actual_name == "archivesize":
                    value = int(value)
                elif actual_name == "buildtime":  # TODO can be optimized and be more general (eg mapping function for time)
                    value = datetime.datetime.fromtimestamp(float(value))
                if actual_name in package_data:
                    self.currentPackage[actual_name] = value
                else:
                    # It is a header attribute
                    self.current_hdr[actual_name] = value
            else:
                continue
                # logging.warning("Couldn't map the attribute: %s to any importLib attribute, ignoring attribute",
                #               extended_name)

    def set_complex_element_node(self, node):
        """
        Parse and set complex elements. Complex means the it contains child nodes. Eg: "provides", "requires", etc
        each node has a list of Dependencies.
        The names should be mapped the same as in HeaderSource.py: rpmProvides, rpmRequires, etc..
        """
        # TODO: fix later
        # if not self.currentPackage:
        #     print("Error: No package being parsed!")
        #     raise ValueError("No package being parsed")
        elt_name = node.localName
        dependencies = []
        for child_node in node.childNodes:
            if child_node.nodeType == node.ELEMENT_NODE:
                dependency = {}
                for attr_name in ('name', 'version', 'flags'):

                    attr_mapped_name = map_dependency_attribute(elt_name, attr_name)
                    attr = child_node.getAttributeNode(
                        "ver" if attr_name == "version" else attr_name  # mapping attr name
                    )
                    if attr:
                        dependency[attr_mapped_name] = attr.value
                    else:
                        dependency[attr_mapped_name] = None
                # print(f"APPENDING DEPENDENCY {dependency.items()}")
                dependencies.append(dependency)
        # print(f"HAROUNE DEBUG: SETTING COMPLEX {elt_name}, NUM DEPENDENCIES={len(dependencies)}", )
        self.current_hdr[elt_name] = dependencies

    def set_text_element_node(self, node):
        """
        Parse and set elements with text content. Eg: <summary>GStreamer ...</summary>
        """
        # TODO: fix later
        # if not self.currentPackage:
        #     print("Error: No package being parsed!")
        #     raise ValueError("No package being parsed")

        self.currentPackage[node.localName] = get_text(node)

    def set_element_node(self, node):
        """
        Parse the given node and the corresponding information to the corresponding package's attribute
        """
        # TODO: fix later
        # if not self.currentPackage:
        #     print("Error: No package being parsed!")
        #     raise ValueError("No package being parsed")

        if node.nodeType == node.ELEMENT_NODE and node.namespaceURI == COMMON_NS and node.localName == "format":
            # Recursively set the child elements of the <format> element
            for child in node.childNodes:
                self.set_element_node(child)
        if node.nodeType == node.ELEMENT_NODE and node.namespaceURI == COMMON_NS and node.localName == "checksum":
            self.set_checksum_node(node)
        if node.nodeType == node.ELEMENT_NODE and node.hasAttributes():
            # node in searchedAttrs
            self.set_attribute_element_node(node)
        elif node.nodeType == node.ELEMENT_NODE and not is_complex(node.localName):
            # node in searchedChars:
            self.set_text_element_node(node)
        elif node.nodeType == node.ELEMENT_NODE and is_complex(node.localName):
            # dealing with ["provides", "requires", "enhances", "obsoletes"]
            self.set_complex_element_node(node)

    def set_package_header(self, node):
        """
        Set the package header. Mainly 'is_source' and 'packaging'
        """
        # TODO: fix later
        # if not self.currentPackage:
        #     print("Error: No package being parsed!")
        #     raise ValueError("No package being parsed")

        self.currentPackage['header'] = RPMHeader()
        arch_node = node.getElementsByTagName("arch")[0]
        if get_text(arch_node) == "src":
            self.currentPackage['header'].is_source = True
        else:
            self.currentPackage['header'].is_source = False

    def parse_primary(self):
        """
        Parser the given primary.xml file (gzip format) using xml.dom.pulldom This is an incremental parsing,
        it means that not the whole xml file is loaded in memory at once, but package by package.
        """
        if not self.primaryFile:
            print("Error: primary_file not defined!")
            raise ValueError("primary_file missing")

        with gzip.open(self.primaryFile) as gz_primary:
            doc = pulldom.parse(gz_primary)
            for event, node in doc:
                if event == pulldom.START_ELEMENT and node.namespaceURI == COMMON_NS and node.tagName == "package":
                    # New package
                    doc.expandNode(node)
                    self.currentPackage = Package()

                    # Tagging 'source' and 'binary' packages
                    self.set_pacakge_header(node)

                    # Parsing package's metadata
                    for child_node in node.childNodes:
                        if child_node.nodeType == child_node.ELEMENT_NODE:
                            self.set_element_node(child_node)

                    # To make the _extract_signatures() function happy # TODO: fix later
                    header_tags = [
                        rpm.RPMTAG_DSAHEADER,
                        rpm.RPMTAG_RSAHEADER,
                        rpm.RPMTAG_SIGGPG,
                        rpm.RPMTAG_SIGPGP,
                    ]
                    for ht in header_tags:
                        self.current_hdr[ht] = None
                    # TODO: We can put them in a list: ignored_attributes
                    self.current_hdr["rpmversion"] = '1'  # TODO fix later
                    self.current_hdr["size"] = 10000  # TODO fix later
                    self.current_hdr["payloadformat"] = "cpio"  # TODO fix later
                    self.current_hdr["cookie"] = "cookie_test"  # TODO fix later
                    self.current_hdr["sigsize"] = 10000  # TODO fix later
                    self.current_hdr["sigmd5"] = "sigmd5_test"  # TODO fix later
                    for elt in ignored_attributes:
                        self.current_hdr[elt] = None

                    is_source = is_source_package(node)
                    package_header = RPM_Header(self.current_hdr, is_source=is_source)
                    self.currentPackage["header"] = package_header

                    # TODO: handle channels, files, tags (required by the importer)
                    self.current_hdr["channels"] = []
                    self.current_hdr["files"] = []
                    self.current_hdr["changelog"] = []
                    self.current_hdr["changelogname"] = None
                    self.current_hdr["changelogtext"] = None
                    self.current_hdr["changelogtime"] = None

                    yield self.currentPackage
