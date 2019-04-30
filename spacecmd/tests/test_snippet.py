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

        mprint = MagicMock()
        shell.client.kickstart.snippet.listCustom = MagicMock(return_value=snippets)
        with patch("spacecmd.snippet.print", mprint):
            out = snippet.do_snippet_list(shell, "", doreturn=True)

        assert out is not None
        assert out == ['snippet - 1', 'snippet - 2', 'snippet - 3']  # Sorted

    @pytest.mark.skip(reason="Not implemented yet")
    def test_snippet_details_noarg(self, shell):
        """
        Test snippet details no args
        """

    @pytest.mark.skip(reason="Not implemented yet")
    def test_snippet_details_args(self, shell):
        """
        Test snippet details args
        """
