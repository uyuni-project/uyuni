# coding: utf-8
"""
Test activation key methods.
"""
from mock import MagicMock, patch
import pytest
import spacecmd.activationkey


@pytest.fixture
def shell():
    """
    Create fake shell.
    """
    base = MagicMock()
    base.do_activationkey_list = MagicMock(return_value="do_activation_list")

    return base


class TestSCActivationKey:
    """
    Test activation key.
    """
    def test_completer_ak_addpackages(self, shell):
        """
        Test tab completer activation keys on addpackages.
        """
        print()
        text = "Communications satellite used by the military for star wars."
        completer = MagicMock()
        with patch("spacecmd.activationkey.tab_completer", completer):
            spacecmd.activationkey.complete_activationkey_addpackages(shell, text, "do this", None, None)
            assert completer.called
            call_id, ret_text = completer.call_args_list[0][0]
            assert call_id == "do_activation_list"
            assert ret_text == text
