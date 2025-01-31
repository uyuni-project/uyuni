"""
Entry point for the download service of the lazy reposync
"""

import argparse
import logging

from lzdownload import lzdownloader


def main():
    parser = argparse.ArgumentParser(
        description="Lazy reposync download service",
        conflict_handler="resolve",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument(
        "-c",
        "--channel",
        help="The channel label of which you want to download packages",
        dest="channel",
        type=str,
        required=True,
    )
    args = parser.parse_args()

    # pylint: disable-next=logging-fstring-interpolation
    logging.info(f"Downloading packages for channel {args.channel}")
    lzdownloader.download_all(args.channel)
