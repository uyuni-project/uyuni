import argparse
import logging

from lzreposync.primary_handler import Handler
from lzreposync.repo import Repo
from lzreposync.rpm_repo import RPMRepo


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
        "-n",
        "--name",
        help="Name of the repository",
        dest="name",
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
    rpm_primary_handler = Handler()
    rpm_repository = RPMRepo(args.name, args.cache, args.url, rpm_primary_handler)  # TODO args.url should be args.repo, no ?
    rpm_repository.get_packages_metadata()
