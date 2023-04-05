# coding: utf-8
"""
Test activation key methods.
"""
from mock import MagicMock, patch
import pytest
import time
import hashlib
import spacecmd.activationkey
from xmlrpc import client as xmlrpclib
from helpers import shell, exc2str, assert_list_args_expect


class TestSCActivationKey:
    """
    Test activation key.
    """
    def test_completer_ak_addpackages(self, shell):
        """
        Test tab completer activation keys on addpackages.
        """
        text = "Communications satellite used by the military for star wars."
        completer = MagicMock()
        with patch("spacecmd.activationkey.tab_completer", completer):
            spacecmd.activationkey.complete_activationkey_addpackages(shell, text, "do this", None, None)
            assert completer.called
            call_id, ret_text = completer.call_args_list[0][0]
            assert call_id == "do_activation_list"
            assert ret_text == text


class TestSCActivationKeyMethods:
    """
    Test actuvation key methods.
    """
    def test_do_activationkey_addpackages_noargs(self, shell):
        """
        Test add packages method call shows help on no args.
        """
        shell.help_activationkey_addpackages = MagicMock()
        shell.client.activationkey.addPackages = MagicMock()

        spacecmd.activationkey.do_activationkey_addpackages(shell, "")
        assert shell.help_activationkey_addpackages.called

    def test_do_activationkey_addpackages_help_args(self, shell):
        """
        Test add packages method call shows help on help args passed.
        """
        shell.help_activationkey_addpackages = MagicMock()
        shell.client.activationkey.addPackages = MagicMock()

        spacecmd.activationkey.do_activationkey_addpackages(shell, "help")
        assert shell.help_activationkey_addpackages.called

    def test_do_activationkey_addpackages_args(self, shell):
        """
        Test add packages method call shows help on args passed.
        """
        shell.help_activationkey_addpackages = MagicMock()
        shell.client.activationkey.addPackages = MagicMock()

        spacecmd.activationkey.do_activationkey_addpackages(shell, "call something here")
        assert not shell.help_activationkey_addpackages.called
        assert shell.client.activationkey.addPackages.called
        session, fun, args = shell.client.activationkey.addPackages.call_args_list[0][0]
        assert session == shell.session
        assert fun == "call"
        assert isinstance(args, list)
        assert len(args) == 2
        for arg in args:
            assert arg["name"] in ["something", "here"]

    def test_do_activationkey_removepackages_noargs(self, shell):
        """
        Test remove packages method call shows help on no args.
        """
        shell.help_activationkey_removepackages = MagicMock()
        shell.client.activationkey.removePackages = MagicMock()

        # TODO: Add help for remove packages!
        spacecmd.activationkey.do_activationkey_removepackages(shell, "")
        assert not shell.help_activationkey_removePackages.called

    def test_do_activationkey_removepackages_help_args(self, shell):
        """
        Test remove packages method call shows help if only one argument is passed.
        """
        shell.help_activationkey_removepackages = MagicMock()
        shell.client.activationkey.removePackages = MagicMock()

        spacecmd.activationkey.do_activationkey_removepackages(shell, "key")
        assert shell.help_activationkey_removepackages.called

    def test_do_activationkey_removepackages_args(self, shell):
        """
        Test remove packages method calls "removePackages" API call.
        """
        shell.help_activationkey_removepackages = MagicMock()
        shell.client.activationkey.removePackages = MagicMock()

        spacecmd.activationkey.do_activationkey_removepackages(shell, "key package")
        assert not shell.help_activationkey_removepackages.called
        assert shell.client.activationkey.removePackages.called
        session, fun, args = shell.client.activationkey.removePackages.call_args_list[0][0]
        assert session == shell.session
        assert fun == "key"
        assert isinstance(args, list)
        assert len(args) == 1
        assert "name" in args[0]
        assert args[0]["name"] == "package"

    def test_do_activationkey_addgroups_noargs(self, shell):
        """
        Test addgroup without args calls help.
        """
        shell.help_activationkey_addgroups = MagicMock()
        shell.client.activationkey.addServerGroups = MagicMock()

        spacecmd.activationkey.do_activationkey_addgroups(shell, "")
        assert shell.help_activationkey_addgroups.called

    def test_do_activationkey_addgroups_help_args(self, shell):
        """
        Test add groups method call shows help if only one argument is passed.
        """
        shell.help_activationkey_addgroups = MagicMock()
        shell.client.activationkey.addServerGroups = MagicMock()

        spacecmd.activationkey.do_activationkey_addgroups(shell, "key")
        assert shell.help_activationkey_addgroups.called

    def test_do_activationkey_addgroups_args(self, shell):
        """
        Test "addgroups" method calls "addServerGroups" API call.
        """
        shell.help_activationkey_addgroups = MagicMock()
        shell.client.activationkey.addServerGroups = MagicMock()
        shell.client.systemgroup.getDetails = MagicMock(return_value={"id": 42})

        spacecmd.activationkey.do_activationkey_addgroups(shell, "key group")
        assert not shell.help_activationkey_addgroups.called
        assert shell.client.activationkey.addServerGroups.called
        session, fun, args = shell.client.activationkey.addServerGroups.call_args_list[0][0]
        assert session == shell.session
        assert fun == "key"
        assert isinstance(args, list)
        assert len(args) == 1
        assert args == [42]

    def test_do_activationkey_removegroups_noargs(self, shell):
        """
        Test removegroup without args calls help.
        """
        shell.help_activationkey_removegroups = MagicMock()
        shell.client.activationkey.removeServerGroups = MagicMock()

        spacecmd.activationkey.do_activationkey_removegroups(shell, "")
        assert shell.help_activationkey_removegroups.called

    def test_do_activationkey_removegroups_help_args(self, shell):
        """
        Test remove groups method call shows help if only one argument is passed.
        """
        shell.help_activationkey_removegroups = MagicMock()
        shell.client.activationkey.removeServerGroups = MagicMock()

        spacecmd.activationkey.do_activationkey_removegroups(shell, "key")
        assert shell.help_activationkey_removegroups.called
        assert not shell.client.activationkey.removeServerGroups.called

    def test_do_activationkey_removegroups_args(self, shell):
        """
        Test "removegroups" method calls "removeServerGroups" API call.
        """
        shell.help_activationkey_removegroups = MagicMock()
        shell.client.activationkey.removeServerGroups = MagicMock()
        shell.client.systemgroup.getDetails = MagicMock(return_value={"id": 42})

        spacecmd.activationkey.do_activationkey_removegroups(shell, "key group")
        assert not shell.help_activationkey_removegroups.called
        assert shell.client.activationkey.removeServerGroups.called
        session, fun, args = shell.client.activationkey.removeServerGroups.call_args_list[0][0]
        assert session == shell.session
        assert fun == "key"
        assert isinstance(args, list)
        assert len(args) == 1
        assert args == [42]

    def test_do_activationkey_addentitlements_noargs(self, shell):
        """
        Test addentitlements without args calls help.
        """
        shell.help_activationkey_addentitlements = MagicMock()
        shell.client.activationkey.addEntitlements = MagicMock()

        spacecmd.activationkey.do_activationkey_addentitlements(shell, "")
        assert shell.help_activationkey_addentitlements.called
        assert not shell.client.activationkey.addEntitlements.called

    def test_do_activationkey_addentitlements_help_args(self, shell):
        """
        Test addentitlements method call shows help if only one argument is passed.
        """
        shell.help_activationkey_addentitlements = MagicMock()
        shell.client.activationkey.addEntitlements = MagicMock()

        spacecmd.activationkey.do_activationkey_addentitlements(shell, "key")
        assert shell.help_activationkey_addentitlements.called
        assert not shell.client.activationkey.addEntitlements.called

    def test_do_activationkey_addentitlements_args(self, shell):
        """
        Test "addentitlements" method calls "addEntitlements" API call.
        """
        shell.help_activationkey_addentitlements = MagicMock()
        shell.client.activationkey.addEntitlements = MagicMock()

        spacecmd.activationkey.do_activationkey_addentitlements(shell, "key entitlement")
        assert not shell.help_activationkey_addentitlements.called
        assert shell.client.activationkey.addEntitlements.called
        session, fun, args = shell.client.activationkey.addEntitlements.call_args_list[0][0]
        assert session == shell.session
        assert fun == "key"
        assert isinstance(args, list)
        assert len(args) == 1
        assert args == ['entitlement']

    def test_do_activationkey_addentitlements_noargs(self, shell):
        """
        Test addentitlements without args calls help.
        """
        shell.help_activationkey_addentitlements = MagicMock()
        shell.client.activationkey.addEntitlements = MagicMock()

        spacecmd.activationkey.do_activationkey_addentitlements(shell, "")
        assert shell.help_activationkey_addentitlements.called
        assert not shell.client.activationkey.addEntitlements.called

    def test_do_activationkey_addentitlements_help_args(self, shell):
        """
        Test addentitlements method call shows help if only one argument is passed.
        """
        shell.help_activationkey_addentitlements = MagicMock()
        shell.client.activationkey.addEntitlements = MagicMock()

        spacecmd.activationkey.do_activationkey_addentitlements(shell, "key")
        assert shell.help_activationkey_addentitlements.called
        assert not shell.client.activationkey.addEntitlements.called

    def test_do_activationkey_addentitlements_args(self, shell):
        """
        Test "addentitlements" method calls "addEntitlements" API call.
        """
        shell.help_activationkey_addentitlements = MagicMock()
        shell.client.activationkey.addEntitlements = MagicMock()

        spacecmd.activationkey.do_activationkey_addentitlements(shell, "key entitlement")
        assert not shell.help_activationkey_addentitlements.called
        assert shell.client.activationkey.addEntitlements.called
        session, fun, args = shell.client.activationkey.addEntitlements.call_args_list[0][0]
        assert session == shell.session
        assert fun == "key"
        assert isinstance(args, list)
        assert len(args) == 1
        assert args == ['entitlement']

    def test_do_activationkey_removeentitlements_noargs(self, shell):
        """
        Test removeentitlements without args calls help.
        """
        shell.help_activationkey_removeentitlements = MagicMock()
        shell.client.activationkey.removeEntitlements = MagicMock()

        spacecmd.activationkey.do_activationkey_removeentitlements(shell, "")
        assert shell.help_activationkey_removeentitlements.called
        assert not shell.client.activationkey.removeEntitlements.called

    def test_do_activationkey_removeentitlements_help_args(self, shell):
        """
        Test removeentitlements method call shows help if only one argument is passed.
        """
        shell.help_activationkey_removeentitlements = MagicMock()
        shell.client.activationkey.removeEntitlements = MagicMock()

        spacecmd.activationkey.do_activationkey_removeentitlements(shell, "key")
        assert shell.help_activationkey_removeentitlements.called
        assert not shell.client.activationkey.removeEntitlements.called

    def test_do_activationkey_removeentitlements_args(self, shell):
        """
        Test "removeentitlements" method calls "removeEntitlements" API call.
        """
        shell.help_activationkey_removeentitlements = MagicMock()
        shell.client.activationkey.removeEntitlements = MagicMock()

        spacecmd.activationkey.do_activationkey_removeentitlements(shell, "key entitlement")
        assert not shell.help_activationkey_removeentitlements.called
        assert shell.client.activationkey.removeEntitlements.called
        session, fun, args = shell.client.activationkey.removeEntitlements.call_args_list[0][0]
        assert session == shell.session
        assert fun == "key"
        assert isinstance(args, list)
        assert len(args) == 1
        assert args == ['entitlement']

    def test_do_activationkey_addchildchannels_noargs(self, shell):
        """
        Test addchildchannels without args calls help.
        """
        shell.help_activationkey_addchildchannels = MagicMock()
        shell.client.activationkey.addChildChannels = MagicMock()

        spacecmd.activationkey.do_activationkey_addchildchannels(shell, "")
        assert shell.help_activationkey_addchildchannels.called
        assert not shell.client.activationkey.addChildChannels.called

    def test_do_activationkey_addchildchannels_help_args(self, shell):
        """
        Test addchildchannels method call shows help if only one argument is passed.
        """
        shell.help_activationkey_addchildchannels = MagicMock()
        shell.client.activationkey.addChildChannels = MagicMock()

        spacecmd.activationkey.do_activationkey_addchildchannels(shell, "key")
        assert shell.help_activationkey_addchildchannels.called
        assert not shell.client.activationkey.addChildChannels.called

    def test_do_activationkey_addchildchannels_args(self, shell):
        """
        Test "addchildchannels" method calls "addChildChannels" API call.
        """
        shell.help_activationkey_addchildchannels = MagicMock()
        shell.client.activationkey.addChildChannels = MagicMock()

        spacecmd.activationkey.do_activationkey_addchildchannels(shell, "key some_channel")
        assert not shell.help_activationkey_addchildchannels.called
        assert shell.client.activationkey.addChildChannels.called
        session, fun, args = shell.client.activationkey.addChildChannels.call_args_list[0][0]
        assert session == shell.session
        assert fun == "key"
        assert isinstance(args, list)
        assert len(args) == 1
        assert args == ['some_channel']

    def test_do_activationkey_removechildchannels_noargs(self, shell):
        """
        Test removechildchannels without args calls help.
        """
        shell.help_activationkey_removechildchannels = MagicMock()
        shell.client.activationkey.removeChildChannels = MagicMock()

        spacecmd.activationkey.do_activationkey_removechildchannels(shell, "")
        assert shell.help_activationkey_removechildchannels.called
        assert not shell.client.activationkey.removeChildChannels.called

    def test_do_activationkey_removechildchannels_help_args(self, shell):
        """
        Test removechildchannels method call shows help if only one argument is passed.
        """
        shell.help_activationkey_removechildchannels = MagicMock()
        shell.client.activationkey.removeChildChannels = MagicMock()

        spacecmd.activationkey.do_activationkey_removechildchannels(shell, "key")
        assert shell.help_activationkey_removechildchannels.called
        assert not shell.client.activationkey.removeChildChannels.called

    def test_do_activationkey_removechildchannels_args(self, shell):
        """
        Test "removechildchannels" method calls "removeChildChannels" API call.
        """
        shell.help_activationkey_removechildchannels = MagicMock()
        shell.client.activationkey.removeChildChannels = MagicMock()

        spacecmd.activationkey.do_activationkey_removechildchannels(shell, "key some_channel")
        assert not shell.help_activationkey_removechildchannels.called
        assert shell.client.activationkey.removeChildChannels.called
        session, fun, args = shell.client.activationkey.removeChildChannels.call_args_list[0][0]
        assert session == shell.session
        assert fun == "key"
        assert isinstance(args, list)
        assert len(args) == 1
        assert args == ['some_channel']

    def test_do_activationkey_listchildchannels_noargs(self, shell):
        """
        Test listchildchannels command triggers help on no args
        """
        shell.help_activationkey_listchildchannels = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(return_value={"child_channel_labels"})

        spacecmd.activationkey.do_activationkey_listchildchannels(shell, "")
        assert shell.help_activationkey_listchildchannels.called
        assert not shell.client.activationkey.getDetails.called

    def test_do_activationkey_listchildchannels_args(self, shell):
        """
        Test listchildchannels command prints child channels by the activation key passed.
        """
        shell.help_activationkey_listchildchannels = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(return_value={
            "child_channel_labels": ["one", "two", "three"]
        })

        mprint = MagicMock()
        with patch("spacecmd.activationkey.print", mprint):
            spacecmd.activationkey.do_activationkey_listchildchannels(shell, "key")
        assert mprint.call_args_list[0][0][0] == "one\nthree\ntwo"  # Sorted

    def test_do_activationkey_listbasechannel_noargs(self, shell):
        """
        Test listbasechannels command triggers help on no args
        """
        shell.help_activationkey_listbasechannel = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(return_value={"base_channel_label"})

        spacecmd.activationkey.do_activationkey_listbasechannel(shell, "")
        assert shell.help_activationkey_listbasechannel.called
        assert not shell.client.activationkey.getDetails.called

    def test_do_activationkey_listbasechannel_args(self, shell):
        """
        Test listbasechannels command prints base channel by the activation key passed.
        """
        shell.help_activationkey_listbasechannel = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(return_value={
            "base_channel_label": "Darth Vader",
        })

        mprint = MagicMock()
        with patch("spacecmd.activationkey.print", mprint):
            spacecmd.activationkey.do_activationkey_listbasechannel(shell, "key")
        assert mprint.call_args_list[0][0][0] == "Darth Vader"

    def test_do_activationkey_listgroups_noargs(self, shell):
        """
        Test listgroups command triggers help on no args
        """
        shell.help_activationkey_listgroups = MagicMock()
        shell.client.activationkey.getDetails = MagicMock()
        shell.client.systemgroup.getDetails = MagicMock(site_effect=[{"name": "RD-2D"}, {"name": "C-3PO"}])

        spacecmd.activationkey.do_activationkey_listgroups(shell, "")
        assert shell.help_activationkey_listgroups.called
        assert not shell.client.activationkey.getDetails.called

    def test_do_activationkey_listgroups_args(self, shell):
        """
        Test listgroups command prints groups by the activation key passed.
        """
        shell.help_activationkey_listgroups = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(return_value={"server_group_ids": [2, 3]})
        shell.client.systemgroup.getDetails = MagicMock(side_effect=[{"name": "RD-2D"}, {"name": "C-3PO"}])

        mprint = MagicMock()
        with patch("spacecmd.activationkey.print", mprint):
            spacecmd.activationkey.do_activationkey_listgroups(shell, "key")
        assert len(mprint.call_args_list) == 2
        assert mprint.call_args_list[0][0][0] == "RD-2D"
        assert mprint.call_args_list[1][0][0] == "C-3PO"

    def test_do_activationkey_listentitlements_noargs(self, shell):
        """
        Test listentitlements command triggers help on no args
        """
        shell.help_activationkey_listentitlements = MagicMock()
        shell.client.activationkey.getDetails = MagicMock()

        spacecmd.activationkey.do_activationkey_listentitlements(shell, "")
        assert shell.help_activationkey_listentitlements.called
        assert not shell.client.activationkey.getDetails.called

    def test_do_activationkey_listentitlements_args(self, shell):
        """
        Test listentitlements command prints entitlements by the activation key passed.
        """
        shell.help_activationkey_listentitlements = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(return_value={"entitlements": ["one", "two", "three"]})

        mprint = MagicMock()
        with patch("spacecmd.activationkey.print", mprint):
            spacecmd.activationkey.do_activationkey_listentitlements(shell, "key")
        assert mprint.call_args_list[0][0][0] == 'one\ntwo\nthree'

    def test_do_activationkey_listpackages_noargs(self, shell):
        """
        Test listpackages command triggers help on no args
        """
        shell.help_activationkey_listpackages = MagicMock()
        shell.client.activationkey.getDetails = MagicMock()

        spacecmd.activationkey.do_activationkey_listpackages(shell, "")
        assert shell.help_activationkey_listpackages.called
        assert not shell.client.activationkey.getDetails.called

    def test_do_activationkey_listpackages_args_arch(self, shell):
        """
        Test listpackages command prints packages by the activation key passed with the arch included.
        """
        shell.help_activationkey_listpackages = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(
            return_value={"packages": [
                {"name": "libzypp", "arch": "ZX80"},
                {"name": "java-11-openjdk-devel", "arch": "CBM64"},
            ]}
        )

        mprint = MagicMock()
        with patch("spacecmd.activationkey.print", mprint):
            spacecmd.activationkey.do_activationkey_listpackages(shell, "key")
        assert mprint.called
        assert len(mprint.call_args_list) == 2
        # keep ordering
        assert mprint.call_args_list[0][0][0] == "libzypp.ZX80"
        assert mprint.call_args_list[1][0][0] == "java-11-openjdk-devel.CBM64"

    def test_do_activationkey_listpackages_args_noarch(self, shell):
        """
        Test listpackages command prints packages by the activation key passed without architecture included.
        """
        shell.help_activationkey_listpackages = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(
            return_value={"packages": [
                {"name": "libzypp"},
                {"name": "java-11-openjdk-devel"},
            ]}
        )

        mprint = MagicMock()
        with patch("spacecmd.activationkey.print", mprint):
            spacecmd.activationkey.do_activationkey_listpackages(shell, "key")
        assert mprint.called
        assert len(mprint.call_args_list) == 2
        # keep ordering
        assert mprint.call_args_list[0][0][0] == "libzypp"
        assert mprint.call_args_list[1][0][0] == "java-11-openjdk-devel"

    def test_do_activationkey_listconfigchannels_noargs(self, shell):
        """
        Test listconfigchannels command triggers help on no args
        """
        shell.help_activationkey_listconfigchannels = MagicMock()
        shell.client.activationkey.listConfigChannels = MagicMock()

        spacecmd.activationkey.do_activationkey_listconfigchannels(shell, "")
        assert shell.help_activationkey_listconfigchannels.called
        assert not shell.client.activationkey.listConfigChannels.called

    def test_do_activationkey_listconfigchannels_args(self, shell):
        """
        Test listconfigchannels command prints entitlements by the activation key passed.
        """
        channels = [
            {"label": "commodore64"},
            {"label": "pascal_for_msdos"},
            {"label": "lightsaber_patches"}
        ]
        shell.help_activationkey_listconfigchannels = MagicMock()
        shell.client.activationkey.listConfigChannels = MagicMock(return_value=channels)

        mprint = MagicMock()
        with patch("spacecmd.activationkey.print", mprint):
            spacecmd.activationkey.do_activationkey_listconfigchannels(shell, "key")
        assert mprint.called
        assert mprint.call_args_list[0][0][0] == 'commodore64\nlightsaber_patches\npascal_for_msdos'

    def test_do_activationkey_addconfigchannels_noargs(self, shell):
        """
        Test addconfigchannels command triggers help on no args.
        """
        shell.help_activationkey_addconfigchannels = MagicMock()
        shell.client.activationkey.addConfigChannels = MagicMock()

        spacecmd.activationkey.do_activationkey_addconfigchannels(shell, "")
        assert shell.help_activationkey_addconfigchannels.called
        assert not shell.client.activationkey.addConfigChannels.called

    def test_do_activationkey_addconfigchannels_unknown_noargs(self, shell):
        """
        Test addconfigchannels command raises an Exception on unknown passed args.
        """
        shell.help_activationkey_addconfigchannels = MagicMock()
        shell.client.activationkey.addConfigChannels = MagicMock()

        with pytest.raises(Exception) as exc:
            spacecmd.activationkey.do_activationkey_addconfigchannels(shell, "--you-shall-not-pass=True")

        assert "unrecognized arguments" in exc2str(exc)
        assert not shell.help_activationkey_addconfigchannels.called
        assert not shell.client.activationkey.addConfigChannels.called

    @patch("spacecmd.activationkey.is_interactive", MagicMock(return_value=False))
    def test_do_activationkey_addconfigchannels_check_args_noninteractive(self, shell):
        """
        Test addconfigchannels command calls addConfigChannels API function on params added.
        """
        shell.help_activationkey_addconfigchannels = MagicMock()
        shell.client.activationkey.addConfigChannels = MagicMock()

        spacecmd.activationkey.do_activationkey_addconfigchannels(shell, "key rd2d-upgrade -b")

        assert not shell.help_activationkey_addconfigchannels.called
        assert shell.client.activationkey.addConfigChannels.called

        session, keys, channels, order = shell.client.activationkey.addConfigChannels.call_args_list[0][0]
        assert shell.session == session
        assert len(keys) == len(channels) == 1
        assert "key" in keys
        assert "rd2d-upgrade" in channels
        assert bool == type(order)
        assert not order

        shell.client.activationkey.addConfigChannels = MagicMock()
        spacecmd.activationkey.do_activationkey_addconfigchannels(shell, "key rd2d-upgrade")
        session, keys, channels, order = shell.client.activationkey.addConfigChannels.call_args_list[0][0]
        assert shell.session == session
        assert len(keys) == len(channels) == 1
        assert "key" in keys
        assert "rd2d-upgrade" in channels
        assert bool == type(order)
        assert order

        shell.client.activationkey.addConfigChannels = MagicMock()
        spacecmd.activationkey.do_activationkey_addconfigchannels(shell, "key rd2d-upgrade -t")
        session, keys, channels, order = shell.client.activationkey.addConfigChannels.call_args_list[0][0]
        assert shell.session == session
        assert len(keys) == len(channels) == 1
        assert "key" in keys
        assert "rd2d-upgrade" in channels
        assert bool == type(order)
        assert order

    def test_do_activationkey_removeconfigchannels_noargs(self, shell):
        """
        Test removeconfigchannels command triggers help on no args.
        """
        shell.help_activationkey_removeconfigchannels = MagicMock()
        shell.client.activationkey.removeConfigChannels = MagicMock()

        spacecmd.activationkey.do_activationkey_removeconfigchannels(shell, "")
        assert shell.help_activationkey_removeconfigchannels.called
        assert not shell.client.activationkey.removeConfigChannels.called
    
    def test_do_activationkey_removeconfigchannels_insuff_args(self, shell):
        """
        Test removeconfigchannels command triggers help on insufficient args.
        """
        shell.help_activationkey_removeconfigchannels = MagicMock()
        shell.client.activationkey.removeConfigChannels = MagicMock()

        spacecmd.activationkey.do_activationkey_removeconfigchannels(shell, "key")
        assert shell.help_activationkey_removeconfigchannels.called
        assert not shell.client.activationkey.removeConfigChannels.called

    def test_do_activationkey_removeconfigchannels_args(self, shell):
        """
        Test removeconfigchannels command is calling removeConfigChannels API by the activation key passed.
        """
        shell.help_activationkey_removeconfigchannels = MagicMock()
        shell.client.activationkey.removeConfigChannels = MagicMock()

        mprint = MagicMock()
        spacecmd.activationkey.do_activationkey_removeconfigchannels(shell, "key some_patches")
        assert not shell.help_activationkey_removeconfigchannels.called
        assert shell.client.activationkey.removeConfigChannels.called

        session, keys, channels = shell.client.activationkey.removeConfigChannels.call_args_list[0][0]
        assert shell.session == session
        assert "key" in keys
        assert "some_patches" in channels
        assert len(keys) == len(channels) == 1

    @patch("spacecmd.activationkey.config_channel_order",
           MagicMock(return_value=["lightsaber_patches", "rd2d_upgrade"]))
    def test_do_activationkey_setconfigchannelorder_noargs(self, shell):
        """
        Test setconfigchannelorder command triggers help on no args.
        """
        for cmd in [""]:
            shell.help_activationkey_setconfigchannelorder = MagicMock()
            shell.client.activationkey.listConfigChannels = MagicMock()
            shell.client.activationkey.setConfigChannels = MagicMock()
            shell.do_configchannel_list = MagicMock()

            spacecmd.activationkey.do_activationkey_setconfigchannelorder(shell, cmd)
            assert shell.help_activationkey_setconfigchannelorder.called
            assert not shell.client.activationkey.setConfigChannels.called

    @patch("spacecmd.activationkey.config_channel_order",
           MagicMock(return_value=["lightsaber_patches", "rd2d_upgrade"]))
    def test_do_activationkey_setconfigchannelorder_args(self, shell):
        """
        Test setconfigchannelorder command triggers setConfigChannels API call with proper function
        """
        shell.help_activationkey_setconfigchannelorder = MagicMock()
        shell.client.activationkey.listConfigChannels = MagicMock()
        shell.client.activationkey.setConfigChannels = MagicMock()
        shell.do_configchannel_list = MagicMock()

        mprint = MagicMock()
        with patch("spacecmd.activationkey.print", mprint):
            spacecmd.activationkey.do_activationkey_setconfigchannelorder(shell, "key")
        assert not shell.help_activationkey_setconfigchannelorder.called
        assert shell.client.activationkey.setConfigChannels.called
        assert len(mprint.call_args_list) == 4
        assert mprint.call_args_list[2][0][0] == "[1] lightsaber_patches"
        assert mprint.call_args_list[3][0][0] == "[2] rd2d_upgrade"

    @patch("spacecmd.activationkey.is_interactive", MagicMock(return_value=False))
    def test_do_activationkey_create_nointeract_argstest(self, shell):
        """
        Test call activation key API "create".
        """
        shell.client.activationkey.create = MagicMock(return_value="superglue")
        shell.list_base_channels = MagicMock(return_value=["lightsaber_patches_sle42sp8"])

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            spacecmd.activationkey.do_activationkey_create(shell, "")

        assert logger.info.called
        assert shell.client.activationkey.create.called
        assert logger.info.call_args_list[0][0][0] == "Created activation key superglue"

        session, name, descr, bch, entl, universal = shell.client.activationkey.create.call_args_list[0][0]
        assert shell.session == session
        assert name == descr == bch == ""
        assert entl == []
        assert not universal

        shell.client.activationkey.create = MagicMock(return_value="woodblock")
        shell.list_base_channels = MagicMock(return_value=["lightsaber_patches_sle42sp8"])

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            spacecmd.activationkey.do_activationkey_create(
                shell, ("--name lightsaber --description 'The signature weapon of the Jedi Order' "
                        "--base-channel lightsaber_patches_sle42sp8 --entitlements expanded,universe "
                        "--universal"))

        assert logger.info.called
        assert shell.client.activationkey.create.called
        assert logger.info.call_args_list[0][0][0] == "Created activation key woodblock"

        session, name, descr, bch, entl, universal = shell.client.activationkey.create.call_args_list[0][0]
        assert shell.session == session
        assert name == "lightsaber"
        assert "Jedi Order" in descr
        assert bch == "lightsaber_patches_sle42sp8"
        assert entl == ["expanded", "universe"]
        assert universal

    def test_do_activationkey_activationkey_delete_insuff_args(self, shell):
        """
        Test activationkey_delete command triggers help on insufficient args.
        """
        shell.help_activationkey_delete = MagicMock()
        shell.client.activationkey.delete = MagicMock()

        spacecmd.activationkey.do_activationkey_delete(shell, "")
        assert shell.help_activationkey_delete.called
        assert not shell.client.activationkey.delete.called

    @patch("spacecmd.activationkey.filter_results", MagicMock(return_value=["some_patches", "some_stuff"]))
    def test_do_activationkey_activationkey_delete_args(self, shell):
        """
        Test activationkey_delete command is calling "delete" (key) API.
        """
        shell.help_activationkey_delete = MagicMock()
        shell.client.activationkey.delete = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.activationkey.print", mprint) as mpr, \
             patch("spacecmd.activationkey.logging", logger) as lgr:
            spacecmd.activationkey.do_activationkey_delete(shell, "key some*")
            assert not shell.help_activationkey_delete.called

        assert not logger.error.called
        assert logger.debug.called
        assert logger.debug.call_args_list[0][0][0] == ("activationkey_delete called with args"
                                                        " ['key', 'some.*'], keys=['some_patches', 'some_stuff']")
        assert logger.debug.call_args_list[1][0][0] == "Deleting key some_patches"
        assert logger.debug.call_args_list[2][0][0] == "Deleting key some_stuff"

        assert shell.client.activationkey.delete.called
        keys = ["some_stuff", "some_patches"]
        for call in shell.client.activationkey.delete.call_args_list:
            session, keyname = call[0]
            assert shell.session == session
            assert keyname in keys
            keys.pop(keys.index(keyname))
        assert not keys

    @patch("spacecmd.activationkey.filter_results", MagicMock(return_value=["some_patches", "some_stuff"]))
    def test_do_activationkey_activationkey_list_args(self, shell):
        """
        Test activationkey_list command is calling listActivationKeys API.
        """
        shell.client.activationkey.listActivationKeys = MagicMock(
            return_value=[
                {"key": "some_patches", "description": "Some key"},
                {"key": "some_stuff", "description": "Some other key"},
                {"key": "some_reactivation", "description": "Kickstart re-activation key"},
            ]
        )

        mprint = MagicMock()
        with patch("spacecmd.activationkey.print", mprint) as mpr:
            ret = sorted(spacecmd.activationkey.do_activationkey_list(shell, "key some*", doreturn=True))
        assert len(ret) == 2
        assert ret == ['some_patches', 'some_stuff']

    def test_do_activationkey_listsystems_noargs(self, shell):
        """
        Test activationkey_listsystems command is invoking help message on insufficient arguments.
        """
        shell.help_activationkey_listsystems = MagicMock()
        shell.client.activationkey.listActivatedSystems = MagicMock()

        spacecmd.activationkey.do_activationkey_listsystems(shell, "")
        assert shell.help_activationkey_listsystems.called
        assert not shell.client.activationkey.listActivatedSystems.called

    def test_do_activationkey_listsystems_args(self, shell):
        """
        Test activationkey_listsystems command is calling listActivatedSystems API function.
        """
        shell.help_activationkey_listsystems = MagicMock()
        shell.client.activationkey.listActivatedSystems = MagicMock(
            return_value=[
                {"hostname": "chair.lan"},
                {"hostname": "houseshoe.lan"},
            ]
        )

        mprint = MagicMock()
        with patch("spacecmd.activationkey.print", mprint) as mpr:
            spacecmd.activationkey.do_activationkey_listsystems(shell, "key")
        assert not shell.help_activationkey_listsystems.called
        assert shell.client.activationkey.listActivatedSystems.called
        assert mprint.called
        assert mprint.call_args_list[0][0][0] == 'chair.lan\nhouseshoe.lan'

    def test_do_activationkey_details_noargs(self, shell):
        """
        Test activationkey_details shows a help screen if no sufficient arguments has been passed.
        """
        shell.help_activationkey_details = MagicMock()
        shell.client.activationkey.getDetails = MagicMock()
        shell.client.activationkey.listConfigChannels = MagicMock()
        shell.client.activationkey.checkConfigDeployment = MagicMock()
        shell.client.systemgroup.getDetails = MagicMock()

        spacecmd.activationkey.do_activationkey_listsystems(shell, "")
        assert shell.help_activationkey_listsystems.called
        assert not shell.client.activationkey.getDetails.called
        assert not shell.client.systemgroup.getDetails.called
        assert not shell.client.activationkey.listConfigChannels.called
        assert not shell.client.activationkey.checkConfigDeployment.called

    def test_do_activationkey_details_args(self, shell):
        """
        Test activationkey_details returns key details if proper arguments passed.
        """
        shell.help_activationkey_details = MagicMock()

        shell.client.activationkey.getDetails = MagicMock(
            return_value={
                "key": "somekey",
                "description": "Key description",
                "universal_default": "yes",
                "usage_limit": "42",
                "contact_method": "230V/AC",
                "server_group_ids": ["a", "b", "c"],
                "child_channel_labels": ["child_channel_1", "child_channel_2"],
                "entitlements": ["entitlement_1", "entitlement_2"],
                "packages": [{"name": "emacs"}],
                "base_channel_label": "base_channel",
            }
        )
        shell.client.activationkey.listConfigChannels = MagicMock(
            return_value=[
                {"label": "somekey_config_channel_1"},
                {"label": "somekey_config_channel_2"},
            ]
        )
        shell.client.activationkey.checkConfigDeployment = MagicMock(return_value=0)
        shell.client.systemgroup.getDetails = MagicMock(
            side_effect=[
                {"name": "group_a"},
                {"name": "group_b"},
                {"name": "group_c"},
            ]
        )

        mprint = MagicMock()
        with patch("spacecmd.activationkey.print", mprint) as mpr:        
            out = spacecmd.activationkey.do_activationkey_details(shell, "somekey")
        assert mprint.called

        expectation = ['Key:                    somekey',
                       'Description:            Key description', 'Universal Default:      yes',
                       'Usage Limit:            42', 'Deploy Config Channels: False',
                       'Contact Method:         230V/AC', '', 'Software Channels', '-----------------',
                       'base_channel', ' |-- child_channel_1', ' |-- child_channel_2', '',
                       'Configuration Channels', '----------------------', 'somekey_config_channel_1',
                       'somekey_config_channel_2', '', 'Entitlements', '------------',
                       'entitlement_1\nentitlement_2', '', 'System Groups', '-------------',
                       'group_a\ngroup_b\ngroup_c', '', 'Packages', '--------', 'emacs']
        assert len(out) == len(expectation)
        for idx, line in enumerate(out):
            assert line == expectation[idx]
    
    def test_do_activationkey_enableconfigdeployment_noargs(self, shell):
        """
        Test activationkey_enableconfigdeployment command is invoking help message on insufficient arguments.
        """
        shell.help_activationkey_enableconfigdeployment = MagicMock()
        shell.client.activationkey.enableConfigDeployment = MagicMock()

        spacecmd.activationkey.do_activationkey_enableconfigdeployment(shell, "")
        assert shell.help_activationkey_enableconfigdeployment.called
        assert not shell.client.activationkey.enableConfigDeployment.called

    def test_do_activationkey_enableconfigdeployment_args(self, shell):
        """
        Test activationkey_enableconfigdeployment command is invoking enableConfigDeployment API call.
        """
        shell.help_activationkey_enableconfigdeployment = MagicMock()
        shell.client.activationkey.enableConfigDeployment = MagicMock()

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            spacecmd.activationkey.do_activationkey_enableconfigdeployment(shell, "foo bar baz")

        assert logger.debug.called
        assert logger.debug.call_args_list[0][0][0] == "Enabling config file deployment for foo"
        assert logger.debug.call_args_list[1][0][0] == "Enabling config file deployment for bar"
        assert logger.debug.call_args_list[2][0][0] == "Enabling config file deployment for baz"

        keynames = ["foo", "bar", "baz"]
        assert shell.client.activationkey.enableConfigDeployment.called
        for call in shell.client.activationkey.enableConfigDeployment.call_args_list:
            session, keyname = call[0]
            assert shell.session == session
            assert keyname in keynames
            keynames.pop(keynames.index(keyname))
        assert keynames == []

    def test_do_activationkey_disableconfigdeployment_noargs(self, shell):
        """
        Test activationkey_disableconfigdeployment command is invoking help message on insufficient arguments.
        """
        shell.help_activationkey_disableconfigdeployment = MagicMock()
        shell.client.activationkey.disableConfigDeployment = MagicMock()

        spacecmd.activationkey.do_activationkey_disableconfigdeployment(shell, "")
        assert shell.help_activationkey_disableconfigdeployment.called
        assert not shell.client.activationkey.disableConfigDeployment.called

    def test_do_activationkey_disableconfigdeployment_args(self, shell):
        """
        Test activationkey_disableconfigdeployment command is invoking disableConfigDeployment API call.
        """
        shell.help_activationkey_disableconfigdeployment = MagicMock()
        shell.client.activationkey.disableConfigDeployment = MagicMock()

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            spacecmd.activationkey.do_activationkey_disableconfigdeployment(shell, "foo bar baz")

        assert logger.debug.called
        assert logger.debug.call_args_list[0][0][0] == "Disabling config file deployment for foo"
        assert logger.debug.call_args_list[1][0][0] == "Disabling config file deployment for bar"
        assert logger.debug.call_args_list[2][0][0] == "Disabling config file deployment for baz"

        keynames = ["foo", "bar", "baz"]
        assert shell.client.activationkey.disableConfigDeployment.called
        for call in shell.client.activationkey.disableConfigDeployment.call_args_list:
            session, keyname = call[0]
            assert shell.session == session
            assert keyname in keynames
            keynames.pop(keynames.index(keyname))
        assert keynames == []

    def test_do_activationkey_addconfigchannels_setbasechannel_noargs(self, shell):
        """
        Test do_activationkey_addconfigchannels_setbasechannel involes help message on issuficient arguments.
        """
        shell.help_activationkey_setbasechannel = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()
        shell.client.activationkey.getDetails = MagicMock()

        spacecmd.activationkey.do_activationkey_setbasechannel(shell, "")
        assert shell.help_activationkey_setbasechannel.called
        assert not shell.client.activationkey.setDetails.called
        assert not shell.client.activationkey.getDetails.called

        spacecmd.activationkey.do_activationkey_setbasechannel(shell, "key")
        assert shell.help_activationkey_setbasechannel.called
        assert not shell.client.activationkey.setDetails.called
        assert not shell.client.activationkey.getDetails.called

    def test_do_activationkey_addconfigchannels_setbasechannel_args(self, shell):
        """
        Test do_activationkey_addconfigchannels_setbasechannel calls setDetails API call on proper args.
        """
        key_details = {
            "base_channel_label": "death_star_channel",
            "description": "Darth Vader's base channel",
            "usage_limit": 42,
            "universal_default": True,
        }
        shell.help_activationkey_setbasechannel = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(return_value=key_details)

        spacecmd.activationkey.do_activationkey_setbasechannel(shell, "red_key death_star_patches_channel")
        session, keyname, details = shell.client.activationkey.setDetails.call_args_list[0][0]
        assert shell.session == session
        assert keyname == "red_key"

        for dkey in ["description", "usage_limit", "universal_default"]:
            assert dkey in details
            assert key_details[dkey] == details[dkey]
        assert details["base_channel_label"] == "death_star_patches_channel"

    def test_do_activationkey_addconfigchannels_setbasechannel_usage_limit(self, shell):
        """
        Test do_activationkey_addconfigchannels_setbasechannel resets usage limit from 0 to -1.
        """
        key_details = {
            "base_channel_label": "death_star_channel",
            "description": "Darth Vader's base channel",
            "usage_limit": 0,
            "universal_default": True,
        }
        shell.help_activationkey_setbasechannel = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(return_value=key_details)

        spacecmd.activationkey.do_activationkey_setbasechannel(shell, "red_key death_star_patches_channel")
        session, keyname, details = shell.client.activationkey.setDetails.call_args_list[0][0]
        assert shell.session == session
        assert keyname == "red_key"

        for dkey in ["description", "universal_default"]:
            assert dkey in details
            assert key_details[dkey] == details[dkey]
        assert details["base_channel_label"] == "death_star_patches_channel"
        assert details["usage_limit"] == -1

    def test_do_activationkey_addconfigchannels_setusagelimit_noargs(self, shell):
        """
        Test do_activationkey_addconfigchannels_setusagelimit involes help message on issuficient arguments.
        """
        shell.help_activationkey_setusagelimit = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()
        shell.client.activationkey.getDetails = MagicMock()

        spacecmd.activationkey.do_activationkey_setusagelimit(shell, "")
        assert shell.help_activationkey_setusagelimit.called
        assert not shell.client.activationkey.setDetails.called
        assert not shell.client.activationkey.getDetails.called

        spacecmd.activationkey.do_activationkey_setusagelimit(shell, "key")
        assert shell.help_activationkey_setusagelimit.called
        assert not shell.client.activationkey.setDetails.called
        assert not shell.client.activationkey.getDetails.called

    def test_do_activationkey_addconfigchannels_setusagelimit_args(self, shell):
        """
        Test do_activationkey_addconfigchannels_setbasechannel resets usage limit from 0 to -1.
        """
        key_details = {
            "base_channel_label": "death_star_channel",
            "description": "Darth Vader's base channel",
            "usage_limit": 0,
            "universal_default": True,
        }
        shell.help_activationkey_setusagelimit = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(return_value=key_details)

        spacecmd.activationkey.do_activationkey_setusagelimit(shell, "red_key 42")
        session, keyname, details = shell.client.activationkey.setDetails.call_args_list[0][0]
        assert shell.session == session
        assert keyname == "red_key"

        for dkey in ["description", "universal_default", "base_channel_label"]:
            assert dkey in details
            assert key_details[dkey] == details[dkey]
        assert type(details["usage_limit"]) == int
        assert details["usage_limit"] == 42

    def test_do_activationkey_addconfigchannels_setuniversaldefault_noargs(self, shell):
        """
        Test do_activationkey_addconfigchannels_setuniversaldefault involes help message on issuficient arguments.
        """
        shell.help_activationkey_setuniversaldefault = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()
        shell.client.activationkey.getDetails = MagicMock()

        spacecmd.activationkey.do_activationkey_setuniversaldefault(shell, "")
        assert shell.help_activationkey_setuniversaldefault.called
        assert not shell.client.activationkey.setDetails.called
        assert not shell.client.activationkey.getDetails.called

    def test_do_activationkey_addconfigchannels_setuniversaldefault_args(self, shell):
        """
        Test do_activationkey_addconfigchannels_setuniversaldefault calls API to set the key settings.
        """
        key_details = {
            "base_channel_label": "death_star_channel",
            "description": "Darth Vader's base channel",
            "usage_limit": 42,
            "universal_default": False,
        }
        shell.help_activationkey_setuniversaldefault = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(return_value=key_details)

        spacecmd.activationkey.do_activationkey_setuniversaldefault(shell, "red_key 42")
        session, keyname, details = shell.client.activationkey.setDetails.call_args_list[0][0]
        assert shell.session == session
        assert keyname == "red_key"

        for dkey in ["description", "usage_limit", "base_channel_label"]:
            assert dkey in details
            assert key_details[dkey] == details[dkey]
        assert type(details["universal_default"]) == bool
        assert details["universal_default"]

    def test_export_activationkey_getdetails_exception_handling(self, shell):
        """
        Test export_activationkey_getdetails XMLRPC failure.
        """
    
        key_details = {
            "server_group_ids": [],
        }
        logger = MagicMock()
        shell.client.activationkey.getDetails = MagicMock()
        shell.client.activationkey.listConfigChannels = MagicMock(side_effect=xmlrpclib.Fault("boom", "box"))
        shell.client.activationkey.checkConfigDeployment = MagicMock()
        shell.client.systemgroup.getDetails = MagicMock(return_value=key_details)

        with patch("spacecmd.activationkey.logging", logger):
            spacecmd.activationkey.export_activationkey_getdetails(shell, "")
        assert logger.debug.call_args_list[1][0][0] == ("activationkey.listConfigChannel threw an exeception, "
                                                        "setting config_channels=False")
        
    def test_export_activationkey_getdetails(self, shell):
        """
        Test export_activationkey_getdetails.
        """

        key_details = {
            "server_group_ids": [1, 2, 3],
        }

        gr_details = [
            {"name": "first"},
            {"name": "second"},
            {"name": "third"},
        ]
        logger = MagicMock()
        shell.client.activationkey.getDetails = MagicMock(return_value=key_details)
        shell.client.activationkey.listConfigChannels = MagicMock(
            return_value=[
                {"label": "rd2d_patches_channel"},
                {"label": "k_2so_firmware_channel"},
            ]
        )
        shell.client.activationkey.checkConfigDeployment = MagicMock(return_value=True)
        shell.client.systemgroup.getDetails = MagicMock(side_effect=gr_details)

        with patch("spacecmd.activationkey.logging", logger):
            out = spacecmd.activationkey.export_activationkey_getdetails(shell, "red_key")

        {
            'server_group_ids': [1, 2, 3],
            'config_channels': ['rd2d_patches_channel',
                                'k_2so_firmware_channel'],
            'config_deploy': True,
            'server_groups': ['first', 'second', 'third']
        }

        assert "server_group_ids" in out
        assert out["server_group_ids"] == [1, 2, 3]
        assert "config_channels" in out
        for cfg_channel in ['rd2d_patches_channel',
                            'k_2so_firmware_channel']:
            assert cfg_channel in out["config_channels"]
        assert "config_deploy" in out
        assert out["config_deploy"]
        assert "server_groups" in out
        assert len(out["server_groups"]) == 3
        for srv_group in ['first', 'second', 'third']:
            assert srv_group in out["server_groups"]

    @patch("spacecmd.activationkey.json_dump_to_file", MagicMock())
    @patch("spacecmd.activationkey.os.path.isfile", MagicMock(return_value=False))
    def test_do_activationkey_export_noarg(self, shell):
        """
        Test do_activationkey_export exports to 'akey_all.json' if not specified otherwise.
        """
        logger = MagicMock()
        shell.do_activationkey_list = MagicMock(return_value=["one", "two", "three"])
        shell.export_activationkey_getdetails = MagicMock(side_effect=[{}, {}, {}])
        shell.client.activationkey.checkConfigDeployment = MagicMock(return_value=True)

        with patch("spacecmd.activationkey.logging", logger):
            spacecmd.activationkey.do_activationkey_export(shell, "")

        assert logger.debug.called
        assert logger.error.called
        assert logger.info.called

        assert logger.error.call_args_list[0][0][0] == "Failed to save exported keys to file: akey_all.json"
        assert logger.debug.call_args_list[0][0][0] == "About to dump 3 keys to akey_all.json"

        expectation = [
            'Exporting ALL activation keys to akey_all.json',
            'Exporting key one to akey_all.json',
            'Exporting key two to akey_all.json',
            'Exporting key three to akey_all.json',
        ]
        for idx, call in enumerate(logger.info.call_args_list):
            assert call[0][0] == expectation[idx]

    @patch("spacecmd.activationkey.json_dump_to_file", MagicMock())
    @patch("spacecmd.activationkey.os.path.isfile", MagicMock(return_value=False))
    def test_do_activationkey_export_filename_arg(self, shell):
        """
        Test do_activationkey_export exports to 'akey_all.json' if not specified otherwise.
        """
        logger = MagicMock()
        shell.do_activationkey_list = MagicMock(return_value=["one", "two", "three"])
        shell.export_activationkey_getdetails = MagicMock(side_effect=[{}, {}, {}])
        shell.client.activationkey.checkConfigDeployment = MagicMock(return_value=True)

        with patch("spacecmd.activationkey.logging", logger):
            spacecmd.activationkey.do_activationkey_export(shell, "-f somefile.json")

        assert logger.debug.called
        assert logger.error.called
        assert logger.info.called

        assert logger.error.call_args_list[0][0][0] == "Failed to save exported keys to file: somefile.json"
        assert logger.debug.call_args_list[0][0][0] == "Passed filename do_activationkey_export somefile.json"

        expectation = [
            'Exporting ALL activation keys to somefile.json',
            'Exporting key one to somefile.json',
            'Exporting key two to somefile.json',
            'Exporting key three to somefile.json',
        ]
        for idx, call in enumerate(logger.info.call_args_list):
            assert call[0][0] == expectation[idx]

    def test_do_activationkey_import_noargs(self, shell):
        """
        Test activationkey_import command is invoking help message on insufficient arguments.
        """
        shell.help_activationkey_import = MagicMock()
        shell.import_activationkey_fromdetails = MagicMock(return_value=False)

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            spacecmd.activationkey.do_activationkey_import(shell, "")

        assert shell.help_activationkey_import.called
        assert not shell.import_activationkey_fromdetails.called
        assert logger.error.called
        assert logger.error.call_args_list[0][0][0] == 'No filename passed'

    def test_do_activationkey_fromdetails_existingkey(self, shell):
        """
        Test import_activationkey_fromdetails does not import anything if key already exist.
        """
        shell.client.activationkey.create = MagicMock()
        shell.do_activationkey_list = MagicMock(return_value=["somekey"])
        shell.client.activationkey.addChildChannels = MagicMock()
        shell.client.activationkey.enableConfigDeployment = MagicMock()
        shell.client.activationkey.disableConfigDeployment = MagicMock()
        shell.client.activationkey.addConfigChannels = MagicMock()
        shell.client.activationkey.addServerGroups = MagicMock()
        shell.client.activationkey.addPackages = MagicMock()

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            ret = spacecmd.activationkey.import_activationkey_fromdetails(shell, {"key": "somekey"})
        assert not shell.client.activationkey.create.called
        assert shell.do_activationkey_list.called
        assert not shell.client.activationkey.addChildChannels.called
        assert not shell.client.activationkey.enableConfigDeployment.called
        assert not shell.client.activationkey.disableConfigDeployment.called
        assert not shell.client.activationkey.addConfigChannels.called
        assert not shell.client.activationkey.addServerGroups.called
        assert not shell.client.activationkey.addServerGroups.called
        assert not shell.client.activationkey.addPackages.called

        assert logger.warning.called
        assert logger.warning.call_args_list[0][0][0] == 'somekey already exists! Skipping!'
        assert type(ret) == bool
        assert not ret

    def test_do_activationkey_fromdetails_newkey_no_usage_limit(self, shell):
        """
        Test import_activationkey_fromdetails no usage limit.
        """
        shell.client.activationkey.create = MagicMock(return_value={})
        shell.do_activationkey_list = MagicMock(return_value=["some_existing_key"])
        shell.client.activationkey.addChildChannels = MagicMock()
        shell.client.activationkey.enableConfigDeployment = MagicMock()
        shell.client.activationkey.disableConfigDeployment = MagicMock()
        shell.client.activationkey.addConfigChannels = MagicMock()
        shell.client.activationkey.addServerGroups = MagicMock()
        shell.client.activationkey.addPackages = MagicMock()

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            ret = spacecmd.activationkey.import_activationkey_fromdetails(
                shell,
                {
                    "key": "somekey",
                    "usage_limit": 0,
                    "base_channel_label": "none",
                    "description": "Key description",
                    "entitlements": ["one", "two", "three"],
                    "universal_default": True,
                }
            )
        assert shell.do_activationkey_list.called
        assert shell.client.activationkey.create.called
        assert not shell.client.activationkey.addChildChannels.called
        assert not shell.client.activationkey.enableConfigDeployment.called
        assert not shell.client.activationkey.disableConfigDeployment.called
        assert not shell.client.activationkey.addConfigChannels.called
        assert not shell.client.activationkey.addServerGroups.called
        assert not shell.client.activationkey.addServerGroups.called
        assert not shell.client.activationkey.addPackages.called
        assert logger.debug.call_args_list[0][0][0] == "Found key somekey, importing as somekey"

        assert len(shell.client.activationkey.create.call_args_list[0][0]) == 6
        session, keyname, descr, basech, entl, udef = shell.client.activationkey.create.call_args_list[0][0]
        assert shell.session == session
        assert keyname == "somekey"
        assert descr == "Key description"
        assert basech == ""
        assert entl == ["one", "two", "three"]
        assert udef

    def test_do_activationkey_fromdetails_newkey_fixed_usage_limit(self, shell):
        """
        Test import_activationkey_fromdetails usage limit given.
        """
        shell.client.activationkey.create = MagicMock(return_value={})
        shell.do_activationkey_list = MagicMock(return_value=["some_existing_key"])
        shell.client.activationkey.addChildChannels = MagicMock()
        shell.client.activationkey.enableConfigDeployment = MagicMock()
        shell.client.activationkey.disableConfigDeployment = MagicMock()
        shell.client.activationkey.addConfigChannels = MagicMock()
        shell.client.activationkey.addServerGroups = MagicMock()
        shell.client.activationkey.addPackages = MagicMock()

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            ret = spacecmd.activationkey.import_activationkey_fromdetails(
                shell,
                {
                    "key": "somekey",
                    "usage_limit": 42,
                    "base_channel_label": "none",
                    "description": "Key description",
                    "entitlements": ["one", "two", "three"],
                    "universal_default": True,
                }
            )
        assert len(shell.client.activationkey.create.call_args_list[0][0]) == 7
        session, keyname, descr, basech, ulim, entl, udef = shell.client.activationkey.create.call_args_list[0][0]
        assert shell.session == session
        assert keyname == "somekey"
        assert descr == "Key description"
        assert basech == ""
        assert ulim == 42
        assert entl == ["one", "two", "three"]
        assert udef

        # This is expected: see mock return on API call "create".
        assert logger.error.call_args_list[0][0][0] == 'Failed to import key somekey'

    def test_do_activationkey_fromdetails_no_cfgdeploy(self, shell):
        """
        Test import_activationkey_fromdetails no configuration deployment.
        """
        shell.client.activationkey.create = MagicMock(return_value={"name": "whatever"})
        shell.do_activationkey_list = MagicMock(return_value=["some_existing_key"])
        shell.client.activationkey.addChildChannels = MagicMock()
        shell.client.activationkey.enableConfigDeployment = MagicMock()
        shell.client.activationkey.disableConfigDeployment = MagicMock()
        shell.client.activationkey.addConfigChannels = MagicMock()
        shell.client.activationkey.addServerGroups = MagicMock()
        shell.client.activationkey.addPackages = MagicMock()
        shell.client.systemgroup.getDetails = MagicMock(return_value=None)
        shell.client.systemgroup.create = MagicMock(
            side_effect=[
                {"id": 1},
                {"id": 2},
                {"id": 3},
            ]
        )

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            ret = spacecmd.activationkey.import_activationkey_fromdetails(
                shell,
                {
                    "key": "somekey",
                    "usage_limit": 42,
                    "base_channel_label": "none",
                    "description": "Key description",
                    "entitlements": ["one", "two", "three"],
                    "universal_default": True,
                    "child_channel_labels": "child_channel_labels",
                    "config_channels": ["config_channel"],
                    "server_groups": ["one", "two", "three"],
                    "packages": ["emacs"],
                    "config_deploy": 0,
                }
            )
        assert not shell.client.activationkey.enableConfigDeployment.called
        assert shell.client.activationkey.disableConfigDeployment.called
        session, keydata = shell.client.activationkey.disableConfigDeployment.call_args_list[0][0]
        assert shell.session == session
        assert keydata == shell.client.activationkey.create()

    def test_do_activationkey_fromdetails_cfgdeploy(self, shell):
        """
        Test import_activationkey_fromdetails configuration deployment was set.
        """
        shell.client.activationkey.create = MagicMock(return_value={"name": "whatever"})
        shell.do_activationkey_list = MagicMock(return_value=["some_existing_key"])
        shell.client.activationkey.addChildChannels = MagicMock()
        shell.client.activationkey.enableConfigDeployment = MagicMock()
        shell.client.activationkey.disableConfigDeployment = MagicMock()
        shell.client.activationkey.addConfigChannels = MagicMock()
        shell.client.activationkey.addServerGroups = MagicMock()
        shell.client.activationkey.addPackages = MagicMock()
        shell.client.systemgroup.getDetails = MagicMock(return_value=None)
        shell.client.systemgroup.create = MagicMock(
            side_effect=[
                {"id": 1},
                {"id": 2},
                {"id": 3},
            ]
        )

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            ret = spacecmd.activationkey.import_activationkey_fromdetails(
                shell,
                {
                    "key": "somekey",
                    "usage_limit": 42,
                    "base_channel_label": "none",
                    "description": "Key description",
                    "entitlements": ["one", "two", "three"],
                    "universal_default": True,
                    "child_channel_labels": "child_channel_labels",
                    "config_channels": ["config_channel"],
                    "server_groups": ["one", "two", "three"],
                    "packages": ["emacs"],
                    "config_deploy": 1,
                }
            )
        assert shell.client.activationkey.enableConfigDeployment.called
        assert not shell.client.activationkey.disableConfigDeployment.called
        session, keydata = shell.client.activationkey.enableConfigDeployment.call_args_list[0][0]
        assert shell.session == session
        assert keydata == shell.client.activationkey.create()

    def test_do_activationkey_fromdetails_groups_pkgs(self, shell):
        """
        Test import_activationkey_fromdetails groups and packages processed.
        """
        shell.client.activationkey.create = MagicMock(return_value={"name": "whatever"})
        shell.do_activationkey_list = MagicMock(return_value=["some_existing_key"])
        shell.client.activationkey.addChildChannels = MagicMock()
        shell.client.activationkey.enableConfigDeployment = MagicMock()
        shell.client.activationkey.disableConfigDeployment = MagicMock()
        shell.client.activationkey.addConfigChannels = MagicMock()
        shell.client.activationkey.addServerGroups = MagicMock()
        shell.client.activationkey.addPackages = MagicMock()
        shell.client.systemgroup.getDetails = MagicMock(return_value=None)
        shell.client.systemgroup.create = MagicMock(
            side_effect=[
                {"id": 1},
                {"id": 2},
                {"id": 3},
            ]
        )

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            ret = spacecmd.activationkey.import_activationkey_fromdetails(
                shell,
                {
                    "key": "somekey",
                    "usage_limit": 42,
                    "base_channel_label": "none",
                    "description": "Key description",
                    "entitlements": ["one", "two", "three"],
                    "universal_default": True,
                    "child_channel_labels": "child_channel_labels",
                    "config_channels": ["config_channel"],
                    "server_groups": ["one", "two", "three"],
                    "packages": ["emacs"],
                    "config_deploy": 1,
                }
            )
        session, keyprm, gids = shell.client.activationkey.addServerGroups.call_args_list[0][0]
        assert shell.session == session
        assert keyprm == {'name': 'whatever'}
        assert gids == [1, 2, 3]

        session, keyprm, pkgs = shell.client.activationkey.addPackages.call_args_list[0][0]
        assert pkgs == ["emacs"]

    @patch("spacecmd.activationkey.is_interactive", MagicMock(return_value=False))
    @patch("spacecmd.activationkey.prompt_user", MagicMock(side_effect=["original_key", "cloned_key"]))
    def test_do_activationkey_clone_noargs(self, shell):
        """
        Test do_activationkey_clone no arguments requires some.
        """
        logger = MagicMock()
        shell.do_activationkey_list = MagicMock()
        shell.help_activationkey_clone = MagicMock()
        shell.export_activationkey_getdetails = MagicMock()
        shell.list_base_channels = MagicMock()
        shell.list_child_channels = MagicMock()
        shell.do_configchannel_list = MagicMock()
        shell.import_activtionkey_fromdetails = MagicMock()

        with patch("spacecmd.activationkey.logging", logger):
            ret = spacecmd.activationkey.do_activationkey_clone(shell, "")

        assert logger.error.called
        assert shell.help_activationkey_clone.called
        assert shell.do_activationkey_list.called
        assert not shell.export_activationkey_getdetails.called
        assert not shell.list_base_channels.called
        assert not shell.list_child_channels.called
        assert not shell.do_configchannel_list.called
        assert not shell.import_activtionkey_fromdetails.called
        assert ret is 1
        assert logger.error.call_args_list[0][0][0] == 'Error - must specify either -c or -x options!'

    @patch("spacecmd.activationkey.is_interactive", MagicMock(return_value=False))
    @patch("spacecmd.activationkey.prompt_user", MagicMock(side_effect=["original_key", "cloned_key"]))
    def test_do_activationkey_clone_wrongargs(self, shell):
        """
        Test do_activationkey_clone wrong arguments prompts for correction.
        """
        shell.do_activationkey_list = MagicMock()
        shell.help_activationkey_clone = MagicMock()
        shell.export_activationkey_getdetails = MagicMock()
        shell.list_base_channels = MagicMock()
        shell.list_child_channels = MagicMock()
        shell.do_configchannel_list = MagicMock()
        shell.import_activtionkey_fromdetails = MagicMock()

        with pytest.raises(Exception) as exc:
            spacecmd.activationkey.do_activationkey_clone(shell, "--nonsense=true")

        assert "unrecognized arguments: --nonsense=true" in exc2str(exc)

    @patch("spacecmd.activationkey.is_interactive", MagicMock(return_value=False))
    @patch("spacecmd.activationkey.prompt_user", MagicMock(side_effect=["original_key", "cloned_key"]))
    def test_do_activationkey_clone_existing_clone_arg(self, shell):
        """
        Test do_activationkey_clone existing clone name passed to the arguments.
        """
        shell.do_activationkey_list = MagicMock(return_value=["key_clone_name"])
        shell.help_activationkey_clone = MagicMock()
        shell.export_activationkey_getdetails = MagicMock()
        shell.list_base_channels = MagicMock()
        shell.list_child_channels = MagicMock()
        shell.do_configchannel_list = MagicMock()
        shell.import_activtionkey_fromdetails = MagicMock()

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            ret = spacecmd.activationkey.do_activationkey_clone(shell, "-c key_clone_name")

        assert logger.error.called
        assert not shell.help_activationkey_clone.called
        assert shell.do_activationkey_list.called
        assert not shell.export_activationkey_getdetails.called
        assert not shell.list_base_channels.called
        assert not shell.list_child_channels.called
        assert not shell.do_configchannel_list.called
        assert not shell.import_activtionkey_fromdetails.called
        assert logger.error.call_args_list[0][0][0] == "Key key_clone_name already exists"
        assert ret is 1

    @patch("spacecmd.activationkey.is_interactive", MagicMock(return_value=False))
    @patch("spacecmd.activationkey.prompt_user", MagicMock(side_effect=["original_key", "cloned_key"]))
    def test_do_activationkey_clone_noargs_to_clone(self, shell):
        """
        Test do_activationkey_clone no arguments to a clone name passed to the arguments.
        """
        shell.do_activationkey_list = MagicMock(return_value=["key_some_clone_name"])
        shell.help_activationkey_clone = MagicMock()
        shell.export_activationkey_getdetails = MagicMock()
        shell.list_base_channels = MagicMock()
        shell.list_child_channels = MagicMock()
        shell.do_configchannel_list = MagicMock()
        shell.import_activtionkey_fromdetails = MagicMock()

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            ret = spacecmd.activationkey.do_activationkey_clone(shell, "-c key_clone_name")

        assert logger.error.called
        assert shell.help_activationkey_clone.called
        assert shell.do_activationkey_list.called
        assert not shell.export_activationkey_getdetails.called
        assert not shell.list_base_channels.called
        assert not shell.list_child_channels.called
        assert not shell.do_configchannel_list.called
        assert not shell.import_activtionkey_fromdetails.called
        assert logger.error.call_args_list[0][0][0] == "Error no activationkey to clone passed!"
        assert ret is 1

    @patch("spacecmd.activationkey.is_interactive", MagicMock(return_value=False))
    @patch("spacecmd.activationkey.prompt_user", MagicMock(side_effect=["original_key", "cloned_key"]))
    def test_do_activationkey_clone_keyargs_to_clone_filtered(self, shell):
        """
        Test do_activationkey_clone filtered out keys.
        """
        shell.do_activationkey_list = MagicMock(return_value=["key_some_clone_name"])
        shell.help_activationkey_clone = MagicMock()
        shell.export_activationkey_getdetails = MagicMock()
        shell.list_base_channels = MagicMock()
        shell.list_child_channels = MagicMock()
        shell.do_configchannel_list = MagicMock()
        shell.import_activtionkey_fromdetails = MagicMock()

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            ret = spacecmd.activationkey.do_activationkey_clone(shell, "orig_key -c key_clone_name")

        assert not logger.error.called
        assert not shell.help_activationkey_clone.called
        assert shell.do_activationkey_list.called
        assert not shell.export_activationkey_getdetails.called
        assert not shell.list_base_channels.called
        assert not shell.list_child_channels.called
        assert not shell.do_configchannel_list.called
        assert not shell.import_activtionkey_fromdetails.called

        expectations = [
            "Got args=['orig_key'] 1",
            "Filtered akeys []",
            "all akeys ['key_some_clone_name']",
        ]
        for idx, call in enumerate(logger.debug.call_args_list):
            assert call[0][0] == expectations[idx]

    @patch("spacecmd.activationkey.is_interactive", MagicMock(return_value=False))
    @patch("spacecmd.activationkey.prompt_user", MagicMock(side_effect=["original_key", "cloned_key"]))
    def test_do_activationkey_clone_keyargs_unfiltered(self, shell):
        """
        Test do_activationkey_clone not filtered out keys.
        """
        shell.do_activationkey_list = MagicMock(return_value=["key_some_clone_name"])
        shell.help_activationkey_clone = MagicMock()
        shell.export_activationkey_getdetails = MagicMock()
        shell.list_base_channels = MagicMock()
        shell.list_child_channels = MagicMock()
        shell.do_configchannel_list = MagicMock()
        shell.import_activtionkey_fromdetails = MagicMock()

        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            ret = spacecmd.activationkey.do_activationkey_clone(shell, "key_some* -c key_clone_name")
        expectations = [
            "Got args=['key_some.*'] 1",
            "Filtered akeys ['key_some_clone_name']",
            "all akeys ['key_some_clone_name']",
            "Cloning key_some_clone_name",
        ]
        for idx, call in enumerate(logger.debug.call_args_list):
            assert call[0][0] == expectations[idx]

    def test_check_activationkey_nokey(self, shell):
        """
        Test check activation key helper returns False on no key.
        """
        shell.is_activationkey = MagicMock(return_value=False)
        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            assert not spacecmd.activationkey.check_activationkey(shell, "")
        assert logger.error.called
        assert logger.error.call_args_list[0][0][0] == 'no activationkey label given'

    def test_check_activationkey_not_a_key(self, shell):
        """
        Test check activation key helper returns False on not a key.
        """
        shell.is_activationkey = MagicMock(return_value=False)
        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            assert not spacecmd.activationkey.check_activationkey(shell, "some_not_a_key")
        assert logger.error.called
        assert logger.error.call_args_list[0][0][0] == 'invalid activationkey label some_not_a_key'

    def test_check_activationkey_correct_key(self, shell):
        """
        Test check activation key helper returns True if a key is correct.
        """
        shell.is_activationkey = MagicMock(return_value=True)
        logger = MagicMock()
        with patch("spacecmd.activationkey.logging", logger):
            assert spacecmd.activationkey.check_activationkey(shell, "some_not_a_key")
        assert not logger.error.called

    def test_activationkey_diff_noarg(self, shell):
        """
        Test dump activation key helper invokes help message on insufficient arguments.
        """
        for args in ["", "one two three", "some more args here"]:
            shell.help_activationkey_diff = MagicMock()
            shell.dump_activationkey = MagicMock()
            shell.check_activationkey = MagicMock()
            shell.do_activationkey_getcorresponding = MagicMock()

            _diff = MagicMock()
            with patch("spacecmd.activationkey.diff", _diff):
                spacecmd.activationkey.do_activationkey_diff(shell, "")

            assert shell.help_activationkey_diff.called
            assert not shell.dump_activationkey.called
            assert not shell.check_activationkey.called
            assert not shell.do_activationkey_getcorresponding.called
            assert not _diff.called

    def test_activationkey_diff_arg(self, shell):
        """
        Test dump activation key helper invokes expected sequence of function calls.
        """
        shell.help_activationkey_diff = MagicMock()
        shell.dump_activationkey = MagicMock()
        shell.check_activationkey = MagicMock()
        shell.do_activationkey_getcorresponding = MagicMock()

        _diff = MagicMock()
        with patch("spacecmd.activationkey.diff", _diff):
            spacecmd.activationkey.do_activationkey_diff(shell, "some_key")

        assert not shell.help_activationkey_diff.called
        assert shell.dump_activationkey.called
        assert shell.check_activationkey.called
        assert shell.do_activationkey_getcorresponding.called
        assert _diff.called

    def test_activationkey_diff(self, shell):
        """
        Test dump activation key helper returns key diffs.
        """
        shell.help_activationkey_diff = MagicMock()
        shell.dump_activationkey = MagicMock(side_effect=[["some data"], ["some data again"]])
        shell.check_activationkey = MagicMock(return_value=True)
        shell.do_activationkey_getcorresponding = MagicMock(return_value="some_channel")

        gsdd = MagicMock(return_value=({"one": "two", "three": "three"}, {"one": "two", "three": "four"}))
        with patch("spacecmd.activationkey.get_string_diff_dicts", gsdd), \
             patch("spacecmd.activationkey.print") as mprint:
            spacecmd.activationkey.do_activationkey_diff(shell, "some_key")

        assert_list_args_expect(
            mprint.call_args_list,
            ['--- some_key\n', '+++ some_channel\n', '@@ -1 +1 @@\n', '-some data', '+some data again']
        )

    def test_do_activationkey_diable_noargs(self, shell):
        """
        Test do_activationkey_disable command triggers help on no args.
        """
        shell.help_activationkey_disable = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()

        spacecmd.activationkey.do_activationkey_disable(shell, "")
        assert shell.help_activationkey_disable.called
        assert not shell.client.activationkey.setDetails.called

    def test_do_activationkey_diable_args(self, shell):
        """
        Test do_activationkey_disable command triggers activationkey.setDetails API call.
        """
        keys = ["key_one", "key_two"]
        shell.help_activationkey_disable = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()
        shell.do_activationkey_list = MagicMock(return_value=keys)

        spacecmd.activationkey.do_activationkey_disable(shell, "key_*")
        assert not shell.help_activationkey_disable.called
        assert shell.client.activationkey.setDetails.called

        for call in shell.client.activationkey.setDetails.call_args_list:
            session, key, arg = call[0]
            assert shell.session == session
            assert key in keys
            assert "disabled" in arg
            assert arg["disabled"]

    def test_do_activationkey_enable_noargs(self, shell):
        """
        Test do_activationkey_enable command triggers help on no args.
        """
        shell.help_activationkey_enable = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()

        spacecmd.activationkey.do_activationkey_enable(shell, "")
        assert shell.help_activationkey_enable.called
        assert not shell.client.activationkey.setDetails.called

    def test_do_activationkey_enable_args(self, shell):
        """
        Test do_activationkey_enable command triggers activationkey.setDetails API call.
        """
        keys = ["key_one", "key_two"]
        shell.help_activationkey_enable = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()
        shell.do_activationkey_list = MagicMock(return_value=keys)

        spacecmd.activationkey.do_activationkey_enable(shell, "key_*")
        assert not shell.help_activationkey_enable.called
        assert shell.client.activationkey.setDetails.called

        for call in shell.client.activationkey.setDetails.call_args_list:
            session, key, arg = call[0]
            assert shell.session == session
            assert key in keys
            assert "disabled" in arg
            assert not arg["disabled"]

    def test_do_activationkey_setdescription_noargs(self, shell):
        """
        Test do_activationkey_setdescription command triggers help on no args.
        """
        shell.help_activationkey_setdescription = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()

        spacecmd.activationkey.do_activationkey_setdescription(shell, "")
        assert shell.help_activationkey_setdescription.called
        assert not shell.client.activationkey.setDetails.called

    def test_do_activationkey_setdescription_args(self, shell):
        """
        Test do_activationkey_setdescription command triggers activationkey.setDetails API call.
        """
        shell.help_activationkey_enable = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()

        spacecmd.activationkey.do_activationkey_setdescription(shell, "key_one some description of it here")
        assert not shell.help_activationkey_enable.called
        assert shell.client.activationkey.setDetails.called

        for call in shell.client.activationkey.setDetails.call_args_list:
            session, key, arg = call[0]
            assert shell.session == session
            assert "description" in arg
            assert arg["description"] == "some description of it here"

    def test_do_activationkey_setcontactmethod_noargs(self, shell):
        """
        Test do_activationkey_setcontactmethod command triggers help on no args.
        """
        shell.help_activationkey_setcontactmethod = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()

        spacecmd.activationkey.do_activationkey_setcontactmethod(shell, "")
        assert shell.help_activationkey_setcontactmethod.called
        assert not shell.client.activationkey.setDetails.called

    def test_do_activationkey_setcontactmethod_args(self, shell):
        """
        Test do_activationkey_setdescription command triggers activationkey.setDetails API call.
        """
        shell.help_activationkey_setcontactmethod = MagicMock()
        shell.client.activationkey.setDetails = MagicMock()

        spacecmd.activationkey.do_activationkey_setcontactmethod(shell, "key_one 230V")
        assert not shell.help_activationkey_setcontactmethod.called
        assert shell.client.activationkey.setDetails.called

        for call in shell.client.activationkey.setDetails.call_args_list:
            session, key, arg = call[0]
            assert shell.session == session
            assert "contact_method" in arg
            assert arg["contact_method"] == "230V"

    
