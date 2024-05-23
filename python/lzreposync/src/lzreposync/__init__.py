import argparse

from lzreposync import parse


def main():
    args_parser = argparse.ArgumentParser(description="Lazy reposync service")
    args_parser.add_argument(
        "--url",
        "-u",
        help="The target url of the remote repository of which we'll "
        "parse the metadata",
    )
    args = args_parser.parse_args()
    parse.download_and_parse_metadata(args.url)
