"""
Grains for Mgr Server
"""

import sys
import logging
import os

from salt.exceptions import CommandExecutionError

log = logging.getLogger(__name__)
RHNCONF = "/etc/rhn/rhn.conf"
RHNCONFDEF = "/usr/share/rhn/config-defaults/rhn.conf"
RHNWEBCONF = "/usr/share/rhn/config-defaults/rhn_web.conf"

def _simple_parse_rhn_conf(cfile):
    result = {}

    if not os.path.exists(cfile):
        return result

    with open(cfile, "r") as config:
        for line in config.readlines():
            line = line.strip()
            if not line or line[0] == '#':
                continue
            k,v = line.split("=", 1)
            result[k.strip()] = v.strip() or None
    return result


def server_grains():
    """ returns if this minion is a Uyuni/SUSE Manager Server and
        if it has a reporting database configured.
    """
    grains = {'is_mgr_server': False, 'has_report_db': False, 'is_uyuni': True}

    config = _simple_parse_rhn_conf(RHNCONF)

    if config.get('web.satellite', '0') == '1':
        grains['is_mgr_server'] = True
        if config.get('report_db_host', False) and config.get('report_db_name', False):
            grains['has_report_db'] = True
            grains['report_db_host'] = config.get('report_db_host')
            grains['report_db_name'] = config.get('report_db_name')
            grains['report_db_port'] = config.get('report_db_port', '5432')
        rhndef = _simple_parse_rhn_conf(RHNCONFDEF)
        if rhndef.get('product_name', 'uyuni') == 'SUSE Manager':
            grains['is_uyuni'] = False
        webconfig = _simple_parse_rhn_conf(RHNWEBCONF)
        if grains['is_uyuni']:
            version = webconfig.get('web.version.uyuni')
        else:
            version = webconfig.get('web.version')
        if version:
            grains['version'] = version.split()[0]

    return grains
