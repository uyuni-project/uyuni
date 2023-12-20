"""
reportdb_user functions
"""

import logging
import os  #  pylint: disable=unused-import
import re  #  pylint: disable=unused-import

from salt.exceptions import CommandExecutionError  #  pylint: disable=unused-import

try:
    import libvirt  #  pylint: disable=unused-import
except ImportError:
    pass

log = logging.getLogger(__name__)

__virtualname__ = "reportdb_user"


def __virtual__():  #  pylint: disable=invalid-name
    """
    Only if the minion is a mgr server and the postgresql module is loaded
    """
    if not __grains__.get("is_mgr_server"):  #  pylint: disable=undefined-variable
        return (False, "Minion is not a mgr server")
    if "postgres.user_exists" not in __salt__:  #  pylint: disable=undefined-variable
        return (
            False,
            "Unable to load postgres module.  Make sure `postgres.bins_dir` is set.",
        )
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
        "result": True if not __opts__["test"] else None,  #  pylint: disable=undefined-variable
        "comment": "",
    }

    if __opts__["test"]:  #  pylint: disable=undefined-variable
        ret["comment"] = "User {} with password will be set".format(name)  #  pylint: disable=consider-using-f-string
        return ret

    try:
        cmd = [
            "uyuni-setup-reportdb-user",
            "--non-interactive",
            "--dbuser",
            name,
            "--dbpassword",
            password,
        ]
        if __salt__["postgres.user_exists"](name):  #  pylint: disable=undefined-variable
            cmd.append("--modify")
        else:
            cmd.append("--add")

        result = __salt__["cmd.run_all"](cmd)  #  pylint: disable=undefined-variable

        if result["retcode"] != 0:
            ret["result"] = False
            ret["comment"] = "Failed to set the user. {}".format(  #  pylint: disable=consider-using-f-string
                result["stderr"] or result["stdout"]
            )
            return ret

        ret["comment"] = "User {} with password set".format(name)  #  pylint: disable=consider-using-f-string
        return ret

    except Exception as err:  #  pylint: disable=broad-exception-caught
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
        "result": True if not __opts__["test"] else None,  #  pylint: disable=undefined-variable
        "comment": "",
    }

    if __opts__["test"]:  #  pylint: disable=undefined-variable
        ret["comment"] = "User {} with password will be set".format(name)  #  pylint: disable=consider-using-f-string
        return ret

    try:
        if not __salt__["postgres.user_exists"](name):  #  pylint: disable=undefined-variable
            if __opts__["test"]:  #  pylint: disable=undefined-variable
                ret["comment"] = "User {} will be removed".format(name)  #  pylint: disable=consider-using-f-string
                return ret
        else:
            if __opts__["test"]:  #  pylint: disable=undefined-variable
                ret["comment"] = "no change needed"
            return ret

        cmd = [
            "uyuni-setup-reportdb-user",
            "--non-interactive",
            "--dbuser",
            name,
            "--dbpassword",
            password,
            "--delete",
        ]
        result = __salt__["cmd.run_all"](cmd)  #  pylint: disable=undefined-variable

        if result["retcode"] != 0:
            ret["result"] = False
            ret["comment"] = "Failed to delete the user. {}".format(  #  pylint: disable=consider-using-f-string
                result["stderr"] or result["stdout"]
            )
            return ret

        ret["comment"] = "User {} deleted".format(name)  #  pylint: disable=consider-using-f-string
        return ret

    except Exception as err:  #  pylint: disable=broad-exception-caught
        ret["result"] = False
        ret["comment"] = str(err)

    return ret
