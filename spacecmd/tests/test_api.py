# coding: utf-8
"""
Test suite for spacecmd.api
"""
from mock import MagicMock, patch
from spacecmd import api


class TestSCAPI:
    """
    Test class for testing spacecmd API.
    """
    def test_no_args(self):
        """
        Test calling API without any arguments.
        """
        shell = MagicMock()
        shell.help_api = MagicMock()
        api.do_api(shell, "")
        assert shell.help_api.called
