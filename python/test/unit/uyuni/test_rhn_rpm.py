#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2026 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

import sys
from unittest.mock import MagicMock

import pytest

try:
    import rpm
except ImportError:
    # The rpm bindings are a native module that is not always available where
    # the unit tests run. Only the two tags read by modularity_label() are
    # needed here, so stub the module with their real values.
    rpm = MagicMock()
    rpm.RPMTAG_MODULARITYLABEL = 5096
    rpm.RPMTAG_DISTTAG = 1155
    sys.modules["rpm"] = rpm

# pylint: disable-next=wrong-import-position
from uyuni.common.rhn_rpm import RPM_Header

MODULE = "postgresql:16:8090020240206124302:rhel8"


def _header(tags):
    """Build an RPM_Header around a plain dict, skipping __init__ (which reads
    the signatures out of a real rpm header)."""
    hdr = RPM_Header.__new__(RPM_Header)
    hdr.hdr = tags
    return hdr


@pytest.mark.parametrize(
    "disttag, expected",
    [
        pytest.param(f"module({MODULE})", MODULE, id="str"),
        pytest.param(f"module({MODULE})".encode(), MODULE.encode(), id="bytes"),
    ],
)
def test_modularity_label_from_disttag(disttag, expected):
    """DISTTAG is returned as bytes by the older rpm bindings and as str since
    Python 3.13: both have to be recognized as a modularity label."""
    assert _header({rpm.RPMTAG_DISTTAG: disttag}).modularity_label() == expected


@pytest.mark.parametrize(
    "disttag",
    [pytest.param("el8", id="str"), pytest.param(b"el8", id="bytes")],
)
def test_modularity_label_ignores_plain_disttag(disttag):
    """A DISTTAG that is not a module(...) wrap yields no modularity label."""
    assert _header({rpm.RPMTAG_DISTTAG: disttag}).modularity_label() is None


def test_modularity_label_prefers_modularitylabel_tag():
    """MODULARITYLABEL wins over DISTTAG when both are present."""
    hdr = _header(
        {
            rpm.RPMTAG_MODULARITYLABEL: "nodejs:20:20240101:abcdef01",
            rpm.RPMTAG_DISTTAG: f"module({MODULE})",
        }
    )
    assert hdr.modularity_label() == "nodejs:20:20240101:abcdef01"


def test_modularity_label_absent():
    """No modularity tag at all yields None."""
    assert _header({}).modularity_label() is None
