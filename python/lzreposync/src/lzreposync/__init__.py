import argparse
import logging
import os.path
from urllib.parse import urljoin

from lzreposync import parse


def main():
    parser = argparse.ArgumentParser(
        description="Lazy reposync service",
        conflict_handler="resolve",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )

    parser.add_argument(
        "--url",
        "-u",
        help="The target url of the remote repository of which we'll "
        "parse the metadata",
        dest="url",
        type=str,
    )

    parser.add_argument(
        "-f",
        "--file",
        help="The target metadata file we want to parse",
        dest="md_file",
        type=str,
    )

    parser.add_argument(
        "-d",
        "--debug",
        help="Show debug messages",
        action="store_const",
        dest="loglevel",
        const=logging.DEBUG,
        default=logging.INFO,
    )

    parser.add_argument(
        "-c",
        "--cache",
        help="Path to the cache directory",
        dest="cache",
        default=".lzreposync-cache",
        type=str,
    )

    args = parser.parse_args()

    logging.getLogger().setLevel(args.loglevel)

    primary_xml = urljoin(args.url, args.md_file)
    parse.download_and_parse_metadata(primary_xml, args.md_file, args.cache)
