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

    def test_configchannel_listsystems_api_noarg(self, shell):
        """
        Test configchannel listsystems function. No arguments passed.

        :param shell:
        :return:
        """
        shell.check_api_version = MagicMock(return_value=True)
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            spacecmd.configchannel.do_configchannel_listsystems(shell, "")

        assert not mprint.called
        assert not shell.client.configchannel.listSubscribedSystems.called
        assert shell.help_configchannel_listsystems.called

    def test_configchannel_listsystems_api_sorted_output(self, shell):
        """
        Test configchannel listsystems function. Output must be sorted.

        :param shell:
        :return:
        """
        shell.client.configchannel.listSubscribedSystems = MagicMock(
            return_value=[
                {"name": "sisteme"}, {"name": "system"}, {"name": "exbox"},
                {"name": "zitrix"}, {"name": "paystation-4"}, {"name": "azure"},
                {"name": "quakearena"}, {"name": "awsbox"}, {"name": "beigebox"},
            ]
        )
        shell.check_api_version = MagicMock(return_value=True)
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            spacecmd.configchannel.do_configchannel_listsystems(shell, "some_channel")

        assert not shell.help_configchannel_listsystems.called
        assert mprint.called
        assert shell.client.configchannel.listSubscribedSystems.called
        assert_expect(mprint.call_args_list,
                      'awsbox\nazure\nbeigebox\nexbox\npaystation-4'
                      '\nquakearena\nsisteme\nsystem\nzitrix')

    def test_configchannel_listfiles_noarg(self, shell):
        """
        Test configchannel_listfiles function. No args.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            spacecmd.configchannel.do_configchannel_listfiles(shell, "")

        assert not shell.client.configchannel.listFiles.called
        assert not mprint.called
        assert shell.help_configchannel_listfiles.called

    def test_configchannel_listfiles_sorted_data(self, shell):
        """
        Test configchannel_listfiles function. Data is sorted for STDOUT.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        shell.client.configchannel.listFiles = MagicMock(return_value=[
            {"path": "/tmp/somefile.txt"}, {"path": "/tmp/zypper.rpm"},
            {"path": "/tmp/aaa_base.rpm"}, {"path": "/tmp/someother.txt"},
            {"path": "/etc/whatever.conf"}, {"path": "/etc/ssh.conf"},
        ])
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            data = spacecmd.configchannel.do_configchannel_listfiles(shell, "some_channel")

        assert not shell.help_configchannel_listfiles.called
        assert shell.client.configchannel.listFiles.called
        assert mprint.called
        assert_expect(mprint.call_args_list,
                      "/etc/ssh.conf\n/etc/whatever.conf\n/tmp/aaa_base.rpm"
                      "\n/tmp/somefile.txt\n/tmp/someother.txt\n/tmp/zypper.rpm")

    def test_configchannel_listfiles_sorted_data_out(self, shell):
        """
        Test configchannel_listfiles function. Data is sorted as a type.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        shell.client.configchannel.listFiles = MagicMock(return_value=[
            {"path": "/tmp/somefile.txt"}, {"path": "/tmp/zypper.rpm"},
            {"path": "/tmp/aaa_base.rpm"}, {"path": "/tmp/someother.txt"},
            {"path": "/etc/whatever.conf"}, {"path": "/etc/ssh.conf"},
        ])
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            data = spacecmd.configchannel.do_configchannel_listfiles(shell, "some_channel", doreturn=True)

        assert not mprint.called
        assert not shell.help_configchannel_listfiles.called
        assert shell.client.configchannel.listFiles.called

        assert data == ['/etc/ssh.conf', '/etc/whatever.conf', '/tmp/aaa_base.rpm',
                        '/tmp/somefile.txt', '/tmp/someother.txt', '/tmp/zypper.rpm']

    def test_configchannel_forcedeploy_noargs(self, shell):
        """
        Test configchannel_forcedeploy function. No arguments.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        shell.user_confirm = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            spacecmd.configchannel.do_configchannel_forcedeploy(shell, "")

        assert not mprint.called
        assert not logger.error.called
        assert not logger.info.called
        assert not logger.warning.called
        assert not shell.client.configchannel.listFiles.called
        assert not shell.client.configchannel.listSubscribedSystems.called
        assert not shell.client.configchannel.deployAllSystems.called
        assert shell.help_configchannel_forcedeploy.called

    def test_configchannel_forcedeploy_too_much_args(self, shell):
        """
        Test configchannel_forcedeploy function. Too much arguments.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        shell.user_confirm = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            spacecmd.configchannel.do_configchannel_forcedeploy(
                shell, "base_channel illegally_entered_channel")

        assert not mprint.called
        assert not logger.error.called
        assert not logger.info.called
        assert not logger.warning.called
        assert not shell.client.configchannel.listFiles.called
        assert not shell.client.configchannel.listSubscribedSystems.called
        assert not shell.client.configchannel.deployAllSystems.called
        assert shell.help_configchannel_forcedeploy.called

    def test_configchannel_forcedeploy_no_files(self, shell):
        """
        Test configchannel_forcedeploy function. No files found (or incomplete data).

        :param shell:
        :return:
        """
        shell.client.configchannel.listFiles = MagicMock(return_value=[
            {"pfad": "/der/hund"},
        ])
        mprint = MagicMock()
        logger = MagicMock()
        shell.user_confirm = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            spacecmd.configchannel.do_configchannel_forcedeploy(shell, "base_channel")

        assert not shell.help_configchannel_forcedeploy.called
        assert not logger.error.called
        assert not logger.info.called
        assert not logger.warning.called
        assert not shell.client.configchannel.listSubscribedSystems.called
        assert not shell.client.configchannel.deployAllSystems.called
        assert shell.client.configchannel.listFiles.called
        assert mprint.called
        assert_expect(mprint.call_args_list,
                      "No files within selected configchannel.")

    def test_configchannel_forcedeploy_no_systems(self, shell):
        """
        Test configchannel_forcedeploy function. No subscribed systems.

        :param shell:
        :return:
        """
        shell.client.configchannel.listFiles = MagicMock(return_value=[
            {"path": "/tmp/file1.txt"},
            {"path": "/tmp/file2.txt"},
        ])
        shell.client.configchannel.listSubscribedSystems(return_value=[])
        mprint = MagicMock()
        logger = MagicMock()
        shell.user_confirm = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            spacecmd.configchannel.do_configchannel_forcedeploy(shell, "base_channel")

        assert not shell.help_configchannel_forcedeploy.called
        assert not logger.error.called
        assert not logger.info.called
        assert not logger.warning.called
        assert not shell.client.configchannel.deployAllSystems.called
        assert shell.client.configchannel.listSubscribedSystems.called
        assert shell.client.configchannel.listFiles.called
        assert mprint.called
        assert_expect(mprint.call_args_list,
                      "Channel has no subscribed Systems")

    def test_configchannel_forcedeploy_deploy_output(self, shell):
        """
        Test configchannel_forcedeploy function. Output test.

        :param shell:
        :return:
        """
        shell.client.configchannel.listFiles = MagicMock(return_value=[
            {"path": "/tmp/file1.txt"},
            {"path": "/tmp/file2.txt"},
        ])
        shell.client.configchannel.listSubscribedSystems = MagicMock(return_value=[
            {"name": "butterfly.acme.org"},
            {"name": "beigebox.acme.org"},
        ])
        mprint = MagicMock()
        logger = MagicMock()
        shell.user_confirm = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            spacecmd.configchannel.do_configchannel_forcedeploy(shell, "base_channel")

        assert not shell.help_configchannel_forcedeploy.called
        assert not logger.error.called
        assert not logger.info.called
        assert not logger.warning.called
        assert shell.client.configchannel.deployAllSystems.called
        assert shell.client.configchannel.listSubscribedSystems.called
        assert shell.client.configchannel.listFiles.called
        assert mprint.called
        assert_expect(mprint.call_args_list,
                      'Force deployment of the following configfiles:',
                      '==============================================',
                      '/tmp/file1.txt\n/tmp/file2.txt', '\nOn these systems:',
                      '=================', 'beigebox.acme.org\nbutterfly.acme.org')

    def test_configchannel_filedetails_no_args(self, shell):
        """
        Test configchannel_filedetails function with no arguments.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        shell.user_confirm = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            spacecmd.configchannel.do_configchannel_filedetails(shell, "")

        assert not shell.client.configchannel.lookupFileInfo.called
        assert not shell.do_configchannel_listfiles.called
        assert not mprint.called
        assert not logger.warning.called
        assert not logger.error.called
        assert not logger.info.called
        assert shell.help_configchannel_filedetails.called

    def test_configchannel_filedetails_wrong_amt_args(self, shell):
        """
        Test configchannel_filedetails function with wrong amount of arguments.

        :param shell:
        :return:
        """
        for args in ["one", "one two three four", "one two three four five"]:
            mprint = MagicMock()
            logger = MagicMock()
            shell.user_confirm = MagicMock()
            with patch("spacecmd.configchannel.print", mprint) as prt, \
                    patch("spacecmd.configchannel.logging", logger) as lgr:
                spacecmd.configchannel.do_configchannel_filedetails(shell, args)

            assert not shell.client.configchannel.lookupFileInfo.called
            assert not shell.do_configchannel_listfiles.called
            assert not mprint.called
            assert not logger.warning.called
            assert not logger.error.called
            assert not logger.info.called
            assert shell.help_configchannel_filedetails.called

    def test_configchannel_filedetails_wrong_revision(self, shell):
        """
        Test configchannel_filedetails function with wrong amount of arguments.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        shell.user_confirm = MagicMock()
        with patch("spacecmd.configchannel.print", mprint) as prt, \
                patch("spacecmd.configchannel.logging", logger) as lgr:
            spacecmd.configchannel.do_configchannel_filedetails(
                shell, "base_channel /tmp/file.txt kaboom")

        assert not shell.client.configchannel.lookupFileInfo.called
        assert not shell.do_configchannel_listfiles.called
        assert not mprint.called
        assert not logger.warning.called
        assert not logger.info.called
        assert shell.help_configchannel_filedetails.called
        assert logger.error.called

        assert_args_expect(logger.error.call_args_list,
                           [(('Invalid revision: %s', 'kaboom'), {})])
