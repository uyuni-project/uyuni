#!/usr/bin/python3

import re
import yaml

# read from file
with open("/etc/uyuni/config.yaml") as source:
    config = yaml.safe_load(source)

    # write to file
    with open("/etc/rhn/rhn.conf", "w") as dest:
       dest.write(f"rhn_parent={config['server']}")

    # read to existing config file
    with open("/etc/salt/broker", "r+") as config_file:
        file_content = config_file.read()
        # make sure to send logs to stdout/stderr instead to file
        file_content = re.sub(r"log_to_file: .*", "log_to_file: 0", file_content)
        # writing back the content
        config_file.seek(0,0)
        config_file.write(file_content)
        config_file.truncate()
