# coding: utf-8
"""
Test suite for snippet source
"""
from mock import MagicMock, patch, mock_open
from spacecmd import snippet
from helpers import shell, assert_expect
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
        assert_expect(logger.warning.call_args_list, *calls)

        stdout_data = [
            'Name:   snippet3', 'Macro:  3rd fragment',
            'File:   /tmp/3', '', 'three', '---',
            'Name:   snippet1', 'Macro:  1st fragment',
            'File:   /tmp/1', '', 'one',
        ]
        assert_expect(mprint.call_args_list, *stdout_data)

    def test_snippet_create_no_args(self, shell):
        """
        Test create snippet with no arguments and no name update.
        """
        snippets = [
            {"name": "snippet3", "contents": "three", "fragment": "3rd fragment", "file": "/tmp/3"},
            {"name": "snippet1", "contents": "one", "fragment": "1st fragment", "file": "/tmp/1"},
            {"name": "snippet2", "contents": "two", "fragment": "2nd fragment", "file": "/tmp/2"},
        ]

        prompt_user = MagicMock(side_effect=["custom-snippet", "/tmp/cs.snip"])
        logger = MagicMock()
        editor = MagicMock(return_value=("editor content", False))
        read_file = MagicMock(return_value="file content")
        mprint = MagicMock()

        shell.client.kickstart.snippet.listCustom = MagicMock(return_value=snippets)
        shell.client.kickstart.snippet.createOrUpdate = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)

        with patch("spacecmd.snippet.logging", logger) as lgr, \
             patch("spacecmd.snippet.prompt_user", prompt_user) as prmt, \
             patch("spacecmd.snippet.editor", editor) as edtr, \
             patch("spacecmd.snippet.read_file", read_file) as rfl, \
             patch("spacecmd.snippet.print", mprint) as prn:
            snippet.do_snippet_create(shell, "")

        assert not logger.error.called
        assert not shell.client.kickstart.snippet.listCustom.called
        assert shell.user_confirm.called
        assert shell.client.kickstart.snippet.createOrUpdate.called

        assert_expect(prompt_user.call_args_list, "Name:", "File:")
        assert_expect(mprint.call_args_list, "", "Snippet: custom-snippet", "Contents", "--------", "file content")

    def test_snippet_create_name_arg(self, shell):
        """
        Test create snippet with only name argument
        """
        snippets = [
            {"name": "snippet3", "contents": "three", "fragment": "3rd fragment", "file": "/tmp/3"},
            {"name": "snippet1", "contents": "one", "fragment": "1st fragment", "file": "/tmp/1"},
            {"name": "snippet2", "contents": "two", "fragment": "2nd fragment", "file": "/tmp/2"},
        ]

        prompt_user = MagicMock(side_effect=["custom-snippet", "/tmp/cs.snip"])
        logger = MagicMock()
        editor = MagicMock(return_value=("editor content", False))
        read_file = MagicMock(return_value="file content")
        mprint = MagicMock()

        shell.client.kickstart.snippet.listCustom = MagicMock(return_value=snippets)
        shell.client.kickstart.snippet.createOrUpdate = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)

        with patch("spacecmd.snippet.logging", logger) as lgr, \
             patch("spacecmd.snippet.prompt_user", prompt_user) as prmt, \
             patch("spacecmd.snippet.editor", editor) as edtr, \
             patch("spacecmd.snippet.read_file", read_file) as rfl, \
             patch("spacecmd.snippet.print", mprint) as prn:
            snippet.do_snippet_create(shell, "--name something")

        assert not shell.client.kickstart.snippet.listCustom.called
        assert not shell.client.kickstart.snippet.createOrUpdate.called
        assert not shell.user_confirm.called
        assert not mprint.called
        assert not prompt_user.called
        assert logger.error.called
        assert logger.error.call_args_list[0][0][0] == "A file is required"

    def test_snippet_create_file_arg(self, shell):
        """
        Test create snippet with only file argument
        """
        snippets = [
            {"name": "snippet3", "contents": "three", "fragment": "3rd fragment", "file": "/tmp/3"},
            {"name": "snippet1", "contents": "one", "fragment": "1st fragment", "file": "/tmp/1"},
            {"name": "snippet2", "contents": "two", "fragment": "2nd fragment", "file": "/tmp/2"},
        ]

        prompt_user = MagicMock(side_effect=["custom-snippet", "/tmp/cs.snip"])
        logger = MagicMock()
        editor = MagicMock(return_value=("editor content", False))
        read_file = MagicMock(return_value="file content")
        mprint = MagicMock()

        shell.client.kickstart.snippet.listCustom = MagicMock(return_value=snippets)
        shell.client.kickstart.snippet.createOrUpdate = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)

        with patch("spacecmd.snippet.logging", logger) as lgr, \
             patch("spacecmd.snippet.prompt_user", prompt_user) as prmt, \
             patch("spacecmd.snippet.editor", editor) as edtr, \
             patch("spacecmd.snippet.read_file", read_file) as rfl, \
             patch("spacecmd.snippet.print", mprint) as prn:
            snippet.do_snippet_create(shell, "--file /path/to/somewhere.snip")

        assert not shell.client.kickstart.snippet.listCustom.called
        assert not shell.client.kickstart.snippet.createOrUpdate.called
        assert not shell.user_confirm.called
        assert not mprint.called
        assert not prompt_user.called
        assert logger.error.called
        assert logger.error.call_args_list[0][0][0] == "A name is required for the snippet"

    def test_snippet_create_args(self, shell):
        """
        Test create snippet with arguments.
        """
        snippets = [
            {"name": "snippet3", "contents": "three", "fragment": "3rd fragment", "file": "/tmp/3"},
            {"name": "snippet1", "contents": "one", "fragment": "1st fragment", "file": "/tmp/1"},
            {"name": "snippet2", "contents": "two", "fragment": "2nd fragment", "file": "/tmp/2"},
        ]

        prompt_user = MagicMock(side_effect=["custom-snippet", "/tmp/cs.snip"])
        logger = MagicMock()
        editor = MagicMock(return_value=("editor content", False))
        read_file = MagicMock(return_value="file content")
        mprint = MagicMock()

        shell.client.kickstart.snippet.listCustom = MagicMock(return_value=snippets)
        shell.client.kickstart.snippet.createOrUpdate = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)

        with patch("spacecmd.snippet.logging", logger) as lgr, \
             patch("spacecmd.snippet.prompt_user", prompt_user) as prmt, \
             patch("spacecmd.snippet.editor", editor) as edtr, \
             patch("spacecmd.snippet.read_file", read_file) as rfl, \
             patch("spacecmd.snippet.print", mprint) as prn:
            snippet.do_snippet_create(shell, "--name foobar --file /path/to/somewhere.snip")

        assert not logger.error.called
        assert not shell.client.kickstart.snippet.listCustom.called
        assert not prompt_user.called
        assert shell.client.kickstart.snippet.createOrUpdate.called
        assert shell.user_confirm.called
        assert mprint.called
        assert_expect(mprint.call_args_list, "", "Snippet: foobar", "Contents", "--------", "file content")

    def test_snippet_create_editor(self, shell):
        """
        Test create snippet using the editor.
        """
        snippets = [
            {"name": "snippet3", "contents": "three", "fragment": "3rd fragment", "file": "/tmp/3"},
            {"name": "snippet1", "contents": "one", "fragment": "1st fragment", "file": "/tmp/1"},
            {"name": "snippet2", "contents": "two", "fragment": "2nd fragment", "file": "/tmp/2"},
        ]

        prompt_user = MagicMock(side_effect=["custom-snippet", "/tmp/cs.snip"])
        logger = MagicMock()
        editor = MagicMock(return_value=("editor content", False))
        read_file = MagicMock(return_value="file content")
        mprint = MagicMock()

        shell.client.kickstart.snippet.listCustom = MagicMock(return_value=snippets)
        shell.client.kickstart.snippet.createOrUpdate = MagicMock()
        shell.user_confirm = MagicMock(side_effect=[False, True])

        with patch("spacecmd.snippet.logging", logger) as lgr, \
             patch("spacecmd.snippet.prompt_user", prompt_user) as prmt, \
             patch("spacecmd.snippet.editor", editor) as edtr, \
             patch("spacecmd.snippet.read_file", read_file) as rfl, \
             patch("spacecmd.snippet.print", mprint) as prn:
            snippet.do_snippet_create(shell, "")

        assert not logger.error.called
        assert not shell.client.kickstart.snippet.listCustom.called
        assert prompt_user.called
        assert shell.client.kickstart.snippet.createOrUpdate.called
        assert shell.user_confirm.called
        assert mprint.called
        assert editor.called
        assert_expect(mprint.call_args_list, "", "Snippet: custom-snippet", "Contents", "--------", "editor content")

    def test_snippet_update_no_args(self, shell):
        """
        Test update snippet with no args
        """
        shell.do_snippet_create = MagicMock()
        shell.help_snippet_update = MagicMock()

        out = snippet.do_snippet_update(shell, "")
        assert shell.help_snippet_update.called
        assert out is None

    def test_snippet_update_args(self, shell):
        """
        Test update snippet with args
        """
        shell.do_snippet_create = MagicMock()
        shell.help_snippet_update = MagicMock()

        out = snippet.do_snippet_update(shell, "custom_name")

        assert not shell.help_snippet_update.called
        assert out is not None
        assert shell.do_snippet_create.called
        assert not shell.do_snippet_create.call_args_list[0][0][0]
        assert "update_name" in shell.do_snippet_create.call_args_list[0][1]
        assert shell.do_snippet_create.call_args_list[0][1]["update_name"] == "custom_name"

    def test_snippet_delete_no_args(self, shell):
        """
        Test delete snippet with no args
        """
        shell.client.kickstart.snippet.delete = MagicMock()
        shell.help_snippet_delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)

        out = snippet.do_snippet_delete(shell, "")

        assert not shell.client.kickstart.snippet.delete.called
        assert shell.help_snippet_delete.called
        assert out is None

    def test_snippet_delete_args(self, shell):
        """
        Test delete snippet with args (snippet name)
        """
        shell.client.kickstart.snippet.delete = MagicMock()
        shell.help_snippet_delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)

        out = snippet.do_snippet_delete(shell, "some-snippet")

        assert shell.client.kickstart.snippet.delete.called
        assert not shell.help_snippet_delete.called
        assert out is None
        assert shell.client.kickstart.snippet.delete.call_args_list[0][0][0] == shell.session
        assert shell.client.kickstart.snippet.delete.call_args_list[0][0][1] == "some-snippet"

    def test_snippet_delete_args_no_confirm(self, shell):
        """
        Test delete snippet with args (snippet name), unconfirmed.
        """
        shell.client.kickstart.snippet.delete = MagicMock()
        shell.help_snippet_delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)

        out = snippet.do_snippet_delete(shell, "some-snippet")

        assert not shell.client.kickstart.snippet.delete.called
        assert not shell.help_snippet_delete.called
        assert out is None
