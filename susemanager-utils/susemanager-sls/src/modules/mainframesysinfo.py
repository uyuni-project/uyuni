# -*- coding: utf-8 -*-
"""
s390 utility for Suse Manager

"""
from __future__ import absolute_import

import logging
import salt.utils
import salt.modules.cmdmod
from salt.exceptions import CommandExecutionError
import os

__salt__ = {
    "cmd.run_all": salt.modules.cmdmod.run_all,
}

log = logging.getLogger(__name__)


# pylint: disable-next=invalid-name
def __virtual__():
    """
    Only works if /usr/bin/read_values is accessible
    """
    return os.access("/usr/bin/read_values", os.X_OK) or os.access(
        "/proc/sysinfo", os.R_OK
    )


def read_values():
    """
    Executes /usr/bin/read_values or if not available
    falls back to 'cat /proc/sysinfo'

    CLI Example:

    .. code-block:: bash

        salt '*' mainframesysinfo.read_values
    """
    if os.access("/usr/bin/read_values", os.X_OK):
        cmd = "/usr/bin/read_values -s"
    else:
        cmd = "cat /proc/sysinfo"
    result = __salt__["cmd.run_all"](cmd, output_loglevel="quiet")

    if result["retcode"] != 0:
        raise CommandExecutionError(result["stderr"])

    return result["stdout"]
