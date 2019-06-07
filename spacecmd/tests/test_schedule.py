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

    def test_schedule_cancel_globbing(self, shell):
        """
        Test do_schedule_cancel with globbing.

        :param shell:
        :return:
        """

        shell.help_schedule_cancel = MagicMock()
        shell.client.schedule.listInProgressActions = MagicMock()
        shell.client.schedule.cancelActions = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.schedule.print", mprint) as prt, \
            patch("spacecmd.schedule.logging", logger) as lgr:
            spacecmd.schedule.do_schedule_cancel(shell, "*")

        assert not shell.client.schedule.listInProgressActions.called
        assert not shell.client.schedule.cancelActions.called
        assert not mprint.called
        assert not logger.warning.called
        assert not shell.help_schedule_cancel.called
        assert shell.user_confirm.called
        assert logger.info.called

        assert_expect(logger.info.call_args_list,
                      "All pending actions left untouched")

    def test_schedule_cancel_invalid_action_id(self, shell):
        """
        Test do_schedule_cancel with invalid action ids.

        :param shell:
        :return:
        """

        shell.help_schedule_cancel = MagicMock()
        shell.client.schedule.listInProgressActions = MagicMock()
        shell.client.schedule.cancelActions = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.schedule.print", mprint) as prt, \
            patch("spacecmd.schedule.logging", logger) as lgr:
            spacecmd.schedule.do_schedule_cancel(shell, "1 two 3, and 4")

        assert not shell.help_schedule_cancel.called
        assert not shell.user_confirm.called
        assert not shell.client.schedule.listInProgressActions.called
        assert shell.client.schedule.cancelActions.called
        assert mprint.called
        assert logger.warning.called
        assert logger.info.called

        assert_list_args_expect(logger.warning.call_args_list,
                                ['"two" is not a valid ID', '"3," is not a valid ID', '"and" is not a valid ID'])
        assert_args_expect(logger.info.call_args_list,
                           [(('Canceled action: %i', 1), {}),
                            (('Canceled action: %i', 4), {}),
                            (('Failed action: %s', 'two'), {}),
                            (('Failed action: %s', '3,'), {}),
                            (('Failed action: %s', 'and'), {})])

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

    def test_schedule_reschedule_globbing(self, shell):
        """
        Test do_schedule_reschedule with globbing.

        :param shell:
        :return:
        """

        shell.help_schedule_reschedule = MagicMock()
        shell.client.schedule.listFailedActions = MagicMock()
        shell.client.schedule.rescheduleActions = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.schedule.print", mprint) as prt, \
            patch("spacecmd.schedule.logging", logger) as lgr:
            spacecmd.schedule.do_schedule_reschedule(shell, "*")

        assert not shell.client.schedule.rescheduleActions.called
        assert not mprint.called
        assert not logger.warning.called
        assert not shell.help_schedule_reschedule.called
        assert shell.client.schedule.listFailedActions.called
        assert shell.user_confirm.called

    def test_schedule_reschedule_failed_actions(self, shell):
        """
        Test do_schedule_reschedule with failed actions.

        :param shell:
        :return:
        """

        shell.help_schedule_reschedule = MagicMock()
        shell.client.schedule.listFailedActions = MagicMock(return_value=[
            {"id": 1}, {"id": 2}, {"id": 3}, {"id": 4}
        ])
        shell.client.schedule.rescheduleActions = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.schedule.print", mprint) as prt, \
            patch("spacecmd.schedule.logging", logger) as lgr:
            spacecmd.schedule.do_schedule_reschedule(shell, "one 2 3, 5 and 4")

        assert not shell.help_schedule_reschedule.called
        assert not shell.user_confirm.called
        assert shell.client.schedule.rescheduleActions.called
        assert mprint.called
        assert logger.warning.called
        assert shell.client.schedule.listFailedActions.called

        assert_expect(mprint.call_args_list, "Rescheduled 2 action(s)")
        assert_list_args_expect(logger.warning.call_args_list,
                                ['"one" is not a valid ID',
                                 '"3," is not a valid ID',
                                 '"5" is not a failed action',
                                 '"and" is not a valid ID'])

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

    def test_schedule_getoutput_noargs(self, shell):
        """
        Test do_schedule_getoutput without arguments.

        :param shell:
        :return:
        """
        shell.client.schedule.listCompletedSystems = MagicMock()
        shell.client.system.getScriptResults = MagicMock()
        shell.help_schedule_getoutput = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.schedule.print", mprint) as prt, \
            patch("spacecmd.schedule.logging", logger) as lgr:
            spacecmd.schedule.do_schedule_getoutput(shell, "")

        assert not shell.client.system.getScriptResults.called
        assert not shell.client.schedule.listCompletedSystems.called
        assert not mprint.called
        assert not logger.warning.called
        assert shell.help_schedule_getoutput.called

