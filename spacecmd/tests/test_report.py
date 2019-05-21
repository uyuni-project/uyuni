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

    def test_report_inactivesystems_systems(self, shell):
        """
        Test do_report_inactivesystems with no arguments, systems found.

        :param shell:
        :return:
        """
        shell.client.system.listInactiveSystems = MagicMock(return_value=[
            {"name": "system-1", "last_checkin": "2019.05.10", "id": 10001000},
            {"name": "system-2", "last_checkin": "2019.05.11", "id": 10001001},
            {"name": "system-3", "last_checkin": "2019.05.12", "id": 10001002},
        ])
        mprint = MagicMock()

        with patch("spacecmd.report.print", mprint) as prn:
            spacecmd.report.do_report_inactivesystems(shell, "")

        assert mprint.called
        assert shell.client.system.listInactiveSystems.called
        assert_expect(shell.client.system.listInactiveSystems.call_args_list,
                      shell.session)
        exp = [
            'System ID   System    Last Checkin',
            '----------  --------  ------------',
            '10001000  system-1  2019.05.10',
            '10001001  system-2  2019.05.11',
            '10001002  system-3  2019.05.12',
        ]
        for call in mprint.call_args_list:
            assert_expect([call], next(iter(exp)))
            exp.pop(0)
        assert not exp
