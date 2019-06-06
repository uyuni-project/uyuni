# coding: utf-8
"""
Test suite for spacecmd.schedule module.
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.schedule


class TestSCSchedule:
    """
    Test suite for "schedule" module.
    """

    def test_schedule_cancel_noargs(self, shell):
        """
        Test do_schedule_cancel without arguments.

        :param shell:
        :return:
        """

        shell.help_schedule_cancel = MagicMock()
        shell.client.schedule.listInProgressActions = MagicMock()
        shell.client.schedule.cancelActions = MagicMock()
        shell.user_confirm = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.schedule.print", mprint) as prt, \
            patch("spacecmd.schedule.logging", logger) as lgr:
            spacecmd.schedule.do_schedule_cancel(shell, "")

        assert not shell.client.schedule.listInProgressActions.called
        assert not shell.client.schedule.cancelActions.called
        assert not shell.user_confirm.called
        assert not mprint.called
        assert not logger.warning.called
        assert shell.help_schedule_cancel.called

    def test_schedule_reschedule_noargs(self, shell):
        """
        Test do_schedule_reschedule without arguments.

        :param shell:
        :return:
        """

        shell.help_schedule_reschedule = MagicMock()
        shell.client.schedule.listInProgressActions = MagicMock()
        shell.client.schedule.rescheduleActions = MagicMock()
        shell.user_confirm = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.schedule.print", mprint) as prt, \
            patch("spacecmd.schedule.logging", logger) as lgr:
            spacecmd.schedule.do_schedule_reschedule(shell, "")

        assert not shell.client.schedule.listInProgressActions.called
        assert not shell.client.schedule.rescheduleActions.called
        assert not shell.user_confirm.called
        assert not mprint.called
        assert not logger.warning.called
        assert shell.help_schedule_reschedule.called

    def test_schedule_details_noargs(self, shell):
        """
        Test do_schedule_details without arguments.

        :param shell:
        :return:
        """
        shell.client.schedule.listCompletedSystems = MagicMock()
        shell.client.schedule.listFailedSystems = MagicMock()
        shell.client.schedule.listInProgressSystems = MagicMock()
        shell.client.schedule.listAllActions = MagicMock()

        shell.help_schedule_details = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.schedule.print", mprint) as prt, \
            patch("spacecmd.schedule.logging", logger) as lgr:
            spacecmd.schedule.do_schedule_details(shell, "")

        assert not shell.client.schedule.listCompletedSystems.called
        assert not shell.client.schedule.listFailedSystems.called
        assert not shell.client.schedule.listInProgressSystems.called
        assert not shell.client.schedule.listAllActions.called

        assert not mprint.called
        assert not logger.warning.called
        assert shell.help_schedule_details.called

