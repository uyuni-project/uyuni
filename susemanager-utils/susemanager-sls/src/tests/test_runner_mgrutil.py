"""
Tests for the mgrutil runner.
"""

import os
import sys

from unittest.mock import patch

from . import mockery

mockery.setup_environment()

# The runners live outside of the src/ tree, make them importable
sys.path.insert(
    0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "modules"))
)

# pylint: disable-next=wrong-import-position
from runners import mgrutil


def _write_known_hosts(home_dir, content):
    """
    Create a known_hosts file in the .ssh directory of the given home directory.

    :param home_dir: path of the home directory
    :param content: content of the known_hosts file
    :return: path of the created known_hosts file
    """
    ssh_dir = os.path.join(home_dir, ".ssh")
    os.makedirs(ssh_dir)
    path = os.path.join(ssh_dir, "known_hosts")
    # pylint: disable-next=unspecified-encoding
    with open(path, "w") as known_hosts:
        known_hosts.write(content)
    return path


def _home_patch(home_dir):
    """
    Patch resolving the home directory of the salt user to the given directory.

    :param home_dir: path to use as home directory
    :return: the patch
    """
    return patch.object(
        mgrutil.os.path,
        "expanduser",
        side_effect=lambda path: home_dir if path.startswith("~") else path,
    )


def test_update_ssh_known_host_renames_entries(tmp_path):
    """
    Test renaming plain, multi-name and [host]:port entries, leaving other
    entries untouched.
    """
    home_dir = str(tmp_path)
    known_hosts = _write_known_hosts(
        home_dir,
        "old.example.com,other.example.com ssh-ed25519 AAAA old-key\n"
        "[old.example.com]:22 ssh-rsa AAAA port22-key\n"
        "[old.example.com]:1233 ssh-rsa AAAA port1233-key\n"
        "|1|hashedvalue= ssh-ed25519 AAAA hashed-key\n"
        "unrelated.example.com ssh-ed25519 AAAA unrelated-key\n",
    )

    with _home_patch(home_dir):
        result = mgrutil.update_ssh_known_host(
            "salt", "old.example.com", "new.example.com", 22
        )

    assert result == {
        "status": "success",
        "comment": "Renamed 2 known_hosts entries from old.example.com to new.example.com",
    }
    # pylint: disable-next=unspecified-encoding
    with open(known_hosts) as known_hosts_file:
        assert known_hosts_file.readlines() == [
            "new.example.com,other.example.com ssh-ed25519 AAAA old-key\n",
            "[new.example.com]:22 ssh-rsa AAAA port22-key\n",
            "[old.example.com]:1233 ssh-rsa AAAA port1233-key\n",
            "|1|hashedvalue= ssh-ed25519 AAAA hashed-key\n",
            "unrelated.example.com ssh-ed25519 AAAA unrelated-key\n",
        ]
    # the file is replaced atomically, no leftover temporary file
    assert os.listdir(os.path.join(home_dir, ".ssh")) == ["known_hosts"]


def test_update_ssh_known_host_no_match(tmp_path):
    """
    Test that the file is not rewritten when no entry matches.
    """
    home_dir = str(tmp_path)
    content = "unrelated.example.com ssh-ed25519 AAAA unrelated-key\n"
    known_hosts = _write_known_hosts(home_dir, content)

    with _home_patch(home_dir):
        result = mgrutil.update_ssh_known_host(
            "salt", "old.example.com", "new.example.com", 22
        )

    assert result["status"] == "success"
    assert "Renamed 0" in result["comment"]
    # pylint: disable-next=unspecified-encoding
    with open(known_hosts) as known_hosts_file:
        assert known_hosts_file.read() == content


def test_update_ssh_known_host_missing_file(tmp_path):
    """
    Test that a missing known_hosts file is reported as success without changes.
    """
    with _home_patch(str(tmp_path)):
        result = mgrutil.update_ssh_known_host(
            "salt", "old.example.com", "new.example.com", 22
        )

    assert result == {
        "status": "success",
        "comment": "No known_hosts file found for user salt, nothing to update",
    }
