#  pylint: disable=missing-module-docstring
import gzip
import logging
import os
import time
import xml.etree.ElementTree as etree
from shutil import copyfile
from urllib.parse import unquote, urlparse

import requests

RETRIES = 4
RETRY_DELAY = 1
FORMAT_PRIORITY = [".xz", ".gz", ""]
log = logging.getLogger(__name__)


#  pylint: disable-next=missing-class-docstring
class UpdateNoticeException(Exception):
    """An exception thrown for bad UpdateNotice data."""

    pass


#  pylint: disable-next=missing-class-docstring
class UpdateNotice:
    """
    Simplified UpdateNotice class implementation
    https://github.com/rpm-software-management/yum/blob/master/yum/update_md.py

    A single update notice (for instance, a security fix).
    """

    # pylint: disable-next=unused-argument
    def __init__(self, elem=None, repoid=None, vlogger=None):
        self._md = {
            "from": "",
            "type": "",
            "title": "",
            "release": "",
            "status": "",
            "version": "",
            "pushcount": "",
            "update_id": "",
            "issued": "",
            "updated": "",
            "description": "",
            "rights": "",
            "severity": "",
            "summary": "",
            "solution": "",
            "references": [],
            "pkglist": [],
            "reboot_suggested": False,
            "restart_suggested": False,
        }

        if elem is not None:
            self._parse(elem)

    def __getitem__(self, item):
        """Allows scriptable metadata access (ie: un['update_id'])."""
        # pylint: disable-next=unidiomatic-typecheck
        if type(item) is int:
            return sorted(self._md)[item]
        ret = self._md.get(item)
        if ret == "":
            ret = None
        return ret

    def __setitem__(self, item, val):
        self._md[item] = val

    def _parse(self, elem):
        """
        Parse an update element::
            <!ELEMENT update (id, synopsis?, issued, updated,
                              references, description, rights?,
                              severity?, summary?, solution?, pkglist)>
                <!ATTLIST update type (errata|security) "errata">
                <!ATTLIST update status (final|testing) "final">
                <!ATTLIST update version CDATA #REQUIRED>
                <!ATTLIST update from CDATA #REQUIRED>
        """
        if elem.tag == "update":
            for attrib in ("from", "type", "status", "version"):
                self._md[attrib] = elem.attrib.get(attrib)
            if self._md["version"] is None:
                self._md["version"] = "0"
            for child in elem:
                if child.tag == "id":
                    if not child.text:
                        raise UpdateNoticeException("No id element found")
                    self._md["update_id"] = child.text
                elif child.tag == "pushcount":
                    self._md["pushcount"] = child.text
                elif child.tag == "issued":
                    self._md["issued"] = child.attrib.get("date")
                elif child.tag == "updated":
                    self._md["updated"] = child.attrib.get("date")
                elif child.tag == "references":
                    self._parse_references(child)
                elif child.tag == "description":
                    self._md["description"] = child.text
                elif child.tag == "rights":
                    self._md["rights"] = child.text
                elif child.tag == "severity":
                    self._md[child.tag] = child.text
                elif child.tag == "summary":
                    self._md["summary"] = child.text
                elif child.tag == "solution":
                    self._md["solution"] = child.text
                elif child.tag == "pkglist":
                    self._parse_pkglist(child)
                elif child.tag == "title":
                    self._md["title"] = child.text
                elif child.tag == "release":
                    self._md["release"] = child.text
        else:
            raise UpdateNoticeException("No update element found")

    def _parse_references(self, elem):
        """
        Parse the update references::
            <!ELEMENT references (reference*)>
            <!ELEMENT reference>
                <!ATTLIST reference href CDATA #REQUIRED>
                <!ATTLIST reference type (self|other|cve|bugzilla) "self">
                <!ATTLIST reference id CDATA #IMPLIED>
                <!ATTLIST reference title CDATA #IMPLIED>
        """
        for reference in elem:
            if reference.tag == "reference":
                data = {}
                for refattrib in ("id", "href", "type", "title"):
                    data[refattrib] = reference.attrib.get(refattrib)
                self._md["references"].append(data)
            else:
                raise UpdateNoticeException("No reference element found")

    def _parse_pkglist(self, elem):
        """
        Parse the package list::
            <!ELEMENT pkglist (collection+)>
            <!ELEMENT collection (name?, package+)>
                <!ATTLIST collection short CDATA #IMPLIED>
                <!ATTLIST collection name CDATA #IMPLIED>
            <!ELEMENT name (#PCDATA)>
        """
        for collection in elem:
            data = {"packages": []}
            if "short" in collection.attrib:
                data["short"] = collection.attrib.get("short")
            for item in collection:
                if item.tag == "name":
                    data["name"] = item.text
                elif item.tag == "package":
                    data["packages"].append(self._parse_package(item))
            self._md["pkglist"].append(data)

    def _parse_package(self, elem):
        """
        Parse an individual package::
            <!ELEMENT package (filename, sum, reboot_suggested, restart_suggested)>
                <!ATTLIST package name CDATA #REQUIRED>
                <!ATTLIST package version CDATA #REQUIRED>
                <!ATTLIST package release CDATA #REQUIRED>
                <!ATTLIST package arch CDATA #REQUIRED>
                <!ATTLIST package epoch CDATA #REQUIRED>
                <!ATTLIST package src CDATA #REQUIRED>
            <!ELEMENT reboot_suggested (#PCDATA)>
            <!ELEMENT restart_suggested (#PCDATA)>
            <!ELEMENT filename (#PCDATA)>
            <!ELEMENT sum (#PCDATA)>
                <!ATTLIST sum type (md5|sha1) "sha1">
        """
        package = {}
        for pkgfield in ("arch", "epoch", "name", "version", "release", "src"):
            package[pkgfield] = elem.attrib.get(pkgfield)

        #  Bad epoch and arch data is the most common (missed) screwups.
        # Deal with bad epoch data.
        if not package["epoch"] or package["epoch"][0] not in "0123456789":
            package["epoch"] = None

        for child in elem:
            if child.tag == "filename":
                package["filename"] = child.text
            elif child.tag == "sum":
                package["sum"] = (child.attrib.get("type"), child.text)
            elif child.tag == "reboot_suggested":
                self._md["reboot_suggested"] = True
            elif child.tag == "restart_suggested":
                self._md["restart_suggested"] = True
        return package


def get_updates(infile, md_type="updateinfo"):
    """
    Return update metadata from the repository if available

    :returns: list
    """
    if md_type == "updateinfo":
        notices = {}
        with gzip.open(infile) as gz_updateinfo:
            # pylint: disable-next=invalid-name,unused-variable
            for _event, elem in etree.iterparse(gz_updateinfo):
                if elem.tag == "update":
                    un = UpdateNotice(elem)
                    key = un["update_id"]
                    # pylint: disable-next=consider-using-f-string
                    key = "%s-%s" % (un["update_id"], un["version"])
                    if key not in notices:
                        notices[key] = un
            return notices.values()
    else:
        return []


def download_file(url, cache_dir="."):
    logging.debug("Downloading %s", url)
    if url.startswith("file://"):
        srcpath = unquote(url[len("file://") :])
        if not os.path.exists(srcpath):
            return ""
        filename = cache_dir + "/" + os.path.basename(url)
        copyfile(srcpath, filename)
        return filename
    for _ in range(0, RETRIES):
        try:
            data = requests.get(
                url
            )  # TODO: enhancement: consider using a timeout argument _pylint:W3101
            if not data.ok:
                return ""
            filename = os.path.join(cache_dir, os.path.basename(urlparse(url).path))
            with open(filename, "wb") as fd:
                for chunk in data.iter_content(chunk_size=1024):
                    fd.write(chunk)
            return filename
        except requests.exceptions.RequestException as exc:
            print("ERROR: requests.exceptions.RequestException occurred:", exc)
            time.sleep(RETRY_DELAY)

    return ""
