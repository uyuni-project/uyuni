# coding: utf-8
"""
Test suite for cryptokey.
"""
from mock import MagicMock, patch
import pytest
import spacecmd.cryptokey
from helpers import shell, assert_expect


class TestSCCryptokey:
    """
    Test cryptokey API.
    """
    def test_cryptokey_create_no_keytype(self, shell):
        """
        Test do_cryptokey_create without correct key type.

        :param shell:
        :return:
        """
        shell.help_cryptokey_create = MagicMock()
        shell.client.kickstart.keys.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        read_file = MagicMock(return_value="contents")
        prompt_user = MagicMock(side_effect=["", "interactive descr", "/tmp/file.txt"])
        editor = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.cryptokey.prompt_user", prompt_user) as pmu, \
            patch("spacecmd.cryptokey.read_file", read_file) as rfl, \
            patch("spacecmd.cryptokey.editor", editor) as edt, \
            patch("spacecmd.cryptokey.logging", logger) as lgr:
            spacecmd.cryptokey.do_cryptokey_create(shell, "")

        assert not shell.help_cryptokey_create.called
        assert not shell.client.kickstart.keys.create.called
        assert read_file.called
        assert prompt_user.called
        assert not editor.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, "Invalid key type")

    def test_cryptokey_create_interactive_no_contents(self, shell):
        """
        Test do_cryptokey_create without arguments (interactive, no contents given).

        :param shell:
        :return:
        """
        shell.help_cryptokey_create = MagicMock()
        shell.client.kickstart.keys.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        read_file = MagicMock()
        prompt_user = MagicMock(side_effect=["g", "interactive descr", ""])
        editor = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.cryptokey.prompt_user", prompt_user) as pmu, \
            patch("spacecmd.cryptokey.read_file", read_file) as rfl, \
            patch("spacecmd.cryptokey.editor", editor) as edt, \
            patch("spacecmd.cryptokey.logging", logger) as lgr:
            spacecmd.cryptokey.do_cryptokey_create(shell, "")

        assert not shell.help_cryptokey_create.called
        assert not shell.client.kickstart.keys.create.called
        assert not read_file.called
        assert prompt_user.called
        assert not editor.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, "No contents of the file")

    def test_cryptokey_create_interactive_wrong_key_type(self, shell):
        """
        Test do_cryptokey_create without arguments (interactive, wrong key type).

        :param shell:
        :return:
        """
        shell.help_cryptokey_create = MagicMock()
        shell.client.kickstart.keys.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        read_file = MagicMock(return_value="contents")
        prompt_user = MagicMock(side_effect=["x", "interactive descr", "/tmp/file.txt"])
        editor = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.cryptokey.prompt_user", prompt_user) as pmu, \
            patch("spacecmd.cryptokey.read_file", read_file) as rfl, \
            patch("spacecmd.cryptokey.editor", editor) as edt, \
            patch("spacecmd.cryptokey.logging", logger) as lgr:
            spacecmd.cryptokey.do_cryptokey_create(shell, "")

        assert not shell.help_cryptokey_create.called
        assert not shell.client.kickstart.keys.create.called
        assert read_file.called
        assert prompt_user.called
        assert not editor.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, "Invalid key type")

    def test_cryptokey_create_GPG_key(self, shell):
        """
        Test do_cryptokey_create with parameters, calling GPG key type.

        :param shell:
        :return:
        """
        shell.help_cryptokey_create = MagicMock()
        shell.client.kickstart.keys.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        read_file = MagicMock(return_value="contents")
        prompt_user = MagicMock(side_effect=[])
        editor = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.cryptokey.prompt_user", prompt_user) as pmu, \
            patch("spacecmd.cryptokey.read_file", read_file) as rfl, \
            patch("spacecmd.cryptokey.editor", editor) as edt, \
            patch("spacecmd.cryptokey.logging", logger) as lgr:
            spacecmd.cryptokey.do_cryptokey_create(shell, "-t g -d description -f /tmp/file.txt")

        assert not editor.called
        assert not shell.help_cryptokey_create.called
        assert not prompt_user.called
        assert not logger.error.called

        assert shell.client.kickstart.keys.create.called
        assert read_file.called

        for call in shell.client.kickstart.keys.create.call_args_list:
            args, kw = call
            assert args == (shell.session, "description", "GPG", "contents")
            assert not kw

    def test_cryptokey_create_SSL_key(self, shell):
        """
        Test do_cryptokey_create with parameters, calling SSL key type.

        :param shell:
        :return:
        """
        shell.help_cryptokey_create = MagicMock()
        shell.client.kickstart.keys.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        read_file = MagicMock(return_value="contents")
        prompt_user = MagicMock(side_effect=[])
        editor = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.cryptokey.prompt_user", prompt_user) as pmu, \
            patch("spacecmd.cryptokey.read_file", read_file) as rfl, \
            patch("spacecmd.cryptokey.editor", editor) as edt, \
            patch("spacecmd.cryptokey.logging", logger) as lgr:
            spacecmd.cryptokey.do_cryptokey_create(shell, "-t s -d description -f /tmp/file.txt")

        assert not editor.called
        assert not shell.help_cryptokey_create.called
        assert not prompt_user.called
        assert not logger.error.called

        assert shell.client.kickstart.keys.create.called
        assert read_file.called

        for call in shell.client.kickstart.keys.create.call_args_list:
            args, kw = call
            assert args == (shell.session, "description", "SSL", "contents")
            assert not kw

    def test_cryptokey_delete_noargs(self, shell):
        """
        Test do_cryptokey_delete without parameters, so help should be displayed.

        :return:
        """
        shell.help_cryptokey_delete = MagicMock()
        shell.client.kickstart.keys.delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        shell.do_cryptokey_list = MagicMock()
        filter_results = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.cryptokey.logging", logger) as lgr, \
            patch("spacecmd.cryptokey.filter_results", filter_results) as frl:
            spacecmd.cryptokey.do_cryptokey_delete(shell, "")

        assert not logger.error.called
        assert not filter_results.called
        assert not shell.client.kickstart.keys.delete.called
        assert not shell.user_confirm.called
        assert shell.help_cryptokey_delete.called
