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

    def test_errata_listaffectedsystems_noargs(self, shell):
        """
        Test do_errata_listaffectedsystems without an arguments.

        :param shell:
        :return:
        """

        shell.help_errata_listaffectedsystems = MagicMock()
        shell.expand_errata = MagicMock()
        shell.client.errata.listAffectedSystems = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            spacecmd.errata.do_errata_listaffectedsystems(shell, "")

        assert not shell.client.errata.listAffectedSystems.called
        assert not shell.expand_errata.called
        assert not mprint.called
        assert shell.help_errata_listaffectedsystems.called

    def test_errata_listaffectedsystems_by_errata_name(self, shell):
        """
        Test do_errata_listaffectedsystems with errata name.

        :param shell:
        :return:
        """

        shell.help_errata_listaffectedsystems = MagicMock()
        shell.expand_errata = MagicMock(return_value=["webstack", "databases"])
        shell.client.errata.listAffectedSystems = MagicMock(side_effect=[
            [{"name": "web1.suse.com"}, {"name": "web2.suse.com"}, {"name": "web3.suse.com"}],
            [{"name": "db1.suse.com"}, {"name": "db2.suse.com"}, {"name": "db3.suse.com"}],
        ])
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            spacecmd.errata.do_errata_listaffectedsystems(shell, "foo")

        assert not shell.help_errata_listaffectedsystems.called
        assert shell.client.errata.listAffectedSystems.called
        assert shell.expand_errata.called
        assert mprint.called

        assert_list_args_expect(mprint.call_args_list,
                                ['webstack:', 'web1.suse.com\nweb2.suse.com\nweb3.suse.com',
                                 '----------', 'databases:', 'db1.suse.com\ndb2.suse.com\ndb3.suse.com'])
