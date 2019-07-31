# coding: utf-8
"""
Kickstart API calls unit tests.

NOTE: This module is quite rarely used within Uyuni/SLE,
      only mostly for cloning, manual editing of the cobbler profiles
      and then deleting them.
"""
import os
from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.kickstart


class TestSCKickStart:
    """
    Test kickstart.
    """
    def test_kickstart_clone_interactive(self, shell):
        """
        Test do_kickstart_clone interactive.
        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr:
            spacecmd.kickstart.do_kickstart_clone(shell, "")

        assert not mprint.called
        assert not shell.client.kickstart.cloneProfile.called
        assert logger.error.called
        assert_expect(logger.error.call_args_list,
                      "No kickstart profiles available")

