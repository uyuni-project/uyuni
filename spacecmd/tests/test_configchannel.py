# coding: utf-8
"""
Configchannel module unit tests.
"""

from unittest.mock import MagicMock, patch, mock_open
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.configchannel
from xmlrpc import client as xmlrpclib
import datetime


class TestSCConfigChannel:
    """
    Test configuration channel.
    """
    def test_configchannel_list_noret(self, shell):
        """
        Test configuration channel list, no data return.

        :param shell:
        :return:
        """
        shell.client.configchannel.listGlobals = MagicMock(return_value=[
            {"label": "base_channel"}, {"label": "boese_channel"},
            {"label": "other_channel"}, {"label": "ze_channel"},
            {"label": "and_some_channel"}, {"label": "another_channel"}
        ])
        mprint = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt:
            ret = spacecmd.configchannel.do_configchannel_list(shell, "", doreturn=False)

        assert ret is None
        assert mprint.called
        assert shell.client.configchannel.listGlobals.called

        assert_expect(mprint.call_args_list,
                      'and_some_channel\nanother_channel\nbase_channel'
                      '\nboese_channel\nother_channel\nze_channel')

    def test_configchannel_list_data(self, shell):
        """
        Test configuration channel list, return data.

        :param shell:
        :return:
        """
        shell.client.configchannel.listGlobals = MagicMock(return_value=[
            {"label": "base_channel"}, {"label": "boese_channel"},
            {"label": "other_channel"}, {"label": "ze_channel"},
            {"label": "and_some_channel"}, {"label": "another_channel"}
        ])
        mprint = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt:
            ret = spacecmd.configchannel.do_configchannel_list(shell, "", doreturn=True)

        assert not mprint.called
        assert ret is not None
        assert shell.client.configchannel.listGlobals.called
        assert ret == ['and_some_channel', 'another_channel', 'base_channel',
                       'boese_channel', 'other_channel', 'ze_channel']

    def test_configchannel_listsystems_api_version_handling(self, shell):
        """
        Test configchannel listsystems function. Check version limitation.

        :param shell:
        :return:
        """
        shell.check_api_version = MagicMock(return_value=False)
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            spacecmd.configchannel.do_configchannel_listsystems(shell, "")

        assert not mprint.called
        assert not shell.client.configchannel.listSubscribedSystems.called
        assert not shell.help_configchannel_listsystems.called
