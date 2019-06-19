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
        assert expiration != self.expiration
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
