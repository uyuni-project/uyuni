# coding: utf-8
"""
Test suite for spacecmd.api
"""
from mock import MagicMock, patch, mock_open
from spacecmd import api
import helpers


class TestSCAPI:
    """
    Test class for testing spacecmd API.
    """
    def test_no_args(self):
        """
        Test calling API without any arguments.
        """
        shell = MagicMock()
        shell.help_api = MagicMock()
        api.do_api(shell, "")
        assert shell.help_api.called

    def test_args_output(self):
        """
        Test output option.
        """
        shell = MagicMock()
        shell.help_api = MagicMock()
        shell.client = MagicMock()
        shell.client.call = MagicMock(return_value=["one", "two", "three"])

        log = MagicMock()
        out = helpers.FileHandleMock()
        with patch("spacecmd.api.open", out, create=True) as mop, \
             patch("spacecmd.api.logging", log) as mlog:
            api.do_api(shell, "call -o /tmp/spacecmd.log")

        assert not mlog.warning.called
        assert out.get_content() == '[\n  "one",\n  "two",\n  "three"\n]'
        assert out.get_init_kwargs() == {}
        assert out.get_init_args() == ('/tmp/spacecmd.log', 'w')

    def test_args_format(self):
        """
        Test format option.
        """
        shell = MagicMock()
        shell.help_api = MagicMock()
        shell.client = MagicMock()
        shell.client.call = MagicMock(return_value=["one", "two", "three"])

        log = MagicMock()
        out = helpers.FileHandleMock()
        with patch("spacecmd.api.open", out, create=True) as mop, \
             patch("spacecmd.api.logging", log) as mlog:
            api.do_api(shell, "call -o /tmp/spacecmd.log -F '>>> %s'")

        assert not mlog.warning.called
        assert out.get_content() == '>>> one\n>>> two\n>>> three\n'
        assert out.get_init_kwargs() == {}
        assert out.get_init_args() == ('/tmp/spacecmd.log', 'w')
