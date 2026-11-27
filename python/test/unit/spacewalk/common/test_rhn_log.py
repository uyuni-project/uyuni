#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2026 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SPDX-License-Identifier: GPL-2.0-only
#
import time
from unittest.mock import patch

import pytest

from spacewalk.common import rhnLog


@pytest.fixture
def berlin_tz(monkeypatch):
    monkeypatch.setenv("TZ", "Europe/Berlin")
    time.tzset()
    yield
    monkeypatch.undo()
    time.tzset()


@pytest.mark.parametrize(
    "timestamp, expected",
    [
        # 2026-10-30 13:58:11 UTC, after the switch to winter time
        (1793368691, "2026/10/30 14:58:11 +01:00"),
        # 2026-07-01 12:00:00 UTC, summer time
        (1782907200, "2026/07/01 14:00:00 +02:00"),
    ],
)
# pylint: disable-next=redefined-outer-name,unused-argument
def test_log_time_uses_offset_in_effect(berlin_tz, timestamp, expected):
    with patch("spacewalk.common.rhnLog.time.time", return_value=timestamp):
        assert rhnLog.log_time() == expected
