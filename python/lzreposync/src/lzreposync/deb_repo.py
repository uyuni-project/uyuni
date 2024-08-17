#  pylint: disable=missing-module-docstring

import logging
import os
import time
import urllib.parse as urlparse  # pylint: disable=F0401,E0611
from shutil import copyfile
from urllib.parse import unquote

import requests

from lzreposync.deb_metadata_parser import DEBMetadataParser
from lzreposync.packages_parser import PackagesParser
from lzreposync.repo import Repo
from lzreposync.translation_parser import TranslationParser
from spacewalk.satellite_tools.syncLib import log2
from spacewalk.server import rhnSQL
from uyuni.common import fileutils

RETRIES = 10
RETRY_DELAY = 1
FORMAT_PRIORITY = [".xz", ".gz", ""]
log = logging.getLogger(__name__)


#  pylint: disable-next=missing-class-docstring
class DebRepo(Repo):
    def __init__(self, name, cache_path, url, channel_label=None):
        super().__init__(name, cache_path, url, None, "deb")
        # 'arch_filter' is None because the arch is specified in the url query
        self.signature_verified = True  # TODO: complete
        self.url = url
        parts = url.rsplit("/dists/", 1)
        self.base_url = parts[0]
        self.url_with_no_arch = None  # The url without getting to the arch path

        parsed_url = urlparse.urlparse(url)
        query = urlparse.parse_qsl(parsed_url.query)

        new_query = []
        suite = None
        component = None
        arch = None
        for qi in query:
            if qi[0] == "uyuni_suite":
                suite = qi[1]
            elif qi[0] == "uyuni_component":
                component = qi[1]
            elif qi[0] == "uyuni_arch":
                arch = qi[1]
            else:
                new_query.append(qi)
        if suite:
            parsed_url = parsed_url._replace(query=urlparse.urlencode(new_query))
            base_url = urlparse.urlunparse(parsed_url)
            path_list = parsed_url.path.split("/")
            # pylint: disable-next=consider-using-f-string
            log2(0, 0, "Base URL: {}".format(base_url))
            # pylint: disable-next=consider-using-f-string
            log2(0, 0, "Suite: {}".format(suite))
            # pylint: disable-next=consider-using-f-string
            log2(0, 0, "Component: {}".format(component))
            if "/" not in suite:
                path_list.append("dists")
            path_list.extend(suite.split("/"))
            if component:
                path_list.extend(component.split("/"))
            parsed_url_with_no_arch = parsed_url._replace(path="/".join(path_list))
            self.url_with_no_arch = urlparse.urlunparse(parsed_url_with_no_arch)
            if "/" not in suite:
                if arch is None:
                    rhnSQL.initDB()
                    h = rhnSQL.prepare(
                        """
                                       SELECT ca.label AS arch_label
                                       FROM rhnChannel AS c
                                       LEFT JOIN rhnChannelArch AS ca
                                           ON c.channel_arch_id = ca.id
                                       WHERE c.label = :channel_label
                                       """
                    )
                    h.execute(channel_label=channel_label)
                    row = h.fetchone_dict()
                    if row and "arch_label" in row:
                        aspl = row["arch_label"].split("-")
                        if len(aspl) == 3 and aspl[0] == "channel" and aspl[2] == "deb":
                            arch_trans = {
                                "ia32": "i386",
                                "arm": "armhf",
                            }
                            if aspl[1] in arch_trans:
                                arch = arch_trans[aspl[1]]
                            else:
                                arch = aspl[1]
                if arch:
                    # pylint: disable-next=consider-using-f-string
                    log2(0, 0, "Channel architecture: {}".format(arch))
                    # pylint: disable-next=consider-using-f-string
                    path_list.append("binary-{}".format(arch))
            while "" in path_list:
                path_list.remove("")
            parsed_url = parsed_url._replace(path="/".join(path_list))
            self.url = urlparse.urlunparse(parsed_url)
            self.base_url = [base_url]

            # Make sure baseurl ends with / and urljoin will work correctly
            if self.base_url[0][-1] != "/":
                self.base_url[0] += "/"

    def verify(self):
        """
        Verify package index checksum and signature.

        :return:
        """
        # TODO

    def _download(self, url):
        logging.debug("DebRepo: Downloading %s", url)
        if url.startswith("file://"):
            srcpath = unquote(url[len("file://") :])
            if not os.path.exists(srcpath):
                return ""
            filename = self.cache_dir + "/" + os.path.basename(url)
            copyfile(srcpath, filename)
            return filename
        for _ in range(0, RETRIES):
            try:
                data = requests.get(
                    url
                )  # TODO: Consider adding a timeout (pylint W3101)
                if not data.ok:
                    return ""
                filename = os.path.join(
                    self.cache_dir, os.path.basename(urlparse.urlparse(url).path)
                )
                with open(filename, "wb") as fd:
                    for chunk in data.iter_content(chunk_size=1024):
                        fd.write(chunk)

                return filename
            except requests.exceptions.RequestException as exc:
                print("ERROR: requests.exceptions.RequestException occurred:", exc)
                time.sleep(RETRY_DELAY)

        return ""

    def get_packages_metadata(self):
        packages_file = self.download_packages_file()
        translation_file = self.download_translation_file()
        if not packages_file:
            print("Error downloading 'Packages' md file. Leaving...")
            return

        base_url = (
            self.base_url[0] if isinstance(self.base_url, list) else self.base_url
        )
        packages_parser = PackagesParser(packages_file, repository=base_url)
        translation_parser = TranslationParser(translation_file, self.cache_dir)
        metadata_parser = DEBMetadataParser(
            packages_parser=packages_parser, translation_parser=translation_parser
        )
        yield from metadata_parser.parse_packages_metadata()
        translation_parser.clear_cache()  # TODO can we make this execute automatically
        packages_file.close()  # TODO optimize

    def download_packages_file(self):
        """
        Locate and download the 'Packages' (.xz or .gz) metadata file.
        Return the decompressed file
        """
        decompressed = None
        for extension in FORMAT_PRIORITY:
            scheme, netloc, path, query, fragid = urlparse.urlsplit(self.url)

            packages_url = urlparse.urlunsplit(
                (
                    scheme,
                    netloc,
                    path
                    + ("/" if not path.endswith("/") else "")
                    + "Packages"
                    + extension,
                    query,
                    fragid,
                )
            )
            filename = self._download(packages_url)
            if filename:
                if query:
                    newfilename = filename.split("?")[0]
                    os.rename(filename, newfilename)
                    filename = newfilename
                decompressed = fileutils.decompress_open(filename)
            if decompressed:
                return decompressed
            print(f"ERROR: Download of Packages{extension} md file failed.")
        return None

    def download_translation_file(self):
        """
        Locate and download the 'Translation' (.xz or .gz) description file.
        Return the decompressed file
        """
        decompressed = None
        for extension in FORMAT_PRIORITY:
            scheme, netloc, path, query, fragid = urlparse.urlsplit(
                self.url_with_no_arch
            )

            translation_url = urlparse.urlunsplit(
                (
                    scheme,
                    netloc,
                    path
                    + ("/" if not path.endswith("/") else "")
                    + "/i18n/"
                    + "Translation-en"
                    + extension,
                    query,
                    fragid,
                )
            )
            filename = self._download(translation_url)
            if filename:
                if query:
                    newfilename = filename.split("?")[0]
                    os.rename(filename, newfilename)
                    filename = newfilename
                decompressed = fileutils.decompress_open(filename)
            if decompressed:
                return decompressed
            print(
                f"ERROR: Download of Translation{extension} descriptions file failed."
            )
        return None
