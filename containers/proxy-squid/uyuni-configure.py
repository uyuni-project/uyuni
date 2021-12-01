#!/usr/bin/python3

import os
import re
import yaml

# read from file
with open("/etc/uyuni/config.yaml") as source:
    config = yaml.safe_load(source)

    # read to existing config file
    with open("/etc/squid/squid.conf", "r") as dest:
        file_content = dest.read()

        # adapt the config file
        file_content = re.sub(r"cache_dir ufs .*", f"cache_dir ufs /var/cache/squid {str(config['max_cache_size_mb'])} 16 256", file_content)
        file_content = re.sub(r"access_log .*", "access_log stdio:/proc/self/fd/1 squid", file_content)

        # write to file
        with open("/etc/squid/squid.conf", "w") as dest:
            dest.write(file_content)

# make sure "squid" is the user and group owner of the cache squid path
os.system('chown -R squid:squid /var/cache/squid')
