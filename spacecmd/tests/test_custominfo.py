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
        Test do_custominfo_createkey description gets the name of the key from interactive prompt.
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

    def test_do_custominfo_createkey_descr_args(self, shell):
        """
        Test do_custominfo_createkey description gets the name of the key from the args.
        """
        shell.client.system.custominfo.createKey = MagicMock()
        prompter = MagicMock(side_effect=Exception("Kaboom"))

        custominfo.do_custominfo_createkey(shell, "keyname keydescr")

        assert shell.client.system.custominfo.createKey.called
        session, keyname, descr = shell.client.system.custominfo.createKey.call_args_list[0][0]
        assert shell.session == session
        assert keyname != descr
        assert keyname == "keyname"
        assert descr == "keydescr"

    def test_do_custominfo_deletekey_noargs(self, shell):
        """
        Test do_custominfo_deletekey shows help on no args.
        """
        errmsg = "No arguments passed"
        shell.client.system.custominfo.deleteKey = MagicMock()
        shell.do_custominfo_listkeys = MagicMock()
        shell.help_custominfo_deletekey = MagicMock(side_effect=Exception(errmsg))
        shell.user_confirm = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.custominfo.logging", logger):
            with pytest.raises(Exception) as exc:
                custominfo.do_custominfo_deletekey(shell, "")

        assert errmsg in str(exc)

    def test_do_custominfo_deletekey_args(self, shell):
        """
        Test do_custominfo_deletekey calls deleteKey API function.
        """
        keylist=["some_key", "some_other_key", "this_key_stays"]
        shell.client.system.custominfo.deleteKey = MagicMock()
        shell.do_custominfo_listkeys = MagicMock(return_value=keylist)
        shell.help_custominfo_deletekey = MagicMock(side_effect=Exception("Kaboom"))
        shell.user_confirm = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.custominfo.logging", logger):
            custominfo.do_custominfo_deletekey(shell, "some*")

        assert shell.client.system.custominfo.deleteKey.called
        for call in shell.client.system.custominfo.deleteKey.call_args_list:
            session, keyname = call[0]
            assert shell.session == session
            assert keyname in keylist
            keylist.pop(keylist.index(keyname))
        assert len(keylist) == 1
        assert "this_key_stays" in keylist

    def test_do_custominfo_listkeys_stdout(self, shell):
        """
        Test do_custominfo_listkeys calls lists all keys calling listAllKeys API function to STDOUT.
        """
        keylist=[
            {"label": "some_key"},
            {"label": "some_other_key"},
            {"label": "this_key_stays"},
        ]
        shell.client.system.custominfo.listAllKeys = MagicMock(return_value=keylist)
        mprint = MagicMock()
        with patch("spacecmd.custominfo.print", mprint):
            ret = custominfo.do_custominfo_listkeys(shell, "")

        assert ret is None
        assert mprint.called

    def test_do_custominfo_listkeys_as_data(self, shell):
        """
        Test do_custominfo_listkeys calls lists all keys calling listAllKeys API function as data.
        """
        keylist=[
            {"label": "some_key"},
            {"label": "some_other_key"},
            {"label": "this_key_stays"},
        ]
        shell.client.system.custominfo.listAllKeys = MagicMock(return_value=keylist)
        mprint = MagicMock()
        with patch("spacecmd.custominfo.print", mprint):
            ret = custominfo.do_custominfo_listkeys(shell, "", doreturn=True)

        assert not mprint.called
        assert isinstance(ret, list)
        for key in keylist:
            assert key["label"] in ret

    def test_do_custominfo_details_noarg(self, shell):
        """
        Test do_custominfo_details shows help when no arguments has been passed.
        """
        keylist=["some_key", "some_other_key", "this_key_stays"]
        shell.help_custominfo_details = MagicMock(side_effect=Exception("Help info"))
        shell.client.system.custominfo.listAllKeys = MagicMock()
        shell.do_custominfo_listkeys = MagicMock(return_value=keylist)
        logger = MagicMock()

        with patch("spacecmd.custominfo.logging", logger):
            with pytest.raises(Exception) as exc:
                custominfo.do_custominfo_details(shell, "")

        assert "Help info" in str(exc)
        assert not logger.debug.called
        assert not logger.error.called
        assert not shell.client.system.custominfo.listAllKeys.called
