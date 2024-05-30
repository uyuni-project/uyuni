import argparse
import logging

from lzreposync import repo
from lzreposync.primary_handler import Handler
from lzreposync.repo import Repo


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
    parse.download_and_parse_metadata(args.url, "primary", args.cache)  # TODO change the name 'primary'
