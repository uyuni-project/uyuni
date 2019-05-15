# coding: utf-8
"""
Test suite for the SSM module commands.
"""

from mock import MagicMock, patch, mock_open
from spacecmd import ssm
from helpers import shell, assert_expect
import pytest


class TestSCSSM:
    """
    Test for SSM module API.
    """
    def test_ssm_add_noarg(self, shell):
        """
        Test do_ssm_add no args.

        :param shell:
        :return:
        """
        shell.help_ssm_add = MagicMock()
        shell.expand_sytems = MagicMock(return_value=[])
        shell.ssm = {}
        shell.get_system_id = MagicMock(return_value=None)

        logger = MagicMock()
        save_cache = MagicMock()
        with patch("spacecmd.ssm.logging", logger) as lgr, \
            patch("spacecmd.ssm.save_cache", save_cache) as svc:
            ssm.do_ssm_add(shell, "")

        assert not logger.warning.called
        assert not logger.debug.called
        assert shell.help_ssm_add.called
