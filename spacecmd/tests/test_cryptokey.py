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
    def test_cryptokey_create_noargs(self, shell):
        """
        Test do_cryptokey_create without arguments help is shown.

        :param shell:
        :return:
        """
        shell.help_cryptokey_create = MagicMock()
        shell.client.kickstart.keys.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        read_file = MagicMock()
        prompt_user = MagicMock(side_effect=["", "interactive descr", ""])
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

        assert_expect(logger.error.call_args_list, "Invalid key type")

