#!/usr/bin/python3
# pylint: disable=invalid-name,inconsistent-quotes
"""Configure script for Uyuni proxy salt broker container."""

import os
import yaml

config_path = "/etc/uyuni/"
confdir = "/etc/salt/broker.d"

# read from the proxy config to add the port config
with open(config_path + "config.yaml", encoding="utf-8") as source:
    config = yaml.safe_load(source)

    publish_port = config.get("salt_publish_port", 4505)
    request_port = config.get("salt_request_port", 4506)

    with open(os.path.join(confdir, "99-ports.conf"), "w", encoding="utf-8") as file:
        file.write(f"""ret_port: {request_port}
publish_port: {publish_port}""")
