# coding: utf-8
"""
Test suite for Scap commands at spacecmd.
"""

from unittest.mock import MagicMock, patch
from helpers import shell
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

