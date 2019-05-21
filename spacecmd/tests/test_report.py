# coding: utf-8
"""
Tests for report.
"""
from unittest.mock import MagicMock, patch
import pytest
from helpers import shell, assert_expect
import spacecmd.report


class TestSCReport:
    """
    Test suite for report.
    """
    def test_report_inactivesystems_noargs(self, shell):
        """
        Test do_report_inactivesystems with no arguments.

        :param shell:
        :return:
        """

        shell.client.system.listInactiveSystems = MagicMock(return_value=[])
        mprint = MagicMock()

        with patch("spacecmd.report.print", mprint) as prn:
            spacecmd.report.do_report_inactivesystems(shell, "")

        assert not mprint.called
        assert shell.client.system.listInactiveSystems.called

        assert_expect(shell.client.system.listInactiveSystems.call_args_list,
                      shell.session)
