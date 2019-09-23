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

    def __init__(self, url):
        self._url = url
        self._flat = None
        self._pkg_index = None, None

    def get_pkg_index_raw(self) -> typing.Tuple[str, bytes]:
        """
        Get Packages.gz or Packages.xz or Packages content, raw.

        :return: bytes of the content
        """
        if self._pkg_index[0] is None:
            for cnt_fname in ["Packages.gz", "Packages.xz", "Packages"]:
                packages_url = os.path.join(self._url, cnt_fname)
                resp = requests.get(packages_url)
                if resp.status_code == http.HTTPStatus.OK:
                    self._pkg_index = cnt_fname, resp.content
                    break
                resp.close()
        return self._pkg_index

    def decompress_pkg_index(self) -> str:
        """
        Find and return contents of Packages.gz file.

        :raises DpkgRepoException if the Packages.gz file cannot be found.
        :return: string
        """
        fname, cnt_data = self.get_pkg_index_raw()
        try:
            if fname.endswith(".gz"):
                cnt_data = zlib.decompress(cnt_data, 0x10 + zlib.MAX_WBITS)
            elif fname.endswith(".xz"):
                cnt_data = lzma.decompress(cnt_data)
        except zlib.error as exc:
            raise DpkgRepoException(exc)
        except Exception as exc:
            raise DpkgRepoException("Unhandled exception occurred while decompressing {}: {}", fname, exc)

        return cnt_data.decode("utf-8")

    def get_release_index(self) -> str:
        """
        Find and return contents of Release file.

        :raises DpkgRepoException if the Release file cannot be found.
        :return: string
        """

    def is_flat(self) -> bool:
        """
        Detect if the repository has flat format.

        :return:
        """
        if self._flat is None:
            resp = requests.get(self._url)
            if resp.status_code != http.HTTPStatus.OK:
                raise DpkgRepoException("HTTP error {} occurred while connecting to the URL".format(resp.status_code))

        return self._flat


if __name__ == "__main__":
    d = DpkgRepo("http://ftp.halifax.rwth-aachen.de/ubuntu/dists/bionic/restricted/binary-amd64/")
    print("Is flat:", d.is_flat())
    print(d.decompress_pkg_index())
