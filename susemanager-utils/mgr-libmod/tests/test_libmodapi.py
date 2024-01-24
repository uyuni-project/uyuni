"""
Unit test for LibmodProc class.
"""
from mgrlibmod.mllib import MLLibmodProc, MLLibmodAPI
from mgrlibmod.mlerrcode import MlConflictingStreams, MlModuleNotFound
import json
import pytest
from unittest import mock
from unittest.mock import mock_open

try:
    import gi

    # pylint: disable-next=unused-import
    from gi.repository import Modulemd
except ImportError:
    gi = None


@pytest.mark.skipif(gi is None, reason="libmodulemd Python bindings is missing")
@pytest.mark.timeout(3)
# pylint: disable-next=empty-docstring
class TestLibmodProc:
    """ """

    def setup_method(self):
        self.libmodproc = MLLibmodProc([])
        self.libmodapi = MLLibmodAPI(None)

    def teardown_method(self):
        del self.libmodproc

    def test_to_json(self):
        """
        test_to_json -- test if the output is successfully serialized into JSON
        """

        # Test module_packages output
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/module_packages-1.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result_dict = self.libmodapi._result
        json_str = self.libmodapi.to_json()

        assert result_dict == json.loads(json_str)

        # Test list_modules output
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/list_modules.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result_dict = self.libmodapi._result
        json_str = self.libmodapi.to_json()

        assert result_dict == json.loads(json_str)

        # Test list_packages output
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/list_packages.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result_dict = self.libmodapi._result
        json_str = self.libmodapi.to_json()

        assert result_dict == json.loads(json_str)

    def test_meta_compressed(self):
        """
        test_meta_compressed -- test if a file is compressed by Gzip.
        """
        data = b"\x1f\x8b"
        with mock.patch("mgrlibmod.mllib.open", mock_open(read_data=data), create=True):
            # pylint: disable-next=protected-access
            assert self.libmodproc._is_meta_compressed("dummy.gz")

    def test_module_packages(self):
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/module_packages-1.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["module_packages"]

        # 'apis' and 'packages' fields should be filled
        assert ["perl-DBI"] == result["apis"]
        assert result["packages"]

        # Assert that the correct context is selected
        selected = next(s for s in result["selected"] if s["name"] == "perl-DBI")
        assert "1.641" == selected["stream"]
        assert "75ec4169" == selected["context"]

        # Assert that the correct dependency for perl selected
        selected = next(s for s in result["selected"] if s["name"] == "perl")
        assert "5.26" == selected["stream"]

        # Explicitly specify the dependency for perl:5.26 (default)
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/module_packages-2.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["module_packages"]

        # Assert that the correct context is selected
        selected = next(s for s in result["selected"] if s["name"] == "perl-DBI")
        assert "1.641" == selected["stream"]
        assert "75ec4169" == selected["context"]

        # Assert that the correct dependency for perl selected
        selected = next(s for s in result["selected"] if s["name"] == "perl")
        assert "5.26" == selected["stream"]

        # Explicitly specify the dependency for perl:5.24
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/module_packages-3.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["module_packages"]

        # Assert that the correct context is selected
        selected = next(s for s in result["selected"] if s["name"] == "perl-DBI")
        assert "1.641" == selected["stream"]
        assert "a7fbf8fd" == selected["context"]

        # Assert that the correct dependency for perl selected
        selected = next(s for s in result["selected"] if s["name"] == "perl")
        assert "5.24" == selected["stream"]

    def test_multiple_versions(self):
        """
        Test enabling a module with multiple versions included
        """
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/module_packages_rhel.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["module_packages"]

        # Assert that the correct name, stream, version and contexts (NSVC) are selected
        expected = {
            ("postgresql", "12", 8010120191120141335, "e4e244f9"),
            ("postgresql", "12", 8030020201207110000, "229f0a1c"),
        }
        selected = {
            (s["name"], s["stream"], s["version"], s["context"])
            for s in result["selected"]
        }
        assert 2 == len(selected)
        assert expected == selected

    def test_self_dependencies(self):
        """
        Test resolution with module streams that 'require' themselves (e.g. CentOS PowerTools)
        """
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/module_packages_powertools.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["module_packages"]

        selected = result["selected"]
        assert 1 == len(selected)
        assert "virt-devel" == selected[0]["name"]
        assert "rhel" == selected[0]["stream"]

    def test_conflicting_streams(self):
        try:
            self.libmodapi.set_repodata(
                # pylint: disable-next=unspecified-encoding
                open("tests/data/conflicting_streams.json", "r").read()
            ).run()
            pytest.fail("Must throw MlConflictingStreams exception")
        except MlConflictingStreams as e:
            expected = [
                {"name": "perl", "stream": "5.24"},
                {"name": "perl", "stream": "5.26"},
            ]

            assert expected == e.data["streams"]

    def test_all_modules(self):
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/all_modules.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["module_packages"]

        # Assert that all the modules are selected successfully
        assert 46 == len(result["selected"])

    def test_all_modules_rhel(self):
        """
        Test enabling all modules in a RHEL repository that contains multiple versions of each module
        """
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/all_modules_rhel.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["module_packages"]

        # Assert that all the modules & versions are selected successfully
        assert 189 == len(result["selected"])

    def test_default_stream(self):
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/default_stream.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["module_packages"]

        # Assert that the default stream is selected
        selected = next(s for s in result["selected"] if s["name"] == "perl")
        assert "5.26" == selected["stream"]

    def test_perl_dependencies(self):
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/perl_dependencies.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["module_packages"]

        # Assert that the default Perl stream is selected
        selected = next(s for s in result["selected"] if s["name"] == "perl")
        assert "5.26" == selected["stream"]

        # Assert that the correct contexts are selected
        selected = next(s for s in result["selected"] if s["name"] == "perl-DBI")
        assert "75ec4169" == selected["context"]
        selected = next(s for s in result["selected"] if s["name"] == "perl-DBD-Pg")
        assert "6ce0b5f7" == selected["context"]
        selected = next(s for s in result["selected"] if s["name"] == "perl-DBD-MySQL")
        assert "a50016cf" == selected["context"]
        selected = next(
            s for s in result["selected"] if s["name"] == "perl-App-cpanminus"
        )
        assert "63feaccd" == selected["context"]

    def test_perl_dependencies_rhel(self):
        """
        Test perl modules in a RHEL repository that contains multiple versions of each module
        """
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/perl_dependencies_rhel.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["module_packages"]

        # Assert that the default Perl stream is selected
        selected = [s for s in result["selected"] if s["name"] == "perl"]
        assert 1 == len(selected)
        assert "5.26" == selected[0]["stream"]

        # Assert that all versions with correct contexts are selected
        # perl-DBI
        expected = {
            ("1.641", 8010020190322130042, "16b3ab4d"),
            ("1.641", 820190116185335, "fbe42456"),
            ("1.641", 8030020200505125553, "1e4bbb35"),
        }
        selected = {
            (s["stream"], s["version"], s["context"])
            for s in result["selected"]
            if s["name"] == "perl-DBI"
        }
        assert expected == selected

        # perl-DBD-Pg
        expected = {
            ("3.7", 820181214121102, "6fcea174"),
            ("3.7", 8010020190322121805, "0d1d6681"),
            ("3.7", 8030020200313075823, "56fce90f"),
            ("3.7", 8010120191115065723, "c5869bed"),
        }
        selected = {
            (s["stream"], s["version"], s["context"])
            for s in result["selected"]
            if s["name"] == "perl-DBD-Pg"
        }
        assert expected == selected

        # perl-DBD-MySQL
        expected = {
            ("4.046", 8030020200511061544, "3a70019f"),
            ("4.046", 820181214121012, "6bc6cad6"),
            ("4.046", 8010020190322121447, "073fa5fe"),
        }
        selected = {
            (s["stream"], s["version"], s["context"])
            for s in result["selected"]
            if s["name"] == "perl-DBD-MySQL"
        }
        assert expected == selected

        # perl-App-cpanminus
        expected = {
            ("1.7044", 820181214184336, "e5ce1481"),
            ("1.7044", 8010020190322100642, "a9207fc6"),
            ("1.7044", 8030020200313075600, "09acf126"),
        }
        selected = {
            (s["stream"], s["version"], s["context"])
            for s in result["selected"]
            if s["name"] == "perl-App-cpanminus"
        }
        assert expected == selected

    def test_not_found(self):
        try:
            self.libmodapi.set_repodata(
                # pylint: disable-next=unspecified-encoding
                open("tests/data/not_found.json", "r").read()
            ).run()
            pytest.fail("Must throw MlModuleNotFound exception")
        except MlModuleNotFound as e:
            expected = [{"name": "notfound", "stream": "mystream"}]
            assert expected == e.data["streams"]

    def test_soft_dependencies(self):
        # Some modules have dependencies to other modules without specifying any stream name
        # For example, 'perl-App-cpanminus' depends on any stream of 'perl-YAML'
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/soft_dependencies.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["module_packages"]

        # Assert that 3 modules are selected (perl, perl-YAML, perl-App-cpanminus)
        assert 3 == len(result["selected"])

        # Assert that the correct context is selected
        selected = next(
            s for s in result["selected"] if s["name"] == "perl-App-cpanminus"
        )
        assert "1.7044" == selected["stream"]
        assert "63feaccd" == selected["context"]

        # Assert that the correct context for the soft dependency is selected
        selected = next(s for s in result["selected"] if s["name"] == "perl-YAML")
        assert "1.24" == selected["stream"]
        assert "b9186a2a" == selected["context"]

    def test_list_modules(self):
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/list_modules.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["list_modules"]

        # Assert total number of modules
        assert 46 == len(result["modules"])

        # Assert that every entry has 'default' and 'streams' fields
        # pylint: disable-next=unused-variable
        for key, value in result["modules"].items():
            assert "default" in value
            assert "streams" in value
            # Assert that streams is a list (serializable)
            # pylint: disable-next=unidiomatic-typecheck
            assert type(value["streams"]) is list
            # Assert that there are no duplicates in stream names
            assert len(value["streams"]) == len(set(value["streams"]))

        # Assert that the streams are correctly returned for a module
        perl_module = result["modules"]["perl"]
        assert "5.26" == perl_module["default"]
        assert not set(["5.24", "5.26"]) ^ set(perl_module["streams"])

    def test_list_packages(self):
        self.libmodapi.set_repodata(
            # pylint: disable-next=unspecified-encoding
            open("tests/data/list_packages.json", "r").read()
        ).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["list_packages"]["packages"]

        # Assert total number of packages
        assert 1742 == len(result)
        # Assert that no duplicates are reported
        assert len(result) == len(set(result))

    def test_list_modules_liberty(self):
        # Liberty Linux has some extra fields in its module metadata ("configurations")
        # Test if mgr-libmod successfully parses it (see bsc#1208908)

        request = {
            "function": "list_modules",
            "paths": ["tests/data/sample-modules-liberty.yaml.gz"],
        }

        self.libmodapi.set_repodata(json.dumps(request)).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["list_modules"]

        assert 4 == len(result["modules"])

        # pylint: disable-next=unused-variable
        for key, value in result["modules"].items():
            assert "streams" in value
            # LL9 doesn't define any defaults
            # pylint: disable-next=singleton-comparison
            assert value["default"] == None
            assert len(value["streams"]) > 0

    def test_module_packages_liberty(self):
        request = {
            "function": "module_packages",
            "paths": ["tests/data/sample-modules-liberty.yaml.gz"],
            "streams": [{"name": "nodejs", "stream": "18"}],
        }

        self.libmodapi.set_repodata(json.dumps(request)).run()
        # pylint: disable-next=protected-access
        result = self.libmodapi._result["module_packages"]

        assert "selected" in result
        assert 2 == len(result["selected"])
        # 2 different versions must be selected
        assert result["selected"][0]["version"] != result["selected"][1]["version"]

        for selection in result["selected"]:
            assert "nodejs" == selection["name"]
            assert "18" == selection["stream"]
