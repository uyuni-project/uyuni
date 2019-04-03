# coding: utf-8
"""
Unit test for the spacecmd.shell module.
"""
from mock import MagicMock, patch
import os
import time
import readline
import pytest
from spacecmd.shell import SpacewalkShell, UnknownCallException


class TestSCShell:
    """
    Test shell in spacecmd.
    """
    def test_shell_history(self):
        """
        Test history length.
        """
        assert SpacewalkShell.HISTORY_LENGTH == 1024
