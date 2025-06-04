#!/usr/bin/env python3

import re
import json

# open salt event log and create correct json from it
errors = []
item = []
f = open("/var/log/rhn/salt-event.log", "r")
for line in f.readlines():
    item.append(line)
    if re.search(r"^\}", line):
        if '"result": false' in "".join(item):
            item[0] = "{"
            errors.append(item)
        item = []
f.close()

# parse json and find results ending as failed
failure_count = 0
for error in errors:
    try:
        joined_error = "".join(error)
        j = json.loads(joined_error)
        if not "return" in j:
            continue
        if not isinstance(j["return"], dict):
            continue
        for k, v in j["return"].items():
            if isinstance(v, dict) and not v.get("result", True):
                failure_count += 1
                print(
                    "\n# Failure",
                    failure_count,
                    ", _stamp:",
                    j["_stamp"],
                    json.dumps(v, sort_keys=True, indent=4),
                )
    except ValueError as e:
        print("JSON cannot be parsed due to {0}".format(e))
        continue
