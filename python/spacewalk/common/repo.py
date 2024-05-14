"""
Repository tools
"""

# coding: utf-8
import typing
import http
import os
import zlib
import lzma
import logging
import tempfile
import subprocess
from urllib import parse
import hashlib
from collections import namedtuple

import requests

# pylint:disable=W0612,W0212,C0301

SPACEWALK_LIB = "/var/lib/spacewalk"
SPACEWALK_GPG_HOMEDIR = os.path.join(SPACEWALK_LIB, "gpgdir")


class GeneralRepoException(Exception):
    """
    Dpkg repository exception
    """


class DpkgRepo:
    """
    Dpkg repository detection.
    The repositories in Debian world have several layouts,
    such as "flat", classic tree, PPA etc.
    """

    PKG_GZ = "Packages.gz"
    PKG_XZ = "Packages.xz"
    PKG_RW = "Packages"

    class ReleaseEntry:  # pylint: disable=W0612,R0903
        """
        Release file entry
        """

        class Checksum:  # pylint: disable=R0903
            """
            Checksums of the Release file
            """

            md5: str = ""
            sha1: str = ""
            sha256: str = ""
            sha384: str = ""
            sha512: str = ""

        def __init__(self, size: int, uri: str):
            self.checksum = DpkgRepo.ReleaseEntry.Checksum()
            self.size = size
            self.uri = uri

    class EntryDict(dict):
        """
        Parsed release container.
        """

        def __init__(self, repo: "DpkgRepo"):
            super().__init__()
            self.__repo = repo

        def get(self, key: typing.Any) -> typing.Optional[typing.Any]:  # type: ignore
            """
            Automatically update key if the repo is flat.

            :param key:
            :return:
            """
            if not self.__repo.is_flat():
                key = "/".join(
                    parse.urlparse(self.__repo._url).path.strip("/").split("/")[-2:]
                    + [key]
                )
            return self[key]

    def __init__(self, url: str, proxies: dict = None, gpg_verify: bool = True):
        self._url = url
        self._flat_checked: typing.Optional[int] = None
        self._flat: bool = False
        self._pkg_index: typing.Tuple[str, bytes] = (
            "",
            b"",
        )
        self._release = DpkgRepo.EntryDict(self)
        self.proxies = proxies
        self.gpg_verify = gpg_verify

    def append_index_file(self, index_file: str) -> str:
        """
        Append an index file, such as Packages.gz or Packagex.xz etc
        to the given URL, if it does not contains any.

        :param index_file: string
        :return: url string
        """
        p_url = parse.urlparse(self._url)
        path = p_url.path
        if not path.endswith(index_file):
            if index_file in path:
                logging.error(
                    # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                    "URL has already {} mentioned in it. Raising \
                               GeneralRepoException!".format(
                        index_file
                    ),
                    exc_info=True,
                )
                raise GeneralRepoException(
                    # pylint: disable-next=consider-using-f-string
                    "URL has already {} mentioned in it.".format(index_file)
                )
            path = os.path.join(path.rstrip("/"), index_file)

        return parse.urlunparse(
            (
                p_url.scheme,
                p_url.netloc,
                path,
                p_url.params,
                p_url.query,
                p_url.fragment,
            )
        )

    def get_pkg_index_raw(self) -> typing.Tuple[str, bytes]:
        """
        Get Packages.gz or Packages.xz or Packages content, raw.

        :return: bytes of the content
        """
        if self._pkg_index[0] == "":
            for cnt_fname in [DpkgRepo.PKG_GZ, DpkgRepo.PKG_XZ, DpkgRepo.PKG_RW]:
                packages_url = self.append_index_file(cnt_fname)
                if packages_url.startswith("file://"):
                    try:
                        with open(packages_url.replace("file://", ""), "rb") as f:
                            self._pkg_index = cnt_fname, f.read()
                            break
                    except FileNotFoundError as ex:
                        logging.debug(
                            # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                            "File not found: {}".format(
                                packages_url.replace("file://", "")
                            ),
                            exc_info=True,
                        )
                else:
                    resp = requests.get(packages_url, proxies=self.proxies)
                    if resp.status_code == http.HTTPStatus.OK:
                        self._pkg_index = cnt_fname, resp.content
                        break
                    resp.close()

        return self._pkg_index

    def decompress_pkg_index(self) -> str:
        """
        Find and return contents of Packages.gz file.

        :raises GeneralRepoException if the Packages.gz file cannot be found.
        :return: string
        """
        fname, cnt_data = self.get_pkg_index_raw()
        try:
            if fname == DpkgRepo.PKG_GZ:
                cnt_data = zlib.decompress(cnt_data, 0x10 + zlib.MAX_WBITS)
            elif fname == DpkgRepo.PKG_XZ:
                cnt_data = lzma.decompress(cnt_data)
        except (zlib.error, lzma.LZMAError) as exc:
            logging.exception(
                "Exception during decompression of pkg index", exc_info=True
            )
            # pylint: disable-next=raise-missing-from
            raise GeneralRepoException(exc)
        except Exception as exc:
            logging.exception(
                "Unknown exception during decompression of \
                               pkg index. Raising GeneralRepoException",
                exc_info=True,
            )
            raise GeneralRepoException(
                # pylint: disable-next=consider-using-f-string
                "Unhandled exception occurred while decompressing {}: {}".format(
                    fname, exc
                )
            ) from exc

        return cnt_data.decode("utf-8")

    def _parse_release_index(self, release: str) -> "EntryDict":
        """
        Parse release index to a structure.

        :param release: decoded content of the Release file
        :return: dictionary
        """
        # Length of hexadecimal representation for each checksum algorithm
        # pylint: disable-next=invalid-name
        LEN_MD5 = 128 // 4
        # pylint: disable-next=invalid-name
        LEN_SHA1 = 160 // 4
        # pylint: disable-next=invalid-name
        LEN_SHA256 = 256 // 4
        # pylint: disable-next=invalid-name
        LEN_SHA384 = 384 // 4
        # pylint: disable-next=invalid-name
        LEN_SHA512 = 512 // 4
        Entry = namedtuple("Entry", "checksum, size, path")
        for line in release.split(os.linesep):
            try:
                entry = Entry._make(
                    filter(None, line.strip().replace("\t", " ").split(" "))
                )
                int(entry.checksum, 0x10)  # assert entry.checksum is hexadecimal
                rel_entry = DpkgRepo.ReleaseEntry(int(entry.size), entry.path)
            except (TypeError, ValueError):
                continue

            if len(entry.checksum) in (
                LEN_MD5,
                LEN_SHA1,
                LEN_SHA256,
                LEN_SHA384,
                LEN_SHA512,
            ):
                self._release.setdefault(rel_entry.uri, rel_entry)
                if len(entry.checksum) == LEN_MD5:
                    self._release[rel_entry.uri].checksum.md5 = entry.checksum
                elif len(entry.checksum) == LEN_SHA1:
                    self._release[rel_entry.uri].checksum.sha1 = entry.checksum
                elif len(entry.checksum) == LEN_SHA256:
                    self._release[rel_entry.uri].checksum.sha256 = entry.checksum
                elif len(entry.checksum) == LEN_SHA384:
                    self._release[rel_entry.uri].checksum.sha384 = entry.checksum
                elif len(entry.checksum) == LEN_SHA512:
                    self._release[rel_entry.uri].checksum.sha512 = entry.checksum

        return self._release

    def _has_valid_gpg_signature(self, uri: str, response=None) -> bool:
        """
        Validate GPG signature of Release file.

        :return: bool
        """
        process = None
        uri = uri.replace("file://", "")
        if not response:
            # There is no response, so this is a local path.
            if os.access(os.path.join(uri, "InRelease"), os.R_OK):
                release_file = os.path.join(uri, "InRelease")
                process = subprocess.Popen(
                    [
                        "gpg",
                        "--verify",
                        "--homedir",
                        SPACEWALK_GPG_HOMEDIR,
                        release_file,
                    ],
                    stdout=subprocess.DEVNULL,
                    stderr=subprocess.DEVNULL,
                )
                out = process.wait(timeout=90)
            elif os.access(os.path.join(uri, "Release"), os.R_OK):
                release_file = os.path.join(uri, "Release")
                release_signature_file = os.path.join(uri, "Release.gpg")
                if os.access(release_signature_file, os.R_OK):
                    process = subprocess.Popen(
                        [
                            "gpg",
                            "--verify",
                            "--homedir",
                            SPACEWALK_GPG_HOMEDIR,
                            release_signature_file,
                            release_file,
                        ],
                        stdout=subprocess.DEVNULL,
                        stderr=subprocess.DEVNULL,
                    )
                    out = process.wait(timeout=90)
                else:
                    logging.error(
                        # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                        "Signature file for GPG check could not be accessed: \
                                   '{}. Raising GeneralRepoException.".format(
                            release_signature_file
                        )
                    )
                    raise GeneralRepoException(
                        # pylint: disable-next=consider-using-f-string
                        "Signature file for GPG check could not be accessed: {}".format(
                            release_signature_file
                        )
                    )
            else:
                logging.error(
                    # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                    "No release file found: '{}'. Raising GeneralRepoException.".format(
                        uri
                    )
                )
                # pylint: disable-next=consider-using-f-string
                raise GeneralRepoException("No release file found: {}".format(uri))
        else:
            # There is a response, so we are dealing with a URL.
            if parse.urlparse(response.url).path.endswith("InRelease"):
                process = subprocess.Popen(
                    ["gpg", "--verify", "--homedir", SPACEWALK_GPG_HOMEDIR],
                    stdin=subprocess.PIPE,
                    stdout=subprocess.DEVNULL,
                    stderr=subprocess.DEVNULL,
                )
                out = process.communicate(response.content, timeout=90)
            else:
                signature_response = requests.get(
                    self._get_parent_url(response.url, 1, "Release.gpg"),
                    proxies=self.proxies,
                )
                if signature_response.status_code != http.HTTPStatus.OK:
                    return False
                else:
                    temp_release_file = tempfile.NamedTemporaryFile()
                    temp_release_file.write(response.content)
                    temp_release_file.seek(0)
                    temp_signature_file = tempfile.NamedTemporaryFile()
                    temp_signature_file.write(signature_response.content)
                    temp_signature_file.seek(0)
                    process = subprocess.Popen(
                        [
                            "gpg",
                            "--verify",
                            "--homedir",
                            SPACEWALK_GPG_HOMEDIR,
                            temp_signature_file.name,
                            temp_release_file.name,
                        ],
                        stdout=subprocess.DEVNULL,
                        stderr=subprocess.DEVNULL,
                    )
                    out = process.wait(timeout=90)

        if process.returncode == 0:
            logging.debug("GPG signature is valid")
            return True
        else:
            logging.debug(
                # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                "GPG signature is invalid. gpg return code: {}".format(
                    process.returncode
                )
            )
            return False

    def get_release_index(self) -> typing.Dict[str, "DpkgRepo.ReleaseEntry"]:
        """
        Find and return contents of Release file.

        InRelease file take precedence over Release file if both exist.
        In either case the file must be signed with a GPG key. The signature is
        verified before the content is parsed.

        :raises GeneralRepoException if the Release file cannot be found or the GPG signature can't be verified.
        :return: string
        """
        if self._url.startswith("file://"):
            return self._get_release_index_from_file()
        else:
            return self._get_release_index_from_http()

    def _get_release_index_from_file(self) -> typing.Dict[str, "DpkgRepo.ReleaseEntry"]:
        # InRelease files take precedence per uyuni-rfc 00057-deb-repo-sync-gpg-check
        logging.debug(
            # pylint: disable-next=logging-format-interpolation,consider-using-f-string
            "Fetching release file from local filesystem: {}".format(
                self._url.replace("file://", "")
            )
        )
        local_path = self._url.replace("file://", "")
        release_file = None
        if os.access(self._get_parent_url(local_path, 2, "InRelease"), os.R_OK):
            release_file = self._get_parent_url(local_path, 2, "InRelease")
            local_path = self._get_parent_url(local_path, 2)
            self._flat = False
        elif os.access(self._get_parent_url(local_path, 2, "Release"), os.R_OK):
            release_file = self._get_parent_url(local_path, 2, "Release")
            local_path = self._get_parent_url(local_path, 2)
            self._flat = False
        else:
            self._flat = True
        self._flat_checked = 1

        # Repo format is not flat
        if not self.is_flat():
            if self.gpg_verify and not self._has_valid_gpg_signature(local_path):
                # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                logging.error("GPG verification failed: {}".format(release_file))
                logging.error("Raising GeneralRepoException!")
                raise GeneralRepoException(
                    # pylint: disable-next=consider-using-f-string
                    "GPG verification failed: {}".format(release_file)
                )
            try:
                with open(release_file, "rb") as f:
                    self._release = self._parse_release_index(f.read().decode("utf-8"))
            except IOError as ex:
                logging.exception(
                    # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                    "IOError while accessing file: '{}'. Raising \
                                   GeneralRepoException!".format(
                        release_file
                    ),
                    exc_info=True,
                )
                raise GeneralRepoException(
                    # pylint: disable-next=consider-using-f-string
                    "IOError while accessing file: {}".format(release_file)
                ) from ex

        # Repo format is flat
        else:
            if os.access(self._get_parent_url(local_path, 0, "InRelease"), os.R_OK):
                release_file = self._get_parent_url(local_path, 0, "InRelease")
            elif os.access(self._get_parent_url(local_path, 0, "Release"), os.R_OK):
                release_file = self._get_parent_url(local_path, 0, "Release")
            else:
                logging.error(
                    # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                    "No release file found in '{}'. Raising \
                                   GeneralRepoException.".format(
                        self._get_parent_url(local_path, 0)
                    )
                )
                raise GeneralRepoException(
                    # pylint: disable-next=consider-using-f-string
                    "No release file found in {}".format(
                        self._get_parent_url(local_path, 0)
                    )
                )

            try:
                with open(release_file, "rb") as f:
                    release_file_content = f.read().decode("utf-8")
                    if self.gpg_verify and not self._has_valid_gpg_signature(
                        local_path
                    ):
                        logging.error(
                            # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                            "GPG verification failed: '{}'. \
                                           Raising GeneralRepoException.".format(
                                release_file
                            )
                        )
                        raise GeneralRepoException(
                            # pylint: disable-next=consider-using-f-string
                            "GPG verification failed: {}".format(release_file)
                        )
                    self._release = self._parse_release_index(release_file_content)
            except IOError as ex:
                logging.exception(
                    # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                    "IOError while accessing file: '{}'. Raising \
                                   GeneralRepoException.".format(
                        release_file
                    ),
                    exc_info=True,
                )
                raise GeneralRepoException(
                    # pylint: disable-next=consider-using-f-string
                    "IOError while accessing file: {}".format(release_file)
                ) from ex

        return self._release

    def _get_release_index_from_http(self) -> typing.Dict[str, "DpkgRepo.ReleaseEntry"]:
        # InRelease files take precedence per uyuni-rfc 00057-deb-repo-sync-gpg-check
        # pylint: disable-next=logging-format-interpolation,consider-using-f-string
        logging.debug("Fetching release file from local http: {}".format(self._url))
        resp = requests.get(
            self._get_parent_url(self._url, 2, "InRelease"), proxies=self.proxies
        )
        if resp.status_code != http.HTTPStatus.OK:
            resp = requests.get(
                self._get_parent_url(self._url, 2, "Release"), proxies=self.proxies
            )

        try:
            if resp.status_code not in [
                http.HTTPStatus.NOT_FOUND,
                http.HTTPStatus.OK,
                http.HTTPStatus.FORBIDDEN,
            ]:
                logging.error(
                    # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                    "Fetching release index failed with http status \
                               '{}'. Raising GeneralRepoException.".format(
                        resp.status_code
                    )
                )
                raise GeneralRepoException(
                    # pylint: disable-next=consider-using-f-string
                    "HTTP error {} occurred while connecting to the URL".format(
                        resp.status_code
                    )
                )

            self._flat = resp.status_code in [
                http.HTTPStatus.NOT_FOUND,
                http.HTTPStatus.FORBIDDEN,
            ]
            self._flat_checked = 1

            if (
                not self.is_flat()
                and self.gpg_verify
                and not self._has_valid_gpg_signature(resp.url, resp)
            ):
                logging.error(
                    "Repo has no valid GPG signature. Raising GeneralRepoException."
                )
                raise GeneralRepoException(
                    # pylint: disable-next=consider-using-f-string
                    "GPG verification failed: {}".format(resp.url)
                )

            self._release = self._parse_release_index(resp.content.decode("utf-8"))

            if not self._release and self.is_flat():
                resp = requests.get(
                    self._get_parent_url(self._url, 0, "InRelease"),
                    proxies=self.proxies,
                )
                if resp.status_code != http.HTTPStatus.OK:
                    resp = requests.get(
                        self._get_parent_url(self._url, 0, "Release"),
                        proxies=self.proxies,
                    )

                if resp.status_code == http.HTTPStatus.OK:
                    if self.gpg_verify and not self._has_valid_gpg_signature(
                        resp.url, resp
                    ):
                        logging.error(
                            "Repo has no valid GPG signature. GeneralRepoException will be raised!"
                        )
                        raise GeneralRepoException(
                            # pylint: disable-next=consider-using-f-string
                            "GPG verification failed: {}".format(resp.url)
                        )
                    self._release = self._parse_release_index(
                        resp.content.decode("utf-8")
                    )
        finally:
            resp.close()

        return self._release

    @staticmethod
    def _get_parent_url(url, depth=1, add_path=""):
        """
        Get parent url from the given one.

        :param url: an url
        :return: parent url
        """
        p_url = parse.urlparse(url)
        p_path = p_url.path.rstrip("/").split("/")
        if depth:
            p_path = p_path[:-depth]

        return parse.urlunparse(
            parse.ParseResult(
                scheme=p_url.scheme,
                netloc=p_url.netloc,
                path="/".join(p_path + add_path.strip("/").split("/")) or "/",
                params=p_url.params,
                query=p_url.query,
                fragment=p_url.fragment,
            )
        )

    def is_flat(self) -> bool:
        """
        Detect if the repository has flat format.

        :return:
        """
        if self._flat_checked is None:
            self.get_release_index()

        return bool(self._flat)

    def verify_packages_index(self) -> bool:
        """
        Verify Packages index with the best available checksum algorithm.

        :return: result (boolean)
        """
        name, data = self.get_pkg_index_raw()

        # If there are no packages in the repo, return True
        if (name, data) == (
            "",
            b"",
        ):
            return True

        entry = self.get_release_index().get(name)
        if entry is None:
            return False

        result = False
        for algorithm in ("sha512", "sha384", "sha256", "sha1", "md5"):
            entry_checksum = getattr(entry.checksum, algorithm, None)
            if entry_checksum:
                result = getattr(hashlib, algorithm)(data).hexdigest() == entry_checksum
                break
            else:
                continue

        return result
