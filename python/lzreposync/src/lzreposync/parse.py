"""
This is a minimal implementation of the lazy reposync parser.
It downloads the target repository's metadata file(s) from the
given url and parses it(them)
"""
import gzip
import logging
import os
import tempfile
import time
import urllib.error
import urllib.request
import xml.sax
from urllib.parse import urljoin
from xml.dom import pulldom
from xml.sax.xmlreader import InputSource

from lzreposync.primary_handler import Handler


def get_text(nodeList):
    rc = []
    for node in nodeList:
        if node.nodeType == node.TEXT_NODE:
            rc.append(node.data)
        return ''.join(rc)


def get_metadata_files(repomd_url):
    repomd_path = urllib.request.urlopen(repomd_url)
    doc = pulldom.parse(repomd_path)
    files = {}
    for event, node in doc:
        if event == pulldom.START_ELEMENT and node.tagName == "data":
            doc.expandNode(node)
            files[node.getAttribute("type")] = {
                "location": node.getElementsByTagName("location")[0].getAttribute("href"),
                "checksum": get_text(node.getElementsByTagName("checksum")[0].childNodes)
            }
    return files


# @profile
def download_and_parse_metadata(repo_url, name, cache_dir):
    if not repo_url:
        print("Error: target url not defined!")
        raise ValueError("Repository URL missing")

    hash_file = os.path.join(cache_dir, name) + ".hash"
    repomd_url = urljoin(repo_url, "repomd.xml")
    metadata_files = get_metadata_files(repomd_url)
    primary_url = urljoin(
        repo_url,
        metadata_files['primary']['location'].lstrip("/repodata")
    )
    primary_hash = metadata_files['primary']['checksum']

    for cnt in range(1, 4):
        try:
            logging.debug("Parsing primary %s, try %s", primary_url, cnt)

            # Download the primary.xml.gz to a file first to avoid
            # connection resets
            with tempfile.TemporaryFile() as tmp_file:
                with urllib.request.urlopen(primary_url) as primary_fd:
                    # Avoid loading large documents into memory at once
                    chunk_size = 1024 * 1024
                    written = True
                    while written:
                        written = tmp_file.write(primary_fd.read(chunk_size))

                # Work on temporary file without loading it into memory at once
                tmp_file.seek(0)
                with gzip.GzipFile(fileobj=tmp_file, mode="rb") as gzip_fd:
                    parser = xml.sax.make_parser()
                    handler = Handler()
                    parser.setContentHandler(handler)
                    parser.setFeature(xml.sax.handler.feature_namespaces, True)
                    input_source = InputSource()
                    input_source.setByteStream(gzip_fd)
                    parser.parse(input_source)
                    packages_count = len(handler.packages)
                    logging.debug("Parsed packages: %s", packages_count)
            break
        except urllib.error.HTTPError as e:
            # We likely hit the repo while it changed:
            # At the time we read repomd.xml refered to an primary.xml.gz
            # that does not exist anymore.
            if cnt < 3 and e.code == 404:
                # primary_url = self.find_primary() TODO still not sure what does this do (in C.B' code)
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
        if not os.path.exists(cache_dir):
            logging.debug("Creating cache directory: %s", cache_dir)
            os.makedirs(cache_dir)
        else:
            # Delete old cache files from directory
            for f in os.listdir(cache_dir):
                os.remove(os.path.join(cache_dir, f))

        # Cache the hash of the file
        with open(hash_file, 'w') as fw:
            logging.debug("Caching file hash in file: %s", hash_file)
            fw.write(primary_hash)
    except OSError as error:
        logging.warning("Error caching the primary XML data: %s", error)
