"""
This is a minimal implementation of the lazy reposync parser.
It downloads the target repository's metadata file(s) from the
given url and parses it(them)
"""

# TODO Check for the signature
# TODO Use SAX parser
# TODO Define the best chunk size (consider calculating the current os's free memory and the file size and extract a formula)
# TODO Evaluate the two versions of the download/ benchmark them/ write some tests about them
# TODO After finishing, complete for all other files, normalize the sql code
# TODO Should we parse the files at the time of download, or in a second step ?

import argparse
import xml.sax

import requests
import os
import shutil
from urllib.parse import urljoin
import zipfile
import tarfile
import gzip

from memory_profiler import profile

from lzreposync.metadata_parser import PrimaryXmlHandler

CACHE_DIR = "./cache/metadata/"  # TODO change it into /var/cache/...
PRIMARY_XML = "00c99a7e67dac325f1cbeeedddea3e4001a8f803c9bf43d9f50b5f1c756b0887-primary.xml.gz"  # TODO can the name of this file change ?

EMPTY_URL_ERROR = "ERROR: URL should not be empty."
FILENAME_ERROR = "ERROR: Filename should not be empty."
UNKNOWN_FORMAT = "ERROR: Unknown file format. Can't extract."

# ARCHIVE EXTENSIONS
ZIP_EXTENSION = ".zip"
TAR_EXTENSION = ".tar"
TAR_GZ_EXTENSION = ".tar.gz"
TGZ_EXTENSION = ".tgz"
GZ_EXTENSION = ".gz"


def download_primary_xml_v1(url: str, primary_xml: str):
    """Download the primary-xml file from the given repository url"""
    try:
        primary_xml_url = urljoin(url, primary_xml)
        with requests.get(primary_xml_url, stream=True) as response:
            response.raise_for_status()  # check for status code
            with open(os.path.join(CACHE_DIR, primary_xml), "wb") as f:
                for chunk in response.iter_content(chunk_size=8192):
                    # chunk_size to be efficiently determined
                    f.write(chunk)
                print(f"file {primary_xml} was downloaded successfully!")
    except requests.exceptions.RequestException as e:
        print(f"Error downloading {primary_xml} file:", e)


@profile
def download_primary_xml_v2(url: str, primary_xml: str):
    """Download the primary-xml file from the given repository url"""
    try:
        primary_xml_url = urljoin(url, primary_xml)
        response = requests.get(primary_xml_url, stream=True)
        with open(os.path.join(CACHE_DIR, primary_xml), "wb") as out_file:
            shutil.copyfileobj(response.raw, out_file)
        print(f"file {primary_xml} was downloaded successfully!")
        response.close()
    except requests.exceptions.RequestException as e:
        print(f"Error downloading {primary_xml} file:", e)


@profile
def parse_primary_xml(file_path):
    handler = PrimaryXmlHandler()
    parser = xml.sax.make_parser()
    # parser.setFeature(xml.sax.handler.feature_namespaces, True)  # TODO Fix problem (probably for the root tag)
    parser.setContentHandler(handler)

    try:
        parser.parse(file_path)
    except Exception as e:
        print("Error parsing file: ", e)


def get_filename(url):
    """Extract filename from file url"""
    filename = os.path.basename(url)
    return filename


def get_file_location(target_path, filename):
    """Concatenate download directory and filename"""
    return target_path + filename


def extract_file(target_path, filename):
    """Extract file based on file extension target_path: string, location where data will be extracted filename:
    string, name of the file along with extension"""
    if filename == "" or filename is None:
        raise Exception(FILENAME_ERROR)

    file_location = get_file_location(target_path, filename)

    if filename.endswith(ZIP_EXTENSION):
        print(f"Extracting {file_location} file")
        zipf = zipfile.ZipFile(file_location, "r")
        zipf.extractall(target_path)
        zipf.close()
    elif (
        filename.endswith(TAR_EXTENSION)
        or filename.endswith(TAR_GZ_EXTENSION)
        or filename.endswith(TGZ_EXTENSION)
    ):
        print(f"Extracting {file_location} file")
        tarf = tarfile.open(file_location, "r")
        tarf.extractall(target_path)
        tarf.close()
    elif filename.endswith(GZ_EXTENSION):
        print(f"Extracting {file_location} file")
        out_file = file_location[:-3]
        with gzip.open(file_location, "rt") as f_in:
            with open(out_file, "w") as f_out:
                shutil.copyfileobj(f_in, f_out)
    else:
        raise ValueError(f"Unknown file format: {UNKNOWN_FORMAT}")
    os.remove(file_location)
    print(f"Removing file: {file_location}")


@profile
def download_and_parse_metadata(url):
    if not url:
        print("Error: target url not defined!")
        raise ValueError("Repository URL missing")

    download_primary_xml_v2(url, PRIMARY_XML)
    extract_file(CACHE_DIR, PRIMARY_XML)
    parse_primary_xml(os.path.join(CACHE_DIR, PRIMARY_XML.rstrip(".gz")))
