# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

#  pylint: disable=missing-module-docstring,invalid-name
import os
import time
from unittest.mock import patch

import pytest

from spacewalk.common import rhnLog


@pytest.fixture
def berlin_tz():
    old_tz = os.environ.get("TZ")
    os.environ["TZ"] = "Europe/Berlin"
    time.tzset()
    yield
    if old_tz is None:
        del os.environ["TZ"]
    else:
        os.environ["TZ"] = old_tz
    time.tzset()


@pytest.mark.parametrize(
    "timestamp, expected",
    [
        # 2026-01-15 12:00:00 UTC, standard time (CET)
        (1768478400, "2026/01/15 13:00:00 +01:00"),
        # 2026-07-15 12:00:00 UTC, daylight saving time (CEST)
        (1784116800, "2026/07/15 14:00:00 +02:00"),
    ],
)
# pylint: disable-next=redefined-outer-name,unused-argument
def test_log_time_offset_follows_dst(berlin_tz, timestamp, expected):
    with patch("spacewalk.common.rhnLog.time.time", return_value=timestamp):
        assert rhnLog.log_time() == expected
