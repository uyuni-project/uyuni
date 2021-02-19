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
    from gi.repository import Modulemd
except ImportError:
    gi = None

@pytest.mark.skipif(gi is None, reason="libmodulemd Python bindings is missing")
class TestLibmodProc:
    """

    """
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
        self.libmodapi.set_repodata(open("tests/data/module_packages-1.json", "r").read()).run()
        result_dict = self.libmodapi._result
        json_str = self.libmodapi.to_json()

        assert result_dict == json.loads(json_str)

        # Test list_modules output
        self.libmodapi.set_repodata(open("tests/data/list_modules.json", "r").read()).run()
        result_dict = self.libmodapi._result
        json_str = self.libmodapi.to_json()

        assert result_dict == json.loads(json_str)

        # Test list_packages output
        self.libmodapi.set_repodata(open("tests/data/list_packages.json", "r").read()).run()
        result_dict = self.libmodapi._result
        json_str = self.libmodapi.to_json()

        assert result_dict == json.loads(json_str)

    def test_meta_compressed(self):
        """
        test_meta_compressed -- test if a file is compressed by Gzip.
        """
        data = b'\x1f\x8b'
        with mock.patch("mgrlibmod.mllib.open", mock_open(read_data=data), create=True):
            assert self.libmodproc._is_meta_compressed("dummy.gz")

    def test_module_packages(self):
        self.libmodapi.set_repodata(open("tests/data/module_packages-1.json", "r").read()).run()
        result = self.libmodapi._result['module_packages']

        # 'apis' and 'packages' fields should be filled
        assert ['perl-DBI'] == result['apis']
        assert result['packages']

        # Assert that the correct context is selected
        selected = next(s for s in result['selected'] if s['name'] == 'perl-DBI')
        assert '1.641' == selected['stream']
        assert '75ec4169' == selected['context']

        # Assert that the correct dependency for perl selected
        selected = next(s for s in result['selected'] if s['name'] == 'perl')
        assert '5.26' == selected['stream']

        # Explicitly specify the dependency for perl:5.26 (default)
        self.libmodapi.set_repodata(open("tests/data/module_packages-2.json", "r").read()).run()
        result = self.libmodapi._result['module_packages']

        # Assert that the correct context is selected
        selected = next(s for s in result['selected'] if s['name'] == 'perl-DBI')
        assert '1.641' == selected['stream']
        assert '75ec4169' == selected['context']

        # Assert that the correct dependency for perl selected
        selected = next(s for s in result['selected'] if s['name'] == 'perl')
        assert '5.26' == selected['stream']

        # Explicitly specify the dependency for perl:5.24
        self.libmodapi.set_repodata(open("tests/data/module_packages-3.json", "r").read()).run()
        result = self.libmodapi._result['module_packages']

        # Assert that the correct context is selected
        selected = next(s for s in result['selected'] if s['name'] == 'perl-DBI')
        assert '1.641' == selected['stream']
        assert 'a7fbf8fd' == selected['context']

        # Assert that the correct dependency for perl selected
        selected = next(s for s in result['selected'] if s['name'] == 'perl')
        assert '5.24' == selected['stream']

    def test_conflicting_streams(self):
        try:
            self.libmodapi.set_repodata(open("tests/data/conflicting_streams.json", "r").read()).run()
            pytest.fail("Must throw MlConflictingStreams exception")
        except MlConflictingStreams as e:
            expected = [
                {'name': 'perl', 'stream': '5.24'},
                {'name': 'perl', 'stream': '5.26'}
            ]

            assert expected == e.data['streams']

    def test_all_modules(self):
        self.libmodapi.set_repodata(open("tests/data/all_modules.json", "r").read()).run()
        result = self.libmodapi._result['module_packages']

        # Assert that all the modules are selected successfully
        assert 46 == len(result['selected'])

    def test_default_stream(self):
        self.libmodapi.set_repodata(open("tests/data/default_stream.json", "r").read()).run()
        result = self.libmodapi._result['module_packages']

        # Assert that the default stream is selected
        selected = next(s for s in result['selected'] if s['name'] == 'perl')
        assert '5.26' == selected['stream']

    def test_perl_dependencies(self):
        self.libmodapi.set_repodata(open("tests/data/perl_dependencies.json", "r").read()).run()
        result = self.libmodapi._result['module_packages']

        selected = next(s for s in result['selected'] if s['name'] == 'perl')
        assert '5.26' == selected['stream']

        # Assert that the correct contexts are selected
        selected = next(s for s in result['selected'] if s['name'] == 'perl-DBI')
        assert '75ec4169' == selected['context']
        selected = next(s for s in result['selected'] if s['name'] == 'perl-DBD-Pg')
        assert '6ce0b5f7' == selected['context']
        selected = next(s for s in result['selected'] if s['name'] == 'perl-DBD-MySQL')
        assert 'a50016cf' == selected['context']
        selected = next(s for s in result['selected'] if s['name'] == 'perl-App-cpanminus')
        assert '63feaccd' == selected['context']

    def test_not_found(self):
        try:
            self.libmodapi.set_repodata(open("tests/data/not_found.json", "r").read()).run()
            pytest.fail("Must throw MlModuleNotFound exception")
        except MlModuleNotFound as e:
            expected = [{'name': 'notfound', 'stream': 'mystream'}]
            assert expected == e.data['streams']

    def test_soft_dependencies(self):
        # Some modules have dependencies to other modules without specifying any stream name
        # For example, 'perl-App-cpanminus' depends on any stream of 'perl-YAML'
        self.libmodapi.set_repodata(open("tests/data/soft_dependencies.json", "r").read()).run()
        result = self.libmodapi._result['module_packages']

        # Assert that 3 modules are selected (perl, perl-YAML, perl-App-cpanminus)
        assert 3 == len(result['selected'])

        # Assert that the correct context is selected
        selected = next(s for s in result['selected'] if s['name'] == 'perl-App-cpanminus')
        assert '1.7044' == selected['stream']
        assert '63feaccd' == selected['context']

        # Assert that the correct context for the soft dependency is selected
        selected = next(s for s in result['selected'] if s['name'] == 'perl-YAML')
        assert '1.24' == selected['stream']
        assert 'b9186a2a' == selected['context']

    def test_list_modules(self):
        self.libmodapi.set_repodata(open("tests/data/list_modules.json", "r").read()).run()
        result = self.libmodapi._result['list_modules']

        # Assert total number of modules
        assert 46 == len(result['modules'])

        # Assert that every entry has 'default' and 'streams' fields
        for key, value in result['modules'].items():
            assert 'default' in value
            assert 'streams' in value
            # Assert that streams is a list (serializable)
            assert type(value['streams']) is list
            # Assert that there are no duplicates in stream names
            assert len(value['streams']) == len(set(value['streams']))

        # Assert that the streams are correctly returned for a module
        perl_module = result['modules']['perl']
        assert '5.26' == perl_module['default']
        assert not set(['5.24', '5.26']) ^ set(perl_module['streams'])

    def test_list_packages(self):
        self.libmodapi.set_repodata(open("tests/data/list_packages.json", "r").read()).run()
        result = self.libmodapi._result['list_packages']['packages']

        # Assert total number of packages
        assert 1742 == len(result)
        # Assert that no duplicates are reported
        assert len(result) == len(set(result))
