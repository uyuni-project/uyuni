"""
Module for Getting Supportdata
"""

from typing import Any, Dict
import logging
import os
import shutil

from datetime import datetime

# Just for lint and static analysis, will be replaced by salt's loader
__grains__ = {}
__salt__ = {}

__virtualname__ = "supportdata"

# create a logger for the module
log = logging.getLogger(__name__)


def _get_supportdata_dir():
    return "/var/log/supportdata-" + datetime.now().strftime("%Y%m%d%H%M%S")


def get(cmd_args: str = "", **kwargs) -> Dict[str, Any]:
    """
    Collect supportdata like config and logfiles from the system
    and upload them to the master's minion files cachedir
    (defaults to ``/var/cache/salt/master/minions/minion-id/files``)

    It needs ``file_recv`` set to ``True`` in the master configuration file.

    cmd_args
        extra commandline arguments for the executed tool

    CLI Example:

    .. code-block:: bash

        salt '*'  supportdata.get
    """
    supportconfig_path = "/sbin/supportconfig"
    mgradm_path = "/usr/bin/mgradm"
    mgrpxy_path = "/usr/bin/mgrpxy"
    sosreport_path = "/usr/sbin/sosreport"
    cmd = []

    success = False
    supportdata_dir = ""
    error = None
    returncode = None

    del kwargs

    output_dir = _get_supportdata_dir()
    extra_args = cmd_args.split()

    if "Suse" in __grains__["os_family"]:
        if os.path.exists(mgradm_path):
            cmd = [mgradm_path, "support", "config", "--output", output_dir]
        elif os.path.exists(mgrpxy_path):
            cmd = [mgrpxy_path, "support", "config", "--output", output_dir]
        elif os.path.exists(supportconfig_path):
            cmd = [supportconfig_path, "-R", output_dir]
    elif "RedHat" in __grains__["os_family"]:
        if os.path.exists(sosreport_path):
            cmd = [sosreport_path, "--batch", "--tmp-dir", output_dir]
    else:
        error = "Getting supportdata not supported for " + __grains__["os"]
        returncode = 1

    if len(cmd) > 0:
        os.makedirs(output_dir, exist_ok=True)
        cmd.extend(extra_args)
        log.debug("executing: %s", cmd)
        ret = __salt__["cmd.run_all"](cmd, python_shell=False)

        log.debug("return: %s", ret)
        returncode = ret["retcode"]
        if returncode != 0:
            error = f'Failed to run {cmd[0]}: {ret["stderr"]}'
        else:
            if __salt__["cp.push_dir"](output_dir):
                # remove the output dir only when the upload was successful
                # on salt-ssh it fail and we need to download it explict via scp
                shutil.rmtree(output_dir, ignore_errors=True)
            supportdata_dir = output_dir
            success = True
    elif returncode is None:
        error = "Required tools to get support data are not installed"
        returncode = 1

    return dict(
        success=success,
        supportdata_dir=supportdata_dir,
        error=error,
        returncode=returncode,
    )
