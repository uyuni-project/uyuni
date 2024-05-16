#!/usr/bin/python3
# pylint: disable=invalid-name
"""Configure script for Uyuni proxy squid container."""

import os
import re
import yaml

# read from file
with open("/etc/uyuni/config.yaml", encoding="utf-8") as source:
    config = yaml.safe_load(source)

    # read to existing config file
    with open("/etc/squid/squid.conf", "r+", encoding="utf-8") as config_file:
        file_content = config_file.read()
        file_content = re.sub(
            r"cache_dir aufs .*",
            f"cache_dir aufs /var/cache/squid {str(config['max_cache_size_mb'])} 16 256",
            file_content,
        )
        file_content = re.sub(
            r"access_log .*", "access_log stdio:/proc/self/fd/1 squid", file_content
        )
        file_content += "\n" + "include /etc/squid/conf.d/*" + "\n"
        # writing back the content
        config_file.seek(0, 0)
        config_file.write(file_content)
        config_file.truncate()

# make sure "squid" is the user and group owner of the cache squid path
os.system("chown -R squid:squid /var/cache/squid")
