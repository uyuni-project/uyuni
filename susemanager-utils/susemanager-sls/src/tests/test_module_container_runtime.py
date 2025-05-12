"""
Unit tests for the container_runtime module
"""

# pylint: disable-next=unused-import
import os

from ..modules import container_runtime

# pylint: disable-next=unused-import
from unittest.mock import patch, mock_open, MagicMock
import pytest


@pytest.fixture(autouse=True)
def stripped_env(monkeypatch):
    """Strip env variables that are use by the system under test."""
    monkeypatch.delenv("container", raising=False)


@pytest.mark.parametrize(
    "mock_read_file_return, mock_exists_return, expected_result",
    [
        ("docker", {"/proc/self/cgroup": True, "/.dockerenv": True}, "docker"),
        ("", {"/proc/vz": True, "/proc/bc": False}, "openvz"),
        ("podman", {"/run/.containerenv": True}, "podman"),
        ("", {"/__runsc_containers__": True}, "gvisor"),
        ("", {"/run/.containerenv": False}, None),
    ],
)
def test_get_container_runtime(
    mock_read_file_return, mock_exists_return, expected_result
):
    mock_read_file = MagicMock(return_value=mock_read_file_return)
    mock_exists = MagicMock(
        side_effect=lambda path: mock_exists_return.get(path, False)
    )
    # pylint: disable-next=protected-access
    container_runtime._read_file = mock_read_file
    with patch("os.path.exists", mock_exists):
        assert container_runtime.get_container_runtime() == expected_result


@pytest.mark.parametrize(
    "file_name, expected_result",
    [
        ("/run/.containerenv", "podman"),
        ("/.dockerenv", "docker"),
        ("/var/run/secrets/kubernetes.io/serviceaccount", "kube"),
    ],
)
def test_detect_container_files(file_name, expected_result):
    mock_exists = MagicMock(side_effect=lambda path: path == file_name)
    with patch("os.path.exists", mock_exists):
        # pylint: disable-next=protected-access
        assert container_runtime._detect_container_files() == expected_result


def test_detect_container_files_not_found():
    mock_exists = MagicMock(side_effect=lambda path: False)
    with patch("os.path.exists", mock_exists):
        # pylint: disable-next=protected-access
        assert container_runtime._detect_container_files() == "not-found"
