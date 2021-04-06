"""
virt utility functions
"""

import libvirt
import re

__virtualname__ = "virt_utils"


def __virtual__():
    """
    Only if the virt module is loaded
    """
    return (
        __virtualname__
        if "virt.vm_info" in __salt__
        else (False, "Module virt_utils: virt module can't be loaded")
    )


def _all_running(name, kind, names, is_running):
    ret = {
        "name": name,
        "changes": {},
        "result": True if not __opts__["test"] else None,
        "comment": "",
    }

    stopped = []
    missing = []
    try:
        info = __salt__["virt.{}_info".format(kind)]()
        for obj_name in names:
            obj_info = info.get(obj_name)
            if not obj_info:
                missing.append(obj_name)
                continue

            if not is_running(obj_info):
                stopped.append(obj_name)

        if missing:
            ret["result"] = False
            ret["comment"] = "{} {}{} not defined".format(
                ", ".join(missing), kind, "s are" if len(missing) > 1 else " is"
            )
            return ret

        if not stopped:
            ret["comment"] = "all {}s are already running".format(kind)
            return ret

        for obj_name in stopped:
            if not __opts__["test"]:
                __salt__["virt.{}_start".format(kind)](obj_name)
            change = "started"
            ret["changes"][obj_name] = change

        ret["comment"] = "{} {}{} been started".format(
            ", ".join(stopped), kind, "s have" if len(stopped) > 1 else " has"
        )

    except libvirt.libvirtError as err:
        ret["result"] = False
        ret["comment"] = err.get_error_message()

    return ret


def network_running(name, networks=None):
    """
    Ensure one or more already defined virtual networks are running.

    :param name: the name of one network to get running
    :param networks: the list of network names to get running
    """
    return _all_running(
        name, "network", networks or [name], lambda info: info.get("active", False)
    )


def pool_running(name, pools=None):
    """
    Ensure one or more already defined virtual storage pool are running.

    :param name: the name of one pool to get running
    :param pools: the list of pool names to get running
    """
    names = pools or [name]
    ret = _all_running(name, "pool", names, lambda info: info["state"] == "running")
    if ret["result"] is False:
        return ret

    # Refresh all pools
    for pool_name in names:
        try:
            # No need to refresh a pool that has just been started
            if pool_name in ret["changes"]:
                continue
            if not __opts__["test"]:
                __salt__["virt.pool_refresh"](pool_name)
            ret["changes"][pool_name] = "refreshed"

        except libvirt.libvirtError as err:
            ret["result"] = False
            ret["comment"] = err.get_error_message()

    return ret


def vm_resources_running(name):
    """
    :param name: name of the VM for which to ensure networks and storage pools are running
    """
    ret = {
        "name": name,
        "changes": {},
        "result": True if not __opts__["test"] else None,
        "comment": "",
    }
    try:
        infos = __salt__["virt.vm_info"](name)
        if not infos.get(name):
            ret["result"] = False
            ret["comment"] = "Virtual machine {} does not exist".format(name)
            return ret

        vm_infos = infos.get(name)

        # Ensure all the networks are started
        networks = [
            nic["source"]["network"]
            for nic in vm_infos.get("nics", {}).values()
            if nic["type"] == "network"
        ]
        net_ret = network_running(name="{}_nets".format(name), networks=networks)

        # Ensure all the pools are started
        pools = [
            disk["file"].split("/")[0]
            for disk in vm_infos.get("disks", {}).values()
            if re.match("^[^/:]+/", disk["file"])
        ]
        pool_ret = pool_running(name="{}_pools".format(name), pools=pools)

        failed = any([net_ret["result"] is False, pool_ret["result"] is False])
        ret["result"] = False if failed else net_ret["result"]
        ret["comment"] = "{}, {}".format(net_ret["comment"], pool_ret["comment"])
        ret["changes"] = {"networks": net_ret["changes"], "pools": pool_ret["changes"]}

    except libvirt.libvirtError as err:
        ret["result"] = False
        ret["comment"] = err.get_error_message()

    return ret
