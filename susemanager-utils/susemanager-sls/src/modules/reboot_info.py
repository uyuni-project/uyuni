#  pylint: disable=missing-module-docstring
# -*- coding: utf-8 -*-
import logging
import os
import salt

log = logging.getLogger(__name__)

__salt__ = {
    "cmd.run_all": salt.modules.cmdmod.run_all,
}

__virtualname__ = "reboot_info"


# pylint: disable-next=invalid-name
def __virtual__():
    """
    Run on Debian, Suse and RedHat systems.
    """
    # pylint: disable-next=undefined-variable
    return __grains__["os_family"] in ["Debian", "Suse", "RedHat"]


def _check_cmd_exit_code(cmd, code):
    output = __salt__["cmd.run_all"](cmd, python_shell=True)
    if "stderr" in output and output["stderr"]:
        log.error(output["stderr"])
    return output["retcode"] == code


def reboot_required():
    """
    Check if reboot is required

    CLI Example:

    .. code-block:: bash

        salt '*' reboot_info.reboot_required
    """
    # pylint: disable-next=undefined-variable
    if __grains__.get("transactional", False):
        result = __salt__["transactional_update.pending_transaction"]()
    # pylint: disable-next=undefined-variable
    elif __grains__["os_family"] == "Debian":
        result = os.path.exists("/var/run/reboot-required")
    # pylint: disable-next=undefined-variable
    elif __grains__["os_family"] == "Suse":
        result = os.path.exists("/run/reboot-needed") or os.path.exists(
            "/boot/do_purge_kernels"
        )
    # pylint: disable-next=undefined-variable
    elif __grains__["os_family"] == "RedHat":
        cmd = (
            "dnf -q needs-restarting -r"
            # pylint: disable-next=undefined-variable
            if __grains__["osmajorrelease"] >= 8
            else "needs-restarting -r"
        )
        result = _check_cmd_exit_code(cmd, 1)

    return {"reboot_required": result}
