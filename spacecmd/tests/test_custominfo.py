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

    @patch("spacecmd.custominfo.print", MagicMock())
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

    def test_do_custominfo_details_no_key(self, shell):
        """
        Test do_custominfo_details shows error to the log if key name doesn't match.
        """
        shell.client.system.custominfo.listAllKeys = MagicMock()
        shell.do_custominfo_listkeys = MagicMock(return_value=["key_one", "key_two"])
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.custominfo.logging", logger):
            with patch("spacecmd.custominfo.print", mprint):
                custominfo.do_custominfo_details(shell, "keyname")

        assert not shell.client.system.custominfo.listAllKeys.called
        assert logger.debug.call_args_list[0][0][0] == "customkey_details called with args: 'keyname', keys: ''."
        assert logger.error.call_args_list[0][0][0] == "No keys matched argument 'keyname'."

    def test_do_custominfo_details_keydetails_notfound(self, shell):
        """
        Test do_custominfo_details nothing happens if keydetails missing.
        """
        shell.client.system.custominfo.listAllKeys = MagicMock()
        shell.do_custominfo_listkeys = MagicMock(return_value=["key_one", "key_two"])
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.custominfo.logging", logger):
            with patch("spacecmd.custominfo.print", mprint):
                custominfo.do_custominfo_details(shell, "key*")

        assert shell.client.system.custominfo.listAllKeys.called
        assert not mprint.called

    def test_do_custominfo_details_keydetails_na(self, shell):
        """
        Test do_custominfo_details prints key details not available in format.
        """
        shell.SEPARATOR = "***"
        shell.client.system.custominfo.listAllKeys = MagicMock(
            return_value=[
                {"label": "key_one"},
                {"label": "key_two"},
            ]
        )
        shell.do_custominfo_listkeys = MagicMock(return_value=["key_one", "key_two"])
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.custominfo.logging", logger):
            with patch("spacecmd.custominfo.print", mprint):
                custominfo.do_custominfo_details(shell, "key*")

        expectations = [
            'Label:        key_one',
            'Description:  N/A',
            'Modified:     N/A',
            'System Count: 0',
            '***',
            'Label:        key_two',
            'Description:  N/A',
            'Modified:     N/A',
            'System Count: 0'
        ]

        assert shell.client.system.custominfo.listAllKeys.called
        assert mprint.called
        for idx, call in enumerate(mprint.call_args_list):
            assert call[0][0] == expectations[idx]

    def test_do_custominfo_details_keydetails(self, shell):
        """
        Test do_custominfo_details prints key details not available in format.
        """
        shell.SEPARATOR = "***"
        shell.client.system.custominfo.listAllKeys = MagicMock(
            return_value=[
                {"label": "key_one", "description": "descr one", "last_modified": "123", "system_count": 1},
                {"label": "key_two", "description": "descr two", "last_modified": "234", "system_count": 2},
            ]
        )
        shell.do_custominfo_listkeys = MagicMock(return_value=["key_one", "key_two"])
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.custominfo.logging", logger):
            with patch("spacecmd.custominfo.print", mprint):
                custominfo.do_custominfo_details(shell, "key*")

        expectations = [
            'Label:        key_one',
            'Description:  descr one',
            'Modified:     123',
            'System Count: 1',
            '***',
            'Label:        key_two',
            'Description:  descr two',
            'Modified:     234',
            'System Count: 2'
        ]

        assert shell.client.system.custominfo.listAllKeys.called
        assert mprint.called
        for idx, call in enumerate(mprint.call_args_list):
            assert call[0][0] == expectations[idx]

    def test_custominfo_updatekey_noarg_name(self, shell):
        """
        Test do_custominfo_updatekey with no arguments falls to the interactive prompt.
        """
        shell.client.system.custominfo.updateKey = MagicMock()
        prompt = MagicMock(side_effect=Exception("interactive mode"))
        with patch("spacecmd.custominfo.prompt_user", prompt):
            with pytest.raises(Exception) as exc:
                custominfo.do_custominfo_updatekey(shell, "")

        assert "interactive mode" in str(exc)

    def test_custominfo_updatekey_noarg_descr(self, shell):
        """
        Test do_custominfo_updatekey with no arguments falls to the interactive prompt.
        """
        shell.client.system.custominfo.updateKey = MagicMock()
        prompt = MagicMock(side_effect=["keyname", Exception("interactive mode for descr")])
        with patch("spacecmd.custominfo.prompt_user", prompt):
            with pytest.raises(Exception) as exc:
                custominfo.do_custominfo_updatekey(shell, "")

        assert "interactive mode for descr" in str(exc)

    def test_custominfo_updatekey_keyonly_arg(self, shell):
        """
        Test do_custominfo_updatekey description is taken interactively.
        """
        shell.client.system.custominfo.updateKey = MagicMock()
        prompt = MagicMock(side_effect=[Exception("interactive mode for descr")])
        with patch("spacecmd.custominfo.prompt_user", prompt):
            with pytest.raises(Exception) as exc:
                custominfo.do_custominfo_updatekey(shell, "keyname")

        assert "interactive mode for descr" in str(exc)

    def test_custominfo_updatekey_all_args(self, shell):
        """
        Test do_custominfo_updatekey description is taken by arguments, interactive mode is not initiated.
        """
        shell.client.system.custominfo.updateKey = MagicMock()
        prompt = MagicMock(side_effect=[Exception("interactive mode for descr")])
        with patch("spacecmd.custominfo.prompt_user", prompt):
            custominfo.do_custominfo_updatekey(shell, "keyname 'some key description here'")

        assert shell.client.system.custominfo.updateKey.called
        session, keyname, description = shell.client.system.custominfo.updateKey.call_args_list[0][0]
        assert shell.session == session
        assert keyname == "keyname"
        assert description == "some key description here"
