#!/usr/bin/python3
 
import os
import yaml

# read from file
with open("/etc/uyuni/config.yaml") as source:
    config = yaml.safe_load(source)

    # read to existing config file
    with open("/etc/squid/squid.conf", "r") as dest:
        fileContent = dest.read()

        # replace the target string
        fileContent = fileContent.replace(
            'cache_dir ufs /var/cache/squid 15000 16 256',
            f"cache_dir ufs /var/cache/squid {str(config['squid_size'])} 16 256")
        fileContent = fileContent.replace(
            'access_log /var/log/squid/access.log squid',
            'access_log stdio:/proc/self/fd/1 squid'
        )

        # write to file
        with open("/etc/squid/squid.conf", "w") as dest:
            dest.write(fileContent)

# make sure "squid" is the user and group owner of the cache squid path
os.system('chown -R squid:squid /var/cache/squid')
