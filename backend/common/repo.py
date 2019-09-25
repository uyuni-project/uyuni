"""
Repository tools
"""
# coding: utf-8
import typing
import requests
import http
import os
import zlib
import lzma
from urllib import parse
import hashlib


class DpkgRepoException(Exception):
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

    class ReleaseEntry:
        """
        Release file entry
        """
        class Checksum:
            """
            Checksums of the Release file
            """
            md5: str = ""
            sha1: str = ""
            sha256: str = ""

        def __init__(self, size: int, uri: str):
            self.checksum = DpkgRepo.ReleaseEntry.Checksum()
            self.size = size
            self.uri = uri

    class EntryDict(dict):
        """
        Parsed release container.
        """
        def __init__(self, repo: "DpkgRepo"):
            dict(self)
            self.__repo = repo

        def get(self, key: typing.Any) -> typing.Optional[typing.Any]:
            if not self.__repo.is_flat():
                key = "/".join(parse.urlparse(self.__repo._url).path.strip("/").split("/")[-2:] + [key])
            return self[key]

    def __init__(self, url):
        self._url = url
        self._flat = None
        self._pkg_index = None, None
        self._release = DpkgRepo.EntryDict(self)

    def get_pkg_index_raw(self) -> typing.Tuple[str, bytes]:
        """
        Get Packages.gz or Packages.xz or Packages content, raw.

        :return: bytes of the content
        """
        if self._pkg_index[0] is None:
            for cnt_fname in [DpkgRepo.PKG_GZ, DpkgRepo.PKG_XZ, DpkgRepo.PKG_RW]:
                packages_url = os.path.join(self._url, cnt_fname)
                resp = requests.get(packages_url)
                if resp.status_code == http.HTTPStatus.OK:
                    self._pkg_index = cnt_fname, resp.content
                    break
                resp.close()

        if self._pkg_index == (None, None,):
            raise DpkgRepoException("No variants of package index has been found on {} repo".format(self._url))

        return self._pkg_index

    def decompress_pkg_index(self) -> str:
        """
        Find and return contents of Packages.gz file.

        :raises DpkgRepoException if the Packages.gz file cannot be found.
        :return: string
        """
        fname, cnt_data = self.get_pkg_index_raw()
        try:
            if fname == DpkgRepo.PKG_GZ:
                cnt_data = zlib.decompress(cnt_data, 0x10 + zlib.MAX_WBITS)
            elif fname == DpkgRepo.PKG_XZ:
                cnt_data = lzma.decompress(cnt_data)
        except (zlib.error, lzma.LZMAError) as exc:
            raise DpkgRepoException(exc)
        except Exception as exc:
            raise DpkgRepoException("Unhandled exception occurred while decompressing {}: {}".format(fname, exc))

        return cnt_data.decode("utf-8")

    def _parse_release_index(self, release: str) -> typing.Dict[str, "DpkgRepo.ReleaseEntry"]:
        """
        Parse release index to a structure.

        :param release: decoded content of the Release file
        :return: dictionary
        """
        for line in release.split(os.linesep):
            cs_s_path = tuple(filter(None, line.strip().replace("\t", " ").split(" ")))
            if len(cs_s_path) == 3 and len(cs_s_path[0]) in [0x20, 0x28, 0x40]:
                try:
                    int(cs_s_path[0], 0x10)
                    rel_entry = DpkgRepo.ReleaseEntry(int(cs_s_path[1]), cs_s_path[2])
                    self._release.setdefault(rel_entry.uri, rel_entry)
                    if len(cs_s_path[0]) == 0x20:
                        self._release[rel_entry.uri].checksum.md5 = cs_s_path[0]
                    elif len(cs_s_path[0]) == 0x28:
                        self._release[rel_entry.uri].checksum.sha1 = cs_s_path[0]
                    elif len(cs_s_path[0]) == 0x40:
                        self._release[rel_entry.uri].checksum.sha256 = cs_s_path[0]

                except ValueError:
                    pass

        return self._release

    def get_release_index(self) -> typing.Dict[str, "DpkgRepo.ReleaseEntry"]:
        """
        Find and return contents of Release file.

        :raises DpkgRepoException if the Release file cannot be found.
        :return: string
        """
        resp = requests.get(self._get_parent_url(self._url, 2, "Release"))
        try:
            self._flat = resp.status_code == http.HTTPStatus.NOT_FOUND
            self._release = self._parse_release_index(resp.content.decode("utf-8"))
            if resp.status_code not in [http.HTTPStatus.NOT_FOUND, http.HTTPStatus.OK]:
                raise DpkgRepoException("HTTP error {} occurred while connecting to the URL".format(resp.status_code))
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
        return parse.urlunparse(parse.ParseResult(scheme=p_url.scheme, netloc=p_url.netloc,
                                                  path="/".join(p_path[:-depth] + add_path.strip("/").split("/")),
                                                  params=p_url.params, query=p_url.query, fragment=p_url.fragment))

    def is_flat(self) -> bool:
        """
        Detect if the repository has flat format.

        :return:
        """
        if self._flat is None:
            self.get_release_index()

        return bool(self._flat)

    def verify_packages_index(self) -> bool:
        """
        Verify Packages index against all listed checksum algorithms.

        :param name: name of the packages index
        :return: result (boolean)
        """
        res = False
        if not self.is_flat():
            name, data = self.get_pkg_index_raw()
            entry = self.get_release_index().get(name)
            for algorithm in ["md5", "sha1", "sha256"]:
                res = getattr(hashlib, algorithm)(data).hexdigest() == getattr(entry.checksum, algorithm)
                if not res:
                    break
        return res



if __name__ == "__main__":
    d = DpkgRepo("http://ftp.halifax.rwth-aachen.de/ubuntu/dists/bionic/restricted/binary-amd64/")
    print("Is flat:", d.is_flat())
    print(d.decompress_pkg_index())
