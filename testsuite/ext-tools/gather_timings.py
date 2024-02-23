#!/usr/bin/env python3
"""Program to gather timeout information from /var/log/rhn/reposync."""

import os
from datetime import datetime
import math

files = [file for file in os.listdir(".") if os.path.isfile(file)]

for file in files:
    if file.endswith(".log"):
        with open(file, "r", encoding="utf-8") as contents:
            lines = contents.readlines()
            first_line = lines[0].rstrip()
            last_line = lines[-1].rstrip()
            first_date = datetime.strptime(
                " ".join(first_line.split()[0:2]), "%Y/%m/%d %H:%M:%S"
            )
            last_date = datetime.strptime(
                " ".join(last_line.split()[0:2]), "%Y/%m/%d %H:%M:%S"
            )
            duration = (last_date - first_date).seconds
            to_use = math.ceil(duration / 30 + 0.001) * 60
            channel = file.rstrip(r"\.log")
            print(f"  '{channel}' => {to_use},")
