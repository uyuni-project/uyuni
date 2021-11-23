#!/usr/bin/python3
 
import yaml

# read from file
with open("/etc/uyuni/config.yaml") as source:
    config = yaml.safe_load(source)

    # read to existing config file
    with open("/etc/squid/squid.conf", "r") as dest:
        fileContent = dest.read()

        # replace the target string
        fileContent = fileContent.replace('_SQUID_SIZE_', str(config["squid_size"]))

        # write to file
        with open("/etc/squid/squid.conf", "w") as dest:
            dest.write(fileContent)
