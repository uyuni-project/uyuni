#!/usr/bin/env python3

import re
import json

# open salt event log and create correct json from it
errors = []
item = [] 
f = open('/var/log/rhn/salt-event.log', "r")
for line in f.readlines():
    item.append(line)
    if re.search(r"^\}", line):
        if '\"result\": false' in "".join(item):
            item[0] = "{"
            errors.append(item)
        item = [] 
f.close()

# parse json and find results ending as failed
failure_count = 0
for error in errors:
    j = json.loads("".join(error))
    if not "return" in j:
        break
    for k in j["return"]:
        if not j["return"][k]["result"]:
            failure_count += 1
            print("\n# Failure", failure_count, ", _stamp:", j['_stamp'], json.dumps(j["return"][k], sort_keys=True, indent=4))
