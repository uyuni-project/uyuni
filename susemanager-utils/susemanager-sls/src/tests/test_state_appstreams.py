import pytest
from unittest.mock import patch

from ..states import appstreams

def mock_enable(appstreams):
    return True, "AppStreams enabled.", {"enabled": appstreams}

def mock_disable(appstreams):
    return True, "AppStreams disabled.", {"disabled": appstreams}

def mock_get_currently_enabled():
    return ["module1", "module2"]

@pytest.mark.parametrize("test_mode, appstreams_to_enable, expected_result", [
    (
        True,
        ["maven", "nginx"],
        {
            "name": "test_state",
            "result": None,
            "changes": {
                "ret": {
                    "enabled": ["maven", "nginx"]
                }
            },
            "comment": "The following appstreams would be enabled: ['maven', 'nginx']"
        }
    ),
    (
        True,
        [],
        {
            "name": "test_state",
            "result": None,
            "changes": {},
            "comment": "No AppStreams to enable provided"
        }
    ),
    (
        False,
        ["maven"],
        {
            "name": "test_state",
            "result": True,
            "changes": {
                "enabled": ["maven"],
            },
            "comment": "AppStreams enabled."
        }
    ),
    (
        False,
        [],
        {
            "name": "test_state",
            "result": True,
            "changes": {},
            "comment": "No AppStreams to enable provided"
        }
    )
])
def test_enabled(test_mode, appstreams_to_enable, expected_result):
    appstreams.__salt__ = {
        "appstreams.get_enabled_modules": mock_get_currently_enabled,
        "appstreams.enable": mock_enable,
        "appstreams.disable": mock_disable,
    }
    appstreams.__opts__ = {"test": test_mode}
    assert appstreams.enabled("test_state", appstreams_to_enable) == expected_result

@pytest.mark.parametrize("test_mode, appstreams_to_disable, expected_result", [
    (
        True,
        ["ruby", "php"],
        {
            "name": "test_state",
            "result": None,
            "changes": {
                "ret": {
                    "disabled": ["ruby", "php"]
                }
            },
            "comment": "The following appstreams would be disabled: ['ruby', 'php']"
        }
    ),
    (
        True,
        [],
        {
            "name": "test_state",
            "result": None,
            "changes": {},
            "comment": "No AppStreams to disable provided"
        }
    ),
    (
        False,
        ["postgresql"],
        {
            "name": "test_state",
            "result": True,
            "changes": {
                "disabled": ["postgresql"],
            },
            "comment": "AppStreams disabled."
        }
    ),
    (
        False,
        [],
        {
            "name": "test_state",
            "result": True,
            "changes": {},
            "comment": "No AppStreams to disable provided"
        }
    )
])
def test_disabled(test_mode, appstreams_to_disable, expected_result):
    appstreams.__salt__ = {
        "appstreams.get_enabled_modules": mock_get_currently_enabled,
        "appstreams.enable": mock_enable,
        "appstreams.disable": mock_disable,
    }
    appstreams.__opts__ = {"test": test_mode}
    assert appstreams.disabled("test_state", appstreams_to_disable) == expected_result