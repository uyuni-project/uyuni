#  pylint: disable=missing-module-docstring

import sys

from lzreposync.packages_parser import PackagesParser
from lzreposync.translation_parser import TranslationParser
from spacewalk.satellite_tools.syncLib import log, log2
from spacewalk.server import rhnSQL
from spacewalk.server.importlib import mpmSource


class BadParserException(Exception):
    def __init__(self, parser):
        super().__init__(f"Bad Parser {parser}")


def parse_deb_packages_metadata(
    packages_file, translation_file, repository_base_url, cache_dir
):
    """
    :packages_file: accepted formats: TextIOWrapper (uncompressed file object)
    :repository_url: it is the base url of the repository, that contains the "/dists" and "/pool" right under it
    Parse the 'Packages' and the 'Translation' metadata file (which contains the full description of each package)
    and yield the package info in 'debBinaryPackage' format
    """

    packages_parser = PackagesParser(packages_file, repository_base_url)
    translation_parser = TranslationParser(translation_file, cache_dir)

    # pylint: disable-next=consider-using-f-string
    log(0, " Parsing %s" % translation_parser.translation_file)
    # Parse and cache the content of Translation-en
    translation_parser.parse_translation_file()
    for deb_pkg_header in packages_parser.parse_packages():
        description_md5 = deb_pkg_header.get(
            "Description-md5", "x"
        )  # The x is just for none
        deb_pkg_header.hdr["description"] = (
            translation_parser.get_pacakge_description_by_description_md5(
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

    translation_parser.clear_cache()
