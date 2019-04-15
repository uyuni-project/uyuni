# coding: utf-8
"""
Test suite for custominfo source
"""
from mock import MagicMock, patch, mock_open
from spacecmd import custominfo
from helpers import shell
import pytest


class TestSCCusomInfo:
    """
    Test for custominfo API.
    """
    def test_do_custominfo_createkey_no_keyname(self, shell):
        """
        Test do_custominfo_createkey do not break on no key name provided, falling back to interactive mode.
        """
        shell.client.system.custominfo.createKey = MagicMock()
        prompter = MagicMock(side_effect=["", "", Exception("Empty key")])

        with patch("spacecmd.custominfo.prompt_user", prompter):
            with pytest.raises(Exception) as exc:
                custominfo.do_custominfo_createkey(shell, "")

        assert "Empty key" in str(exc)


    def test_do_custominfo_createkey_no_descr(self, shell):
        """
        Test do_custominfo_createkey description gets the name of the key, if not provided.
        """
        shell.client.system.custominfo.createKey = MagicMock()
        prompter = MagicMock(side_effect=["keyname", ""])

        with patch("spacecmd.custominfo.prompt_user", prompter):
            custominfo.do_custominfo_createkey(shell, "")

        assert shell.client.system.custominfo.createKey.called
        session, keyname, descr = shell.client.system.custominfo.createKey.call_args_list[0][0]
        assert shell.session == session
        assert keyname == descr

    def test_do_custominfo_createkey_descr_interactive(self, shell):
        """
        Test do_custominfo_createkey description gets the name of the key from the args.
        """
        shell.client.system.custominfo.createKey = MagicMock()
        prompter = MagicMock(side_effect=["keyname", "keydescr"])

        with patch("spacecmd.custominfo.prompt_user", prompter):
            custominfo.do_custominfo_createkey(shell, "")

        assert shell.client.system.custominfo.createKey.called
        session, keyname, descr = shell.client.system.custominfo.createKey.call_args_list[0][0]
        assert shell.session == session
        assert keyname != descr
        assert keyname == "keyname"
        assert descr == "keydescr"

