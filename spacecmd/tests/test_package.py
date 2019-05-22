# coding: utf-8
"""
Test suite for spacecmd.package module.
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect
import spacecmd.package


class TestSCPackage:
    """
    Test suite for package module.
    """
    def test_package_details_noargs(self, shell):
        """
        Test do_package_details with no arguments call.

        :param shell:
        :return:
        """
        shell.help_package_details = MagicMock()
        shell.get_package_id = MagicMock()
        shell.client.packages.listProvidingChannels = MagicMock()
        shell.client.system.listSystemsWithPackage = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.report.print", mprint) as prn:
            spacecmd.package.do_package_details(shell, "")

        assert not shell.get_package_id.called
        assert not shell.client.packages.listProvidingChannels.called
        assert not shell.client.system.listSystemsWithPackage.called
        assert shell.help_package_details.called
