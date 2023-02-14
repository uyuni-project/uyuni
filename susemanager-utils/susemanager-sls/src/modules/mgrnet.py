"""
Module for gathering DNS FQDNs
"""

import logging
import re
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

import salt.utils.network

try:
    from salt.utils.network import _get_interfaces
except:
    from salt.grains.core import _get_interfaces

try:
    from salt.utils.path import which as _which
except:
    from salt.utils import which as _which

log = logging.getLogger(__name__)


def __virtual__():
    """
    Only works on POSIX-like systems having 'host' or 'nslookup' available
    """
    if not (_which("host") or _which("nslookup")):
        return (False, "Neither 'host' nor 'nslookup' is available on the system")
    return True


def dns_fqdns():
    """
    Return all known DNS FQDNs for the system by enumerating all interfaces and
    then trying to reverse resolve them with native DNS tools
    """
    # Provides:
    # dns_fqdns

    grains = {}
    fqdns = set()
    cmd_run_all_func = __salt__["cmd.run_all"]
    if _which("host"):
        cmd = "host"
        cmd_ret_regex = re.compile(r".* domain name pointer (.*)\.$")
    elif _which("nslookup"):
        cmd = "nslookup"
        cmd_ret_regex = re.compile(r".*\tname = (.*)\.$")
    else:
        log.error("Neither 'host' nor 'nslookup' is available on the system")
        return {"dns_fqdns": []}

    def _lookup_dns_fqdn(ip):
        try:
            ret = cmd_run_all_func([cmd, ip], ignore_retcode=True)
        except Exception as e:
            log.error("Error while trying to use '%s' to resolve '%s': %s", cmd, ip, e)
        if ret["retcode"] != 0:
            log.debug("Unable to resolve '%s' using '%s': %s", ip, cmd, ret)
            return []
        fqdns = []
        for line in ret["stdout"].split("\n"):
            match = cmd_ret_regex.match(line)
            if match:
                fqdns.append(match.group(1))
        return fqdns

    start = time.time()

    addresses = salt.utils.network.ip_addrs(
        include_loopback=False, interface_data=_get_interfaces()
    )
    addresses.extend(
        salt.utils.network.ip_addrs6(
            include_loopback=False, interface_data=_get_interfaces()
        )
    )

    results = []
    try:
        # Create a ThreadPoolExecutor to process the underlying calls
        # to resolve DNS FQDNs in parallel.
        with ThreadPoolExecutor(max_workers=8) as executor:
            results = dict((executor.submit(_lookup_dns_fqdn, ip), ip) for ip in addresses)
            for item in as_completed(results):
                item = item.result()
                if item:
                    fqdns.update(item)
    except Exception as exc:  # pylint: disable=broad-except
        log.error(
            "Exception while running ThreadPoolExecutor for FQDNs resolution: %s",
            exc,
        )

    elapsed = time.time() - start
    log.debug("Elapsed time getting DNS FQDNs: %s seconds", elapsed)

    return {"dns_fqdns": sorted(list(fqdns))}
