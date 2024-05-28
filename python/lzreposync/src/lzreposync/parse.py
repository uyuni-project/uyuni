"""
This is a minimal implementation of the lazy reposync parser.
It downloads the target repository's metadata file(s) from the
given url and parses it(them)
"""
import logging
import tempfile
import time
import urllib.request
import urllib.error
import xml.sax
from urllib.parse import urljoin
from xml.dom import pulldom
from xml.sax.xmlreader import InputSource

import os
import gzip

from memory_profiler import profile

from lzreposync.primary_handler import Handler

# @profile
def download_and_parse_metadata(primary_url, name, cache_dir):
    if not primary_url:
        print("Error: target url not defined!")
        raise ValueError("Repository URL missing")

    _rpms = None
    hash_file = os.path.join(cache_dir, name) + ".hash"
    hash_func = hashlib.sha256()

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
                        chunk = primary_fd.read(chunk_size)
                        written = tmp_file.write(chunk)
                        hash_func.update(chunk)

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
                    _rpms = handler.rpms.values()
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
        file_hash = hash_func.hexdigest()
        with open(hash_file, 'w') as fw:
            logging.debug("Caching file hash in file: %s", hash_file)
            fw.write(file_hash)
    except OSError as error:
        logging.warning("Error caching the primary XML data: %s", error)
