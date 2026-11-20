# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

"""
Tests for RPM header string handling.
"""

import pytest

pytest.importorskip("rpm")

# pylint: disable-next=wrong-import-position
from uyuni.common.rhn_rpm import RPM_Header

# "方正" encoded as GBK: not valid UTF-8
GBK_NAME = b"/usr/share/fonts/truetype/\xb7\xbd\xd5\xfd.ttf"


def _header(**tags):
    # Bypass __init__: it extracts signatures from a real rpm header
    header = RPM_Header.__new__(RPM_Header)
    header.hdr = tags
    return header


@pytest.mark.parametrize(
    "raw",
    [
        # rpm < 4.15 returns bytes
        GBK_NAME,
        # rpm >= 4.15 returns surrogate-escaped str
        GBK_NAME.decode("utf-8", "surrogateescape"),
    ],
)
def test_non_utf8_header_data_is_storable(raw):
    header = _header(filenames=[raw], summary=raw)

    for value in (header["filenames"][0], header["summary"]):
        # psycopg2 encodes parameters as UTF-8; surrogates make it fail
        value.encode("utf-8")
        assert value == "/usr/share/fonts/truetype/.ttf"


def test_valid_utf8_header_data_is_unchanged():
    name = "/usr/share/fonts/truetype/方正仿宋.ttf"
    header = _header(filenames=[name, name.encode("utf-8")], size=42)

    assert header["filenames"] == [name, name]
    assert header["size"] == 42
