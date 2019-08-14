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

