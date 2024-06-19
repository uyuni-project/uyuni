import logging

from lzreposync.filelists_parser import FilelistsParser
from lzreposync.primary_parser import PrimaryParser


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

        # Parse and cache the content of filelists.xml
        self.filelists_parser.parse_filelists()

        for package in self.primary_parser.parse_primary():  # parse_primary() is a generator
            package["header"]["filenames"] = self.filelists_parser.get_package_filelist(package["checksum"])["files"]
            yield package


