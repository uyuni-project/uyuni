"""
Module for Getting Supportdata
"""

from typing import Any, Dict, List
import logging
import os
import re
import shutil
import time

from datetime import datetime

# Just for lint and static analysis, will be replaced by salt's loader
__grains__ = {}
__salt__ = {}
__opts__ = {}

__virtualname__ = "supportdata"

# create a logger for the module
log = logging.getLogger(__name__)


def _get_supportdata_dir():
    return "/var/log/supportdata-" + datetime.now().strftime("%Y%m%d%H%M%S")


def _cleanup_outdated_data():
    def _log_error(*args):
        path = args[1]
        err = args[2]
        log.error("Failed to remove %s: %s", path, err[1])

    for d in os.listdir("/var/log/"):
        fullpath = os.path.join("/var/log", d)
        if os.path.isdir(fullpath) and re.match(r"^supportdata-[0-9]+$", d):
            if (time.time() - os.path.getmtime(fullpath)) > 3600:
                # older than 1 hour
                # pylint: disable-next=deprecated-argument
                shutil.rmtree(fullpath, onerror=_log_error)


def _get_command(output_dir: str) -> List[str]:
    supportconfig_path = "/sbin/supportconfig"
    mgradm_path = "/usr/bin/mgradm"
    mgrpxy_path = "/usr/bin/mgrpxy"
    sosreport_path = "/usr/sbin/sosreport"
    sosreport_alt_path = "/usr/bin/sosreport"
    cmd = []

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
    elif "Debian" in __grains__["os_family"]:
        if os.path.exists(sosreport_alt_path):
            cmd = [sosreport_alt_path, "--batch", "--tmp-dir", output_dir]
    else:
        cmd = None
    return cmd


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
    success = False
    supportdata_dir = ""
    error = None
    returncode = None

    del kwargs
    _cleanup_outdated_data()

    output_dir = _get_supportdata_dir()
    extra_args = cmd_args.split()

    cmd = _get_command(output_dir)

    if cmd is None:
        error = "Getting supportdata not supported for " + __grains__["os"]
        returncode = 1
    elif len(cmd) > 0:
        os.makedirs(output_dir, exist_ok=True)
        cmd.extend(extra_args)
        log.debug("executing: %s", cmd)
        ret = __salt__["cmd.run_all"](cmd, runas="root")

        log.debug("return: %s", ret)
        returncode = ret["retcode"]
        if returncode != 0:
            error = f'Failed to run {cmd[0]}: {ret["stderr"]}'
        else:
            if "master_uri" in __opts__ and __salt__["cp.push_dir"](output_dir):
                # remove the output dir only when the upload was successful
                # with salt-ssh "master_uri" is not in opts and we need to
                # download it explicitly via scp
                shutil.rmtree(output_dir, ignore_errors=True)
            supportdata_dir = output_dir
            success = True
    else:
        error = "Required tools to get support data are not installed"
        returncode = 1

    return dict(
        success=success,
        supportdata_dir=supportdata_dir,
        error=error,
        returncode=returncode,
    )
