# coding: utf-8
"""
Test suite for custominfo source
"""
from mock import MagicMock, patch, mock_open
from spacecmd import custominfo
from helpers import shell
import pytest


class TestSCCusomInfo:
    """
    Test for custominfo API.
    """
    def test_do_custominfo_createkey(self, shell):
        """
        Test do_custominfo_createkey do not break on no key name provided, falling back to interactive mode.
        """
        shell.client.system.custominfo.createKey = MagicMock()
        prompter = MagicMock(side_effect=["", "", Exception("Empty key")])

        with patch("spacecmd.custominfo.prompt_user", prompter):
            with pytest.raises(Exception) as exc:
                custominfo.do_custominfo_createkey(shell, "")

        assert "Empty key" in str(exc)

