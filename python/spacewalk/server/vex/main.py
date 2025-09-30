import argparse
import logging

from vex.parser.csaf_parser import CSAFParser
from vex.parser.cyclonedx_parser import CycloneDXParser
from vex.downloader.downloader import Downloader
#from vex.parser.openvex_parser import OpenVEXParser

from vex.utils import detect_vex_format

logging.basicConfig(level=logging.INFO)

def main():
    parser = argparse.ArgumentParser(description="VEX Parser")
    parser.add_argument("-f", "--file", required=False, help="Path to the VEX document")
    parser.add_argument("-x", "--format", required=False, choices=["csaf", "cyclonedx", "openvex"], help="VEX format", default="")
    parser.add_argument("-p", "--persist", action="store_true", required=False, help="Persist to the database")
    parser.add_argument("-i", "--info", action="store_true", required=False, help="Display VEX info")
    parser.add_argument("-d", "--downloader", action="store_true", required=False, help="Download vex files")
    args = parser.parse_args()

    if args.file:
        if args.format == "csaf":
            parser = CSAFParser()
            logging.info("CSAF format selected.")
        elif args.format == "cyclonedx":
            parser = CycloneDXParser()
            logging.info("CycloneDX format selected.")
        elif args.format == "openvex":
            #parser = OpenVEXParser()
            logging.info("OpenVEX format selected.")
            pass
        else:
            logging.info("No format specified, trying to auto-detection.")
            parser = detect_vex_format(args.file)

        parser.parse(args.file)
        vulns =  parser.get_vulnerabilities()

        if args.info:
            for vuln in vulns:
                #print(vuln)
                vuln.show_vulnerability()

        if args.persist:
            parser.persist_data()

    if args.downloader:
        downloader = Downloader(CSAFParser())

        downloader.download()

if __name__ == "__main__":
<<<<<<< HEAD
    main()
=======

    main()
>>>>>>> f5318b15ca5 (Fixversions extracted)
