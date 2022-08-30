# coding: utf-8
"""
Test suite for spacecmd.api
"""
from mock import MagicMock, patch, mock_open
from spacecmd import api
import helpers
import datetime


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
        assert out._closed

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
        assert out._closed

    def test_args_args(self):
        """
        Test args option.
        """
        shell = MagicMock()
        shell.help_api = MagicMock()
        shell.client = MagicMock()
        shell.client.call = MagicMock(return_value=["one", "two", "three"])
        shell.session = "session"

        log = MagicMock()
        out = helpers.FileHandleMock()
        with patch("spacecmd.api.open", out, create=True) as mop, \
             patch("spacecmd.api.logging", log) as mlog:
            api.do_api(shell, "call -A first,second,123 -o /tmp/spacecmd.log")
        assert shell.client.call.called
        assert shell.client.call.call_args_list[0][0] == ('session', 'first', 'second', 123)
        assert out._closed

    def test_args_datetime(self):
        """
        Test args option.
        """
        shell = MagicMock()
        shell.help_api = MagicMock()
        shell.client = MagicMock()
        shell.client.call = MagicMock(return_value=["one", "two", "three"])
        shell.session = "session"

        log = MagicMock()
        out = helpers.FileHandleMock()
        with patch("spacecmd.api.open", out, create=True) as mop, \
                patch("spacecmd.api.logging", log) as mlog:
            api.do_api(shell, "call -A first,second,2022-05-05 -o /tmp/spacecmd.log")
        assert shell.client.call.called
        assert shell.client.call.call_args_list[0][0] == ('session', 'first', 'second',
                                                          datetime.datetime(2022, 5, 5, 0, 0))
        assert out._closed

    def test_args_json(self):
        """
        Test args option.
        """
        shell = MagicMock()
        shell.help_api = MagicMock()
        shell.client = MagicMock()
        shell.client.call = MagicMock(return_value=["one", "two", "three"])
        shell.session = "session"

        log = MagicMock()
        out = helpers.FileHandleMock()
        with patch("spacecmd.api.open", out, create=True) as mop, \
                patch("spacecmd.api.logging", log) as mlog:
            api.do_api(shell, "call -A '[\"first\",\"second\",\"2022-05-05\",4]' -o /tmp/spacecmd.log")
        assert shell.client.call.called
        assert shell.client.call.call_args_list[0][0] == ('session', 'first', 'second',
                                                          datetime.datetime(2022, 5, 5, 0, 0),4)
        assert out._closed
