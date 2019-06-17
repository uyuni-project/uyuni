# coding: utf-8
"""
Test case for spacecmd.user module
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.user
from xmlrpc import client as xmlrpclib


class TestSCUser:
    """
    Test suite for "user" module.
    """
    def test_user_create_interactive(self, shell):
        """
        Test do_user_create interactive mode.

        :param shell:
        :return:
        """
        shell.client.user.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=1)
        logger = MagicMock()
        getps = MagicMock(return_value="1234567890")
        prompter = MagicMock(side_effect=["lksw", "Luke", "Skywalker",
                                          "l.skywalker@suse.com"])
        with patch("spacecmd.user.logging", logger) as lgr, \
                patch("spacecmd.user.prompt_user", prompter) as pmt, \
                patch("spacecmd.user.getpass", getps) as gpw:
            spacecmd.user.do_user_create(shell, "")

        assert shell.client.user.create.called
        assert not logger.warning.called
        assert_args_expect(shell.client.user.create.call_args_list,
                           [((shell.session, 'lksw', '1234567890', 'Luke',
                              'Skywalker', 'l.skywalker@suse.com', 1), {})])

    def test_user_create_args(self, shell):
        """
        Test do_user_create parameters/arguments mode.

        :param shell:
        :return:
        """
        shell.client.user.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        logger = MagicMock()
        getps = MagicMock(return_value="1234567890")
        prompter = MagicMock(side_effect=Exception("Should not happen"))
        with patch("spacecmd.user.logging", logger) as lgr, \
                patch("spacecmd.user.prompt_user", prompter) as pmt, \
                patch("spacecmd.user.getpass", getps) as gpw:
            spacecmd.user.do_user_create(shell, "-u lksw -f Luke -l Skywalker "
                                                "-e l.skywalker@suse.com -p 1234567890")

        assert shell.client.user.create.called
        assert not logger.error.called
        assert not logger.warning.called
        assert_args_expect(shell.client.user.create.call_args_list,
                           [((shell.session, 'lksw', '1234567890', 'Luke',
                              'Skywalker', 'l.skywalker@suse.com', 0), {})])

    def test_user_create_no_username(self, shell):
        """
        Test do_user_create, missing user name

        :param shell:
        :return:
        """
        shell.client.user.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        logger = MagicMock()
        getps = MagicMock(return_value="1234567890")
        prompter = MagicMock(side_effect=Exception("Should not happen"))
        with patch("spacecmd.user.logging", logger) as lgr, \
                patch("spacecmd.user.prompt_user", prompter) as pmt, \
                patch("spacecmd.user.getpass", getps) as gpw:
            spacecmd.user.do_user_create(shell, "-f Luke -l Skywalker "
                                                "-e l.skywalker@suse.com -p 1234567890")

        assert not shell.client.user.create.called
        assert not logger.warning.called
        assert logger.error.called
        assert_expect(logger.error.call_args_list, "A username is required")
