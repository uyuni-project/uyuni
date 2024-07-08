#  pylint: disable=missing-module-docstring

import hashlib
import logging
import os
import tempfile
import time
import urllib.error
import urllib.request
from urllib.parse import urljoin
from xml.dom import pulldom

import gnupg

from lzreposync.filelists_parser import FilelistsParser
from lzreposync.primary_parser import PrimaryParser
from lzreposync.repo import Repo
from lzreposync.rpm_metadata_parser import MetadataParser


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

    def __init__(self, name, cache_path, repository):
        super().__init__(name, cache_path, repository)
        self.signature_verified = (
            False  # Tell whether the signature is checked against the repomd.xml file
        )

    def verify_signature(self):
        """
        Verify the signature of the repomd.xml file using GnuPG
        """
        gpg = gnupg.GPG()

        repomd_url = self.get_repo_path("repomd.xml")
        repomd_signature_url = urljoin(self.repository, "repomd.xml.asc")
        repomd_pub_key_url = urljoin(self.repository, "repomd.xml.key")
        downloaded_repomd_path = "/tmp/repomd.xml"

        # Download and save the repomd.xml locally
        logging.debug("Downloading repomd.xml file to %s", downloaded_repomd_path)
        urllib.request.urlretrieve(repomd_url, downloaded_repomd_path)

        with urllib.request.urlopen(
            repomd_signature_url
        ) as repomd_sig_fd, urllib.request.urlopen(
            repomd_pub_key_url
        ) as repo_pub_key_fd:
            gpg.import_keys(repo_pub_key_fd.read())
            verified = gpg.verify_file(repomd_sig_fd, downloaded_repomd_path)

        # Remove the saved repomd.xml file
        if os.path.exists(downloaded_repomd_path):
            logging.debug("Removing file %s", downloaded_repomd_path)
            os.remove(downloaded_repomd_path)
        if verified.valid:
            logging.debug("Valid signature for file repomd.xml")
            self.signature_verified = verified.valid
        else:
            logging.debug("Invalid signature for file repomd.xml")

        return verified.valid

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
        repomd_url = self.get_repo_path("repomd.xml")
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

    def find_metadata_file_url(self, file_name) -> (str, str):
        """
        Return the corresponding metadata file's url given its name.
        An example of these files can be 'primary', 'filelists', 'other', etc...
        """
        if not self.metadata_files:
            self.metadata_files = self.get_metadata_files()
        md_file_url = urljoin(
            self.repository,
            self.metadata_files[file_name]["location"].lstrip("/repodata"),
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
        if not self.signature_verified:
            logging.debug("Checking signature for file repomd.xml")
            verified = self.verify_signature()
            if not verified:
                raise SignatureVerificationException("repomd.xml")

        hash_file = (
            os.path.join(self.cache_dir, self.name) + ".hash"
        )  # TODO change for both primary and filelists

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
                    primary_parser = PrimaryParser(primary_tmp_file)
                    filelists_parser = FilelistsParser(filelists_tmp_file)
                    metadata_parser = MetadataParser(
                        primary_parser=primary_parser, filelists_parser=filelists_parser
                    )
                    yield from metadata_parser.parse_packages_metadata()
                    filelists_parser.clear_cache()  # TODO can we make this execute automatically
                break
            except urllib.error.HTTPError as e:
                # TODO: check for primary and filelists separately (if one fails, don't download both files again)
                # We likely hit the repo while it changed:
                # At the time we read repomd.xml referred to an primary.xml.gz
                # that does not exist anymore.
                if cnt < 3 and e.code == 404:
                    primary_url = self.find_metadata_file_url("primary")
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

            # Cache the hash of the file
            with open(hash_file, "w", encoding="utf-8") as fw:
                logging.debug("Caching file hash in file: %s", hash_file)
                fw.write(primary_hash)
        except OSError as error:
            logging.warning("Error caching the primary XML data: %s", error)
