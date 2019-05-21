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

    def test_report_inactivesystems_wrong_args(self, shell):
        """
        Test do_report_inactivesystems with wrong days count, defaults to 7.

        :param shell:
        :return:
        """

        for m_arg in ["nonsense", "-1"]:
            shell.client.system.listInactiveSystems = MagicMock(return_value=[])
            mprint = MagicMock()

            with patch("spacecmd.report.print", mprint) as prn:
                spacecmd.report.do_report_inactivesystems(shell, m_arg)

            assert not mprint.called
            assert shell.client.system.listInactiveSystems.called

            for call in shell.client.system.listInactiveSystems.call_args_list:
                args, kw = call
                assert args == (shell.session, 7)
                assert not kw

    def test_report_inactivesystems_args(self, shell):
        """
        Test do_report_inactivesystems with days count.

        :param shell:
        :return:
        """

        shell.client.system.listInactiveSystems = MagicMock(return_value=[])
        mprint = MagicMock()

        with patch("spacecmd.report.print", mprint) as prn:
            spacecmd.report.do_report_inactivesystems(shell, "3")

        assert not mprint.called
        assert shell.client.system.listInactiveSystems.called

        for call in shell.client.system.listInactiveSystems.call_args_list:
            args, kw = call
            assert args == (shell.session, 3)
            assert not kw

    def test_report_outofdatesystems(self, shell):
        """
        Test do_report_outofdatesystems.

        :param shell:
        :return:
        """
        shell.client.system.listOutOfDateSystems = MagicMock(return_value=[
            {"name": "system-one", "outdated_pkg_count": 5},
            {"name": "system-two", "outdated_pkg_count": 15},
            {"name": "system-three", "outdated_pkg_count": 25},
        ])
        mprint = MagicMock()
        with patch("spacecmd.report.print", mprint) as prn:
            spacecmd.report.do_report_outofdatesystems(shell, "")

        assert mprint.called

        exp = [
            'System        Packages',
            '------------  --------',
            'system-one           5',
            'system-three        25',
            'system-two          15'
        ]
        for call in mprint.call_args_list:
            assert_expect([call], next(iter(exp)))
            exp.pop(0)
        assert not exp
