#!/usr/bin/python3

import os
import re
import shutil

import yaml

# read from file
with open("/etc/uyuni/config.yaml") as source:
    config = yaml.safe_load(source)

    squid_conf_dir = "/etc/squid/conf.d/"
    if not os.path.exists(squid_conf_dir):
        os.makedirs(squid_conf_dir)

    # read to existing config file
    with open("/etc/squid/squid.conf", "r+") as config_file:
        file_content = config_file.read()
        file_content = re.sub(r"cache_dir aufs .*", f"cache_dir aufs /var/cache/squid {str(config['max_cache_size_mb'])} 16 256", file_content)
        file_content = re.sub(r"access_log .*", "access_log stdio:/proc/self/fd/1 squid", file_content)
        # writing back the content
        config_file.seek(0,0)
        config_file.write(file_content)
        config_file.truncate()

    # create a file for delay pools configuration
    if int(config["bandwidth_limit_kbps"]) > 0:

        # copy over a delay config file
        shutil.copyfile("/etc/squid/conf.d.backup/delay.conf", "/etc/squid/conf.d/delay.conf")

        bandwidth_limit = config["bandwidth_limit_kbps"] / 8 * 1000
        #
        with open("/etc/squid/conf.d/delay.conf", "r+") as config_file:
            file_content = config_file.read()
            file_content = re.sub(r"delay_parameters 1 .*",
                                  f"delay_parameters 1 {bandwidth_limit}/{bandwidth_limit}",
                                  file_content)

            # writing back the content
            config_file.seek(0, 0)
            config_file.write(file_content)
            config_file.truncate()

# make sure "squid" is the user and group owner of the cache squid path
os.system('chown -R squid:squid /var/cache/squid')
