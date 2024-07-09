#  pylint: disable=missing-module-docstring
import datetime
import gzip
import io
import unittest

from lzreposync.filelists_parser import FilelistsParser
from lzreposync.primary_parser import (
    PrimaryParser,
    complex_attrs,
    map_dependency_attribute,
)
from lzreposync.rpm_metadata_parser import MetadataParser


def test_download_and_parse_metadata():
    """TODO"""


def test_verify_signature():
    """TODO"""


class LazyRepoSyncTest(unittest.TestCase):

    def test_parse_primary(self):
        """
        Test the parsing functionality for rpm's primary.xml metadata file
        """

        # Prepare test data: valid rpm package metadata in primary.xml
        primary_xml = """<?xml version="1.0" encoding="UTF-8"?>
                        <metadata xmlns="http://linux.duke.edu/metadata/common" xmlns:rpm="http://linux.duke.edu/metadata/rpm" packages="1">
                        <package type="rpm">
                          <name>gstreamer-plugins-bad</name>
                          <arch>aarch64</arch>
                          <version epoch="0" ver="1.22.0" rel="lp155.3.4.1"/>
                          <checksum type="sha256" pkgid="YES">5f32047b55c0ca2dcc00a00270cd0b10df4df40c6cd9355eeff9b6aa0997657b</checksum>
                          <summary>GStreamer Streaming-Media Framework Plug-Ins</summary>
                          <description>GStreamer is a streaming media framework based on graphs of filters
                        that operate on media data...
                        </description>
                          <packager>http://bugs.opensuse.org</packager>
                          <url>https://gstreamer.freedesktop.org</url>
                          <time file="1700989109" build="1696852591"/>
                          <size package="2197472" installed="11946385" archive="11965968"/>
                          <location href="aarch64/gstreamer-plugins-bad-1.22.0-lp155.3.4.1.aarch64.rpm"/>
                          <format>
                            <rpm:license>LGPL-2.1-or-later</rpm:license>
                            <rpm:vendor>openSUSE</rpm:vendor>
                            <rpm:group>Productivity/Multimedia/Other</rpm:group>
                            <rpm:buildhost>armbuild26</rpm:buildhost>
                            <rpm:sourcerpm>gstreamer-plugins-bad-1.22.0-lp155.3.4.1.src.rpm</rpm:sourcerpm>
                            <rpm:header-range start="6200" end="149568"/>
                            <rpm:provides>
                              <rpm:entry name="gst-plugins-bad" flags="EQ" epoch="0" ver="1.22.0"/>
                              <rpm:entry name="gstreamer-plugins-bad" flags="EQ" epoch="0" ver="1.22.0" rel="lp155.3.4.1"/>
                            </rpm:provides>
                            <rpm:requires>
                              <rpm:entry name="glib2-tools" pre="1"/>
                              <rpm:entry name="glib2-tools"/>
                            </rpm:requires>
                            <rpm:obsoletes>
                              <rpm:entry name="libgstvdpau" flags="LT" epoch="0" ver="1.18.0"/>
                            </rpm:obsoletes>
                            <rpm:enhances>
                              <rpm:entry name="gstreamer"/>
                            </rpm:enhances>
                          </format>
                        </package>
                        </metadata>
                    """
        package_data = {
            "package_size": "2197472",
            "checksum": "5f32047b55c0ca2dcc00a00270cd0b10df4df40c6cd9355eeff9b6aa0997657b",
            "checksum_type": "sha256",
            "header_start": "6200",
            "header_end": "149568",
        }
        package_header_data = {
            "name": "gstreamer-plugins-bad",
            "arch": "aarch64",
            "version": "1.22.0",
            "release": "lp155.3.4.1",
            "epoch": "0",
            "summary": "GStreamer Streaming-Media Framework Plug-Ins",
            "description": "GStreamer is a streaming media framework based on graphs of filters\n\
                        that operate on media data...",
            "packager": "http://bugs.opensuse.org",
            "url": "https://gstreamer.freedesktop.org",
            "buildtime": datetime.datetime.fromtimestamp(float(1696852591)),
            "installed_size": "11946385",
            "archivesize": int(11965968),
            "license": "LGPL-2.1-or-later",
            "vendor": "openSUSE",
            "group": "Productivity/Multimedia/Other",
            "buildhost": "armbuild26",
            "sourcerpm": "gstreamer-plugins-bad-1.22.0-lp155.3.4.1.src.rpm",
        }

        compressed_primary = gzip.compress(bytes(primary_xml, "utf-8"))
        primary_xml_file_obj = io.BytesIO(compressed_primary)

        # Parse the test package md
        primary_parser = PrimaryParser(primary_xml_file_obj)
        parsed_packages = list(primary_parser.parse_primary())

        # Assertions
        test_package = parsed_packages[0]
        test_package_elements = test_package.keys()
        test_package_hdr_elements = test_package["header"].keys()
        self.assertEqual(len(parsed_packages), 1)

        # Check package data
        for elt, val in package_data.items():
            self.assertTrue(elt in test_package_elements)
            self.assertEqual(test_package[elt], val)

        # Check package's header data
        for elt, val in package_header_data.items():
            self.assertTrue(elt in test_package_hdr_elements)
            self.assertEqual(test_package["header"][elt], val)

        # Check package's capabilities
        for dep in complex_attrs:
            for attribute in (
                ("name", "version", "flags")
                if dep != "changelog"
                else ("name", "text", "time")
            ):
                dep_mapped_name = map_dependency_attribute(dep, attribute)
                if dep == "provides" or dep == "requires":
                    self.assertEqual(len(test_package["header"][dep_mapped_name]), 2)
                    continue
                if dep == "obsoletes" or dep == "enhances":
                    self.assertEqual(len(test_package["header"][dep_mapped_name]), 1)
                    continue

                # Else: non-existing attributes
                self.assertEqual(len(test_package["header"][dep_mapped_name]), 0)

    def test_parse_file_lists(self):
        """
        Test the parsing functionality for rpm's filelists.xml metadata file
        """

        file_lists_xml = """<?xml version="1.0" encoding="UTF-8"?>
                            <filelists xmlns="http://linux.duke.edu/metadata/filelists" packages="2">
                            <package pkgid="5f32047b55c0ca2dcc00a00270cd0b10df4df40c6cd9355eeff9b6aa0997657b" name="gstreamer-plugins-bad" arch="aarch64">
                            <version epoch="0" ver="1.1.0" rel="lp155.3.4.1"/>
                             <file>/usr/lib64/gstreamer-1.0/libgstaiff.so</file>
                             <file>/usr/lib64/gstreamer-1.0/libgstaccurip.so</file>
                             <file>/usr/lib64/gstreamer-1.0/libgstadpcmdec.so</file>
                             <file>/usr/lib64/gstreamer-1.0/libgstadpcmenc.so</file>
                            </package>
                            <package pkgid="5aac91b3ec4b358b22fe50cf0a23d6ea6139ca2f1909f99b6c3b5734ca12530f" name="gstreamer-plugins-bad" arch="ppc64le">
                            <version epoch="0" ver="2.2.0" rel="lp155.3.4.1"/>
                             <file>/usr/lib64/gstreamer-1.0/libgstaccurip.so</file>
                             <file>/usr/lib64/gstreamer-1.0/libgstadpcmdec.so</file>
                             <file>/usr/lib64/gstreamer-1.0/libgstadpcmenc.so</file>
                            </package>
                            </filelists>"""
        compressed_file_lists = gzip.compress(bytes(file_lists_xml, "utf-8"))
        file_lists_xml_obj_file = io.BytesIO(compressed_file_lists)

        file_lists_parser = FilelistsParser(file_lists_xml_obj_file)
        file_lists_parser.parse_filelists()

        package_files_5f32 = file_lists_parser.get_package_filelist(
            "5f32047b55c0ca2dcc00a00270cd0b10df4df40c6cd9355eeff9b6aa0997657b"
        )
        package_files_5aac = file_lists_parser.get_package_filelist(
            "5aac91b3ec4b358b22fe50cf0a23d6ea6139ca2f1909f99b6c3b5734ca12530f"
        )

        # Delete the cached files
        file_lists_parser.clear_cache()

        # First package
        self.assertEqual(
            package_files_5f32["pkgid"],
            "5f32047b55c0ca2dcc00a00270cd0b10df4df40c6cd9355eeff9b6aa0997657b",
        )
        self.assertEqual(len(package_files_5f32["files"]), 4)
        self.assertEqual(package_files_5f32["version"], "1.1.0")
        self.assertEqual(package_files_5f32["epoch"], "0")
        self.assertEqual(package_files_5f32["release"], "lp155.3.4.1")
        self.assertEqual(
            package_files_5f32["files"][0], "/usr/lib64/gstreamer-1.0/libgstaiff.so"
        )

        # Second package
        self.assertEqual(
            package_files_5aac["pkgid"],
            "5aac91b3ec4b358b22fe50cf0a23d6ea6139ca2f1909f99b6c3b5734ca12530f",
        )
        self.assertEqual(len(package_files_5aac["files"]), 3)
        self.assertEqual(package_files_5aac["version"], "2.2.0")
        self.assertEqual(package_files_5aac["epoch"], "0")
        self.assertEqual(package_files_5aac["release"], "lp155.3.4.1")
        self.assertEqual(
            package_files_5aac["files"][0], "/usr/lib64/gstreamer-1.0/libgstaccurip.so"
        )

    def test_parse_metadata(self):
        """TODO: both valid primary and filelists
        Create header with full information
        """

        # Primary test file
        with open("primary-sample-2-.xml", "r", encoding="utf-8") as primary_xml:
            # Convert it to gzip format
            compressed_primary = gzip.compress(bytes(primary_xml.read(), "utf-8"))
            primary_xml_file_obj = io.BytesIO(compressed_primary)

        # FileLists test file
        with open("filelists-sample-2-.xml", "r", encoding="utf-8") as file_lists_xml:
            # Convert it to gzip format
            compressed_file_lists = gzip.compress(bytes(file_lists_xml.read(), "utf-8"))
            file_lists_xml_obj_file = io.BytesIO(compressed_file_lists)

        primary_parser = PrimaryParser(primary_xml_file_obj)
        file_lists_parser = FilelistsParser(file_lists_xml_obj_file)
        rpm_metadata_parser = MetadataParser(primary_parser, file_lists_parser)
        package_gen = rpm_metadata_parser.parse_packages_metadata()

        package_gen = rpm_metadata_parser.parse_packages_metadata()
        package1 = next(package_gen)
        package2 = next(package_gen)

        # First pacakge
        pkgid = package1["checksum"]
        self.assertEqual(
            pkgid, "5f32047b55c0ca2dcc00a00270cd0b10df4df40c6cd9355eeff9b6aa0997657b"
        )
        self.assertEqual(package1["header"]["name"], "gstreamer-plugins-bad")
        # files
        self.assertEqual(len(package1["header"]["filenames"]), 8)
        self.assertEqual(
            package1["header"]["filenames"][0],
            file_lists_parser.get_package_filelist(pkgid)["files"][0],
        )
        # capabilities
        self.assertEqual(len(package1["header"]["provides"]), 5)
        self.assertEqual(len(package1["header"]["provideversion"]), 5)
        self.assertEqual(len(package1["header"]["provideflags"]), 5)

        self.assertEqual(len(package1["header"]["requirename"]), 7)
        self.assertEqual(len(package1["header"]["requireversion"]), 7)
        self.assertEqual(len(package1["header"]["requireflags"]), 7)

        self.assertEqual(len(package1["header"][5055]), 1)  # 'enhancesname'
        self.assertEqual(len(package1["header"][5056]), 1)  # 'enhancesversion'
        self.assertEqual(len(package1["header"][5057]), 1)  # 'enhancesflags'

        self.assertEqual(len(package1["header"]["obsoletename"]), 1)  # 'enhancesflags'
        self.assertEqual(
            len(package1["header"]["obsoleteversion"]), 1
        )  # 'enhancesflags'
        self.assertEqual(len(package1["header"]["obsoleteflags"]), 1)  # 'enhancesflags'

        # Second pacakge
        pkgid2 = package2["checksum"]
        self.assertEqual(
            pkgid2, "5aac91b3ec4b358b22fe50cf0a23d6ea6139ca2f1909f99b6c3b5734ca12530f"
        )

        file_lists_parser.clear_cache()

    def test_insert_batch_into_db(self):
        """
        TODO: create a sample batch of package and verify it has been inserted correctly
        """


if __name__ == "__main__":
    unittest.main()
