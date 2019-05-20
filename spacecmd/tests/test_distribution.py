# coding: utf-8
"""
Test distribution
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect
import pytest
import spacecmd.distribution


class TestSCDistribution:
    """
    Test suite for distribution commands of the spacecmd.
    """
    def test_distribution_create_no_args(self, shell):
        """
        Test do_distribution_create with no args.

        :param shell:
        :return:
        """
        shell.client.kickstart.tree.listInstallTypes = MagicMock(return_value=[
            {"label": "image"},
        ])
        shell.client.kickstart.tree.update = MagicMock()
        shell.client.kickstart.tree.create = MagicMock()
        shell.list_base_channels = MagicMock(return_value=["base-channel"])

        mprint = MagicMock()
        prompt = MagicMock(side_effect=[
            "name", "/path/tree", "base-channel", "image"
        ])
        logger = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
            patch("spacecmd.distribution.prompt_user", prompt) as prmt, \
            patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_create(shell, "")

        assert mprint.called
        assert prompt.called
        assert shell.client.kickstart.tree.listInstallTypes.called
        assert shell.client.kickstart.tree.create.called
        assert not shell.client.kickstart.tree.update.called

        # Check STDOUT consistency
        exp = [
            '', 'Base Channels',
            '-------------', 'base-channel', '', '',
            'Install Types', '-------------', 'image', ''
        ]
        for call in mprint.call_args_list:
            assert_expect([call], next(iter(exp)))
            exp.pop(0)
        assert not exp

        for call in shell.client.kickstart.tree.create.call_args_list:
            args, kw = call
            assert args == (shell.session, "name", "/path/tree", "base-channel", "image")
            assert not kw

        assert_expect(shell.client.kickstart.tree.listInstallTypes.call_args_list,
                      shell.session)

    def test_distribution_create_no_args_update_mode(self, shell):
        """
        Test do_distribution_create with no args with update mode.

        :param shell:
        :return:
        """
        shell.client.kickstart.tree.listInstallTypes = MagicMock(return_value=[
            {"label": "image"},
        ])
        shell.client.kickstart.tree.update = MagicMock()
        shell.client.kickstart.tree.create = MagicMock()
        shell.list_base_channels = MagicMock(return_value=["base-channel"])

        mprint = MagicMock()
        prompt = MagicMock(side_effect=[
            "name", "/path/tree", "base-channel", "image"
        ])
        logger = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
            patch("spacecmd.distribution.prompt_user", prompt) as prmt, \
            patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_create(shell, "", update=True)

        assert not mprint.called
        assert not prompt.called
        assert not shell.client.kickstart.tree.listInstallTypes.called
        assert not shell.client.kickstart.tree.create.called
        assert not shell.client.kickstart.tree.update.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, "The name of the distribution is required")

    def test_distribution_create_args_ds_update_mode(self, shell):
        """
        Test do_distribution_create with distribution name in update mode.

        :param shell:
        :return:
        """
        shell.client.kickstart.tree.listInstallTypes = MagicMock(return_value=[
            {"label": "image"},
        ])
        shell.client.kickstart.tree.update = MagicMock()
        shell.client.kickstart.tree.create = MagicMock()
        shell.list_base_channels = MagicMock(return_value=["base-channel"])

        mprint = MagicMock()
        prompt = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
            patch("spacecmd.distribution.prompt_user", prompt) as prmt, \
            patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_create(shell, "-n myname", update=True)

        assert not mprint.called
        assert not prompt.called
        assert not shell.client.kickstart.tree.listInstallTypes.called
        assert not shell.client.kickstart.tree.create.called
        assert not shell.client.kickstart.tree.update.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, "A path is required")

    def test_distribution_create_args_dspt_update_mode(self, shell):
        """
        Test do_distribution_create with distribution name and path in update mode.

        :param shell:
        :return:
        """
        shell.client.kickstart.tree.listInstallTypes = MagicMock(return_value=[
            {"label": "image"},
        ])
        shell.client.kickstart.tree.update = MagicMock()
        shell.client.kickstart.tree.create = MagicMock()
        shell.list_base_channels = MagicMock(return_value=["base-channel"])

        mprint = MagicMock()
        prompt = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
            patch("spacecmd.distribution.prompt_user", prompt) as prmt, \
            patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_create(
                shell, "-n myname -p /path/tree", update=True)

        assert not mprint.called
        assert not prompt.called
        assert not shell.client.kickstart.tree.listInstallTypes.called
        assert not shell.client.kickstart.tree.create.called
        assert not shell.client.kickstart.tree.update.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, "A base channel is required")

    def test_distribution_create_args_dsptbc_update_mode(self, shell):
        """
        Test do_distribution_create with distribution name, path and base channel
        in update mode.

        :param shell:
        :return:
        """
        shell.client.kickstart.tree.listInstallTypes = MagicMock(return_value=[
            {"label": "image"},
        ])
        shell.client.kickstart.tree.update = MagicMock()
        shell.client.kickstart.tree.create = MagicMock()
        shell.list_base_channels = MagicMock(return_value=["base-channel"])

        mprint = MagicMock()
        prompt = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
            patch("spacecmd.distribution.prompt_user", prompt) as prmt, \
            patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_create(
                shell, "-n myname -p /path/tree -b base-channel", update=True)

        assert not mprint.called
        assert not prompt.called
        assert not shell.client.kickstart.tree.listInstallTypes.called
        assert not shell.client.kickstart.tree.create.called
        assert not shell.client.kickstart.tree.update.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, "An install type is required")

    def test_distribution_create_args_dsptbcit_update_mode(self, shell):
        """
        Test do_distribution_create with distribution name, path, base channel
        and install type in update mode.

        :param shell:
        :return:
        """
        shell.client.kickstart.tree.listInstallTypes = MagicMock(return_value=[
            {"label": "image"},
        ])
        shell.client.kickstart.tree.update = MagicMock()
        shell.client.kickstart.tree.create = MagicMock()
        shell.list_base_channels = MagicMock(return_value=["base-channel"])

        mprint = MagicMock()
        prompt = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
            patch("spacecmd.distribution.prompt_user", prompt) as prmt, \
            patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_create(
                shell, "-n myname -p /path/tree -b base-channel -t image", update=True)

        assert not mprint.called
        assert not prompt.called
        assert not shell.client.kickstart.tree.create.called
        assert not logger.error.called
        assert not shell.client.kickstart.tree.listInstallTypes.called
        assert shell.client.kickstart.tree.update.called

        for call in shell.client.kickstart.tree.update.call_args_list:
            args, kw = call
            assert args == (shell.session, "myname", "/path/tree", "base-channel", "image")
            assert not kw

    def test_distribution_list_noarg_noret(self, shell):
        """
        Test do_distribution_list without argumnets, no return option.

        :param shell:
        :return:
        """
        shell.client.kickstart.listAutoinstallableChannels = MagicMock(return_value=[
            {"label": "channel-name"},
        ])
        shell.client.kickstart.tree.list = MagicMock(return_value=[
            {"label": "some-channel"},
            {"label": "some-other-channel"},
        ])
        mprint = MagicMock()
        with patch("spacecmd.distribution.print", mprint) as prn:
            out = spacecmd.distribution.do_distribution_list(shell, "")

        assert out is None
        assert mprint.called
        assert_expect(mprint.call_args_list, "some-channel\nsome-other-channel")

    def test_distribution_list_noarg_ret(self, shell):
        """
        Test do_distribution_list without argumnets, return data mode.

        :param shell:
        :return:
        """
        shell.client.kickstart.listAutoinstallableChannels = MagicMock(return_value=[
            {"label": "channel-name"},
        ])
        shell.client.kickstart.tree.list = MagicMock(return_value=[
            {"label": "some-channel"},
            {"label": "some-other-channel"},
        ])
        mprint = MagicMock()
        with patch("spacecmd.distribution.print", mprint) as prn:
            out = spacecmd.distribution.do_distribution_list(shell, "", doreturn=True)

        assert out is not None
        assert type(out) == list
        assert not mprint.called
        assert out == ['some-channel', 'some-other-channel']

    def test_distribution_delete_noargs(self, shell):
        """
        Test do_distribution_delete with no arguments.

        :param shell:
        :return:
        """
        shell.do_distribution_list = MagicMock()
        shell.help_distribution_delete = MagicMock()
        shell.client.kickstart.tree.delete = MagicMock()
        shell.user_confirm = MagicMock()
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
                patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_delete(shell, "")

        assert not logger.debug.called
        assert not logger.error.called
        assert not mprint.called
        assert not shell.do_distribution_list.called
        assert not shell.client.kickstart.tree.delete.called
        assert not shell.user_confirm.called
        assert shell.help_distribution_delete.called

    def test_distribution_delete_args_no_match(self, shell):
        """
        Test do_distribution_delete with wrong arguments.

        :param shell:
        :return:
        """
        shell.do_distribution_list = MagicMock(return_value=["bar"])
        shell.help_distribution_delete = MagicMock()
        shell.client.kickstart.tree.delete = MagicMock()
        shell.user_confirm = MagicMock()
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
                patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_delete(shell, "foo*")

        assert logger.debug.called
        assert logger.error.called
        assert not mprint.called
        assert not shell.client.kickstart.tree.delete.called
        assert not shell.user_confirm.called
        assert not shell.help_distribution_delete.called

        assert_expect(logger.debug.call_args_list,
                      "distribution_delete called with args ['foo.*'], dists=[]")
        assert_expect(logger.error.call_args_list,
                      "No distributions matched argument ['foo.*']")

    def test_distribution_delete_args_match_no_confirm(self, shell):
        """
        Test do_distribution_delete with correct arguments, not confirmed to delete.

        :param shell:
        :return:
        """
        shell.do_distribution_list = MagicMock(return_value=["bar"])
        shell.help_distribution_delete = MagicMock()
        shell.client.kickstart.tree.delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
                patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_delete(shell, "b*")

        assert not logger.error.called
        assert not shell.client.kickstart.tree.delete.called
        assert not shell.help_distribution_delete.called
        assert logger.debug.called
        assert mprint.called
        assert shell.user_confirm.called

        assert_expect(logger.debug.call_args_list,
                      "distribution_delete called with args ['b.*'], dists=['bar']")
        assert_expect(shell.user_confirm.call_args_list,
                      "Delete distribution tree(s) [y/N]:")
        assert_expect(mprint.call_args_list, "bar")

    def test_distribution_delete_args_match_confirm(self, shell):
        """
        Test do_distribution_delete with correct arguments, confirmed to delete.

        :param shell:
        :return:
        """
        shell.do_distribution_list = MagicMock(return_value=["bar"])
        shell.help_distribution_delete = MagicMock()
        shell.client.kickstart.tree.delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
                patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_delete(shell, "b*")

        assert not logger.error.called
        assert not shell.help_distribution_delete.called
        assert shell.client.kickstart.tree.delete.called
        assert logger.debug.called
        assert mprint.called
        assert shell.user_confirm.called

        assert_expect(logger.debug.call_args_list,
                      "distribution_delete called with args ['b.*'], dists=['bar']")
        assert_expect(shell.user_confirm.call_args_list,
                      "Delete distribution tree(s) [y/N]:")
        assert_expect(mprint.call_args_list, "bar")

        for call in shell.client.kickstart.tree.delete.call_args_list:
            args, kw = call
            assert args == (shell.session, "bar",)

    def test_distribution_details_noargs(self, shell):
        """
        Test do_distribution_details with no arguments.

        :param shell:
        :return:
        """
        shell.help_distribution_details = MagicMock()
        shell.client.kickstart.tree.getDetails = MagicMock()
        shell.client.channel.software.getDetails = MagicMock()
        shell.do_distribution_list = MagicMock()
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
                patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_details(shell, "")

        assert not logger.error.called
        assert not logger.debug.called
        assert not shell.client.kickstart.tree.getDetails.called
        assert not shell.client.channel.software.getDetails.called
        assert not shell.do_distribution_list.called
        assert shell.help_distribution_details.called

    def test_distribution_details_no_dists(self, shell):
        """
        Test do_distribution_details with no distributions found.

        :param shell:
        :return:
        """
        shell.help_distribution_details = MagicMock()
        shell.client.kickstart.tree.getDetails = MagicMock()
        shell.client.channel.software.getDetails = MagicMock()
        shell.do_distribution_list = MagicMock(return_value=[])
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
                patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_details(shell, "test*")

        assert not shell.client.kickstart.tree.getDetails.called
        assert not shell.client.channel.software.getDetails.called
        assert not shell.help_distribution_details.called
        assert not mprint.called
        assert logger.debug.called
        assert logger.error.called
        assert shell.do_distribution_list.called

        assert_expect(logger.debug.call_args_list,
                      "distribution_details called with args ['test.*'], dists=[]")
        assert_expect(logger.error.call_args_list,
                      "No distributions matched argument ['test.*']")

    def test_distribution_details_list(self, shell):
        """
        Test do_distribution_details lister.

        :param shell:
        :return:
        """
        shell.help_distribution_details = MagicMock()
        shell.client.kickstart.tree.getDetails = MagicMock(side_effect=[
            {"channel_id": "ch-id-1", "label": "dist-1", "abs_path": "/tmp/d1"},
            {"channel_id": "ch-id-2", "label": "dist-2", "abs_path": "/tmp/d2"},
        ])
        shell.client.channel.software.getDetails = MagicMock(side_effect=[
            {"label": "channel-one"},
            {"label": "channel-two"},
        ])
        shell.do_distribution_list = MagicMock(return_value=["dist-1", "dist-2"])
        shell.SEPARATOR = "---"
        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.distribution.print", mprint) as prn, \
                patch("spacecmd.distribution.logging", logger) as lgr:
            spacecmd.distribution.do_distribution_details(shell, "dist*")

        assert not shell.help_distribution_details.called
        assert not logger.error.called
        assert shell.client.kickstart.tree.getDetails.called
        assert shell.client.channel.software.getDetails.called
        assert logger.debug.called
        assert shell.do_distribution_list.called
        assert mprint.called

        exp = [
            'Name:    dist-1',
            'Path:    /tmp/d1',
            'Channel: channel-one',
            '---',
            'Name:    dist-2',
            'Path:    /tmp/d2',
            'Channel: channel-two'
        ]
        for call in mprint.call_args_list:
            assert_expect([call], next(iter(exp)))
            exp.pop(0)
        assert not exp

    def test_distribution_rename_noargs(self, shell):
        """
        Test do_distribution_rename without arguments.

        :param shell:
        :return:
        """
        for args in ["", "foo"]:
            shell.help_distribution_rename = MagicMock()
            shell.client.kickstart.tree.rename = MagicMock()

            spacecmd.distribution.do_distribution_rename(shell, "")

            assert not shell.client.kickstart.tree.rename.called
            assert shell.help_distribution_rename.called

    def test_distribution_rename(self, shell):
        """
        Test do_distribution_rename.

        :param shell:
        :return:
        """
        shell.help_distribution_rename = MagicMock()
        shell.client.kickstart.tree.rename = MagicMock()

        spacecmd.distribution.do_distribution_rename(shell, "source destination")

        assert not shell.help_distribution_rename.called
        assert shell.client.kickstart.tree.rename.called

        for call in shell.client.kickstart.tree.rename.call_args_list:
            args, kw = call
            assert args == (shell.session, "source", "destination")

