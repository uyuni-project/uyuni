# coding: utf-8
"""
Test suite for Scap commands at spacecmd.
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect
import pytest
import spacecmd.scap


class TestScap:
    """
    Test suite for scap.
    """

    def test_scap_listxccdfscans_noarg(self, shell):
        """
        Test calling scap listxccdfscans without arguments.

        :param shell:
        :return:
        """
        shell.help_scap_listxccdfscans = MagicMock()
        shell.ssm = MagicMock()
        shell.expand_systems = MagicMock()
        shell.get_system_id = MagicMock()
        shell.client.system.scap.listXccdfScans = MagicMock()

        mprint = MagicMock()
        with patch("spacecmd.scap.print", mprint):
            spacecmd.scap.do_scap_listxccdfscans(shell, "")

        assert shell.help_scap_listxccdfscans.called
        assert not shell.ssm.keys.called
        assert not shell.expand_systems.called
        assert not shell.get_system_id.called
        assert not shell.client.system.scap.listXccdfScans.called
        assert not mprint.called

    def test_scap_listxccdfscans_ssm_arg(self, shell):
        """
        Test calling scap listxccdfscans with ssm argument.
        No systems has been scanned.

        :param shell:
        :return:
        """
        shell.help_scap_listxccdfscans = MagicMock()
        shell.ssm = MagicMock()
        shell.ssm.keys = MagicMock(return_value=[])
        shell.expand_systems = MagicMock()
        shell.get_system_id = MagicMock()
        shell.client.system.scap.listXccdfScans = MagicMock()

        mprint = MagicMock()
        with patch("spacecmd.scap.print", mprint):
            spacecmd.scap.do_scap_listxccdfscans(shell, "ssm")

        assert not shell.help_scap_listxccdfscans.called
        assert shell.ssm.keys.called
        assert not shell.expand_systems.called
        assert not shell.get_system_id.called
        assert not shell.client.system.scap.listXccdfScans.called
        assert not mprint.called

    def test_scap_listxccdfscans_system_arg(self, shell):
        """
        Test calling scap listxccdfscans with a system name argument.

        :param shell:
        :return:
        """
        shell.help_scap_listxccdfscans = MagicMock()
        shell.SEPARATOR = "---"
        shell.ssm = MagicMock()
        shell.ssm.keys = MagicMock(return_value=[])
        shell.expand_systems = MagicMock(return_value=["chair-1", "table-2"])
        shell.get_system_id = MagicMock(side_effect=["001", "002"])
        shell.client.system.scap.listXccdfScans = MagicMock(side_effect=[
            [
                {"xid": 1, "profile": "001", "path": "/etc/first", "completed": "true"},
                {"xid": 2, "profile": "001", "path": "/etc/second", "completed": "false"},
            ],
            [
                {"xid": 3, "profile": "002", "path": "/opt/etc/third", "completed": "false"},
                {"xid": 4, "profile": "002", "path": "/opt/etc/fourth", "completed": "true"},
            ],
        ])

        mprint = MagicMock()
        with patch("spacecmd.scap.print", mprint):
            spacecmd.scap.do_scap_listxccdfscans(shell, "chair table")

        assert not shell.help_scap_listxccdfscans.called
        assert not shell.ssm.keys.called
        assert shell.expand_systems.called
        assert shell.get_system_id.called
        assert shell.client.system.scap.listXccdfScans.called
        assert mprint.called

        expectations = [
            'System: chair-1', '',
            'XID: 1 Profile: 001 Path: (/etc/first) Completed: true',
            'XID: 2 Profile: 001 Path: (/etc/second) Completed: false',
            shell.SEPARATOR,
            'System: table-2',
            '',
            'XID: 3 Profile: 002 Path: (/opt/etc/third) Completed: false',
            'XID: 4 Profile: 002 Path: (/opt/etc/fourth) Completed: true',
        ]
        assert_expect(mprint.call_args_list, *expectations)

    def test_scap_getxccdfscanruleresults_noargs(self, shell):
        """
        Test getxccdfscanruleresults without args

        :param shell:
        :return:
        """
        shell.help_scap_getxccdfscanruleresults = MagicMock()
        shell.client.system.scap.getXccdfScanRuleResults = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.scap.print", mprint):
            spacecmd.scap.do_scap_getxccdfscanruleresults(shell, "")

        assert shell.help_scap_getxccdfscanruleresults.called
        assert not shell.client.system.scap.getXccdfScanRuleResults.called
        assert not mprint.called

    def test_scap_getxccdfscanruleresults_xids_no_rules(self, shell):
        """
        Test getxccdfscanruleresults with XIDs but no rules

        :param shell:
        :return:
        """
        shell.help_scap_getxccdfscanruleresults = MagicMock()
        shell.SEPARATOR = "---"
        shell.client.system.scap.getXccdfScanRuleResults = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.scap.print", mprint):
            spacecmd.scap.do_scap_getxccdfscanruleresults(shell, "1 2 3")

        assert not shell.help_scap_getxccdfscanruleresults.called
        assert shell.client.system.scap.getXccdfScanRuleResults.called
        assert mprint.called

        expectations = [
            'XID: 1', '', '---', 'XID: 2',
            '', '---', 'XID: 3', '',
        ]
        assert_expect(mprint.call_args_list, *expectations)

    def test_scap_getxccdfscanruleresults_xids_with_rules(self, shell):
        """
        Test getxccdfscanruleresults with XIDs with rules

        :param shell:
        :return:
        """
        shell.help_scap_getxccdfscanruleresults = MagicMock()
        shell.SEPARATOR = "---"
        shell.client.system.scap.getXccdfScanRuleResults = MagicMock(side_effect=[
            [
                {"idref": "001A", "result": "result placeholder - 1", "idents": "idents placeholder - 1"},
                {"idref": "001B", "result": "result placeholder - 2", "idents": "idents placeholder - 2"},
            ],
            [
                {"idref": "002A", "result": "result placeholder - 1", "idents": "idents placeholder - 1"},
                {"idref": "002B", "result": "result placeholder - 2", "idents": "idents placeholder - 2"},
            ],
            [
                {"idref": "003A", "result": "result placeholder - 1", "idents": "idents placeholder - 1"},
                {"idref": "003B", "result": "result placeholder - 2", "idents": "idents placeholder - 2"},
            ]
        ])
        mprint = MagicMock()
        with patch("spacecmd.scap.print", mprint):
            spacecmd.scap.do_scap_getxccdfscanruleresults(shell, "1 2 3")

        assert not shell.help_scap_getxccdfscanruleresults.called
        assert shell.client.system.scap.getXccdfScanRuleResults.called
        assert mprint.called

        expectations = [
            'XID: 1', '',
            'IDref: 001A Result: result placeholder - 1 Idents: (idents placeholder - 1)',
            'IDref: 001B Result: result placeholder - 2 Idents: (idents placeholder - 2)',
            '---', 'XID: 2', '',
            'IDref: 002A Result: result placeholder - 1 Idents: (idents placeholder - 1)',
            'IDref: 002B Result: result placeholder - 2 Idents: (idents placeholder - 2)',
            '---', 'XID: 3', '',
            'IDref: 003A Result: result placeholder - 1 Idents: (idents placeholder - 1)',
            'IDref: 003B Result: result placeholder - 2 Idents: (idents placeholder - 2)',
        ]
        assert_expect(mprint.call_args_list, *expectations)

    def test_scap_getxccdfscandetails_no_args(self, shell):
        """
        Test getxccdfscandetails with no args.

        :param shell:
        :return:
        """
        shell.help_scap_getxccdfscandetails = MagicMock()
        shell.SEPARATOR = "---"
        shell.client.system.scap.getXccdfScanDetails = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.scap.print", mprint):
            spacecmd.scap.do_scap_getxccdfscandetails(shell, "")
        assert shell.help_scap_getxccdfscandetails.called
        assert not shell.client.system.scap.getXccdfScanDetails.called
        assert not mprint.called

