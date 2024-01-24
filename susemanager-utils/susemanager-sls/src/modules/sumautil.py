# -*- coding: utf-8 -*-
"""
Utility module for Suse Manager

"""
from __future__ import absolute_import

import logging
import socket
import os
import re
import time
import salt.utils

# pylint: disable-next=unused-import
from salt.exceptions import CommandExecutionError

try:
    from salt.utils.path import which_bin as _which_bin
# pylint: disable-next=bare-except
except:
    from salt.utils import which_bin as _which_bin


__salt__ = {
    "cmd.run_all": salt.modules.cmdmod.run_all,
}

log = logging.getLogger(__name__)

__virtualname__ = "sumautil"

SYSFS_NET_PATH = "/sys/class/net"


# pylint: disable-next=invalid-name
def __virtual__():
    """
    Only run on Linux systems
    """
    # pylint: disable-next=undefined-variable
    return __grains__["kernel"] == "Linux" and __virtualname__ or False


def cat(path):
    """
    Cat the specified file.

    CLI Example:

    .. code-block:: bash

        salt '*' sumautil.cat /tmp/file
    """
    # pylint: disable-next=consider-using-f-string
    cmd = "cat %s" % path
    result = __salt__["cmd.run_all"](cmd, output_loglevel="quiet")

    if result["retcode"] != 0:
        return {"retcode": 1, "stderr": result["stderr"]}

    return {"retcode": 0, "stdout": result["stdout"]}


def primary_ips():
    """
    Get the source IPs that the minion uses to connect to the master.
    Returns the IPv4 and IPv6 address (if available).

    CLI Example:

    .. code-block:: bash

        salt '*' sumautil.primary_ip
    """

    # pylint: disable-next=unnecessary-lambda-assignment
    get_master_ip = lambda family, host: socket.getaddrinfo(host, 0, family)[0][-1][0]

    # pylint: disable-next=undefined-variable
    master = __opts__.get("master", "")
    # pylint: disable-next=logging-format-interpolation,consider-using-f-string
    log.debug("Using master: {0}".format(str(master)))

    ret = dict()
    for sock_family, sock_descr in list(
        {socket.AF_INET: "IPv4", socket.AF_INET6: "IPv6"}.items()
    ):
        try:
            # pylint: disable-next=consider-using-f-string
            ret["{0}".format(sock_descr)] = __salt__["network.get_route"](
                get_master_ip(sock_family, master)
            )
            log.debug(
                # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                "network.get_route({0}): ".format(ret["{0} source".format(sock_descr)])
            )
        # pylint: disable-next=broad-exception-caught
        except Exception as err:
            # pylint: disable-next=logging-format-interpolation,consider-using-f-string
            log.debug("{0} is not available? {1}".format(sock_descr, err))

    return ret


def get_net_module(iface):
    """
    Returns the kernel module used for the give interface
    or None if the module could not be determined of if the
    interface name is wrong.
    Uses '/sys/class/net' to find out the module.

    CLI Example:

    .. code-block:: bash

        salt '*' sumautil.get_net_module eth0
    """
    sysfspath = os.path.join(SYSFS_NET_PATH, iface, "device/driver")

    return (
        os.path.exists(sysfspath) and os.path.split(os.readlink(sysfspath))[-1] or None
    )


def get_net_modules():
    """
    Returns a dictionary of all network interfaces and their
    corresponding kernel module (if it could be determined).

    CLI Example:

    .. code-block:: bash

        salt '*' sumautil.get_net_modules
    """
    drivers = dict()
    for devdir in os.listdir(SYSFS_NET_PATH):
        try:
            drivers[devdir] = get_net_module(devdir)
        except OSError as devdir:
            log.warning(
                # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                "An error occurred getting net driver for {0}".format(devdir),
                exc_info=True,
            )

    return drivers or None


def get_kernel_live_version():
    """
    Returns the patch version of live patching if it is active,
    otherwise None

    CLI Example:

    .. code-block:: bash

        salt '*' sumautil.get_kernel_live_version
    """
    kernel_live_version = _klp()
    if not kernel_live_version:
        log.debug("No kernel live patch is active")

    return kernel_live_version


def _klp():
    """
    klp to identify the current kernel live patch

    :return:
    """
    # get 'kgr' for versions prior to SLE 15

    klp = _which_bin(["klp", "kgr"])
    patchname = None
    if klp is not None:
        try:
            # loop until patching is finished
            # pylint: disable-next=unused-variable
            for i in range(10):
                stat = __salt__["cmd.run_all"](
                    # pylint: disable-next=consider-using-f-string
                    "{0} status".format(klp),
                    output_loglevel="quiet",
                )
                # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                log.debug("klp status: {0}".format(stat["stdout"]))
                if stat["stdout"].strip().splitlines()[0] == "ready":
                    break
                time.sleep(1)
            re_active = re.compile(r"^\s+active:\s*(\d+)$")
            ret = __salt__["cmd.run_all"](
                # pylint: disable-next=consider-using-f-string
                "{0} -v patches".format(klp),
                output_loglevel="quiet",
            )
            # pylint: disable-next=logging-format-interpolation,consider-using-f-string
            log.debug("klp patches: {0}".format(ret["stdout"]))
            if ret["retcode"] == 0:
                for line in ret["stdout"].strip().splitlines():
                    if line.startswith("#"):
                        continue

                    match_active = re_active.match(line)
                    if match_active and int(match_active.group(1)) > 0:
                        return {"mgr_kernel_live_version": patchname}
                    elif line.startswith("kgraft") or line.startswith("livepatch"):
                        # kgr patches have prefix 'kgraft', whereas klp patches start with 'livepatch'
                        patchname = line.strip()

        # pylint: disable-next=broad-exception-caught
        except Exception as error:
            # pylint: disable-next=logging-format-interpolation,consider-using-f-string
            log.error("klp: {0}".format(str(error)))
