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
        assert not editor.called
        assert read_file.called
        assert prompt_user.called
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
        assert not editor.called
        assert prompt_user.called
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
        assert not editor.called
        assert read_file.called
        assert prompt_user.called
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
        assert not shell.do_cryptokey_list.called
        assert shell.help_cryptokey_delete.called

    def test_cryptokey_delete_no_exist(self, shell):
        """
        Test do_cryptokey_delete with non-existing key.

        :return:
        """
        shell.help_cryptokey_delete = MagicMock()
        shell.client.kickstart.keys.delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        shell.do_cryptokey_list = MagicMock(return_value=["one", "two", "three"])
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.cryptokey.logging", logger) as lgr, \
            patch("spacecmd.cryptokey.print", mprint) as prn:
            spacecmd.cryptokey.do_cryptokey_delete(shell, "foo*")

        assert not shell.client.kickstart.keys.delete.called
        assert not shell.help_cryptokey_delete.called
        assert not shell.user_confirm.called
        assert not mprint.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, "No keys matched argument ['foo.*']")

    def test_cryptokey_delete_not_confirmed(self, shell):
        """
        Test do_cryptokey_delete with non-existing key.

        :return:
        """
        shell.help_cryptokey_delete = MagicMock()
        shell.client.kickstart.keys.delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)
        shell.do_cryptokey_list = MagicMock(return_value=["one", "two", "three"])
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.cryptokey.logging", logger) as lgr, \
            patch("spacecmd.cryptokey.print", mprint) as prn:
            spacecmd.cryptokey.do_cryptokey_delete(shell, "t*")

        assert not shell.client.kickstart.keys.delete.called
        assert not shell.help_cryptokey_delete.called
        assert not logger.error.called
        assert shell.user_confirm.called
        assert mprint.called

        assert_expect(mprint.call_args_list, 'three\ntwo')

    def test_cryptokey_delete_confirmed_deleted(self, shell):
        """
        Test do_cryptokey_delete with non-existing key.

        :return:
        """
        shell.help_cryptokey_delete = MagicMock()
        shell.client.kickstart.keys.delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        shell.do_cryptokey_list = MagicMock(return_value=["one", "two", "three"])
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.cryptokey.logging", logger) as lgr, \
            patch("spacecmd.cryptokey.print", mprint) as prn:
            spacecmd.cryptokey.do_cryptokey_delete(shell, "t*")

        assert not logger.error.called
        assert not shell.help_cryptokey_delete.called
        assert shell.client.kickstart.keys.delete.called
        assert shell.user_confirm.called
        assert mprint.called

        assert_expect(mprint.call_args_list, 'three\ntwo')
        exp = [
            (shell.session, "two",),
            (shell.session, "three",),
        ]

        for call in shell.client.kickstart.keys.delete.call_args_list:
            args, kw = call
            assert not kw
            assert args == next(iter(exp))
            exp.pop(0)
        assert not exp

    def test_cryptokey_list_no_stdout(self, shell):
        """
        Test do_cryptokey_list no STDOUT.

        :param shell:
        :return:
        """
        shell.client.kickstart.keys.listAllKeys = MagicMock(
            return_value=[
                {"description": "keydescr-1"},
                {"description": "keydescr-2"},
            ]
        )
        mprint = MagicMock()
        with patch("spacecmd.cryptokey.print", mprint) as prn:
            out = spacecmd.cryptokey.do_cryptokey_list(shell, "", doreturn=True)

        assert not mprint.called
        assert bool(out)
        assert shell.client.kickstart.keys.listAllKeys.called
        assert out == ['keydescr-1', 'keydescr-2']

