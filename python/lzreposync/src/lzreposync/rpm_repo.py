#  pylint: disable=missing-module-docstring

import hashlib
import logging
import os
import re
import subprocess
import tempfile
import time
import urllib.error
import urllib.request
from urllib.parse import urljoin
from xml.dom import pulldom

from lzreposync.repo import Repo
from lzreposync.rpm_metadata_parser import parse_rpm_packages_metadata
from spacewalk.common.repo import GeneralRepoException

SPACEWALK_LIB = "/var/lib/spacewalk"
SPACEWALK_GPG_HOMEDIR = os.path.join(SPACEWALK_LIB, "gpgdir")


class ChecksumVerificationException(ValueError):
    def __init__(self, file_name=""):
        self.message = f"File {file_name} checksum verification failed"
        super().__init__(self.message)


class SignatureVerificationException(Exception):
    def __init__(self, file_name):
        self.message = f"Invalid signature for file {file_name}"
        super().__init__(self.message)


def get_text(node_list):
    rc = []
    for node in node_list:
        if node.nodeType == node.TEXT_NODE:
            rc.append(node.data)
        return "".join(rc)


# pylint: disable-next=missing-class-docstring
class RPMRepo(Repo):

    def __init__(self, name, cache_path, repository, arch_filter=".*"):
        # Adding 'noarch' to be parsed if not specified
        if arch_filter != ".*" and "noarch" not in arch_filter:
            arch_filter = re.sub(
                "[()]", "", arch_filter
            )  # remove left & right parenthesis
            arch_filter = f"(noarch|{arch_filter})"

        super().__init__(
            name=name,
            cache_path=cache_path,
            repository=repository,
            arch_filter=arch_filter,
        )
        # Verify the gpg signature
        logging.debug("Checking signature for file repomd.xml")
        verified = self.verify_signature()
        if not verified:
            raise SignatureVerificationException("repomd.xml")

    def verify_signature(self):
        """
        Verify the signature of the repomd.xml file using GnuPG
        """

        repomd_url = self.get_repo_path("repodata/repomd.xml")
        repomd_signature_url = urljoin(self.repository, "repodata/repomd.xml.asc")
        downloaded_repomd_path = "/tmp/repomd.xml"
        downloaded_repomd_asc_path = "/tmp/repomd.xml.asc"

        # Download and save the repomd.xml and the repomd.xml.asc files locally
        logging.debug("Downloading repomd.xml file to %s", downloaded_repomd_path)
        urllib.request.urlretrieve(repomd_url, downloaded_repomd_path)
        logging.debug(
            "Downloading repomd.xml.asc file to %s", downloaded_repomd_asc_path
        )
        urllib.request.urlretrieve(repomd_signature_url, downloaded_repomd_asc_path)

        try:
            verified = self._has_valid_gpg_signature(
                downloaded_repomd_path, downloaded_repomd_asc_path
            )
            if verified:
                logging.debug("Valid signature for file repomd.xml")
            else:
                logging.debug("Invalid signature for file repomd.xml")
            return verified
        except GeneralRepoException:
            logging.error("Error verifying signature !")
            raise
        finally:
            # Remove the saved repomd.xml and repomd.xml.asc files
            if os.path.exists(downloaded_repomd_path):
                logging.debug("Removing file %s", downloaded_repomd_path)
                os.remove(downloaded_repomd_path)
            if os.path.exists(downloaded_repomd_asc_path):
                logging.debug("Removing file %s", downloaded_repomd_asc_path)
                os.remove(downloaded_repomd_asc_path)

    # pretty much like: spacewalk/common/repo.py:_has_valid_gpg_signature
    @staticmethod
    def _has_valid_gpg_signature(file: str, signature_file) -> bool:
        """
        Validate GPG signature of the given file.

        :return: bool
        """
        process = None
        file = file.replace("file://", "")
        if os.access(file, os.R_OK):
            # release_signature_file = os.path.join(file, "Release.gpg")
            if os.access(signature_file, os.R_OK):
                process = subprocess.Popen(
                    [
                        "gpg",
                        "--verify",
                        "--homedir",
                        SPACEWALK_GPG_HOMEDIR,
                        signature_file,
                        file,
                    ],
                    stdout=subprocess.DEVNULL,
                    stderr=subprocess.DEVNULL,
                )
                process.wait(timeout=90)
            else:
                logging.error(
                    "Signature file for GPG check could not be accessed: \
                               '%s. Raising GeneralRepoException.",
                    signature_file,
                )
                raise GeneralRepoException(
                    f"Signature file for GPG check could not be accessed: {signature_file}"
                )
        else:
            logging.error(
                # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                "No release file found: '{}'. Raising GeneralRepoException.".format(
                    file
                )
            )
            raise GeneralRepoException(f"No file found: {file}")

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

    def get_metadata_files(self):
        """
        Return a dict containing the metadata files' information in the following format
        {
            "type [eg: primary]" : {
                                    "location": "...",
                                    "checksum": "...",
                                    }
        }
        """
        repomd_url = self.get_repo_path("repodata/repomd.xml")
        repomd_path = urllib.request.urlopen(repomd_url)
        doc = pulldom.parse(repomd_path)
        files = {}
        for event, node in doc:
            if event == pulldom.START_ELEMENT and node.tagName == "data":
                doc.expandNode(node)
                files[node.getAttribute("type")] = {
                    "location": node.getElementsByTagName("location")[0].getAttribute(
                        "href"
                    ),
                    "checksum": get_text(
                        node.getElementsByTagName("checksum")[0].childNodes
                    ),
                }
        return files

    def find_metadata_file_url(self, file_name, update=False) -> (str, str):
        """
        Return the corresponding metadata file's url given its name.
        An example of these files can be 'primary', 'filelists', 'other', etc...
        :update: download the latest version again, and override the local one
        """
        if not self.metadata_files or update:
            self.metadata_files = self.get_metadata_files()
        md_file = self.metadata_files.get(file_name)
        if not md_file:
            print(f"File {md_file} does not exist in this repository")
            return None
        md_file_url = urljoin(
            self.repository,
            self.metadata_files[file_name]["location"],
        )
        return md_file_url

    def find_metadata_file_checksum(self, file_name):
        """
        Return the corresponding metadata file's checksum given its name.
        """
        if not self.metadata_files:
            self.metadata_files = self.get_metadata_files()
        return self.metadata_files[file_name]["checksum"]

    def get_packages_metadata(self):
        if not self.repository:
            print("Error: target url not defined!")
            raise ValueError("Repository URL missing")

        primary_hash_file = os.path.join(self.cache_dir, "primary") + ".hash"
        filelists_hash_file = os.path.join(self.cache_dir, "filelists") + ".hash"

        primary_url = self.find_metadata_file_url("primary")
        primary_hash = self.find_metadata_file_checksum("primary")
        filelists_url = self.find_metadata_file_url("filelists")
        filelists_hash = self.find_metadata_file_checksum("filelists")

        for cnt in range(1, 4):
            try:
                # Download the primary.xml.gz and filelists.xml.gz to temporary files first to avoid
                # connection resets
                with tempfile.TemporaryFile() as primary_tmp_file, tempfile.TemporaryFile() as filelists_tmp_file:
                    # Downloading primary.xml.gz
                    logging.debug("Downloading primary %s, try %s", primary_url, cnt)
                    with urllib.request.urlopen(primary_url) as primary_fd:
                        # Avoid loading large documents into memory at once
                        hash_func = hashlib.sha256()
                        chunk_size = 1024 * 1024
                        written = True
                        while written:
                            chunk = primary_fd.read(chunk_size)
                            hash_func.update(chunk)
                            written = primary_tmp_file.write(chunk)

                    # Verify the checksum of the md file (currently primary.xml)
                    if primary_hash != hash_func.hexdigest():
                        raise ChecksumVerificationException("primary.xml.gz")

                    # Downloading filelists.xml.gz
                    logging.debug("Downloading filelists %s, try %s", primary_url, cnt)
                    with urllib.request.urlopen(filelists_url) as filelists_fd:
                        # Avoid loading large documents into memory at once
                        hash_func = hashlib.sha256()
                        chunk_size = 1024 * 1024
                        written = True
                        while written:
                            chunk = filelists_fd.read(chunk_size)
                            hash_func.update(chunk)
                            written = filelists_tmp_file.write(chunk)

                    # Verify the checksum of the md file (currently primary.xml)
                    if filelists_hash != hash_func.hexdigest():
                        raise ChecksumVerificationException("filelists.xml.gz")

                    # Work on temporary file without loading it into memory at once
                    primary_tmp_file.seek(0)
                    filelists_tmp_file.seek(0)
                    packages = parse_rpm_packages_metadata(
                        primary_tmp_file,
                        filelists_tmp_file,
                        self.repository,
                        self.cache_dir,
                        self.arch_filter,
                    )
                    yield from packages
                break
            except urllib.error.HTTPError as e:
                # We likely hit the repo while it changed:
                # At the time we read repomd.xml referred to an primary.xml.gz and/or filelists.xml.gz
                # that does not exist anymore.
                if cnt < 3 and e.code == 404:
                    primary_url = self.find_metadata_file_url("primary", update=True)
                    filelists_url = self.find_metadata_file_url(
                        "filelists", update=True
                    )
                    time.sleep(2)
                else:
                    raise
            except OSError:
                if cnt < 3:
                    time.sleep(2)
                else:
                    raise

        try:
            # Prepare cache directory
            if not os.path.exists(self.cache_dir):
                logging.debug("Creating cache directory: %s", self.cache_dir)
                os.makedirs(self.cache_dir)
            else:
                # Delete old cache files from directory
                for f in os.listdir(self.cache_dir):
                    os.remove(os.path.join(self.cache_dir, f))

            # Cache the hash/checksum of primary
            with open(primary_hash_file, "w", encoding="utf-8") as fw:
                logging.debug("Caching file hash in file: %s", primary_hash_file)
                fw.write(primary_hash)
            with open(filelists_hash_file, "w", encoding="utf-8") as fw:
                logging.debug("Caching file hash in file: %s", filelists_hash_file)
                fw.write(filelists_hash)
        except OSError as error:
            logging.warning("Error caching the primary XML data: %s", error)
