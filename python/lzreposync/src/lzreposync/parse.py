"""
This is a minimal implementation of the lazy reposync parser.
It downloads the target repository's metadata file(s) from the
given url and parses it(them)
"""

import logging
import pickle
import tempfile
import time
import urllib.request
import urllib.error
import xml.sax
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

    download_primary_xml_v2(url, PRIMARY_XML)
    extract_file(CACHE_DIR, PRIMARY_XML)
    parse_primary_xml(os.path.join(CACHE_DIR, PRIMARY_XML.rstrip(".gz")))
