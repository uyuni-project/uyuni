#  pylint: disable=missing-module-docstring

import datetime
import gzip
import re
from urllib.parse import urljoin
from xml.dom import pulldom

import rpm

from spacewalk.server.importlib import headerSource
from uyuni.common.rhn_rpm import RPM_Header

COMMON_NS = "http://linux.duke.edu/metadata/common"
RPM_NS = "http://linux.duke.edu/metadata/rpm"

# Data to be included in the package object
package_data = [
    "package_size",
    "checksum",
    "checksum_type",
    "header_start",
    "header_end",
    "remote_path",
]

# List of complex attributes and their mapping classes in importLib
complex_attrs = {
    "provides": headerSource.rpmProvides,
    "requires": headerSource.rpmRequires,
    "enhances": headerSource.rpmEnhances,
    "obsoletes": headerSource.rpmObsoletes,
    "conflicts": headerSource.rpmConflicts,
    "breaks": headerSource.rpmBreaks,
    "oldenhances": headerSource.rpmOldEnhances,
    "suggests": headerSource.rpmSuggests,
    "oldsuggests": headerSource.rpmOldSuggests,
    "supplements": headerSource.rpmSupplements,
    "oldsupplements": headerSource.rpmOldSupplements,
    "recommends": headerSource.rpmRecommends,
    "oldrecommends": headerSource.rpmOldRecommends,
    "predepends": headerSource.rpmPredepends,
    "changelog": headerSource.rpmChangeLog,
}


def map_attribute(attribute: str):
    """
    Map the given attribute name form the metadata file to the corresponding
    name in the importLib Package class.
    If no mapping found, return None.
    """
    # TODO: there's still a time/file attribute not yet handled
    attributes = {
        "version/ver": "version",
        "version/rel": "release",
        "version/epoch": "epoch",
        "time/build": "buildtime",
        "size/package": "package_size",
        "size/installed": "size",  # becomes "installed_size" in the db
        "size/archive": "archivesize",
        "header-range/start": "header_start",
        "header-range/end": "header_end",
        "location/href": "remote_path",
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

    dependency_cls = complex_attrs.get(dependency_name)
    if dependency_cls:
        return dependency_cls.tagMap.get(attribute)
    return None


def is_complex(attribute: str):
    """
    Return True if the attribute is represented by a list of objects in importLib
    Eg: provides: [Dependency]
    """
    return attribute in complex_attrs


def get_text(node):
    """
    Building the text content of the node as it can be formed by multiple lines
    """
    text_content = []
    for child in node.childNodes:
        if child.nodeType == node.TEXT_NODE:
            text_content.append(child.nodeValue)

    return "".join(text_content).strip()


def is_source_package(node):
    arch_node = node.getElementsByTagName("arch")[0]
    return get_text(arch_node) == "src"


def map_flag(flag: str) -> int:
    """
    Map the given flag into the correct int value
    For more information, please see: https://github.com/rpm-software-management/createrepo_c/blob/424616d851d6fe58e89ae9b1b318853f8a899195/src/misc.c#L50
    """
    if flag == "LT":
        return 2
    if flag == "GT":
        return 4
    if flag == "EQ":
        return 8
    if flag == "LE":
        return 10
    if flag == "GE":
        return 12
    return 0


#  pylint: disable-next=missing-class-docstring
class PrimaryParser:
    def __init__(self, primary_file, repository="", arch_filter=".*"):
        """
        primary_file: In gzip format
        # TODO: use  uyuni.common.fileutils.decompress_open for different format handling (gz, xz, ect), However! we should close the file manually
        """
        if self.is_valid_primary_file(primary_file):
            self.primary_file = primary_file
        else:
            raise ValueError(
                f"Bad format for primary file {primary_file}. Accepted formats: gzip"
            )
        self.current_package = None
        self.current_hdr = None
        self.repository = repository
        self.arch_filter = arch_filter

    def is_valid_primary_file(self, primary_file):
        """
        Check whether the format of the given primary file is supported.
        Currently supported: Gzip
        """
        # Checking Gzip format
        with gzip.open(primary_file) as fd:
            # Checking the file magic number
            try:
                fd.read(1)
                try:
                    # If primary_file is an Object-like file, set the file's cursor at the beginning again
                    primary_file.seek(0)
                except AttributeError:
                    pass
                return True
            except OSError:
                return False

    def parse_primary(self):
        """
        Parser the primary.xml file (gzip format) using xml.dom.pulldom This is an incremental parsing,
        it means that not the whole xml file is loaded in memory at once, but package by package.
        IMPORTANT!: We are ignoring 'src' packages
        """

        with gzip.open(self.primary_file) as gz_primary:
            doc = pulldom.parse(gz_primary)
            for event, node in doc:
                if (
                    event == pulldom.START_ELEMENT
                    and node.namespaceURI == COMMON_NS
                    and node.tagName == "package"
                ):
                    # New package
                    doc.expandNode(node)

                    arch_node = node.getElementsByTagName("arch")[0]
                    pkg_arch = get_text(arch_node)
                    # Filter by arch and ignoring src packages
                    if pkg_arch == "src" or not re.fullmatch(
                        self.arch_filter, pkg_arch
                    ):
                        continue
                    self.current_package = {}
                    self.current_hdr = {}

                    ### SETTING FAKE DATA FOR SOME ATTRIBUTES
                    # TODO: Fix these fake attributes
                    self.current_hdr["rpmversion"] = "1"
                    self.current_hdr["size"] = 10000
                    self.current_hdr["payloadformat"] = "cpio"
                    self.current_hdr["cookie"] = "cookie_test"
                    self.current_hdr["sigsize"] = 10000
                    self.current_hdr["sigmd5"] = "sigmd5_test"
                    # Setting possibly missing attributes: checking for all dependencies
                    # importLib doesn't accept None values but rather empty arrays ([])
                    # TODO: Double check the following list, it might not be completely correct,
                    #  because, the hdr can have two type of keys referring to the same element (for example: '1156' and 'suggestsname' refer to the same thing),
                    #  so the list should contain only keys that we looked for during and the parsing, and the missing ones,
                    #  which means, for example, we shouldn't look for possibly missing '1156' key while we've already
                    #  set the 'suggestsname' key
                    possibly_missing_dependencies = [
                        "provides",
                        "provideversion",
                        "provideflags",
                        "requirename",
                        "requireversion",
                        "requireflags",
                        "changelogname",
                        "changelogtext",
                        "changelogtime",
                        "obsoletename",
                        "obsoleteversion",
                        "obsoleteflags",
                        "conflictname",
                        "conflictversion",
                        "conflictflags",
                        1159,
                        1160,
                        1161,
                        1156,
                        1157,
                        1158,
                        5052,
                        5053,
                        5054,
                        5055,
                        5056,
                        5057,
                        5049,
                        5050,
                        5051,
                        5046,
                        5047,
                        5048,
                    ]
                    for dep in possibly_missing_dependencies:
                        if dep not in self.current_hdr:
                            self.current_hdr[dep] = []

                    header_tags = [
                        rpm.RPMTAG_DSAHEADER,
                        rpm.RPMTAG_RSAHEADER,
                        rpm.RPMTAG_SIGGPG,
                        rpm.RPMTAG_SIGPGP,
                        rpm.RPMTAG_FILEDIGESTALGO,
                    ]
                    for ht in header_tags:
                        self.current_hdr[ht] = None

                    # Parsing package's metadata
                    for child_node in node.childNodes:
                        if child_node.nodeType == child_node.ELEMENT_NODE:
                            self.set_element_node(child_node)

                    # Set the full url for the remote_path
                    self.current_package["remote_path"] = urljoin(
                        self.repository, self.current_package.get("remote_path", "")
                    )

                    is_source = is_source_package(node)
                    package_header = RPM_Header(self.current_hdr, is_source=is_source)
                    self.current_package["header"] = package_header

                    yield self.current_package

    def set_checksum_node(self, node):
        """
        Parse the given "checksum" node and the result Checksum object to the current package
        """
        if not isinstance(self.current_package, dict):
            print("Error: No package being parsed!")
            raise ValueError("No package being parsed")

        self.current_package["checksum"] = get_text(node)
        self.current_package["checksum_type"] = node.attributes["type"].value

    def set_attribute_element_node(self, node):
        """
        Parse the given attribute element node and add its information to the currentPackage.
        node: self-closing element. Eg: <version epoch="0" ver="1.22.0" rel="lp155.3.4.1"/>
        """
        if not isinstance(self.current_package, dict):
            print("Error: No package being parsed!")
            raise ValueError("No package being parsed")

        elt_name = node.localName
        for attr in node.attributes.keys():
            extended_name = "/".join([elt_name, attr])  # Eg: version/ver
            actual_name = map_attribute(extended_name)
            if actual_name:
                value = node.getAttributeNode(
                    attr
                ).value  # TODO: I think this can be changed by getAttribute(attr)
                if actual_name == "archivesize":
                    value = int(value)
                elif actual_name == "buildtime":
                    value = datetime.datetime.fromtimestamp(float(value))
                if actual_name in package_data:
                    self.current_package[actual_name] = value
                else:
                    # It is a header attribute
                    self.current_hdr[actual_name] = value
            else:
                continue

    def set_complex_element_node(self, node):
        """
        Parse and set complex elements. Complex means the it contains child nodes. Eg: "provides", "requires", etc
        each node has a list of Dependencies.
        The names should be mapped the same as in HeaderSource.py: rpmProvides, rpmRequires, etc..
        """
        if not isinstance(self.current_package, dict):
            print("Error: No package being parsed!")
            raise ValueError("No package being parsed")
        elt_name = node.localName
        for child_node in node.childNodes:
            if child_node.nodeType == node.ELEMENT_NODE:
                for attr_name in ("name", "version", "flags"):
                    attr_mapped_name = map_dependency_attribute(elt_name, attr_name)
                    # pylint: disable-next=unidiomatic-typecheck
                    if not isinstance(
                        self.current_hdr.get(attr_mapped_name), list
                    ):  # Check if list is not initialized
                        self.current_hdr[attr_mapped_name] = []
                    attr = child_node.getAttributeNode(
                        "ver" if attr_name == "version" else attr_name
                    )  # map attr name
                    if attr:
                        if attr_name != "flags":
                            self.current_hdr[attr_mapped_name].append(
                                attr.value.encode("utf-8")
                            )
                        else:
                            self.current_hdr[attr_mapped_name].append(
                                map_flag(attr.value)
                            )
                    else:
                        if attr_name == "flags":
                            self.current_hdr[attr_mapped_name].append(0)
                        else:
                            # Setting some fake data TODO:fix
                            self.current_hdr[attr_mapped_name].append(b"")

    def set_text_element_node(self, node):
        """
        Parse and set elements with text content. Eg: <summary>GStreamer ...</summary>
        """
        if not isinstance(self.current_package, dict):
            print("Error: No package being parsed!")
            raise ValueError("No package being parsed")

        mapped_name = map_attribute(node.localName) or node.localName
        if mapped_name:
            if mapped_name in package_data:
                self.current_package[mapped_name] = get_text(node)
            else:
                self.current_hdr[mapped_name] = get_text(node)

    def set_element_node(self, node):
        """
        Parse the given node and the corresponding information to the corresponding package's attribute
        """
        if not isinstance(self.current_package, dict):
            print("Error: No package being parsed!")
            raise ValueError("No package being parsed")

        if (
            node.nodeType == node.ELEMENT_NODE
            and node.namespaceURI == COMMON_NS
            and node.localName == "format"
        ):
            # Recursively set the child elements of the <format> element
            for child in node.childNodes:
                self.set_element_node(child)
        if (
            node.nodeType == node.ELEMENT_NODE
            and node.namespaceURI == COMMON_NS
            and node.localName == "checksum"
        ):
            self.set_checksum_node(node)
        if node.nodeType == node.ELEMENT_NODE and node.hasAttributes():
            # node in searchedAttrs
            self.set_attribute_element_node(node)
        elif node.nodeType == node.ELEMENT_NODE and not is_complex(node.localName):
            # node in searchedChars:
            self.set_text_element_node(node)
        elif node.nodeType == node.ELEMENT_NODE and is_complex(node.localName):
            # dealing with ["provides", "requires", "enhances", "obsoletes", etc..]
            self.set_complex_element_node(node)
