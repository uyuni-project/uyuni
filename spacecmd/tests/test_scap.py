# coding: utf-8
"""
Test suite for Scap commands at spacecmd.
"""

from unittest.mock import MagicMock, patch
from helpers import shell
import pytest


class TestScap:
    """
    Test suite for scap.
    """

    def test_scap_listxccdfscans_noarg(self, shell):
        """
        Test calling scap listxccdfscans without arguments.

        :param shell:
        :return:
        """
