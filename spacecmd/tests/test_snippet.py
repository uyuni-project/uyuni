# coding: utf-8
"""
Test suite for snippet source
"""
from mock import MagicMock, patch, mock_open
from spacecmd import snippet
from helpers import shell
import pytest


class TestSCSnippets:
    """
    Test for snippet API.
    """
    def test_snippet_list_noarg(self, shell):
        """
        Test snippet list noargs
        """
        snippets = [
            {"name": "snippet - 3"},
            {"name": "snippet - 1"},
            {"name": "snippet - 2"},
        ]

        mprint = MagicMock()
        shell.client.kickstart.snippet.listCustom = MagicMock(return_value=snippets)
        with patch("spacecmd.snippet.print", mprint):
            out = snippet.do_snippet_list(shell, "")

        assert out is None
        assert mprint.call_args_list[0][0] == ('snippet - 1\nsnippet - 2\nsnippet - 3',)  # Sorted

    def test_snippet_list_args(self, shell):
        """
        Test snippet list with the args
        """
        snippets = [
            {"name": "snippet - 3"},
            {"name": "snippet - 1"},
            {"name": "snippet - 2"},
        ]

        shell.client.kickstart.snippet.listCustom = MagicMock(return_value=snippets)

        mprint = MagicMock()
        with patch("spacecmd.snippet.print", mprint):
            out = snippet.do_snippet_list(shell, "", doreturn=True)

        assert out is not None
        assert out == ['snippet - 1', 'snippet - 2', 'snippet - 3']  # Sorted

    def test_snippet_details_noarg(self, shell):
        """
        Test snippet details no args
        """
        snippets = [
            {"name": "snippet - 3"},
            {"name": "snippet - 1"},
            {"name": "snippet - 2"},
        ]
        shell.client.kickstart.snippet.listCustom = MagicMock(return_value=snippets)
        shell.help_snippet_details = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.snippet.print", mprint) as mpr, patch("spacecmd.snippet.logging", logger) as lgr:
            snippet.do_snippet_details(shell, "")
        assert not mprint.called
        assert not logger.warning.called
        assert shell.help_snippet_details.called

    def test_snippet_details_args(self, shell):
        """
        Test snippet details with the args
        """
        snippets = [
            {"name": "snippet3", "contents": "three", "fragment": "3rd fragment", "file": "/tmp/3"},
            {"name": "snippet1", "contents": "one", "fragment": "1st fragment", "file": "/tmp/1"},
            {"name": "snippet2", "contents": "two", "fragment": "2nd fragment", "file": "/tmp/2"},
        ]
        shell.client.kickstart.snippet.listCustom = MagicMock(return_value=snippets)
        shell.SEPARATOR = "---"
        shell.help_snippet_details = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.snippet.print", mprint) as mpr, patch("spacecmd.snippet.logging", logger) as lgr:
            snippet.do_snippet_details(shell, "snippet4 snippet5 snippet3 snippet1")
        assert not shell.help_snippet_details.called
        assert logger.warning.called
        assert mprint.called

        calls = [
            "snippet4 is not a valid snippet",
            "snippet5 is not a valid snippet",
        ]

        for call in logger.warning.call_args_list:
            assert call[0][0] == next(iter(calls))
            calls.pop(0)
        assert not calls

        stdout_data = [
            'Name:   snippet3', 'Macro:  3rd fragment',
            'File:   /tmp/3', '', 'three', '---',
            'Name:   snippet1', 'Macro:  1st fragment',
            'File:   /tmp/1', '', 'one',
        ]

        for call in mprint.call_args_list:
            assert call[0][0] == next(iter(stdout_data))
            stdout_data.pop(0)
        assert not stdout_data
