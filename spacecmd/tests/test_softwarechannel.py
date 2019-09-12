# coding: utf-8
"""
Test software channel module.
"""

from mock import MagicMock, patch
import spacecmd.softwarechannel
from xmlrpc import client as xmlrpclib
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect


class TestSCSoftwareChannel:
    """
    Test suite for software channel.
    """
    def test_softwarechannel_list_doreturn_nolabels(self, shell):
        """
        Test do_softwarechannel_list no labels, return data.

        :param shell:
        :return:
        """
        shell.client.channel.listAllChannels = MagicMock(return_value=[])
        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            out = spacecmd.softwarechannel.do_softwarechannel_list(shell, "", doreturn=True)
        assert out is not None
        assert out == []
        assert not shell.help_softwarechannel_list.called
        assert not shell.client.channel.software.getDetails.called
        assert not shell.list_child_channels.called
        assert not shell.list_base_channels.called

    def test_softwarechannel_list_noreturn_nolabels(self, shell):
        """
        Test do_softwarechannel_list no labels, no return data.

        :param shell:
        :return:
        """
        shell.client.channel.listAllChannels = MagicMock(return_value=[])
        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            out = spacecmd.softwarechannel.do_softwarechannel_list(shell, "", doreturn=False)
        assert out is None
        assert not shell.help_softwarechannel_list.called
        assert not shell.client.channel.software.getDetails.called
        assert not shell.list_child_channels.called
        assert not shell.list_base_channels.called

    def test_softwarechannel_list_noreturn_labels_std(self, shell):
        """
        Test do_softwarechannel_list with label, no return data, standard output.

        :param shell:
        :return:
        """
        shell.client.channel.listAllChannels = MagicMock(return_value=[
            {"label": "label-one"}, {"label": "label-last"},
            {"label": "label-two"}, {"label": "label-three"},
        ])

        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            out = spacecmd.softwarechannel.do_softwarechannel_list(
                shell, "", doreturn=False)

        assert out is None
        assert not shell.help_softwarechannel_list.called
        assert not shell.client.channel.software.getDetails.called
        assert not shell.list_child_channels.called
        assert shell.client.channel.listAllChannels.called

        assert_list_args_expect(mprint.call_args_list,
                                ["label-last", "label-one", "label-three", "label-two"])

    def test_softwarechannel_list_noreturn_labels_verbose(self, shell):
        """
        Test do_softwarechannel_list with label, no return data, verbose output.

        :param shell:
        :return:
        """
        shell.client.channel.listAllChannels = MagicMock(return_value=[
            {"label": "test_channel"},
        ])
        shell.client.channel.software.getDetails = MagicMock(side_effect=[
            {"summary": "Summary of test_channel"},
            {"summary": "Summary of child_channel"},
        ])
        shell.list_child_channels = MagicMock(return_value=[
            "child_channel"
        ])

        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            out = spacecmd.softwarechannel.do_softwarechannel_list(
                shell, "-v", doreturn=False)

        assert out is None
        assert not shell.help_softwarechannel_list.called
        assert not shell.list_child_channels.called
        assert shell.client.channel.software.getDetails.called
        assert shell.client.channel.listAllChannels.called

        assert_expect(mprint.call_args_list,
                      'test_channel : Summary of test_channel')

    def test_softwarechannel_list_noreturn_labels_verbose_tree(self, shell):
        """
        Test do_softwarechannel_list with label, no return data, verbose output with tree.

        :param shell:
        :return:
        """
        shell.client.channel.listAllChannels = MagicMock(return_value=[
            {"any_channel": "any_channel"},
        ])
        shell.client.channel.software.getDetails = MagicMock(side_effect=[
            {"summary": "Summary of test_channel"},
            {"summary": "Summary of child_channel"},
        ])
        shell.list_child_channels = MagicMock(return_value=[
            "child_channel"
        ])

        shell.list_base_channels = MagicMock(return_value=[
            "base_channel"
        ])

        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            out = spacecmd.softwarechannel.do_softwarechannel_list(
                shell, "-v -t", doreturn=False)

        assert out is None
        assert not shell.client.channel.listAllChannels.called
        assert not shell.help_softwarechannel_list.called
        assert shell.list_child_channels.called
        assert shell.client.channel.software.getDetails.called

        assert_list_args_expect(mprint.call_args_list,
                                ['base_channel : Summary of test_channel',
                                 ' |-child_channel : Summary of child_channel'])

    def test_softwarechannel_listmanageablechannels_noarg(self, shell):
        """
        Test do_softwarechannel_listmanageablechannels without arguments.

        :param shell:
        :return:
        """
        shell.client.channel.listManageableChannels = MagicMock(return_value=[
            {"label": "x_channel"},
            {"label": "z_channel"},
            {"label": "a_channel"},
        ])

        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            out = spacecmd.softwarechannel.do_softwarechannel_listmanageablechannels(shell, "")

        assert out is None
        assert not shell.client.channel.software.getDetails.called
        assert shell.client.channel.listManageableChannels.called
        assert_list_args_expect(mprint.call_args_list,
                                ["a_channel", "x_channel", "z_channel"])

    def test_softwarechannel_listmanageablechannels_default_verbose(self, shell):
        """
        Test do_softwarechannel_listmanageablechannels with verbose arg (all).

        :param shell:
        :return:
        """
        shell.client.channel.listManageableChannels = MagicMock(return_value=[
            {"label": "x_channel"},
            {"label": "z_channel"},
            {"label": "b_channel"},
            {"label": "a_channel"},
        ])
        shell.client.channel.software.getDetails = MagicMock(side_effect=[
            {"summary": "A summary"},
            {"summary": "B summary"},
            {"summary": "X summary"},
            {"summary": "Z summary"},
        ])

        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            out = spacecmd.softwarechannel.do_softwarechannel_listmanageablechannels(
                shell, "--verbose")

        assert out is None
        assert shell.client.channel.software.getDetails.called
        assert shell.client.channel.listManageableChannels.called
        assert_list_args_expect(mprint.call_args_list,
                                ["a_channel : A summary",
                                 "b_channel : B summary",
                                 "x_channel : X summary",
                                 "z_channel : Z summary"])

    def test_softwarechannel_listmanageablechannels_data_sparse(self, shell):
        """
        Test do_softwarechannel_listmanageablechannels data out, short.

        :param shell:
        :return:
        """
        shell.client.channel.listManageableChannels = MagicMock(return_value=[
            {"label": "x_channel"},
            {"label": "z_channel"},
            {"label": "a_channel"},
        ])

        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            out = spacecmd.softwarechannel.do_softwarechannel_listmanageablechannels(shell, "", doreturn=True)

        assert out is not None
        assert not shell.client.channel.software.getDetails.called
        assert shell.client.channel.listManageableChannels.called
        assert out == ["a_channel", "x_channel", "z_channel"]

    def test_softwarechannel_listmanageablechannels_data_verbose(self, shell):
        """
        Test do_softwarechannel_listmanageablechannels with verbose arg (all).

        :param shell:
        :return:
        """
        shell.client.channel.listManageableChannels = MagicMock(return_value=[
            {"label": "x_channel"},
            {"label": "z_channel"},
            {"label": "b_channel"},
            {"label": "a_channel"},
        ])
        shell.client.channel.software.getDetails = MagicMock(side_effect=[
            {"summary": "A summary"},
            {"summary": "B summary"},
            {"summary": "X summary"},
            {"summary": "Z summary"},
        ])

        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            out = spacecmd.softwarechannel.do_softwarechannel_listmanageablechannels(
                shell, "--verbose", doreturn=True)

        assert out is not None
        assert not shell.client.channel.software.getDetails.called
        assert shell.client.channel.listManageableChannels.called
        assert out == ["a_channel", "b_channel", "x_channel", "z_channel"]

    def test_listchildchannels(self, shell):
        """
        Test do_softwarechannel_listchildchannels noargs.

        :param shell:
        :return:
        """
        shell.list_child_channels = MagicMock(return_value=["x_child_channel", "z_child_channel",
                                                            "b_child_channel", "a_child_channel",])
        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            spacecmd.softwarechannel.do_softwarechannel_listchildchannels(shell, "")

        assert not shell.client.channel.software.getDetails.called
        assert_list_args_expect(mprint.call_args_list,
                                ['a_child_channel\nb_child_channel\nx_child_channel\nz_child_channel'])

    def test_listchildchannels_verbose(self, shell):
        """
        Test do_softwarechannel_listchildchannels verbose.

        :param shell:
        :return:
        """
        shell.list_child_channels = MagicMock(return_value=["x_child_channel", "z_child_channel",
                                                            "b_child_channel", "a_child_channel",])
        shell.client.channel.software.getDetails = MagicMock(side_effect=[
            {"summary": "A summary"},
            {"summary": "B summary"},
            {"summary": "X summary"},
            {"summary": "Z summary"},
        ])
        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            spacecmd.softwarechannel.do_softwarechannel_listchildchannels(shell, "--verbose")

        assert shell.client.channel.software.getDetails.called
        assert_list_args_expect(mprint.call_args_list,
                                ['a_child_channel : A summary',
                                 'b_child_channel : B summary',
                                 'x_child_channel : X summary',
                                 'z_child_channel : Z summary'])

    def test_listsystems_noargs(self, shell):
        """
        Test do_softwarechannel_listsystems no args.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            out = spacecmd.softwarechannel.do_softwarechannel_listsystems(shell, "")

        assert out is None
        assert not mprint.called
        assert not shell.client.channel.software.listSubscribedSystems.called
        assert shell.help_softwarechannel_listsystems.called

    def test_listsystems_noargs_channel_no_data(self, shell):
        """
        Test do_softwarechannel_listsystems one channel, no data.

        :param shell:
        :return:
        """
        shell.client.channel.software.listSubscribedSystems = MagicMock(return_value=[
            {"name": "one.acme.lan"},
            {"name": "two.acme.lan"},
            {"name": "zetta.acme.lan"},
            {"name": "third.zoo.lan"},
        ])
        mprint = MagicMock()
        with patch("spacecmd.softwarechannel.print", mprint) as prt:
            out = spacecmd.softwarechannel.do_softwarechannel_listsystems(shell, "my_channel")

        assert not shell.help_softwarechannel_listsystems.called
        assert out is None
        assert mprint.called
        assert shell.client.channel.software.listSubscribedSystems.called
        assert_list_args_expect(mprint.call_args_list,
                                ['one.acme.lan\nthird.zoo.lan\ntwo.acme.lan\nzetta.acme.lan'])
