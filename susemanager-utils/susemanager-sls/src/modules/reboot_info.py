"""Find out if a reboot is needed for different Linux distributions."""

import logging
import os

log = logging.getLogger(__name__)

__virtualname__ = "reboot_info"

# Just for lint and static analysis, will be replaced by salt's loader
__grains__ = {}
__salt__ = {}


# pylint: disable-next=invalid-name
def __virtual__():
    """
    Run on Debian, Suse and RedHat systems.
    """
    return __grains__["os_family"] in ["Debian", "Suse", "RedHat"]


def _check_cmd_exit_code(cmd, code):
    output = __salt__["cmd.run_all"](cmd, ignore_retcode=True)
    if output.get("stderr"):
        log.error(output["stderr"])
    return output["retcode"] == code


def reboot_required():
    """
    Check if reboot is required

    CLI Example:

    .. code-block:: bash

        salt '*' reboot_info.reboot_required
    """
    result = False
    if __grains__.get("transactional", False):
        result = __salt__["transactional_update.pending_transaction"]()
    elif __grains__["os_family"] == "Debian":
        result = os.path.exists("/var/run/reboot-required")
    elif __grains__["os_family"] == "Suse":
        result = os.path.exists("/run/reboot-needed") or os.path.exists(
            "/boot/do_purge_kernels"
        )
    elif __grains__["os_family"] == "RedHat":
        cmd = (
            "dnf -q needs-restarting -r"
            if __grains__["osmajorrelease"] >= 8
            else "needs-restarting -r"
        )
        result = _check_cmd_exit_code(cmd, 1)

    return {"reboot_required": result}
