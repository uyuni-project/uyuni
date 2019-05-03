# coding: utf-8
"""
Tests for spacewalk-manager-channel-lifecycle script.
"""

from mock import MagicMock, patch
from . import helpers

helpers.symlink_source("spacewalk-manage-channel-lifecycle", "smcl")
from . import smcl
helpers.unsymlink_source("smcl")


class TestSMCL:
    """
    Integration/unit tests fusion for spacewalk-manage-channel-lifecycle script.
    """
    def test_get_current_phase(self):
        """
        Get configuration credentials.

        :return:
        """
        smcl.phases = ["dev", "test", "prod"]
        assert smcl.get_current_phase("develop") == "dev"

    def test_configuration_saved(self):
        """
        Test configuration file is saved to the disk.

        :return:
        """
