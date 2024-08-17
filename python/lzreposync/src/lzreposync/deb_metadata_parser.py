#  pylint: disable=missing-module-docstring

import logging
import sys

from lzreposync.packages_parser import PackagesParser
from lzreposync.translation_parser import TranslationParser
from spacewalk.satellite_tools.syncLib import log, log2
from spacewalk.server import rhnSQL
from spacewalk.server.importlib import mpmSource


class BadParserException(Exception):
    def __init__(self, parser):
        super().__init__(f"Bad Parser {parser}")


#  pylint: disable-next=missing-class-docstring
class DEBMetadataParser:
    def __init__(self, packages_parser, translation_parser):
        self.packages_parser = packages_parser
        self.translation_parser = translation_parser

    def parse_packages_metadata(self):
        """
        Parse the 'Packages' and the 'Translation' metadata file (which contains the full description of each package)
        and yield the package info in 'debBinaryPackage' format
        """
        if not isinstance(self.packages_parser, PackagesParser):
            logging.error("Bad packages_parser %s", self.packages_parser)
            raise BadParserException(self.packages_parser)
        if not isinstance(self.translation_parser, TranslationParser):
            logging.error("Bad translation_parser %s", self.translation_parser)
            raise BadParserException(self.translation_parser)

        # pylint: disable-next=consider-using-f-string
        log(0, " Parsing %s" % self.translation_parser.translation_file)
        # Parse and cache the content of Translation-en
        self.translation_parser.parse_translation_file()
        for deb_pkg_header in self.packages_parser.parse_packages():
            description_md5 = deb_pkg_header.get(
                "Description-md5", "x"
            )  # The x is just for none
            deb_pkg_header.hdr["description"] = (
                self.translation_parser.get_pacakge_description_by_description_md5(
                    description_md5
                )
            )
            # pylint: disable=W0703,W0706
            try:
                deb_package = mpmSource.create_package(
                    deb_pkg_header,
                    size=deb_pkg_header["Size"],
                    checksum_type="md5",
                    checksum=deb_pkg_header["MD5sum"],
                    relpath=None,  # This is the path on the filesystem
                    org_id=1,  # TODO: how to set this - ask team
                    channels=[],
                    expand_full_filelist=False,
                    remote_path=deb_pkg_header["remote_path"],
                )
                deb_package.arch = deb_pkg_header["arch"]
                yield deb_package
            except (KeyboardInterrupt, rhnSQL.SQLError):
                raise
            except Exception as e:
                e_message = f"Exception: {e}"
                log2(0, 1, e_message, stream=sys.stderr)
                # raise e  # Ignore the package and continue
                continue
