"""
Unit tests for the sap module
"""
from ..modules import sap

from unittest.mock import patch, MagicMock
import pytest

@pytest.mark.parametrize(
    "mock_walk_return, expected_result",
    [
        (
            [
                ("/usr/sap", ["F1B", "F2C"], []),
                ("/usr/sap/F1B", ["ASCS00", "DVEBMGS00"], []),
                ("/usr/sap/F2C", ["HDB00"], []),
            ],
            [
                {"system_id": "F1B", "instance_type": "ASCS"},
                {"system_id": "F1B", "instance_type": "DVEBMGS"},
                {"system_id": "F2C", "instance_type": "HDB"},
            ],
        ),
        ([], []),
    ],
)
def test_get_workloads(mock_walk_return, expected_result):
    mock_os_path_exists = MagicMock(return_value=True)
    mock_os_walk = MagicMock(return_value=mock_walk_return)
    with patch("os.path.exists", mock_os_path_exists), patch("os.walk", mock_os_walk):
        assert sap.get_workloads() == expected_result

def test_no_sap_directory():
    with patch("os.path.exists", return_value=False):
        result = sap.get_workloads()
        assert (len(result) > 0) == False
