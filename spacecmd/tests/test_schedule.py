# coding: utf-8
"""
Test suite for spacecmd.schedule module.
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.schedule
from xmlrpc import client as xmlrpclib


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

    def test_schedule_reschedule_missing_actions(self, shell):
        """
        Test do_schedule_reschedule with missing actions.

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
            spacecmd.schedule.do_schedule_reschedule(shell, "one 5 and 6")

        assert not shell.client.schedule.rescheduleActions.called
        assert not mprint.called
        assert not shell.help_schedule_reschedule.called
        assert not shell.user_confirm.called
        assert logger.warning.called
        assert shell.client.schedule.listFailedActions.called
        assert_list_args_expect(logger.warning.call_args_list,
                                ['"one" is not a valid ID',
                                 '"5" is not a failed action',
                                 '"and" is not a valid ID',
                                 '"6" is not a failed action',
                                 'No failed actions to reschedule'])

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

    def test_schedule_details_invalid_action_id(self, shell):
        """
        Test do_schedule_details with invalid action ID.

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
            spacecmd.schedule.do_schedule_details(shell, "something")

        assert not shell.client.schedule.listCompletedSystems.called
        assert not shell.client.schedule.listFailedSystems.called
        assert not shell.client.schedule.listInProgressSystems.called
        assert not shell.client.schedule.listAllActions.called

        assert not mprint.called
        assert logger.warning.called
        assert not shell.help_schedule_details.called

        assert_expect(logger.warning.call_args_list,
                      'The ID "something" is invalid')

    def test_schedule_details_missing_action_id(self, shell):
        """
        Test do_schedule_details with the missing action ID.

        :param shell:
        :return:
        """
        shell.client.schedule.listCompletedSystems = MagicMock(return_value=[])
        shell.client.schedule.listFailedSystems = MagicMock(return_value=[])
        shell.client.schedule.listInProgressSystems = MagicMock(return_value=[])
        shell.client.schedule.listAllActions = MagicMock(return_value=[
            {"id": 1}, {"id": 2}, {"id": 3}
        ])

        shell.help_schedule_details = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.schedule.print", mprint) as prt, \
                patch("spacecmd.schedule.logging", logger) as lgr:
            spacecmd.schedule.do_schedule_details(shell, "42")

        assert not mprint.called
        assert not logger.warning.called
        assert not shell.help_schedule_details.called

        assert shell.client.schedule.listCompletedSystems.called
        assert shell.client.schedule.listFailedSystems.called
        assert shell.client.schedule.listInProgressSystems.called
        assert shell.client.schedule.listAllActions.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list,
                      'No action found with the ID "42"')

    def test_schedule_details_report(self, shell):
        """
        Test do_schedule_details report layout

        :param shell:
        :return:
        """
        shell.client.schedule.listCompletedSystems = MagicMock(return_value=[
            {"server_name": "one"},
            {"server_name": "two"},
            {"server_name": "three"},
        ])
        shell.client.schedule.listFailedSystems = MagicMock(return_value=[
            {"server_name": "failed-machine"},
        ])
        shell.client.schedule.listInProgressSystems = MagicMock(return_value=[
            {"server_name": "four"},
            {"server_name": "five"},
        ])
        shell.client.schedule.listAllActions = MagicMock(return_value=[
            {"id": 1, "name": "Reboot Coffee Machine",
             "scheduler": "qa-guy", "earliest": "2019-01-01"},
            {"id": 2, "name": "Upgrade Coffee Machine",
             "scheduler": "qa-guy", "earliest": "2019-01-01"},
            {"id": 3, "name": "Reinstall Coffee Machine Firmware",
             "scheduler": "qa-guy", "earliest": "2019-01-01"},
        ])

        shell.help_schedule_details = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.schedule.print", mprint) as prt, \
                patch("spacecmd.schedule.logging", logger) as lgr:
            spacecmd.schedule.do_schedule_details(shell, "3")

        assert not logger.warning.called
        assert not shell.help_schedule_details.called
        assert not logger.error.called

        assert shell.client.schedule.listCompletedSystems.called
        assert shell.client.schedule.listFailedSystems.called
        assert shell.client.schedule.listInProgressSystems.called
        assert shell.client.schedule.listAllActions.called
        assert mprint.called

        assert_list_args_expect(mprint.call_args_list,
                                ['ID:        3', 'Action:    Reinstall Coffee Machine Firmware',
                                 'User:      qa-guy', 'Date:      2019-01-01', '',
                                 'Completed:   3', 'Failed:      1', 'Pending:     2', '',
                                 'Completed Systems',
                                 '-----------------', 'one', 'two', 'three', '',
                                 'Failed Systems', '--------------', 'failed-machine', '',
                                 'Pending Systems', '---------------', 'four', 'five'])

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

    def test_schedule_getoutput_invalid_action_id(self, shell):
        """
        Test do_schedule_getoutput with an invalid action ID.

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
            spacecmd.schedule.do_schedule_getoutput(shell, "fortytwo")

        assert not shell.client.system.getScriptResults.called
        assert not shell.client.schedule.listCompletedSystems.called
        assert not mprint.called
        assert not logger.warning.called
        assert not shell.help_schedule_getoutput.called

        assert logger.error.called

        assert_expect(logger.error.call_args_list,
                      '"fortytwo" is not a valid action ID')

    def test_schedule_getoutput_no_script_results(self, shell):
        """
        Test do_schedule_getoutput with no script results (failed or None)

        :param shell:
        :return:
        """
        shell.client.schedule.listCompletedSystems = MagicMock(
            return_value=[
                {"server_name": "web.foo.com", "timestamp": "2019-01-01",
                 "message": "Message from the web.foo.com"},
                {"server_name": "web1.foo.com", "timestamp": "2019-01-01",
                 "message": "Message from the web1.foo.com as well"},
                {"server_name": "web2.foo.com", "timestamp": "2019-01-01",
                 "message": "And some more message from web2.foo.com here"}
            ]
        )
        shell.client.schedule.listFailedSystems = MagicMock(
            return_value=[
                {"server_name": "faulty.foo.com", "timestamp": "2019-01-01",
                 "message": "Nothing good is happening on faulty.foo.com"},
            ])
        shell.client.system.getScriptResults = MagicMock(
            side_effect=xmlrpclib.Fault(faultCode=42, faultString="Happy NPE!"))
        shell.help_schedule_getoutput = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.schedule.print", mprint) as prt, \
                patch("spacecmd.schedule.logging", logger) as lgr:
            spacecmd.schedule.do_schedule_getoutput(shell, "42")

        assert not logger.warning.called
        assert not shell.help_schedule_getoutput.called
        assert shell.client.system.getScriptResults.called
        assert shell.client.schedule.listCompletedSystems.called
        assert shell.client.schedule.listFailedSystems.called
        assert mprint.called
        assert logger.debug.called

        assert_args_expect(logger.debug.call_args_list,
                           [(('Exception occurrect while get script results: %s',
                              "<Fault 42: 'Happy NPE!'>"), {})])
        assert_list_args_expect(mprint.call_args_list,
                                ['System:    web.foo.com',
                                 'Completed: 2019-01-01', '', 'Output', '------',
                                 'Message from the web.foo.com', '----------', 'System:    web1.foo.com',
                                 'Completed: 2019-01-01', '', 'Output', '------',
                                 'Message from the web1.foo.com as well', '----------',
                                 'System:    web2.foo.com',
                                 'Completed: 2019-01-01', '', 'Output', '------',
                                 'And some more message from web2.foo.com here',
                                 '----------', 'System:    faulty.foo.com',
                                 'Completed: 2019-01-01', '', 'Output', '------',
                                 'Nothing good is happening on faulty.foo.com'])
