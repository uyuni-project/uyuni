#!/usr/bin/python3
 
import yaml

# read from file
with open("/etc/uyuni/config.yaml") as source:
    config = yaml.safe_load(source)

    # write to file
    with open("/etc/rhn/rhn.conf", "w") as dest:
       dest.write(f"rhn_parent={config['server']}")
