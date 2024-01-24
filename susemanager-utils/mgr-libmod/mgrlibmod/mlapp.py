"""
CLI app
"""
# pylint: disable-next=unused-import
from mgrlibmod import mllib, mltypes, mlerrcode
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
    ap.add_argument(
        "-l", "--list", action="store_true", help="Show list of supported functions"
    )
    ap.add_argument(
        "-v", "--verbose", action="store_true", help="Verbose output (debug mode)"
    )
    ap.add_argument(
        "-p", "--pretty", action="store_true", help="Pretty-print JSON responses"
    )

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
    if opts.list:
        print("Supported functions:\n")
        for m in dir(mllib.MLLibmodAPI):
            if m.startswith("_function__"):
                print("  -", m[11:])
        print()
    elif opts.example:
        example: str = """
Usage example:

1. Create input JSON file with the following type (values are just as an example)
   and save it e.g. as "input.json":

        {
            "function": "module_packages",
            "paths": [
                "data/some-modules.yaml.gz",
                "data/some-other-modules.yaml.gz"
            ],
            "streams": [
                {
                    "name": "postgresql",
                    "stream": "10"
                },
                {
                    "name": "rhn-tools",
                    "stream": "1.0"
                }
            ]
        }

2. Run it to resolve the modules and packages:

        mgr-libmod < input.json > output.json

To get the full list of supported functions, call "-l" option:

        mgr-libmod -l
"""
        print(example.strip() + "\n")
    else:
        try:
            print(
                mllib.MLLibmodAPI(opts)
                .set_repodata(get_stdin_data())
                .run()
                .to_json(pretty=opts.pretty)
            )
        # pylint: disable-next=broad-exception-caught
        except Exception as exc:
            print(mltypes.MLErrorType(exc).to_json(pretty=opts.pretty))
            if opts.verbose:  # Local debugging
                raise exc
