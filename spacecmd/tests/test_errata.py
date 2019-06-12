# coding: utf-8
"""
Test suite for the errata module.
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.errata
from xmlrpc import client as xmlrpclib


class TestSCErrata:
    """
    Test suite for "errata" module.
    """
    def test_errata_list_nodata(self, shell):
        """
        Test do_errata_list return no data

        :param shell:
        :return:
        """
        shell.generate_errata_cache = MagicMock()
        shell.all_errata = {"one": None, "two": None}
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            out = spacecmd.errata.do_errata_list(shell, "")

        assert out is None
        assert shell.generate_errata_cache.called
        assert_expect(mprint.call_args_list, "one\ntwo")

    def test_errata_list_with_data(self, shell):
        """
        Test do_errata_list return data for further processing

        :param shell:
        :return:
        """
        shell.generate_errata_cache = MagicMock()
        shell.all_errata = {"one": None, "two": None}
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            out = spacecmd.errata.do_errata_list(shell, "", doreturn=True)

        assert not mprint.called
        assert out is not None
        assert sorted(out) == ["one", "two"]
        assert shell.generate_errata_cache.called

