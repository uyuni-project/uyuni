"""
Download functions for the lazy reposync
"""

import os.path

from lzdownload import db_utils
from spacewalk.satellite_tools.download import ThreadedDownloader, TextLogger

DOWNLOAD_DIR = "/tmp/spacewalk/packages/1/stage"  # TODO: to be reviewed


def prepare_download_params_from_package(package: dict):
    params = {}
    url = "/".join(package["remote_path"].split("/")[:-2]) + "/"
    relative_path = "/".join(package["remote_path"].split("/")[-2:])
    params["urls"] = [url]
    params["relative_path"] = relative_path
    params["authtoken"] = None
    params["target_file"] = os.path.join(
        DOWNLOAD_DIR, package["checksum"], package["source_rpm"]
    )
    params["ssl_ca_cert"] = None
    params["ssl_client_cert"] = None
    params["ssl_client_key"] = None
    params["checksum_type"] = package["checksum_type"]
    params["checksum"] = package["checksum"]
    params["bytes_range"] = None
    params["http_headers"] = tuple()
    params["timeout"] = 300
    params["minrate"] = 1000
    params["proxies"] = {}
    params["urlgrabber_logspec"] = None

    return params


def download_all(channel):
    """
    Downloading all packages of the given channel
    :channel: channel label
    """
    packages = db_utils.get_all_packages_metadata_from_channel(channel)
    downloader = ThreadedDownloader()
    to_download_count = 0
    for package in packages:
        params = prepare_download_params_from_package(package)
        downloader.add(params)
        to_download_count += 1

    logger = TextLogger(None, to_download_count)
    downloader.set_log_obj(logger)
    downloader.run()
