# coding: utf-8
"""
Unit test for the spacecmd.shell module.
"""
from spacecmd.shell import SpacewalkShell


class TestSCShell:
    """
    Test shell in spacecmd.
    """
    def test_shell_history(self):
        """
        Test history length.
        """
        assert SpacewalkShell.HISTORY_LENGTH == 1024
