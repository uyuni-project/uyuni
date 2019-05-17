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
