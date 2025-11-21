
from vex.parser.csaf_parser import CSAFParser

class TestCSAFParser:
    def test_parse(self):
        #document = ".\\tests\\test_files\\csaf\\cve-2022-35256.json"   # Windows
        document = "./tests/test_files/csaf/cve-2022-35256.json"    # Linux

        parser = CSAFParser()

        parser.parse(document)
        vulns = parser.get_vulnerabilities()

        assert len(vulns) == 1
        assert vulns[0].get_id() == "CVE-2022-35256"
        assert len(vulns) == 1
        assert "llhttp parser" in vulns[0].get_description()


if __name__ == "__main__":
    test = TestCSAFParser()
    test.test_parse()