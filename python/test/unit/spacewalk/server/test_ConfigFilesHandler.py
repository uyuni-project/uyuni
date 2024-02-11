#!/usr/bin/python3
#  pylint: disable=missing-module-docstring,invalid-name

import base64
from unittest.mock import patch

import pytest
from spacewalk.server import configFilesHandler


@pytest.fixture
def skeleton_row():
    return {
        "path": "/etc/dummy",
        "config_channel": "dummy",
        "file_contents": None,
        "is_binary": None,
        "checksum_type": "sha256",
        "checksum": None,
        "delim_start": "{|",
        "delim_end": "|}",
        "revision": 1,
        "username": "root",
        "groupname": "root",
        "filemode": 644,
        "label": "dummy-file",
        "selinux_ctx": None,
        "symlink": None,
    }


def my_read_lob(lob):
    """Same as postgresql driver, but does not need to setup a DB connection."""
    return bytes(lob)


def my_get_client_capabilities():
    return ["configfiles.base64_enc"]


get_client_capabilities = (
    "spacewalk.server.configFilesHandler.rhnCapability.get_client_capabilities"
)
read_lob = "spacewalk.server.configFilesHandler.rhnSQL.read_lob"


@patch(get_client_capabilities, my_get_client_capabilities)
@patch(read_lob, my_read_lob)
# pylint: disable-next=redefined-outer-name
def test_format_file_results_utf8(skeleton_row):
    utf8_string = "Hello I am an UTF-8 string. Look at me¡¡¡"
    row = skeleton_row
    row["file_contents"] = utf8_string.encode("utf8")
    row["is_binary"] = "N"

    formatted = configFilesHandler.format_file_results(row)

    assert formatted["file_contents"] == base64.encodestring(
        utf8_string.encode("utf8")
    ).decode("utf8")


@patch(get_client_capabilities, my_get_client_capabilities)
@patch(read_lob, my_read_lob)
# pylint: disable-next=redefined-outer-name
def test_format_file_results_binary_blob(skeleton_row):
    blob = b"\xaa\xdb\xe3\xdc\xfd\x19\xdc\x12\xc3\x0f\x07\x03\x89\xe0\xde"
    row = skeleton_row
    row["file_contents"] = blob
    row["is_binary"] = "Y"

    formatted = configFilesHandler.format_file_results(row)

    assert formatted["file_contents"] == base64.encodestring(blob).decode("utf8")


@patch(get_client_capabilities, my_get_client_capabilities)
@patch(read_lob, my_read_lob)
# pylint: disable-next=redefined-outer-name
def test_format_file_results_latin1(skeleton_row):
    latin1_string = "Hello I am a Latin-1 string. Don't look at me¡¡¡"
    row = skeleton_row
    row["file_contents"] = latin1_string.encode("latin-1")
    row["is_binary"] = "N"

    formatted = configFilesHandler.format_file_results(row)

    assert formatted["file_contents"] == base64.encodestring(
        # NOTE: currently the original encoding is lost before encoding in base64
        # this test keeps the behaviour as is
        latin1_string.encode("utf8")
    ).decode("utf8")
