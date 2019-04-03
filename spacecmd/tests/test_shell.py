# coding: utf-8
"""
Unit test for the spacecmd.shell module.
"""
from mock import MagicMock, patch
import os
import time
import readline
import pytest
from spacecmd.shell import SpacewalkShell, UnknownCallException


class TestSCShell:
    """
    Test shell in spacecmd.
    """
    @patch("spacecmd.shell.atexit", MagicMock())
    def test_shell_history(self):
        """
        Test history length.
        """
        assert SpacewalkShell.HISTORY_LENGTH == 1024

    @patch("spacecmd.shell.atexit", MagicMock())
    @patch("spacecmd.shell.readline.get_completer_delims",
           MagicMock(return_value=readline.get_completer_delims()))
    @patch("spacecmd.shell.sys.exit", MagicMock())
    def test_shell_delimeters(self):
        """
        Test shell delimieters are set without hyphens
        or colons during the tab completion.
        """
        cfg_dir = "/tmp/shell/{}/conf".format(int(time.time()))
        m_logger = MagicMock()

        cpl_setter = MagicMock()
        with patch("spacecmd.shell.logging", m_logger) as lgr, \
            patch("spacecmd.shell.readline.set_completer_delims", cpl_setter):
            options = MagicMock()
            options.nohistory = True
            shell = SpacewalkShell(options, cfg_dir, None)

            assert shell.history_file == "{}/history".format(cfg_dir)
            assert not m_logger.error.called
            assert cpl_setter.call_args[0][0] != readline.get_completer_delims()
            assert cpl_setter.call_args[0][0] == ' \t\n`~!@#$%^&*()=+[{]}\\|;\'",<>?'

