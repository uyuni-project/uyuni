"""
Copyright (C) 2017 Oracle and/or its affiliates. All rights reserved.

# SPDX-License-Identifier: GPL-2.0-only

Utility to get system UTC offset and format as needed by DBs.
"""

import time


def get_utc_offset():
    """Return the UTC offset, allowing for DST."""
    is_dst = time.daylight and time.localtime().tm_isdst > 0
    utc_offset = -time.timezone
    if is_dst:
        utc_offset = -time.altzone
    mins = divmod(utc_offset, 60)[0]
    hours, mins = divmod(mins, 60)
    # pylint: disable-next=consider-using-f-string
    return "{0:+03d}:{1:02d}".format(hours, mins)


if __name__ == "__main__":
    # pylint: disable-next=consider-using-f-string
    print("UTC offset (allowing for DST if in effect): %s" % get_utc_offset())
