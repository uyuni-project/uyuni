# coding: utf-8
"""
Test spacecmd.utils
"""
from unittest.mock import MagicMock, patch, mock_open
import pytest
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect, exc2str
import spacecmd.utils
from xmlrpc import client as xmlrpclib
import os
import tempfile
import shutil
import datetime
import pickle
import hashlib
import time


class TestSCUtilsCacheIntegration:
    """
    Fusion integration test for saving and loading cache file.
    This creates and saves cache, loads it and expires it into
    a temporary directory.
    """
    def setup_method(self):
        """
        Setup test

        :return:
        """
        self.data = {"key": hashlib.sha256(str(time.time()).encode("utf-8")).hexdigest()}
        self.temp = tempfile.mkdtemp()
        self.expiration = datetime.datetime(2019, 1, 1, 10, 30, 45)
        self.cachefile = os.path.join(self.temp, "spacecmd.cache")

    def teardown_method(self):
        """
        Teardown method

        :return:
        """
        shutil.rmtree(self.temp)
        self.data.clear()
        self.expiration = None
        self.cachefile = None

    def test_save_cache(self):
        """
        Save cache.

        :return:
        """
        spacecmd.utils.save_cache(cachefile=self.cachefile, data=self.data, expire=self.expiration)
        assert os.path.exists(self.cachefile)
        out = pickle.load(open(self.cachefile, "rb"))

        assert "expire" in out
        assert "expire" not in self.data
        assert out["expire"] == self.expiration
        assert self.data["key"] == out["key"]

    @patch("spacecmd.utils.open", MagicMock(side_effect=IOError("Wrong polarity on neutron flow")))
    def test_save_cache_io_error(self):
        """
        Handle saving cache when IOError happens.

        :return:
        """
        logger = MagicMock()
        with patch("spacecmd.utils.logging", logger) as lgr:
            spacecmd.utils.save_cache(cachefile=self.cachefile,
                                      data=self.data, expire=self.expiration)
        assert logger.error.called
        assert_args_expect(logger.error.call_args_list,
                           [(("Couldn't write to %s", self.cachefile,), {})])

    def test_load_cache(self):
        """
        Load cache.

        :return:
        """
        spacecmd.utils.save_cache(cachefile=self.cachefile, data=self.data, expire=self.expiration)

        assert os.path.exists(self.cachefile)

        out, expiration = spacecmd.utils.load_cache(self.cachefile)

        assert "expire" not in out
        assert expiration == self.expiration
        assert self.data["key"] == out["key"]

    def test_load_corrupted_cache(self):
        """
        Load corrupted cache.

        :return:
        """
        with open(self.cachefile, "wb") as che:
            che.write(b"\x00\x00\x00\x00")
        assert os.path.exists(self.cachefile)

        out, expiration = spacecmd.utils.load_cache(self.cachefile)

        assert out == {}
        assert expiration != self.expiration is not None
        assert not os.path.exists(self.cachefile)


class TestSCUtils:
    """
    Test suite for utils functions.
    """

    def test_parse_command_arguments(self):
        """
        Test argument parser.
        :return:
        """

        arg_parser = spacecmd.utils.get_argument_parser()
        args, opts = spacecmd.utils.parse_command_arguments("one two three", argument_parser=arg_parser, glob=True)
        assert args == ["one", "two", "three"] == opts.leftovers

        arg_parser.add_argument("-a", "--arg")
        args, opts = spacecmd.utils.parse_command_arguments("--arg idea", argument_parser=arg_parser, glob=True)

        assert opts.leftovers == []
        assert opts.arg == "idea"

    def test_is_interactive(self):
        """
        Test is_interactive check
        :return:
        """

        arg_parser = spacecmd.utils.get_argument_parser()
        args, opts = spacecmd.utils.parse_command_arguments("arg", argument_parser=arg_parser, glob=True)
        assert not spacecmd.utils.is_interactive(opts)

        arg_parser.add_argument("-a", "--arg")
        args, opts = spacecmd.utils.parse_command_arguments("--arg idea", argument_parser=arg_parser, glob=True)
        assert not spacecmd.utils.is_interactive(opts)

        args, opts = spacecmd.utils.parse_command_arguments("", argument_parser=arg_parser, glob=True)
        assert spacecmd.utils.is_interactive(opts)

    def test_filter_results(self):
        """
        Test results filtering.

        :return:
        """
        out = spacecmd.utils.filter_results(["space", "spacecmd", "cmdspace", "somespacecmd",
                                             "somecmd", "cmdsome", "piglet"],
                                            ["space*", "pig"], search=True)
        assert out == ['space', 'spacecmd', 'cmdspace', 'somespacecmd', 'piglet']

        out = spacecmd.utils.filter_results(["space", "spacecmd", "cmdspace", "somespacecmd",
                                             "somecmd", "cmdsome", "piglet"],
                                            ["space*", "pig"], search=False)
        assert out == ['space']

    @patch("spacecmd.utils.mkstemp", MagicMock(return_value=(1, "test",)))
    @patch("spacecmd.utils.os.fdopen", MagicMock(side_effect=IOError("Electromagnetic energy loss")))
    def test_editor_ioerror_handle(self):
        """
        Test to handle IOError by an external editor when the temporary file cannot be written.

        :return:
        """
        spawner = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.utils.os.spawnlp", spawner) as spw, \
            patch("spacecmd.utils.logging", logger) as lgr:
            spacecmd.utils.editor("clock speed adjustments")

        assert logger.warning.called
        assert logger.error.called
        assert not spawner.called

    @patch("spacecmd.utils.mkstemp", MagicMock(return_value=(1, "test",)))
    @patch("spacecmd.utils.os.fdopen", MagicMock(return_value=MagicMock()))
    @patch("spacecmd.utils.os.environ", {})
    def test_editor_editor_failed(self):
        """
        Test to handle editor launch failures.

        :return:
        """
        spawner = MagicMock(return_value=42)
        logger = MagicMock()
        with patch("spacecmd.utils.os.spawnlp", spawner) as spw, \
            patch("spacecmd.utils.logging", logger) as lgr:
            spacecmd.utils.editor("clock speed adjustments")

        assert not logger.warning.called
        assert logger.error.called
        assert spawner.called

        assert_args_expect(logger.error.call_args_list,
                           [(('Editor "%s" exited with code %i', "vim", 42), {}),
                            (('Editor "%s" exited with code %i', "vi", 42), {}),
                            (('Editor "%s" exited with code %i', "emacs", 42), {}),
                            (('Editor "%s" exited with code %i', "nano", 42), {}),
                            (('No editors found',), {})])

    @patch("spacecmd.utils.mkstemp", MagicMock(return_value=(1, "test",)))
    @patch("spacecmd.utils.os.fdopen", MagicMock(return_value=MagicMock()))
    @patch("spacecmd.utils.os.environ", {})
    @patch("spacecmd.utils.os.path.isfile", MagicMock(return_value=True))
    def test_editor_file_removal(self):
        """
        Test handle template files by the editor.

        :return:
        """
        spawner = MagicMock(return_value=0)
        logger = MagicMock()
        remover = MagicMock()
        with patch("spacecmd.utils.os.spawnlp", spawner) as spw, \
            patch("spacecmd.utils.logging", logger) as lgr, \
            patch("spacecmd.utils.os.remove", remover) as rmr, \
            patch("spacecmd.utils.open", new_callable=mock_open, read_data="contents data"):
            out = spacecmd.utils.editor("clock speed adjustments", delete=True)

        assert not logger.error.called
        assert remover.called
        assert out == ('contents data', '')

    @patch("spacecmd.utils.input", MagicMock(return_value="single line data"))
    @patch("spacecmd.utils.sys.stdin.read", MagicMock(return_value="data\nand\nother\ndata"))
    def test_prompt_user_single_line(self):
        """
        Test prompt user, single line.
        :return:
        """
        assert spacecmd.utils.prompt_user("") == "single line data"

    @patch("spacecmd.utils.input", MagicMock(return_value=""))
    @patch("spacecmd.utils.sys.stdin.read", MagicMock(return_value="data\nand\nother\ndata"))
    def test_prompt_user_single_line_blank(self):
        """
        Test prompt user, single blank line.
        :return:
        """
        assert spacecmd.utils.prompt_user("") == ""

    @patch("spacecmd.utils.input", MagicMock(return_value="single line data"))
    @patch("spacecmd.utils.sys.stdin.read", MagicMock(return_value="data\nand\nother\ndata"))
    def test_prompt_user_multi_line_blank(self):
        """
        Test prompt user, multiline, blank.
        :return:
        """
        assert spacecmd.utils.prompt_user("", multiline=True) == "data\nand\nother\ndata"

    def test_time_input_default(self):
        """
        Test time input, default.

        :return:
        """

        dt = MagicMock()
        dt.now = MagicMock(return_value=datetime.datetime(2019, 5, 1, 10, 45))
        with patch("spacecmd.utils.datetime", dt) as dtm:
            out = spacecmd.utils.parse_time_input()

        assert bool(out)
        assert str(out) == "20190501T10:45:00"

    def test_time_input_yyyymmddhhmm(self):
        """
        Test time input, YYYYMMDDHHMM.

        :return:
        """

        out = spacecmd.utils.parse_time_input("201905011045")

        assert bool(out)
        assert str(out) == "20190501T10:45:00"

    def test_time_input_yyyymmdd(self):
        """
        Test time input, YYYYMMDD.

        :return:
        """

        out = spacecmd.utils.parse_time_input("20190501")

        assert bool(out)
        assert str(out) == "20190501T00:00:00"

    def test_time_input_yyyymmddhh(self):
        """
        Test time input, YYYYMMDDHH.

        :return:
        """

        out = spacecmd.utils.parse_time_input("2019050110")

        assert bool(out)
        assert str(out) == "20190501T10:00:00"

    def test_time_input_yyyymmddhhmmss(self):
        """
        Test time input, YYYYMMDDHHMMSS.

        :return:
        """

        out = spacecmd.utils.parse_time_input("20190501104531")

        assert bool(out)
        assert str(out) == "20190501T10:45:31"

    def test_build_package_names(self):
        """
        Test package name build.

        :return:
        """
        out = spacecmd.utils.build_package_names({"name": "emacs", "version": "42",
                                                  "release": "13", "epoch": "1",
                                                  "arch": "x86_64", "arch_label": "amd"})
        assert out == "emacs-42-13:1.x86_64"

    def test_build_package_names_no_epoch(self):
        """
        Test package name build, no epoch.

        :return:
        """
        out = spacecmd.utils.build_package_names({"name": "emacs", "version": "42",
                                                  "release": "13",
                                                  "arch": "x86_64", "arch_label": "amd"})
        assert out == "emacs-42-13.x86_64"

    def test_build_package_names_empty_epoch(self):
        """
        Test package name build, empty epoch.

        :return:
        """
        out = spacecmd.utils.build_package_names({"name": "emacs", "version": "42",
                                                  "release": "13", "epoch": "",
                                                  "arch": "x86_64", "arch_label": "amd"})
        assert out == "emacs-42-13.x86_64"

    def test_build_package_names_amd64_uc(self):
        """
        Test package name when amd64 arch uppercase.

        :return:
        """
        out = spacecmd.utils.build_package_names({"name": "emacs", "version": "42",
                                                  "release": "13", "epoch": "2",
                                                  "arch": "AMD64", "arch_label": "amd"})
        assert out == "emacs-42-13:2.x86_64"

    def test_build_package_names_amd64_lc(self):
        """
        Test package name when amd64 arch lowercase

        :return:
        """
        out = spacecmd.utils.build_package_names({"name": "emacs", "version": "42",
                                                  "release": "13", "epoch": "2",
                                                  "arch": "amd64", "arch_label": "amd"})
        assert out == "emacs-42-13:2.x86_64"

    def test_print_errata_summary_no_date_key(self):
        """
        Print errata summary. No "date" key.

        :return:
        """
        erratum = {"issue_date": "2019.01.15", "advisory_name": "CVE-12345-678",
                   "advisory_synopsis": "Sometimes synopsis has a long text here. " * 5}
        mprint = MagicMock()
        with patch("spacecmd.utils.print", mprint) as prt:
            spacecmd.utils.print_errata_summary(erratum=erratum)

        assert_expect(mprint.call_args_list,
                      'CVE-12345-678   Sometimes synopsis has a long text here.                      2019.01.15')

    def test_print_errata_summary_no_date_no_issue_date_key(self):
        """
        Print errata summary. No "date" key, no "issue_date" key.

        :return:
        """
        erratum = {"advisory_name": "CVE-12345-678",
                   "advisory_synopsis": "Sometimes synopsis has a long text here. " * 5}
        mprint = MagicMock()
        with patch("spacecmd.utils.print", mprint) as prt:
            spacecmd.utils.print_errata_summary(erratum=erratum)

        assert_expect(mprint.call_args_list,
                      'CVE-12345-678   Sometimes synopsis has a long text here.                           N/A')

    def test_print_errata_list_no_errata(self):
        """
        Test print errata list without errata data.

        :return:
        """
        errata = []
        mprint = MagicMock()
        with patch("spacecmd.utils.print", mprint) as prt:
            spacecmd.utils.print_errata_list(errata=errata)

        assert not mprint.called

    def test_print_errata_list_by_advisory_type(self):
        """
        Test print errata list by advisory type.

        :return:
        """
        errata = [{"advisory_type": "security",
                   "advisory_name": "CVE-123-4567", "advisory_synopsis": "text here " * 10,
                   "date": "2019.01.15"},
                  {"advisory_type": "bug fix",
                   "advisory_name": "CVE-123-4567", "advisory_synopsis": "text here " * 10,
                   "date": "2019.01.15"},
                  {"advisory_type": "product enhancement",
                   "advisory_name": "CVE-123-4567", "advisory_synopsis": "text here " * 10,
                   "date": "2019.01.15"}]
        mprint = MagicMock()
        with patch("spacecmd.utils.print", mprint) as prt:
            spacecmd.utils.print_errata_list(errata=errata)

        assert mprint.called
        assert_list_args_expect(mprint.call_args_list,
                                ['Security Errata',
                                 '---------------',
                                 'CVE-123-4567    text here text here text here text here '
                                 'text here             2019.01.15', '', 'Bug Fix Errata', '--------------',
                                 'CVE-123-4567    text here text here text here text here '
                                 'text here             2019.01.15', '', 'Enhancement Errata', '------------------',
                                 'CVE-123-4567    text here text here text here text here '
                                 'text here             2019.01.15']
                                )

    def test_max_length(self):
        """
        Test find the longest string in a list.

        :return:
        """
        items = [
            "The POP server is out of Coke",
            "User to computer ratio is too low",
            "This is an undocumented feature"
        ]
        assert spacecmd.utils.max_length(items=items) == 33

    @patch("spacecmd.utils.os.path.isdir", MagicMock(return_value=False))
    def test_list_locales_no_data(self):
        """
        Test locale list when no data (no directory found).

        :return:
        """
        assert spacecmd.utils.list_locales() == []

    @patch("spacecmd.utils.os.path.isdir", MagicMock(return_value=True))
    @patch("spacecmd.utils.os.listdir", MagicMock(side_effect=[["Europe"], ["Berlin", "London"]]))
    def test_list_locales_no_data(self):
        """
        Test locale list when no data (no directory found).

        :return:
        """
        logger = MagicMock()
        with patch("spacecmd.utils.logging", logger) as lgr:
            out = spacecmd.utils.list_locales()

        assert out == ['Europe/Berlin', 'Europe/London']

    def test_parse_str(self):
        """
        Test parsing string utility function.

        :return:
        """
        assert spacecmd.utils.parse_str("1234567", int) == 1234567
        assert spacecmd.utils.parse_str("1234567") == 1234567
        assert spacecmd.utils.parse_str("True") == True
        assert spacecmd.utils.parse_str("False") == False
        assert spacecmd.utils.parse_str("ABC1234567") == "ABC1234567"
        assert spacecmd.utils.parse_str('{"foo": "bar"}') == {"foo": "bar"}
        assert spacecmd.utils.parse_str('{"number": 123}') == {"number": 123}

    def test_parse_list_str(self):
        """
        Test parsing list string utility function
        :return:
        """

        assert spacecmd.utils.parse_list_str("") == [""]
        assert spacecmd.utils.parse_list_str("a,b") == ["a", "b"]
        assert spacecmd.utils.parse_list_str("a,b,") == ["a", "b", ""]
        assert spacecmd.utils.parse_list_str("a,,b,c") == ["a", "", "b", "c"]
        assert spacecmd.utils.parse_list_str("a:b:", ":") == ["a", "b", ""]

    def test_parse_api_args(self):
        """
        Test simple json-like expression parser.

        :return:
        """
        assert spacecmd.utils.parse_api_args('{"channelLabel": "foo-i386-5"}')[0]["channelLabel"] == "foo-i386-5"

        i, s, d = spacecmd.utils.parse_api_args('1234567,abcXYZ012,{"channelLabel": "foo-i386-5"}')
        assert i == 1234567
        assert s == "abcXYZ012"
        assert d["channelLabel"] == "foo-i386-5"

        i, s, d = spacecmd.utils.parse_api_args('[1234567,"abcXYZ012",{"channelLabel": "foo-i386-5"}]')
        assert i == 1234567
        assert s == "abcXYZ012"
        assert d["channelLabel"] == "foo-i386-5"

        i, b, s, b2 = spacecmd.utils.parse_api_args('1234,True,abc1234,False')
        assert i == 1234
        assert b == True
        assert s == "abc1234"
        assert b2 == False

    def test_json_dump_to_file(self):
        """
        Test JSON dump to file.

        :return:
        """
        filename = "/tmp/something"
        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.utils.open", new_callable=mock_open, read_data="contents data") as opn, \
                patch("spacecmd.utils.logging", logger) as lgr, \
            patch("spacecmd.utils.print", mprint) as prt:
            out = spacecmd.utils.json_dump_to_file(None, filename=filename)

        assert out
        assert not logger.error.called
        assert not mprint.called

    def test_json_dump_to_file_ioerror(self):
        """
        Test JSON dump to file, handling IOError.

        :return:
        """
        filename = "/tmp/something"
        logger = MagicMock()
        with patch("spacecmd.utils.open", MagicMock(side_effect=IOError("write-only file system"))) as opn, \
            patch("spacecmd.utils.logging", logger) as lgr:
            out = spacecmd.utils.json_dump_to_file(None, filename=filename)

        assert not out
        assert logger.error.called

        assert_args_expect(logger.error.call_args_list,
                           [(('Could not open file %s for writing: %s',
                              '/tmp/something', 'write-only file system'), {})])

    def test_json_read_from_file(self):
        """
        Test JSON read from file (success).

        :return:
        """
        filename = "/tmp/something"
        logger = MagicMock()
        with patch("spacecmd.utils.open", new_callable=mock_open, read_data='{"foo": "bar", "int": 123}') as opn, \
            patch("spacecmd.utils.logging", logger) as lgr:
            out = spacecmd.utils.json_read_from_file(filename=filename)

        assert not logger.error.called
        assert out is not None
        assert "foo" in out and "int" in out
        assert out["foo"] == "bar" and out["int"] == 123

    def test_json_read_from_file_ioerror(self):
        """
        Test JSON read from file IOError handling.

        :return:
        """
        filename = "/tmp/something"
        logger = MagicMock()
        with patch("spacecmd.utils.open", MagicMock(side_effect=IOError("Hard drive is sleeping"))) as opn, \
            patch("spacecmd.utils.logging", logger) as lgr:
            out = spacecmd.utils.json_read_from_file(filename=filename)

        assert logger.error.called
        assert out is None

        assert_args_expect(logger.error.call_args_list,
                           [(('Could not open file %s for reading: %s', '/tmp/something', 'Hard drive is sleeping'), {})])

    @patch("spacecmd.utils.json.loads", MagicMock(side_effect=ValueError("Curly brackets replaced by dashes")))
    def test_json_read_from_file_valueerror(self):
        """
        Test JSON read from file ValueError handling.

        :return:
        """
        filename = "/tmp/something"
        logger = MagicMock()
        with patch("spacecmd.utils.open", new_callable=mock_open, read_data='{"foo": "bar", "int": 123}') as opn, \
            patch("spacecmd.utils.logging", logger) as lgr:
            out = spacecmd.utils.json_read_from_file(filename=filename)

        assert logger.error.called
        assert out is None

        assert_args_expect(logger.error.call_args_list,
                           [(('Could not parse JSON data from %s: %s',
                              '/tmp/something', 'Curly brackets replaced by dashes'), {})])

    def test_json_read_from_file_general_exception(self):
        """
        Test JSON read from file General Exception handling.

        :return:
        """
        filename = "/tmp/something"
        logger = MagicMock()
        with patch("spacecmd.utils.open", MagicMock(side_effect=Exception("Admin went for lunch"))) as opn, \
            patch("spacecmd.utils.logging", logger) as lgr:
            out = spacecmd.utils.json_read_from_file(filename=filename)

        assert logger.error.called
        assert out is None

        assert_args_expect(logger.error.call_args_list,
                           [(('Error processing file %s: %s',
                              '/tmp/something', 'Admin went for lunch'), {})])

    def test_get_string_diff_dicts(self):
        """
        Test string diffs that generates transformation regexps from source
        to destination and vice versa.

        :return:
        """
        rpl_a, rpl_b = spacecmd.utils.get_string_diff_dicts("rhel6-x86_64-dev-application1",
                                                            "rhel6-x86_64-qas-application1")
        assert r"(^|-)dev(-|$)" in rpl_a
        assert rpl_a[r"(^|-)dev(-|$)"] == r"\1DIFF(dev|qas)\2"
        assert r"(^|-)qas(-|$)" in rpl_b
        assert rpl_b["(^|-)qas(-|$)"] == r"\1DIFF(dev|qas)\2"

    def test_replace(self):
        """
        Replace data in dictionary's values.

        :return:
        """
        line = "Cellular megabot interference"
        repldict = {"mega": "tele", "bot": "phone"}
        assert spacecmd.utils.replace(line, replacedict=repldict) == "Cellular telephone interference"

    def test_get_normalised_text_no_modifier(self):
        """
        Test text normalisation without modifier.

        :return:
        """
        text = """
1-sle15-x86_64-dev
2-sle15-x86_64-prd
3-sle15-x86_64-dev
4-sle15-x86_64-prd
5-sle15-x86_64-prd
""".strip().split("\n")
        assert spacecmd.utils.get_normalized_text(text) == ['1-sle15-x86_64-dev', '2-sle15-x86_64-prd',
                                                            '3-sle15-x86_64-dev', '4-sle15-x86_64-prd',
                                                            '5-sle15-x86_64-prd']

    def test_get_normalised_text(self):
        """
        Test text normalisation.

        :return:
        """
        text = """
1-sle15-x86_64-dev
2-sle15-x86_64-prd
3-sle15-x86_64-dev
4-sle15-x86_64-prd
5-sle15-x86_64-prd
""".strip().split("\n")
        assert spacecmd.utils.get_normalized_text(text, replacedict={"dev": "prd"}) == ['1-sle15-x86_64-prd',
                                                                                        '2-sle15-x86_64-prd',
                                                                                        '3-sle15-x86_64-prd',
                                                                                        '4-sle15-x86_64-prd',
                                                                                        '5-sle15-x86_64-prd']

    def test_file_is_binary(self):
        """
        Test determination whether the file is in binary format.

        :return:
        """
        temp = tempfile.mkdtemp()
        cachefile = os.path.join(temp, "somefile.bin")
        with open(cachefile, "wb") as fhb:
            fhb.write(b"\x00\x00\x00\x00")
        assert spacecmd.utils.file_is_binary(None, cachefile)
        shutil.rmtree(temp)

    def test_file_is_not_binary(self):
        """
        Test determination whether the file is in binary format.

        :return:
        """
        temp = tempfile.mkdtemp()
        cachefile = os.path.join(temp, "somefile.bin")
        with open(cachefile, "wb") as fhb:
            fhb.write(b"some text")
        assert not spacecmd.utils.file_is_binary(None, cachefile)
        shutil.rmtree(temp)

    def test_string_to_bool_false_type(self):
        """
        Test string conversion to boolean handling false type

        :return:
        """
        for value in [b"", 1, {}, [], ()]:
            with pytest.raises(IOError) as exc:
                spacecmd.utils.string_to_bool(value)
            assert "Parameter" in exc2str(exc)

    def test_string_to_bool_correct_type(self):
        """
        Test string conversion to boolean handling correct string type

        :return:
        """
        assert not spacecmd.utils.string_to_bool("false")
        assert not spacecmd.utils.string_to_bool("no")
        assert spacecmd.utils.string_to_bool("yes")
        assert spacecmd.utils.string_to_bool("true")
