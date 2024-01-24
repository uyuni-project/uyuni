"""
reportdb_user functions
"""

import logging
import os
import re

from salt.exceptions import CommandExecutionError
try:
    import libvirt
except ImportError:
    pass

log = logging.getLogger(__name__)

__virtualname__ = "reportdb_user"


def __virtual__():
    """
    Only if the minion is a mgr server and the postgresql module is loaded
    """
    if not __grains__.get("is_mgr_server"):
        return (False, "Minion is not a mgr server")
    if "postgres.user_exists" not in __salt__:
        return (False, "Unable to load postgres module.  Make sure `postgres.bins_dir` is set.")
    return __virtualname__


def present(name, password):
    """
    Ensure that the named user is present in the configured report database

    :param name: the username
    :param password: the password
    """
    ret = {
        "name": name,
        "changes": {},
        "result": True if not __opts__["test"] else None,
        "comment": "",
    }

    if __opts__["test"]:
        ret["comment"] = "User {} with password will be set".format(name)
        return ret

    try:
        cmd = ['uyuni-setup-reportdb-user', '--non-interactive', '--dbuser', name, '--dbpassword', password]
        if __salt__["postgres.user_exists"](name):
            cmd.append('--modify')
        else:
            cmd.append('--add')

        result = __salt__["cmd.run_all"](cmd)

        if result["retcode"] != 0:
            ret["result"] = False
            ret["comment"] = "Failed to set the user. {}".format(
                result["stderr"] or result["stdout"])
            return ret

        ret["comment"] = "User {} with password set".format(name)
        return ret

    except Exception as err:
        ret["result"] = False
        ret["comment"] = str(err)

    return ret


def absent(name, password):
    """
    Ensure that the named user is absent in the configured reporting database

    :param name: the username
    :param password: the password
    """
    ret = {
        "name": name,
        "changes": {},
        "result": True if not __opts__["test"] else None,
        "comment": "",
    }

    if __opts__["test"]:
        ret["comment"] = "User {} with password will be set".format(name)
        return ret

    try:
        if not __salt__["postgres.user_exists"](name):
            if __opts__["test"]:
                ret["comment"] = "User {} will be removed".format(name)
                return ret
        else:
            if __opts__["test"]:
                ret["comment"] = "no change needed"
            return ret

        cmd = ['uyuni-setup-reportdb-user', '--non-interactive', '--dbuser', name, '--dbpassword', password, '--delete']
        result = __salt__["cmd.run_all"](cmd)

        if result["retcode"] != 0:
            ret["result"] = False
            ret["comment"] = "Failed to delete the user. {}".format(
                result["stderr"] or result["stdout"])
            return ret

        ret["comment"] = "User {} deleted".format(name)
        return ret

    except Exception as err:
        ret["result"] = False
        ret["comment"] = str(err)

    return ret

