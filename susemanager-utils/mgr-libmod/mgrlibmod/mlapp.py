"""
CLI app
"""
from mgrlibmod import mllib
from typing import List
import argparse
import os
import sys


def get_opts() -> argparse.Namespace:
    """
    get_args parses the CLI arguments.
    """
    ap: argparse.ArgumentParser = argparse.ArgumentParser(
        description="mgr-libmod -- Utility to resolve module dependencies"
    )
    ap.add_argument("-e", "--example", action="store_true", help="Show usage example")
    ap.add_argument("-v", "--verbose", action="store_true", help="Verbose output (debug mode)")
    return ap.parse_args()


def get_stdin_data() -> str:
    """
    get_stdin_data read JSON data from the STDIN and return as a string.

    :return: input data
    :rtype: str
    """
    out: List[str] = []
    for line in sys.stdin.readlines():
        out.append(line.strip())

    return os.linesep.join(out)


def main():
    """
    main function for the CLI app.
    """
    opts: argparse.Namespace = get_opts()
    if opts.example:
        example: str = """
Usage example:

1. Create JSON file with the following type (values are just as an example):

        {
            "paths": ["my/path/1.yaml", "my/path/2.yaml"],
            "streams": [["postgresql", "9.6"], ["idm", "client"]]
        }

2. cat yourfile.json | mgr-libmod > output.json
        """
        print(example.strip() + "\n")
    else:
        try:
            print(mllib.MLLibmodAPI(opts).set_repodata(get_stdin_data()).run().to_json(pretty=True))
        except Exception as exc:
            print("ERROR:", exc)
            if opts.verbose:
                raise exc
