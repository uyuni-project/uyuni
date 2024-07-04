#  pylint: disable=missing-module-docstring

import logging

from lzreposync.filelists_parser import FilelistsParser
from lzreposync.primary_parser import PrimaryParser
from spacewalk.satellite_tools.syncLib import log


def set_fake_files_data(package, files_count):
    """
    Fake data related to the files. Eg: 'filedevices', 'fileinodes', 'filemodes', ect
    TODO: This is just a dump implementation to make things work, it can be enhanced and generalized later on
    """
    # TODO search how we can set the correct values

    package["header"]["filedevices"] = [1 for _ in range(files_count)]
    package["header"]["fileinodes"] = [i for i in range(files_count)]
    package["header"]["filemodes"] = [16877 for _ in range(files_count)]
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
class MetadataParser:
    def __init__(self, primary_parser, filelists_parser):
        self.primary_parser = primary_parser
        self.filelists_parser = filelists_parser

    def parse_packages_metadata(self):
        if not isinstance(self.primary_parser, PrimaryParser):
            logging.error("Bad primary_parser %s", self.primary_parser)
            return []  # TODO should we return None instead ?
        if not isinstance(self.filelists_parser, FilelistsParser):
            logging.error("Bad filelists_parser %s", self.primary_parser)
            return []

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
            files_count = len(package["header"]["filenames"])
            package = set_fake_files_data(package, files_count)
            print(f"Yielding pacakge {package['checksum']}")
            yield package
