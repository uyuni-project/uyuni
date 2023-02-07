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
from helpers import exc2str


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

    @patch("spacecmd.shell.atexit", MagicMock())
    @patch("spacecmd.shell.readline.set_completer_delims", MagicMock())
    @patch("spacecmd.shell.readline.get_completer_delims",
           MagicMock(return_value=readline.get_completer_delims()))
    @patch("spacecmd.shell.sys.exit", MagicMock())
    @patch("spacecmd.shell.os.path.isfile", MagicMock(side_effect=IOError("No such file")))
    def test_shell_no_history_file(self):
        """
        Test shell no history file should capture IOError and log it.
        """
        cfg_dir = "/tmp/shell/{}/conf".format(int(time.time()))
        m_logger = MagicMock()
        cpl_setter = MagicMock()
        with patch("spacecmd.shell.logging", m_logger):
            options = MagicMock()
            options.nohistory = False
            shell = SpacewalkShell(options, cfg_dir, None)

            assert shell.history_file == "{}/history".format(cfg_dir)
            assert not os.path.exists(shell.history_file)
            assert m_logger.error.call_args[0][0] == "Could not read history file"

    @patch("spacecmd.shell.atexit", MagicMock())
    @patch("spacecmd.shell.print", MagicMock())
    @patch("spacecmd.shell.sys.exit", MagicMock(side_effect=Exception("Exit attempt")))
    @patch("spacecmd.shell.readline.set_completer_delims", MagicMock())
    @patch("spacecmd.shell.readline.get_completer_delims", MagicMock(return_value=readline.get_completer_delims()))
    def test_shell_precmd_exit_keywords(self):
        """
        Test 'precmd' method of the shell on exit keywords.
        """
        options = MagicMock()
        options.nohistory = True
        shell = SpacewalkShell(options, "", None)
        shell.config["server"] = ""
        for cmd in ["exit", "quit", "eof"]:
            with pytest.raises(Exception) as exc:
                shell.precmd(cmd)
            assert "Exit attempt" in exc2str(exc)

    @patch("spacecmd.shell.atexit", MagicMock())
    @patch("spacecmd.shell.readline.set_completer_delims", MagicMock())
    @patch("spacecmd.shell.readline.get_completer_delims", MagicMock(return_value=readline.get_completer_delims()))
    def test_shell_precmd_common_keywords(self):
        """
        Test 'precmd' method of the shell on common keywords, e.g. login, logout, clear etc.
        """
        options = MagicMock()
        options.nohistory = True
        shell = SpacewalkShell(options, "", None)
        shell.config["server"] = ""
        for cmd in ["help", "login", "logout", "history", "clear"]:
            assert shell.precmd(cmd) == cmd

    @patch("spacecmd.shell.atexit", MagicMock())
    @patch("spacecmd.shell.readline.set_completer_delims", MagicMock())
    @patch("spacecmd.shell.readline.get_completer_delims", MagicMock(return_value=readline.get_completer_delims()))
    def test_shell_precmd_empty_line(self):
        """
        Test 'precmd' method of the shell on empty line.
        """
        options = MagicMock()
        options.nohistory = True
        shell = SpacewalkShell(options, "", None)
        shell.config["server"] = ""
        assert shell.precmd("") == ""

    @patch("spacecmd.shell.atexit", MagicMock())
    @patch("spacecmd.shell.readline.set_completer_delims", MagicMock())
    @patch("spacecmd.shell.readline.get_completer_delims", MagicMock(return_value=readline.get_completer_delims()))
    def test_shell_precmd_session_login(self):
        """
        Test 'precmd' method of the shell on session login.
        """
        options = MagicMock()
        options.nohistory = True
        shell = SpacewalkShell(options, "", None)
        shell.config["server"] = ""
        shell.do_login = MagicMock(side_effect=Exception("login attempt"))

        with pytest.raises(Exception) as exc:
            shell.precmd("system_list")
        assert "login attempt" in exc2str(exc)

    @patch("spacecmd.shell.atexit", MagicMock())
    @patch("spacecmd.shell.readline.set_completer_delims", MagicMock())
    @patch("spacecmd.shell.readline.get_completer_delims", MagicMock(return_value=readline.get_completer_delims()))
    def test_shell_precmd_help_keyword(self):
        """
        Test 'precmd' method of the shell on --help/-h arguments.
        """
        options = MagicMock()
        options.nohistory = True
        shell = SpacewalkShell(options, "", None)
        shell.config["server"] = ""
        shell.session = True

        assert shell.precmd("system_list --help") == "help system_list"
        assert shell.precmd("system_list -h") == "help system_list"

    @patch("spacecmd.shell.atexit", MagicMock())
    @patch("spacecmd.shell.readline.set_completer_delims", MagicMock())
    @patch("spacecmd.shell.readline.get_completer_delims", MagicMock(return_value=readline.get_completer_delims()))
    def test_shell_precmd_one_char_cmd(self):
        """
        Test 'precmd' method one char.
        """
        options = MagicMock()
        options.nohistory = True
        shell = SpacewalkShell(options, "", None)
        shell.config["server"] = ""
        shell.session = True

        assert shell.precmd("x") == "x"

    @patch("spacecmd.shell.atexit", MagicMock())
    @patch("spacecmd.shell.print", MagicMock())
    @patch("spacecmd.shell.readline.set_completer_delims", MagicMock())
    @patch("spacecmd.shell.readline.get_history_item", MagicMock(return_value="repeated item"))
    @patch("spacecmd.shell.readline.get_completer_delims", MagicMock(return_value=readline.get_completer_delims()))
    def test_shell_precmd_history(self):
        """
        Test 'precmd' method getting history item.
        """
        options = MagicMock()
        options.nohistory = True
        shell = SpacewalkShell(options, "", None)
        shell.config["server"] = ""
        shell.session = True

        assert shell.precmd("!!") == "repeated item"

    @patch("spacecmd.shell.atexit", MagicMock())
    @patch("spacecmd.shell.readline.set_completer_delims", MagicMock())
    @patch("spacecmd.shell.readline.get_completer_delims", MagicMock(return_value=readline.get_completer_delims()))
    def test_shell_postcmd(self):
        """
        Test 'postcmd' method of the shell.
        """
        options = MagicMock()
        options.nohistory = True
        shell = SpacewalkShell(options, "", None)
        shell.config["server"] = ""
        shell.session = True
        shell.ssm = {1: "one"}
        shell.postcmd("result", "command")
        assert shell.prompt == "spacecmd {SSM:1}> "

    @patch("spacecmd.shell.atexit", MagicMock())
    @patch("spacecmd.shell.readline.set_completer_delims", MagicMock())
    @patch("spacecmd.shell.readline.get_completer_delims", MagicMock(return_value=readline.get_completer_delims()))
    def test_shell_default(self):
        """
        Test 'default' method of the shell.
        """
        cmd = MagicMock()
        with patch("spacecmd.shell.Cmd.default", cmd):
            options = MagicMock()
            options.nohistory = True
            shell = SpacewalkShell(options, "", None)
            shell.config["server"] = ""
            shell.session = True

            with pytest.raises(UnknownCallException):
                shell.default("test")
            assert cmd.call_args[0][1] == "test"
