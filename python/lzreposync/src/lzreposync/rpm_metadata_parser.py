#  pylint: disable=missing-module-docstring

import logging
import sys

from lzreposync.filelists_parser import FilelistsParser
from lzreposync.primary_parser import PrimaryParser
from spacewalk.satellite_tools.syncLib import log, log2
from spacewalk.server import rhnSQL
from spacewalk.server.importlib import mpmSource


def set_fake_file_data(package, files_count):
    """
    Fake data related to the files. Eg: 'filedevices', 'fileinodes', 'filemodes', ect
    TODO: (enhancement) can we fake the data in a better way
    """
    package["header"]["filedevices"] = [1 for _ in range(files_count)]
    package["header"]["fileinodes"] = [1 for i in range(files_count)]
    # package["header"]["filemodes"] = [16877 for _ in range(files_count)]
    package["header"]["fileusername"] = [b"root" for _ in range(files_count)]
    package["header"]["filegroupname"] = [b"root" for _ in range(files_count)]
    package["header"]["filerdevs"] = [0 for _ in range(files_count)]
    package["header"]["filesizes"] = [500 for _ in range(files_count)]
    package["header"]["longfilesizes"] = [500 for _ in range(files_count)]
    package["header"]["filemtimes"] = [1712697084 for _ in range(files_count)]
    package["header"]["filemd5s"] = [
        b"9e3b73207" for _ in range(files_count)
    ]  # can be b''
    package["header"]["filelinktos"] = [b"" for _ in range(files_count)]
    package["header"]["fileflags"] = [0 for _ in range(files_count)]
    package["header"]["fileverifyflags"] = [4294967295 for _ in range(files_count)]
    package["header"]["filelangs"] = [b"" for _ in range(files_count)]

    package["header"]["rpmversion"] = "1"
    package["header"]["payloadformat"] = "cpio"
    package["header"]["cookie"] = "cookie_test"
    package["header"]["sigsize"] = 10000
    package["header"]["sigmd5"] = "sigmd5_test"

    return package


# pylint: disable-next=missing-class-docstring
class BadParserException(Exception):
    def __init__(self, parser):
        super().__init__(f"Bad Parser {parser}")


def parse_rpm_packages_metadata(
    primary_xml, filelists_xml, repository_url, cache_dir, arch_filter=".*"
):
    """
    Parse both primary.xml and filelists.xml files and yield packages metadata
    Yield an instance inheriting from spacewalk.server.importLib.IncompletePackage
    :primary_xml: in Gzip format
    :filelists_xml: in Gzip format
    """
    primary_parser = PrimaryParser(primary_xml, repository_url, arch_filter)
    filelists_parser = FilelistsParser(filelists_xml, cache_dir, arch_filter)

    # pylint: disable-next=consider-using-f-string
    log(0, " Parsing %s" % filelists_parser.filelists_file)
    # Parse and cache the content of filelists.xml
    filelists_parser.parse_filelists()

    for package in primary_parser.parse_primary():  # parse_primary() is a generator
        parsed_filenames = filelists_parser.get_package_filelist(package["checksum"])[
            "filenames"
        ]
        parsed_filetypes = filelists_parser.get_package_filelist(package["checksum"])[
            "filetypes"
        ]
        if (
            package["header"].get("filenames")
            and package["header"].get("filetypes")
            and len(package["header"].get("filenames"))
            == len(package["header"].get("filetypes"))
        ):
            # Some file information has already been parsed from the Primary file.
            # Remove duplicates
            filenames = set(
                parsed_filenames
            )  # using hash set will be faster for searching in O(1) time
            for i in range(len(package["header"].get("filenames"))):
                if package["header"].get("filenames")[i] not in filenames:
                    parsed_filenames.append(package["header"].get("filenames")[i])
                    parsed_filetypes.append(package["header"].get("filetypes")[i])

        package["header"]["filenames"] = parsed_filenames
        package["header"]["filemodes"] = parsed_filetypes
        files_count = len(package["header"]["filenames"])
        package = set_fake_file_data(package, files_count)
        logging.debug(
            # pylint: disable-next=logging-format-interpolation,consider-using-f-string
            "Yielding pacakge {}".format(package["checksum"])
        )
        # pylint: disable=W0703,W0706
        try:
            rpm_package = mpmSource.create_package(
                package["header"],
                size=package["package_size"],
                checksum_type=package["checksum_type"],
                checksum=package["checksum"],
                relpath=None,  # This is the path on the filesystem
                org_id=1,  # TODO: correct
                header_start=package["header_start"],
                header_end=package["header_end"],
                channels=[],
                expand_full_filelist=False,
                remote_path=package["remote_path"],
            )
            rpm_package.arch = package["header"]["arch"]

            yield rpm_package

        except (KeyboardInterrupt, rhnSQL.SQLError):
            raise
        except Exception as e:
            e_message = f"Exception: {e}"
            log2(0, 1, e_message, stream=sys.stderr)
            raise e  # Ignore the package and continue
            # continue

    filelists_parser.clear_cache()
