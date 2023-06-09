#!/usr/bin/python3

import os
import re
import shutil

import yaml


def set_cache_parameters(content, squid_config):
    # max_cache_size_mb
    content = re.sub(r"cache_dir aufs .*",
                     f"cache_dir aufs /var/cache/squid {str(squid_config['max_cache_size_mb'])} 16 256",
                     content)
    content = re.sub(r"access_log .*",
                     "access_log stdio:/proc/self/fd/1 squid",
                     content)

    # bandwidth_limit_kbps
    bandwidth_limit = int(squid_config["bandwidth_limit_kbps"])
    if bandwidth_limit > 0:
        bandwidth_limit = bandwidth_limit / 8 * 1000
        content = re.sub(r"delay_pools 1 .*",
                         "delay_pools 2",
                         content)
        content = re.sub(r"delay_parameters 1 .*",
                         f"delay_parameters 1 {bandwidth_limit}/{bandwidth_limit}",
                         content)

    # client_lifetime
    content = re.sub(r"client_lifetime .*",
                     f"client_lifetime {str(squid_config['client_lifetime'])} minutes",
                     content)

    # request_timeout
    content = re.sub(r"request_timeout .*",
                     f"request_timeout {str(squid_config['request_timeout'])} minutes",
                     content)

    # range_offset_limit
    range_offset_limit = int(squid_config["range_offset_limit"])
    if range_offset_limit > 0:
        content = re.sub(r"range_offset_limit .*",
                         f"range_offset_limit {range_offset_limit} KB",
                         content)

    # read_ahead_gap
    content = re.sub(r"read_ahead_gap .*",
                     f"read_ahead_gap {str(squid_config['read_ahead_gap'])} KB",
                     content)

    # client_request_buffer_max_size
    content = re.sub(r"client_request_buffer_max_size .*",
                     f"client_request_buffer_max_size {str(squid_config['client_request_buffer_max_size'])} KB",
                     content)

    return content


# read from file
with open("/etc/uyuni/config.yaml") as source:
    config = yaml.safe_load(source)

    squid_conf_dir = "/etc/squid/conf.d/"
    if not os.path.exists(squid_conf_dir):
        os.makedirs(squid_conf_dir)

    # read to existing config file
    with open("/etc/squid/squid.conf", "r+") as config_file:
        file_content = config_file.read()
        file_content = set_cache_parameters(file_content, config["squid_config"])
        # writing back the content
        config_file.seek(0,0)
        config_file.write(file_content)
        config_file.truncate()

# make sure "squid" is the user and group owner of the cache squid path
os.system('chown -R squid:squid /var/cache/squid')
