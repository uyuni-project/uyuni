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
    TODO: This is just a dump implementation to make things work, it can be enhanced and generalized later on
    """
    # TODO search how we can set the correct values

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

    return package


# pylint: disable-next=missing-class-docstring
class BadParserException(Exception):
    def __init__(self, parser):
        super().__init__(f"Bad Parser {parser}")


# pylint: disable-next=missing-class-docstring
class RPMMetadataParser:
    def __init__(self, primary_parser, filelists_parser):
        self.primary_parser = primary_parser
        self.filelists_parser = filelists_parser

    def parse_packages_metadata(self):
        """
        Parse both primary.xml and filelists.xml files and return yield packages metadata
        Yield an instance inheriting from spacewalk.server.importLib.IncompletePackage
        """
        if not isinstance(self.primary_parser, PrimaryParser):
            logging.error("Bad primary_parser %s", self.primary_parser)
            raise BadParserException(self.primary_parser)
        if not isinstance(self.filelists_parser, FilelistsParser):
            logging.error("Bad filelists_parser %s", self.primary_parser)
            raise BadParserException(self.primary_parser)

        # pylint: disable-next=consider-using-f-string
        log(0, " Parsing %s" % self.filelists_parser.filelists_file)
        # Parse and cache the content of filelists.xml
        self.filelists_parser.parse_filelists()

        for (
            package
        ) in self.primary_parser.parse_primary():  # parse_primary() is a generator
            package["header"]["filenames"] = self.filelists_parser.get_package_filelist(
                package["checksum"]
            )["files"]
            package["header"]["filemodes"] = self.filelists_parser.get_package_filelist(
                package["checksum"]
            )["filetypes"]
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

                yield rpm_package

            except (KeyboardInterrupt, rhnSQL.SQLError):
                raise
            except Exception as e:
                e_message = f"Exception: {e}"
                log2(0, 1, e_message, stream=sys.stderr)
                raise e  # Ignore the package and continue
                # continue
