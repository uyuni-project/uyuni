# coding: utf-8
"""
Test activation key methods.
"""
from mock import MagicMock, patch
import pytest
import time
import hashlib
import spacecmd.activationkey


@pytest.fixture
def shell():
    """
    Create fake shell.
    """
    base = MagicMock()
    base.session = hashlib.sha256(str(time.time()).encode("utf-8")).hexdigest()
    base.client = MagicMock()
    base.client.activationkey = MagicMock()
    base.do_activationkey_list = MagicMock(return_value="do_activation_list")

    return base


class TestSCActivationKey:
    """
    Test activation key.
    """
    def test_completer_ak_addpackages(self, shell):
        """
        Test tab completer activation keys on addpackages.
        """
        print()
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
        print()
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

        assert "unrecognized arguments" in str(exc)
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
