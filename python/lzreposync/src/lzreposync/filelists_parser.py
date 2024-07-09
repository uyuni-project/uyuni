#  pylint: disable=missing-module-docstring

import gzip
import logging
import os
import re
import shutil
import xml.etree.ElementTree as ET
from xml.dom import pulldom


def map_attribute(attr):
    attr_map = {"ver": "version", "rel": "release"}
    return attr_map.get(attr, attr)


def cache_xml_node(node, cache_dir):
    """
    Saving the content of the given xml node into a xml file in the given cache directory
    node: of type xml.dom.minidom.Element
    """
    pkgid = node.getAttributeNode("pkgid").value

    xml_content = node.toxml()
    cache_file = os.path.join(cache_dir, pkgid)

    if not os.path.exists(cache_dir):
        logging.debug("Creating cache directory: %s", cache_dir)
        os.makedirs(cache_dir)

    with open(cache_file, "w", encoding="utf-8") as pkg_files:
        logging.debug("Caching file %s", cache_file)
        pkg_files.write(xml_content)


# pylint: disable-next=missing-class-docstring
class FilelistsParser:
    def __init__(self, filelists_file, cache_dir="./.cache", arch_filter=".*"):
        """
        filelists_file: In gzip format
        """
        self.filelists_file = filelists_file
        self.cache_dir = cache_dir
        self.arch_filter = arch_filter
        self.num_packages = -1  # The number of packages in the given filelist file
        self.num_parsed_packages = 0  # The number packages parsed
        self.parsed = False  # Tell whether the filelists file has been parsed or not

    def parse_filelists(self):
        """
        Parse the given filelists.xml file (in gzip format) and save the filelist information
        of each package in a separate file, where the name of the file is the 'pkgid' with no extension,
        for eg the file name should be like: 1c51349b5b35baa58f4941528d25a1306e84b71109051705138dc3577a38bad4
        """

        with gzip.open(self.filelists_file) as gz_filelists:
            doc = pulldom.parse(gz_filelists)
            for event, node in doc:
                if event == pulldom.START_ELEMENT and node.tagName == "filelists":
                    # saving the num of packages contained in the filelists file
                    num_packages = node.getAttributeNode("packages").value
                    self.num_packages = num_packages

                elif event == pulldom.START_ELEMENT and node.tagName == "package":
                    doc.expandNode(node)
                    pkg_arch = node.getAttributeNode("arch").value
                    if re.fullmatch(self.arch_filter, pkg_arch):  # Filter by arch
                        # Save the content of the package's filelist info in cache directory
                        cache_xml_node(node, self.cache_dir)
                        self.num_parsed_packages += 1

            self.parsed = True

    def get_package_filelist(self, pkgid):
        """
        Read the filelist information for the package with the given pkgid,
        parse the information and return a dict containing the filelist info
        """

        filelist_path = os.path.join(self.cache_dir, pkgid)

        # Read the cached filelist file
        if not os.path.exists(filelist_path):
            logging.debug("No filelist file found for package %s", pkgid)
            if not self.parsed:
                logging.debug("Parsing filelists file...")
                self.parse_filelists()
                self.parsed = True
            else:
                logging.error("Couldn't find filelist file for package %s", pkgid)
                return

        with open(
            os.path.join(self.cache_dir, pkgid), "r", encoding="utf-8"
        ) as filelist_xml:
            tree = ET.parse(filelist_xml)
            root = tree.getroot()

            filelist = {}
            filelist["pkgid"] = pkgid
            filelist["files"] = []
            # Setting version information (normally it is the same as the one in primary.xml file for the same package)
            for attr in ("ver", "epoch", "rel"):
                try:
                    filelist[map_attribute(attr)] = root[0].attrib[attr]
                except KeyError as key:
                    logging.debug("missing %s information for package %s", key, pkgid)

            for file in root[1:]:
                filelist["files"].append(file.text)

        return filelist

    def clear_cache(self):
        """
        Remove the cached filelist files from the cache directory, including the cache directory
        """
        if os.path.exists(self.cache_dir):
            logging.debug("Removing %s directory and its content")
            shutil.rmtree(self.cache_dir)
