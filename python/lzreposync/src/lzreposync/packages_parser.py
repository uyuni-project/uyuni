#  pylint: disable=missing-module-docstring

import logging
import sys
from urllib.parse import urljoin

from uyuni.common.rhn_pkg import InvalidPackageError
from uyuni.common.usix import raise_with_tb


#  pylint: disable-next=missing-class-docstring
class LzDebHeader:
    def __init__(self, hdr):
        self.packaging = "deb"
        self.signatures = []
        self.is_source = 0
        self.deb = None
        self.hdr = hdr

    @staticmethod
    def is_signed():
        return 0

    def __getitem__(self, name):
        return self.hdr.get(str(name))

    def __setitem__(self, name, item):
        self.hdr[name] = item

    def __delitem__(self, name):
        del self.hdr[name]

    def __getattr__(self, name):
        return getattr(self.hdr, name)

    def __len__(self):
        return len(self.hdr)


#  pylint: disable-next=missing-class-docstring
class PackagesParser:
    def __init__(self, packages_file, repository=""):
        """
        packages_file: accepted formats: TextIOWrapper (uncompressed file object)
        repository: it is the base url of the repository, that contains the "/dists" and "/pool" right under it
        """
        self.packages_file = packages_file
        if repository and not repository.endswith("/"):
            repository += "/"
        self.repo = repository
        self.parsed_packages = 0

    def parse_packages(self):
        for package in self.split_packages(self.packages_file):
            pkg_hdr = self.get_deb_package_hdr(package)
            pkg_deb_header = LzDebHeader(pkg_hdr)
            self.parsed_packages += 1
            yield pkg_deb_header

    @staticmethod
    def split_packages(packages_file):
        """
        Lazy parsing of the Packages file
        packages_file: currently an "_io.TextIOWrapper" format (or any file-like object)
        """
        curr_package = {}
        for line in packages_file:
            if line not in ["\n", "\r\n"]:
                row = line.split(": ")
                if len(row) != 2:
                    logging.debug(
                        "Bad format in Packages file: In row: %s. Skipping package..",
                        row,
                    )
                    continue
                key, val = row[0], row[1].rstrip("\n")
                curr_package[key] = val
            else:
                # End of package data
                yield curr_package
                curr_package = {}

    def get_deb_package_hdr(self, pkg_data: dict):
        """
        Return the hdr dict object containing the formatted package's metadata
        """
        try:
            hdr = {
                "name": pkg_data["Package"],
                "arch": pkg_data["Architecture"] + "-deb",
                "summary": pkg_data.get(
                    "Description", "No Summary Provided\n"
                ).splitlines()[0],
                "epoch": "",
                "version": 0,
                "release": 0,
                "description": pkg_data.get("Description", ""),
            }
            for hdr_k, deb_k in [
                ("requires", "Depends"),
                ("provides", "Provides"),
                ("conflicts", "Conflicts"),
                ("obsoletes", "Replaces"),
                ("recommends", "Recommends"),
                ("suggests", "Suggests"),
                ("breaks", "Breaks"),
                ("predepends", "Pre-Depends"),
                ("payload_size", "Installed-Size"),
                ("maintainer", "Maintainer"),
            ]:
                if deb_k in pkg_data:
                    hdr[hdr_k] = pkg_data[deb_k]
            for k in pkg_data:
                if k not in hdr:
                    hdr[k] = pkg_data[k]

            version = pkg_data["Version"]
            if version.find(":") != -1:
                hdr["epoch"], version = version.split(":")
                hdr["version"] = version
            if version.find("-") != -1:
                # pylint: disable-next=invalid-name
                version_tmpArr = version.split("-")
                hdr["version"] = "-".join(version_tmpArr[:-1])
                hdr["release"] = version_tmpArr[-1]
            else:
                hdr["version"] = version
                hdr["release"] = "X"

            # Setting the "remote_path" attribute
            hdr["remote_path"] = urljoin(self.repo, hdr["Filename"])

            return hdr
        # pylint: disable-next=broad-exception-caught
        except Exception:
            e = sys.exc_info()[1]
            raise_with_tb(InvalidPackageError(e), sys.exc_info()[2])
            return None
