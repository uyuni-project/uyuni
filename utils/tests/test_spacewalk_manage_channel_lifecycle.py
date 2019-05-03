# coding: utf-8
"""
Tests for spacewalk-manager-channel-lifecycle script.
"""
import os
import pytest
import tempfile
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

    @pytest.mark.skip(reason="TBD")
    def test_argparse_port(self):
        """
        Dummy stub test for porting deprecated optparser to argparse.

        :return:
        """

    def test_configuration_saved_read(self):
        """
        Test configuration file is saved to the disk and can be read.

        :return:
        """
        with tempfile.TemporaryDirectory(prefix="smcl-", dir="/tmp") as tmpdir:
            smcl.CONF_DIR = os.path.join(tmpdir, ".spacewalk-manage-channel-lifecycle")
            smcl.USER_CONF_FILE = os.path.join(smcl.CONF_DIR, "settings.conf")
            smcl.SESSION_CACHE = os.path.join(smcl.CONF_DIR, "session")

            config = smcl.Config(smcl.USER_CONF_FILE)
            config.set("Millenium Falcon", "space speed", "75 MGLT")
            config.set("Millenium Falcon", "atmospheric speed", "1050 km/h")
            smcl.setup_config(config)

            # Save
            assert os.path.exists(os.path.join(tmpdir, ".spacewalk-manage-channel-lifecycle/settings.conf"))

            r_cfg = smcl.Config(smcl.USER_CONF_FILE)
            smcl.setup_config(r_cfg)

            assert r_cfg.get("Millenium Falcon", "space speed") == "75 MGLT"
            assert r_cfg.get("Millenium Falcon", "atmospheric speed") == "1050 km/h"
            assert r_cfg.get("general", "phases") == "dev, test, prod"
            assert r_cfg.get("general", "exclude channels") == ""
