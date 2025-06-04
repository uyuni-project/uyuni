from vex.utils import detect_vex_format
from vex.parser.csaf_parser import CSAFParser

class TestFileFormat:

    def test_csaf_format(self):
        #csaf_format = detect_vex_format(".\\tests\\test_files\\csaf\\cve-2022-35256.json") #Windows
        csaf_format = detect_vex_format("./tests/test_files/csaf/cve-2022-35256.json")      #Linux

        print(csaf_format)
        assert type(csaf_format) == CSAFParser

    def test_unknown_format(self):
        uk_format = detect_vex_format("README.md")
        print(uk_format)
        assert uk_format == "Unknown"

if __name__ == "__main__":
    test = TestFileFormat
    test.test_csaf_format()