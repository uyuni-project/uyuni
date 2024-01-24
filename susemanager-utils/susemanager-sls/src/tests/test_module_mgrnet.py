#  pylint: disable=missing-module-docstring,unused-import
import sys

from unittest.mock import MagicMock, patch
from . import mockery

mockery.setup_environment()

# pylint: disable-next=wrong-import-position
from ..modules import mgrnet


mgrnet.__salt__ = {}


def test_mgrnet_virtual():
    """
    Test __virtual__ function for the possible cases
    when either 'host' or 'nslookup' is available or none of them
    """

    with patch.object(
        mgrnet,
        "_which",
        MagicMock(side_effect=[True, False, True, False, False]),
    ):
        ret = mgrnet.__virtual__()
        assert ret is True

        ret = mgrnet.__virtual__()
        assert ret is True

        ret = mgrnet.__virtual__()
        assert ret[0] is False


def test_mgrnet_dns_fqdns():
    """
    Test getting possible FQDNs with DNS tools
    """

    check_calls = {"host": [], "nslookup": []}

    ipv4_addresses = ["10.0.0.1", "172.16.0.1", "192.168.0.1", "10.10.1.1"]
    ipv6_addresses = ["fd12:3456:789a:1::1", "fd12:abcd:1234:1::1"]

    names = {
        "10.0.0.1": "host10.example.org",
        "172.16.0.1": "host172.example.org",
        "192.168.0.1": "host10.example.org",
        "fd12:3456:789a:1::1": "ipv6host3456.example.org",
        "fd12:abcd:1234:1::1": "ipv6hostabcd.example.org",
    }

    # pylint: disable-next=unused-argument
    def _cmd_run_host_nslookup(cmd, ignore_retcode=False):
        """
        This function is emulating the output of 'host' or 'nslookup'
        """
        ip = cmd[1]
        cmd = cmd[0]
        check_calls[cmd].append(ip)
        rc = 0
        if ":" in ip:
            # the conversion is not very accurate here, but it's enough for testing
            # pylint: disable-next=consider-using-f-string
            ptr = "{}.ip6.arpa".format(".".join(reversed([*ip.replace(":", "")])))
        else:
            # pylint: disable-next=consider-using-f-string
            ptr = "{}.in-addr.arpa".format(".".join(reversed(ip.split())))
        if cmd == "host":
            if ip in names:
                # pylint: disable-next=consider-using-f-string
                out = "{} domain name pointer {}.\n".format(ptr, names[ip])
            else:
                # pylint: disable-next=consider-using-f-string
                out = "Host {}. not found: 3(NXDOMAIN)\n".format(ptr)
                rc = 1
        else:
            if ip in names:
                # pylint: disable-next=consider-using-f-string
                out = "{}\tname = {}.\n".format(ptr, names[ip])
            else:
                # pylint: disable-next=consider-using-f-string
                out = "** server can't find {}: NXDOMAIN\n".format(ptr)
                rc = 1
        return {"retcode": rc, "stdout": out}

    with patch.dict(
        mgrnet.__salt__, {"cmd.run_all": _cmd_run_host_nslookup}
    ), patch.object(
        mgrnet,
        "_which",
        MagicMock(side_effect=[True, False, True, False, False]),
    ), patch.object(
        mgrnet.salt.utils.network,
        "ip_addrs",
        MagicMock(side_effect=[ipv4_addresses.copy(), ipv4_addresses.copy()]),
    ), patch.object(
        mgrnet.salt.utils.network,
        "ip_addrs6",
        MagicMock(side_effect=[ipv6_addresses.copy(), ipv6_addresses.copy()]),
    ):
        # Test 'host' util output
        ret = mgrnet.dns_fqdns()
        assert sorted(ret["dns_fqdns"]) == sorted(set(names.values()))

        # Test 'nslookup' util output
        ret = mgrnet.dns_fqdns()
        assert sorted(ret["dns_fqdns"]) == sorted(set(names.values()))

        # Check if 'host' and 'nslookup' were called for all of IPv4 and IPv6 addresses
        for ip in ipv4_addresses:
            assert ip in check_calls["host"]
            assert ip in check_calls["nslookup"]
        for ip in ipv6_addresses:
            assert ip in check_calls["host"]
            assert ip in check_calls["nslookup"]

        assert len(check_calls["host"]) == len(ipv4_addresses) + len(ipv6_addresses)

        # Test the case when neither 'host' nor 'nslookup' is present on the system
        ret = mgrnet.dns_fqdns()
        assert ret == {"dns_fqdns": []}
