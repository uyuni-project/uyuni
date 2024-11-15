"""
Grains for Mgr Server
"""

import logging
import os
import json

# Import salt libs
import salt.utils.http as http

log = logging.getLogger(__name__)
RHNCONF = "/var/lib/containers/storage/volumes/etc-rhn/_data/rhn.conf"
API = "https://localhost/rhn/manager/api"
TUKIT_CONF = "/etc/tukit.conf.d/suma_mgrserver.conf"


def _tukit_conf():
    if not os.path.exists("/usr/bin/mgradm"):
        return

    if os.path.exists(TUKIT_CONF):
        return
    os.mkdir("/etc/tukit.conf.d")
    with open(TUKIT_CONF, "w", encoding="UTF-8") as tukit:
        tukit.write('BINDDIRS[etcrhn]="/var/lib/containers/storage/volumes/etc-rhn/"\n')


def _api_query(endpoint):
    body = http.query(os.path.join(API, endpoint), raise_error=False, verify_ssl=False)[
        "body"
    ]
    if body:
        apires = json.loads(body)
        if apires["success"]:
            return apires["result"]
    return None


def _simple_parse_rhn_conf(cfile):
    result = {}

    if not os.path.exists(cfile):
        return result

    # pylint: disable-next=unspecified-encoding
    with open(cfile, "r") as config:
        for line in config.readlines():
            line = line.strip()
            if not line or line[0] == "#":
                continue
            k, v = line.split("=", 1)
            result[k.strip()] = v.strip() or None
    return result


def server_grains():
    """
    Returns grains relevant for Uyuni/SUMA server.
    Expectation: Uyuni/SUMA is running as container.
    """

    _tukit_conf()
    grains = {"is_mgr_server": False}

    config = _simple_parse_rhn_conf(RHNCONF)

    if config.get("web.satellite", "0") == "1":
        grains["is_mgr_server"] = True
        hostname = config.get("report_db_host", "")
        if hostname == "localhost":
            hostname = config.get("java.hostname", "")
        if hostname and config.get("report_db_name", False):
            grains["has_report_db"] = True
            grains["report_db_host"] = hostname
            grains["report_db_name"] = config.get("report_db_name")
            grains["report_db_port"] = config.get("report_db_port", "5432")
        else:
            grains["has_report_db"] = False

        version = _api_query("api/systemVersion")
        product = _api_query("api/productName")

        if product and product == "SUSE Manager":
            grains["is_uyuni"] = False
        else:
            grains["is_uyuni"] = True

        if version:
            grains["version"] = version.split()[0]

    log.debug("mgr_server => server_grains return %s", grains)
    return grains
