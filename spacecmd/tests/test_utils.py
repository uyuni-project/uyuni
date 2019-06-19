# coding: utf-8
"""
Test spacecmd.utils
"""
from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.utils
from xmlrpc import client as xmlrpclib
import os
import tempfile
import shutil
import datetime
import pickle


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
        self.temp = tempfile.mkdtemp()

    def teardown_method(self):
        """
        Teardown method

        :return:
        """
        shutil.rmtree(self.temp)

    def test_save_cache(self):
        """
        Save cache

        :return:
        """
        data = {"key": "value"}
        cachefile = os.path.join(self.temp, "spacecmd.cache")
        expiration = datetime.datetime(2019, 1, 1, 10, 30, 45)
        spacecmd.utils.save_cache(cachefile=cachefile, data=data, expire=expiration)
        assert os.path.exists(cachefile)
        out = pickle.load(open(cachefile, "rb"))

        assert "expire" in out
        assert "expire" not in data
        assert out["expire"] == expiration
        assert data["key"] == out["key"]



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
