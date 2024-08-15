#  pylint: disable=missing-module-docstring
import datetime
import gzip
import io
import time
import unittest

from more_itertools import ilen
from psycopg2 import errors
from psycopg2.errorcodes import UNIQUE_VIOLATION

from lzreposync.deb_metadata_parser import DEBMetadataParser
from lzreposync.filelists_parser import FilelistsParser
from lzreposync.import_utils import import_packages_in_batch_from_parser
from lzreposync.packages_parser import PackagesParser
from lzreposync.primary_parser import (
    PrimaryParser,
)
from lzreposync.repo_dto import RepoDTO
from lzreposync.rpm_metadata_parser import RPMMetadataParser
from lzreposync.translation_parser import TranslationParser
from spacewalk.server import rhnSQL, rhnChannel
from spacewalk.server.rhnChannel import channel_info
from uyuni.common import fileutils


def test_verify_signature():
    """TODO"""


def _get_all_arches():
    """
    return the list of all compatible packages' arches regardless of the channel arch
    """
    rhnSQL.initDB()
    h = rhnSQL.prepare(
        """
    SELECT pa.label FROM rhnchannelpackagearchcompat cpac, rhnpackagearch pa 
    WHERE pa.id = cpac.package_arch_id"""
    )
    h.execute()
    all_arches = list(map(lambda d: d.get("label"), h.fetchall_dict()))
    rhnSQL.closeDB()
    return all_arches


def _get_repository_by_channel_label(channel_label):
    """
    Fetch the first matching repository's information of a given channel form the database
    return the corresponding RepoDTO object
    """
    rhnSQL.initDB()
    h = rhnSQL.prepare(
        """
        select c.label as channel_label, s.id, c_ark.name as channel_arch, s.source_url, s.metadata_signed, s.label as repo_label, cst.label as repo_type_label
        from rhnChannel c,
             rhnChannelArch c_ark,
             rhnContentSource s,
             rhnChannelContentSource cs,
             rhnContentSourceType cst
        where c.label = :channel_label
          and c.channel_arch_id = c_ark.id
          and s.id = cs.source_id
          and cst.id = s.type_id
          and cs.channel_id = c.id"""
    )
    h.execute(channel_label=channel_label)
    source = h.fetchone_dict()
    repository = None
    if source:
        repository = RepoDTO(
            channel_label=source["channel_label"],
            repo_id=source["id"],
            channel_arch=source["channel_arch"],
            repo_label=source["repo_label"],
            repo_type=source["repo_type_label"],
            source_url=source["source_url"],
            metadata_signed=source["metadata_signed"],
        )
    else:
        print(f"INFO: Couldn't find repo for channel {channel_label}")

    rhnSQL.closeDB()
    return repository


# stolen from python/spacewalk/server/test/misc_functions.py
def _create_channel(label, channel_family, org_id=None, channel_arch=None):
    vdict = _new_channel_dict(
        label=label,
        channel_family=channel_family,
        org_id=org_id,
        channel_arch=channel_arch,
    )
    c = rhnChannel.Channel()
    c.load_from_dict(vdict)
    c.save()
    return c


# stolen from python/spacewalk/server/test/misc_functions.py
def _new_channel_dict(**kwargs):
    # pylint: disable-next=invalid-name
    _counter = 0

    label = kwargs.get("label")
    if label is None:
        # pylint: disable-next=consider-using-f-string
        label = "rhn-unittest-%.3f-%s" % (time.time(), _counter)
        # pylint: disable-next=invalid-name
        _counter = _counter + 1

    release = kwargs.get("release") or "release-" + label
    # pylint: disable-next=redefined-outer-name
    os = kwargs.get("os") or "Unittest Distro"
    if "org_id" in kwargs:
        # pylint: disable-next=unused-variable
        org_id = kwargs["org_id"]
    else:
        org_id = "rhn-noc"

    vdict = {
        "label": label,
        "name": kwargs.get("name") or label,
        "summary": kwargs.get("summary") or label,
        "description": kwargs.get("description") or label,
        "basedir": kwargs.get("basedir") or "/",
        "channel_arch": kwargs.get("channel_arch") or "i386",
        "channel_families": [kwargs.get("channel_family") or label],
        "org_id": kwargs.get("org_id"),
        "gpg_key_url": kwargs.get("gpg_key_url"),
        "gpg_key_id": kwargs.get("gpg_key_id"),
        "gpg_key_fp": kwargs.get("gpg_key_fp"),
        "end_of_life": kwargs.get("end_of_life"),
        "dists": [
            {
                "release": release,
                "os": os,
            }
        ],
    }
    return vdict


def _empty_database():
    # TODO: there are other tables that can be freed up
    rhnSQL.initDB()
    delete_channel_package_sql = """
            DELETE FROM rhnChannelPackage"""
    delete_channel_sql = """
            DELETE FROM rhnChannel"""
    delete_package_sql = """
            DELETE FROM rhnPackage"""
    delete_content_source_sql = """
            DELETE FROM rhnContentSource"""
    delete_channel_content_source_sql = """
            DELETE FROM rhnContentSource"""
    rhnSQL.execute(delete_channel_package_sql)
    rhnSQL.execute(delete_package_sql)
    rhnSQL.execute(delete_channel_sql)
    rhnSQL.execute(delete_content_source_sql)
    rhnSQL.execute(delete_channel_content_source_sql)
    rhnSQL.commit()
    rhnSQL.closeDB()


class LazyRepoSyncTest(unittest.TestCase):

    def setUp(self):
        # Start with a fresh/empty database
        _empty_database()

    def tearDown(self):
        # Empty database
        # _empty_database()
        ...

    @staticmethod
    def _init_channel(test_channel_label, channel_arch, org_id=1):
        """
        Create a new test channel using the channel family private-channel-family-1
        """
        rhnSQL.initDB()
        try:
            # Channel family "private-channel-family-1" is automatically created when starting the susemanager docker db
            channel_family_label = "private-channel-family-1"

            # Create a new channel using the channel family info
            _create_channel(
                test_channel_label,
                channel_family_label,
                org_id=org_id,
                channel_arch=channel_arch,
            )
            rhnSQL.commit()
        except errors.lookup(UNIQUE_VIOLATION):
            print(f"INFO: Channel {test_channel_label} already exists!")
        finally:
            rhnSQL.closeDB()

    @staticmethod
    def _create_content_source(
        channel_label,
        repo_label,
        source_url,
        metadata_signed="N",
        org_id=1,
        source_type="yum",
        repo_id=1,
    ):
        """
        Create a new content source and associate it with the given channel
        source_type: yum|deb
        """
        try:
            rhnSQL.initDB()
            fetch_source_type_query = rhnSQL.prepare(
                """
                SELECT id from rhnContentSourceType where label = :source_type_label"""
            )
            fetch_source_type_query.execute(source_type_label=source_type)
            type_id = fetch_source_type_query.fetchone_dict()["id"]

            add_repo_query = rhnSQL.prepare(
                """INSERT INTO rhnContentSource(id, org_id, type_id, source_url, label, metadata_signed) VALUES (:repo_id, :org_id, 
                :type_id, :source_url, :label, :metadata_signed) 
                """
            )
            add_repo_query.execute(
                repo_id=repo_id,
                org_id=org_id,
                type_id=type_id,
                source_url=source_url,
                label=repo_label,
                metadata_signed=metadata_signed,
            )

            fetch_source_id_query = rhnSQL.prepare(
                """
                SELECT id from rhnContentSource LIMIT 1"""
            )
            fetch_source_id_query.execute()
            source_id = fetch_source_id_query.fetchone_dict()["id"]

            # associate the source/repo with the channel
            fetch_channel_id_query = rhnSQL.prepare(
                """
                    SELECT id FROM rhnChannel WHERE label = :channel_label"""
            )
            fetch_channel_id_query.execute(channel_label=channel_label)
            channel_id = fetch_channel_id_query.fetchone_dict()["id"]

            associate_repo_channel_query = rhnSQL.prepare(
                """INSERT INTO rhnChannelContentSource(source_id, channel_id) VALUES (:source_id, :channel_id)
                """
            )
            associate_repo_channel_query.execute(
                source_id=source_id, channel_id=channel_id
            )
            rhnSQL.commit()
        except errors.lookup(UNIQUE_VIOLATION):
            print(f"INFO: Source {repo_label} already exists!")
        finally:
            rhnSQL.closeDB()

    @staticmethod
    def _get_channel_info(channel_label):
        rhnSQL.initDB()
        channel = channel_info(channel_label)
        rhnSQL.commit()
        rhnSQL.closeDB()
        return channel

    def test_parse_filelists(self):
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
                             <file type="dir">/etc/dirsrv</file>
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
        self.assertEqual(len(package_files_5f32["files"]), 5)
        self.assertEqual(package_files_5f32["version"], "1.1.0")
        self.assertEqual(package_files_5f32["epoch"], "0")
        self.assertEqual(package_files_5f32["release"], "lp155.3.4.1")
        self.assertEqual(
            package_files_5f32["files"][0], "/usr/lib64/gstreamer-1.0/libgstaiff.so"
        )
        self.assertEqual(32768, package_files_5f32["filetypes"][0])
        self.assertEqual(16384, package_files_5f32["filetypes"][-1])

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
        primary_parser = PrimaryParser("tests/test-files/primary-sample-2.xml.gz")
        file_lists_parser = FilelistsParser(
            "tests/test-files/filelists-sample-2.xml.gz"
        )
        rpm_metadata_parser = RPMMetadataParser(primary_parser, file_lists_parser)
        package_gen = rpm_metadata_parser.parse_packages_metadata()
        package1 = next(package_gen)
        package2 = next(package_gen)
        pkgid = package1["checksum"]
        pkgid2 = package2["checksum"]

        # First pacakge
        self.assertEqual(
            pkgid, "5f32047b55c0ca2dcc00a00270cd0b10df4df40c6cd9355eeff9b6aa0997657b"
        )
        self.assertEqual(package1["name"], "gstreamer-plugins-bad")
        # capabilities
        self.assertEqual(5, len(package1["provides"]))
        self.assertEqual(6, len(package1["requires"]))
        self.assertEqual(1, len(package1["enhances"]))
        self.assertEqual(1, len(package1["obsoletes"]))
        # Second pacakge
        self.assertEqual(
            pkgid2, "5aac91b3ec4b358b22fe50cf0a23d6ea6139ca2f1909f99b6c3b5734ca12530f"
        )

        file_lists_parser.clear_cache()

    def test_arch_filter_primary(self):
        """
        Test the primary.xml parsing functionality with the 'arch' filter
        """

        primary_gz_test = "tests/test-files/primary-sample-10.xml.gz"
        archs = {
            "x86_64": 1,
            "aarch64": 2,
            "ppc64le": 4,
            "noarch": 3,
            "(x86_64|aarch64)": 3,
            "(ppc64le|noarch)": 7,
            ".*": 10,
        }
        for arch, count in archs.items():
            primary_parser = PrimaryParser(primary_gz_test, arch_filter=arch)
            parsed_packages = list(primary_parser.parse_primary())
            self.assertEqual(len(parsed_packages), count)

    def test_arch_filter_file_lists(self):
        """
        Test the filelists.xml parsing functionality with the 'arch' filter
        """
        file_lists_gz_test = "tests/test-files/filelists-sample-10.xml.gz"
        archs = {
            "aarch64": 2,
            "ppc64le": 2,
            "src": 2,
            "x86_64": 2,
            "s390x": 2,
            "(ppc64le|s390x|src)": 6,
            ".*": 10,
        }
        for arch, count in archs.items():
            file_lists_parser = FilelistsParser(file_lists_gz_test, arch_filter=arch)
            file_lists_parser.parse_filelists()
            self.assertEqual(file_lists_parser.num_parsed_packages, count)

            file_lists_parser.clear_cache()

    def test_parse_primary_file_centos_stream_9(self):
        """
        Test the primary.xml parser on centos stream 9
        """
        primary_gz = "tests/test-files/centos-stream-9-primary.xml.gz"
        # The existing archs and their counts
        arch_count = {
            "x86_64": 8788,
            "noarch": 5340,
            "i686": 2574,
        }
        for arch, count in arch_count.items():
            primary_parser = PrimaryParser(primary_gz, arch_filter=arch)
            parsed_packages = primary_parser.parse_primary()
            self.assertEqual(ilen(parsed_packages), count)
            del primary_parser
            del parsed_packages

        # Check first package metadata
        primary_parser = PrimaryParser(primary_gz)
        parsed_packages = primary_parser.parse_primary()
        package = next(parsed_packages)

        self.assertEqual(
            package["checksum"],
            "a40e4ffc68c318223c4bd2633a561aedd5e9d29b533587a01d7c397de1d8ada4",
        )
        self.assertEqual(package["package_size"], "2894740")
        self.assertEqual(package["header"]["name"], "389-ds-base")
        self.assertEqual(package["header"]["arch"], "x86_64")
        self.assertEqual(package["header"]["version"], "2.4.5")
        self.assertEqual(package["header"]["epoch"], "0")
        self.assertEqual(package["header"]["release"], "3.el9")
        self.assertEqual(package["header"]["summary"], "389 Directory Server (base)")
        self.assertEqual(
            package["header"]["description"],
            "389 Directory Server is an LDAPv3 compliant server.  The base "
            "package includes\nthe LDAP server and command line utilities for "
            "server administration.",
        )
        self.assertEqual(package["header"]["packager"], "builder@centos.org")
        self.assertEqual(package["header"]["url"], "https://www.port389.org")
        self.assertEqual(
            package["header"]["buildtime"],
            datetime.datetime.fromtimestamp(float(1705913669)),
        )
        self.assertEqual(package["header"]["size"], "11450211")
        self.assertEqual(package["header"]["archivesize"], int(11490736))
        self.assertEqual(package["header"]["group"], "Unspecified")
        self.assertEqual(
            package["header"]["buildhost"], "x86-03.stream.rdu2.redhat.com"
        )

        # Check package's capabilities
        self.assertEqual(len(package["header"]["provides"]), 149)
        self.assertEqual(len(package["header"]["requirename"]), 95)
        self.assertEqual(len(package["header"]["conflictname"]), 2)
        self.assertEqual(len(package["header"]["obsoletename"]), 4)

    def test_parse_filelists_file_centos_stream_9(self):

        filelists_gz = "tests/test-files/centos-stream-9-filelists.xml.gz"
        test_package = {
            "pkgid": "a40e4ffc68c318223c4bd2633a561aedd5e9d29b533587a01d7c397de1d8ada4",
            "version": "2.4.5",
            "release": "3.el9",
        }

        filelists_parser = FilelistsParser(filelists_file=filelists_gz)
        filelists_parser.parse_filelists()
        pacakge = filelists_parser.get_package_filelist(pkgid=test_package["pkgid"])

        self.assertEqual(filelists_parser.num_parsed_packages, 16702)
        self.assertIsNotNone(pacakge["pkgid"])
        self.assertEqual(pacakge["version"], test_package["version"])
        self.assertEqual(pacakge["release"], test_package["release"])

        filelists_parser.clear_cache()

    def test_parse_and_import_centos_stream_9_repo(self):
        """
        Test parsing and importing repository metadata of centos stream 9
        source: https://mirror.stream.centos.org/9-stream/AppStream/x86_64/os/ (But we're using local files)
        """

        test_channel_label = "test_channel"
        num_packages = 16702
        test_repo_label = "centos_stream_9"
        test_repo_url = "https://mirror.stream.centos.org/9-stream/AppStream/x86_64/os/"
        channel_arch = "x86_64"  # This is only the channel arch, the imported packages could be of different archs

        self._init_channel(test_channel_label, channel_arch=channel_arch)
        channel = self._get_channel_info(test_channel_label)
        self._create_content_source(
            channel_label=test_channel_label,
            repo_label=test_repo_label,
            source_url=test_repo_url,
        )
        compatible_arches = _get_all_arches()

        primary_gz = "tests/test-files/centos-stream-9-primary.xml.gz"
        filelists_gz = "tests/test-files/centos-stream-9-filelists.xml.gz"
        primary_parser = PrimaryParser(primary_gz)
        filelists_parser = FilelistsParser(filelists_gz)
        rpm_metadata_parser = RPMMetadataParser(
            primary_parser=primary_parser, filelists_parser=filelists_parser
        )
        import_packages_in_batch_from_parser(
            rpm_metadata_parser,
            batch_size=20,
            channel=channel,
            compatible_arches=compatible_arches,
        )
        filelists_parser.clear_cache()

        rhnSQL.initDB()
        # Check imported packages
        packages_count_query = rhnSQL.prepare("SELECT count(id) from rhnPackage")
        packages_count_query.execute()
        packages_count = packages_count_query.fetchone()[0]

        # Check associated packages to channel
        channel_packages_count_query = rhnSQL.prepare(
            "SELECT count(*) FROM rhnChannelPackage WHERE channel_id = :channel_id"
        )
        channel_packages_count_query.execute(channel_id=channel["id"])
        channel_packages_count = channel_packages_count_query.fetchone()[0]

        self.assertEqual(num_packages, packages_count)
        self.assertEqual(num_packages, channel_packages_count)

        rhnSQL.commit()
        rhnSQL.closeDB()

    def test_parse_and_import_videolan_tumbleweed_repo(self):
        """
        Test parsing and importing repository metadata of tumbleweed
        source: http://download.videolan.org/SuSE/Tumbleweed/ (But we're using local files)
        """

        test_channel_label = "test_channel"
        num_packages = 349  # ignoring the 15 src packages
        test_repo_label = "videolan_Tumbleweed"
        test_repo_url = "http://download.videolan.org/SuSE/Tumbleweed/"
        channel_arch = "x86_64"  # This is only the channel arch, the imported packages could be of different archs
        batch_size = 20

        # TODO: where are ['packager'] information stored in the db
        first_package_info = {
            "checksum": "213fb460c2aed866c2c3b8eef964329c81812aa862f86d076b6637e50918b23f",
            "name": "dcatools",
            "arch": "i586",
            "version": "0.0.7+2",
            "epoch": "0",
            "release": "7.15",
            "summary": "a free DTS Coherent Acoustics decoder",
            "description": "libdca is a free library for decoding DTS Coherent Acoustics streams.\nThe code is "
            "written by Gildas Bazin and was based on the a52dec project.\nIt is released under the "
            "terms of the GPL license.",
            "buildtime": datetime.datetime.fromtimestamp(float(1716978620)),
            "package_size": 25781,
            "installed_size": 49890,
            "archivesize": 51128,
            "buildhost": "reproducible",
            "header_start": 4664,
            "header_end": 10225,
            "license": "GPL-2.0-or-later",
            "vendor": "VideoLAN Project (http://www.videolan.org)",
            # "sourcerpm": "libdca-0.0.7+2-7.15.src.rpm",  # source_rpm_id value in rhnSourceRpm was always null
            "group": "Productivity/Multimedia/Other",
            "provides": ["dcatools", "dcatools(x86-32)"],
            "requires": [
                "/bin/sh",
                "libm.so.6",
                "libm.so.6(GLIBC_2.29)",
                "libc.so.6(GLIBC_2.34)",
                "libdca.so.0",
            ],
        }

        self._init_channel(test_channel_label, channel_arch=channel_arch)
        channel = self._get_channel_info(test_channel_label)
        self._create_content_source(
            channel_label=test_channel_label,
            repo_label=test_repo_label,
            source_url=test_repo_url,
        )
        compatible_arches = _get_all_arches()

        primary_gz = (
            "tests/test-files/c12180-tumbleweed-primary.xml.gz"
        )
        filelists_gz = (
            "tests/test-files/42b53c-tumbleweed-filelists.xml.gz"
        )

        primary_parser = PrimaryParser(primary_gz)
        filelists_parser = FilelistsParser(filelists_gz)

        rpm_metadata_parser = RPMMetadataParser(
            primary_parser=primary_parser, filelists_parser=filelists_parser
        )
        import_packages_in_batch_from_parser(
            rpm_metadata_parser,
            batch_size=batch_size,
            channel=channel,
            compatible_arches=compatible_arches,
        )

        rhnSQL.initDB()
        # Check imported packages
        packages_count_query = rhnSQL.prepare(
            """SELECT count(id) from rhnPackage
            """
        )
        packages_count_query.execute()
        packages_count = packages_count_query.fetchone()[0]

        # Check associated packages to channel
        channel_packages_count_query = rhnSQL.prepare(
            """
            SELECT count(*) FROM rhnChannelPackage WHERE channel_id = :channel_id"""
        )
        channel_packages_count_query.execute(channel_id=channel["id"])
        channel_packages_count = channel_packages_count_query.fetchone()[0]

        fetch_package_sql = """
                    select p.id, p.summary, p.description, p.vendor, p.copyright, p.remote_path, p.package_size,
                    p.payload_size, p.installed_size, p.build_host, p.build_time, p.header_start, p.header_end,
                    ct.label as checksum_type, ck.checksum, pn.name, pevr.epoch, pevr.version, pevr.release,
                    pg.name as group, pa.name arch

                    from rhnPackage p,
                         rhnChecksumType ct,
                         rhnChecksum ck,
                         rhnPackageName pn,
                         rhnPackageEvr pevr,
                         rhnPackageGroup pg,
                         rhnPackageArch pa
                    where ck.checksum = :pkg_checksum
                      and p.checksum_id = ck.id
                      and ck.checksum_type_id = ct.id
                      and p.name_id = pn.id
                      and p.evr_id = pevr.id
                      and p.package_group = pg.id
                      and p.package_arch_id = pa.id
                    """

        fetched_package = rhnSQL.fetchone_dict(
            fetch_package_sql, pkg_checksum=first_package_info["checksum"]
        )

        self.assertEqual(num_packages, packages_count)
        self.assertEqual(num_packages, channel_packages_count)
        self.assertIsNotNone(fetched_package)
        self.assertEqual(
            first_package_info["checksum"], fetched_package.get("checksum")
        )
        self.assertEqual(first_package_info["name"], fetched_package.get("name"))
        self.assertEqual(first_package_info["arch"], fetched_package.get("arch"))
        self.assertEqual(first_package_info["version"], fetched_package.get("version"))
        self.assertEqual(first_package_info["epoch"], fetched_package.get("epoch"))
        self.assertEqual(first_package_info["release"], fetched_package.get("release"))
        self.assertEqual(first_package_info["summary"], fetched_package.get("summary"))
        self.assertEqual(
            first_package_info["description"], fetched_package.get("description")
        )
        self.assertEqual(
            first_package_info["buildtime"], fetched_package.get("build_time")
        )
        self.assertEqual(
            first_package_info["package_size"], fetched_package.get("package_size")
        )
        self.assertEqual(
            first_package_info["archivesize"], fetched_package.get("payload_size")
        )
        self.assertEqual(
            first_package_info["installed_size"], fetched_package.get("installed_size")
        )
        self.assertEqual(
            first_package_info["buildhost"], fetched_package.get("build_host")
        )
        self.assertEqual(
            first_package_info["header_start"], fetched_package.get("header_start")
        )
        self.assertEqual(
            first_package_info["license"], fetched_package.get("copyright")
        )
        self.assertEqual(first_package_info["vendor"], fetched_package.get("vendor"))
        self.assertEqual(first_package_info["group"], fetched_package.get("group"))

        # Checking capabilities of first pacakge
        fetch_capability_sql = """
                select pc.name as capability from rhnPackage{} pp, rhnPackageCapability pc, rhnPackage p, rhnChecksum pck 
                where p.checksum_id = pck.id and pp.capability_id = pc.id and pp.package_id = p.id 
                and pck.checksum = :pkg_checksum"""
        for cap in ("provides", "requires"):
            result = rhnSQL.execute(
                fetch_capability_sql.format(cap),
                pkg_checksum=first_package_info["checksum"],
            )
            cap_result_list = result.fetchall_dict()
            capabilities = list(map(lambda d: d.get("capability"), cap_result_list))
            package_cap = first_package_info[cap]
            self.assertTrue(capabilities, package_cap)

        # Checking files
        test_pkg_checksum = "d3c2384fd95d4f1b834a7d98fa331b9d31a09860e3ea0b5a742b7a83356c77ae"  # random package
        fetch_files_sql = """
        select pc.name capability, pf.file_mode from rhnPackageFile pf, rhnPackageCapability pc, rhnPackage p, 
        rhnChecksum pck where pf.package_id = p.id and p.checksum_id = pck.id 
        and pck.checksum = :pkg_checksum
        and pc.id = pf.capability_id"""
        result = rhnSQL.execute(fetch_files_sql, pkg_checksum=test_pkg_checksum)
        files_list = result.fetchall_dict()
        self.assertEqual(105, len(files_list))
        self.assertEqual("/usr/src/debug/faad2-2_10_0", files_list[0].get("capability"))
        self.assertEqual(16384, files_list[0].get("file_mode"))
        self.assertEqual(
            "/usr/src/debug/faad2-2_10_0/frontend/audio.c",
            files_list[2].get("capability"),
        )
        self.assertEqual(32768, files_list[2].get("file_mode"))

        rhnSQL.commit()
        rhnSQL.closeDB()
        filelists_parser.clear_cache()
        # TODO Test erratum/patches (or not..it's already tested in reposync and it's the exact same code)

    def test_parse_packages_file_ubuntu_jammy_main_amd64(self):
        """
        Test the parse of the Packages md file of ubuntu-jammy-main-amd64 repo:
        here;s the repo's url: https://ubuntu.mirrors.uk2.net/ubuntu/dists/jammy/main/binary-amd64/
        """
        packages_file = fileutils.decompress_open(
            "tests/test-files/Packages_ubuntu_jammy_main_amd64.gz"
        )
        num_packages = 6090
        repository = "https://ubuntu.mirrors.uk2.net/ubuntu"
        packages_parser = PackagesParser(
            packages_file=packages_file, repository=repository
        )
        packages_gen = packages_parser.parse_packages()

        self.assertEqual(num_packages, ilen(packages_gen))
        del packages_parser
        del packages_gen
        packages_file.close()
        packages_file = fileutils.decompress_open(
            "tests/test-files/Packages_ubuntu_jammy_main_amd64.gz"
        )

        # Create the parser again because the generator has been consumed by ilen()
        packages_parser = PackagesParser(
            packages_file=packages_file, repository=repository
        )
        packages_gen = packages_parser.parse_packages()
        package_one = next(packages_gen)
        self.assertEqual(package_one.get("name"), "accountsservice")
        self.assertEqual(package_one.get("arch"), "amd64-deb")
        self.assertEqual(
            package_one.get("summary"), "query and manipulate user account information"
        )
        self.assertEqual(package_one.get("version"), "22.07.5")
        self.assertEqual(package_one.get("release"), "2ubuntu1")
        self.assertEqual(package_one.get("recommends"), "default-logind | logind")
        self.assertEqual(package_one.get("suggests"), "gnome-control-center")
        self.assertEqual(
            package_one.get("remote_path"),
            "https://ubuntu.mirrors.uk2.net/ubuntu/pool/main/a/accountsservice/accountsservice_22.07.5-2ubuntu1_amd64.deb",
        )

        packages_file.close()

    def test_parse_translation_file_ubuntu_jammy_main_amd64(self):
        """
        Test the parse of the Translation description file of ubuntu-jammy-main-amd64 repo:
        here;s the repo's url: https://ubuntu.mirrors.uk2.net/ubuntu/dists/jammy/main/binary-amd64/
        """
        translation_file = fileutils.decompress_open(
            "tests/test-files/Translation_ubuntu_jammy_main_amd64.gz"
        )
        packages_file = fileutils.decompress_open(
            "tests/test-files/Packages_ubuntu_jammy_main_amd64.gz"
        )
        first_package_description_md5 = "8aeed0a03c7cd494f0c4b8d977483d7e"
        first_package_description = """query and manipulate user account information\nThe AccountService project provides a set of D-Bus\ninterfaces for querying and manipulating user account\ninformation and an implementation of these interfaces,\nbased on the useradd, usermod and userdel commands.\n"""
        translation_parser = TranslationParser(translation_file=translation_file)
        packages_parser = PackagesParser(packages_file=packages_file)

        package_one_description = (
            translation_parser.get_pacakge_description_by_description_md5(
                first_package_description_md5
            )
        )
        self.assertEqual(first_package_description, package_one_description)
        # Checking that all packages have their corresponding full description
        for package in packages_parser.parse_packages():
            self.assertIsNotNone(
                translation_parser.get_pacakge_description_by_description_md5(
                    package.get("Description-md5")
                )
            )

        translation_parser.clear_cache()
        translation_file.close()

    def test_parse_and_import_ubuntu_jammy_main_amd64(self):
        """
        Test parsing and importing repository metadata of ubuntu-jammy-main-amd64 repo
        source: https://ubuntu.mirrors.uk2.net/ubuntu/dists/jammy/main/binary-amd64/
        """

        channel_arch = "amd64-deb"
        test_channel_label = "test_channel"
        test_repo_label = "ubuntu_jammy"
        test_repo_url = (
            "https://ubuntu.mirrors.uk2.net/ubuntu/dists/jammy/main/binary-amd64/"
        )
        base_url = "https://ubuntu.mirrors.uk2.net/ubuntu/"
        batch_size = 20
        num_packages = 6090
        first_package_info = {
            "checksum": "7b8d61673a0a9cd753e09eb70b371e44",
            "name": "accountsservice",
            "arch": "amd64",
            "version": "22.07.5",
            "epoch": None,
            "release": "2ubuntu1",
            "summary": "query and manipulate user account information",
            "description": "query and manipulate user account information\nThe AccountService project provides a set of D-Bus\ninterfaces for querying and manipulating user account\ninformation and an implementation of these interfaces,\nbased on the useradd, usermod and userdel commands.\n",
            "buildtime": None,
            "package_size": 69644,
            "installed_size": None,
            "archivesize": 500,
            "remote_path": "https://ubuntu.mirrors.uk2.net/ubuntu/pool/main/a/accountsservice/accountsservice_22.07.5-2ubuntu1_amd64.deb",
            "buildhost": None,
            "header_start": -1,
            "header_end": -1,
            "license": None,
            "vendor": "Debian",
            "requires": [
                "dbus _0",
                "libaccountsservice0 _1",
                "libc6 _2",
                "libglib2.0-0 _3",
                "libpolkit-gobject-1-0 _4",
            ],
            "recommends": ["default-logind | logind_0"],
            "suggests": ["gnome-control-center_0"],
        }

        self._init_channel(test_channel_label, channel_arch=channel_arch)
        channel = self._get_channel_info(test_channel_label)
        self._create_content_source(
            channel_label=test_channel_label,
            repo_label=test_repo_label,
            source_url=test_repo_url,
        )
        compatible_arches = _get_all_arches()

        packages_file = fileutils.decompress_open(
            "tests/test-files/Packages_ubuntu_jammy_main_amd64.gz"
        )
        translation_file = fileutils.decompress_open(
            "tests/test-files/Translation_ubuntu_jammy_main_amd64.gz"
        )
        packages_parser = PackagesParser(packages_file, base_url)
        translation_parser = TranslationParser(translation_file)
        deb_metadata_parser = DEBMetadataParser(
            packages_parser=packages_parser, translation_parser=translation_parser
        )

        import_packages_in_batch_from_parser(
            deb_metadata_parser,
            batch_size=batch_size,
            channel=channel,
            compatible_arches=compatible_arches,
        )

        rhnSQL.initDB()
        # Check imported packages
        packages_count_query = rhnSQL.prepare(
            """SELECT count(id) from rhnPackage
            """
        )
        packages_count_query.execute()
        packages_count = packages_count_query.fetchone()[0]

        # Check associated packages to channel
        channel_packages_count_query = rhnSQL.prepare(
            """
                    SELECT count(*) FROM rhnChannelPackage WHERE channel_id = :channel_id"""
        )
        channel_packages_count_query.execute(channel_id=channel["id"])
        channel_packages_count = channel_packages_count_query.fetchone()[0]

        fetch_package_sql = """
                    select p.id, p.summary, p.description, p.vendor, p.copyright, p.remote_path, p.package_size, 
                    p.payload_size, p.installed_size, p.build_host, p.build_time, p.header_start, p.header_end, 
                    ct.label as checksum_type, ck.checksum, pn.name, pevr.epoch, pevr.version, pevr.release, 
                    pg.name as group, pa.name arch

                    from rhnPackage p,
                         rhnChecksumType ct,
                         rhnChecksum ck,
                         rhnPackageName pn,
                         rhnPackageEvr pevr,
                         rhnPackageGroup pg,
                         rhnPackageArch pa
                    where ck.checksum = :pkg_checksum
                      and p.checksum_id = ck.id
                      and ck.checksum_type_id = ct.id
                      and p.name_id = pn.id
                      and p.evr_id = pevr.id
                      and p.package_group = pg.id
                      and p.package_arch_id = pa.id
                    """

        fetched_package = rhnSQL.fetchone_dict(
            fetch_package_sql, pkg_checksum=first_package_info["checksum"]
        )

        print(f"Fetched_package: {fetched_package}")

        self.assertEqual(num_packages, packages_count)
        self.assertEqual(num_packages, channel_packages_count)
        self.assertIsNotNone(fetched_package)
        self.assertEqual(
            fetched_package.get("checksum"), first_package_info["checksum"]
        )
        self.assertEqual(fetched_package.get("name"), first_package_info["name"])
        # self.assertEqual(fetched_package.get("arch"), test_package_info["arch"])
        self.assertEqual(fetched_package.get("version"), first_package_info["version"])
        self.assertEqual(fetched_package.get("epoch"), first_package_info["epoch"])
        self.assertEqual(fetched_package.get("release"), first_package_info["release"])
        self.assertEqual(fetched_package.get("summary"), first_package_info["summary"])
        self.assertEqual(
            fetched_package.get("description"), first_package_info["description"]
        )
        self.assertEqual(
            fetched_package.get("build_time"), first_package_info["buildtime"]
        )
        self.assertEqual(
            fetched_package.get("package_size"), first_package_info["package_size"]
        )
        self.assertEqual(
            fetched_package.get("payload_size"), first_package_info["archivesize"]
        )
        self.assertEqual(
            fetched_package.get("installed_size"), first_package_info["installed_size"]
        )
        self.assertEqual(
            fetched_package.get("remote_path"), first_package_info["remote_path"]
        )
        self.assertEqual(
            fetched_package.get("build_host"), first_package_info["buildhost"]
        )
        self.assertEqual(
            fetched_package.get("header_start"), first_package_info["header_start"]
        )
        self.assertEqual(
            fetched_package.get("copyright"), first_package_info["license"]
        )
        self.assertEqual(fetched_package.get("vendor"), first_package_info["vendor"])

        # Checking capabilities of first pacakge
        fetch_capability_sql = """
                        select pc.name as capability from rhnPackage{} pp, rhnPackageCapability pc, rhnPackage p, rhnChecksum pck 
                        where p.checksum_id = pck.id and pp.capability_id = pc.id and pp.package_id = p.id 
                        and pck.checksum = :pkg_checksum"""
        for cap in ("requires", "recommends", "suggests"):
            result = rhnSQL.execute(
                fetch_capability_sql.format(cap),
                pkg_checksum=first_package_info["checksum"],
            )
            cap_result_list = result.fetchall_dict()
            capabilities = list(map(lambda d: d.get("capability"), cap_result_list))
            self.assertEqual(capabilities, first_package_info[cap])

        rhnSQL.commit()
        rhnSQL.closeDB()
        translation_parser.clear_cache()


if __name__ == "__main__":
    unittest.main()
