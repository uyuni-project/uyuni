# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

"""
Grain exposing the system boot time as Unix epoch seconds.
"""

B_TIME_FILE = "/proc/stat"


def boot_time():
    """
    Return the system boot time as Unix epoch seconds.
    """
    try:
        with open(B_TIME_FILE, "r", encoding="utf-8") as proc_stat:
            for line in proc_stat.read().splitlines():
                if line.startswith("btime "):
                    return {"boot_time": int(line.split()[1])}
    except (OSError, ValueError, IndexError):
        pass

    return {}
