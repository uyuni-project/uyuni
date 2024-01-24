"""
reportdb_user functions
"""

import logging

# pylint: disable-next=unused-import
import os

# pylint: disable-next=unused-import
import re

# pylint: disable-next=unused-import
from salt.exceptions import CommandExecutionError

try:
    # pylint: disable-next=unused-import
    import libvirt
except ImportError:
    pass

log = logging.getLogger(__name__)

__virtualname__ = "reportdb_user"


# pylint: disable-next=invalid-name
def __virtual__():
    """
    Only if the minion is a mgr server and the postgresql module is loaded
    """
    # pylint: disable-next=undefined-variable
    if not __grains__.get("is_mgr_server"):
        return (False, "Minion is not a mgr server")
    # pylint: disable-next=undefined-variable
    if "postgres.user_exists" not in __salt__:
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
        # pylint: disable-next=undefined-variable
        "result": True if not __opts__["test"] else None,
        "comment": "",
    }

    # pylint: disable-next=undefined-variable
    if __opts__["test"]:
        # pylint: disable-next=consider-using-f-string
        ret["comment"] = "User {} with password will be set".format(name)
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
        # pylint: disable-next=undefined-variable
        if __salt__["postgres.user_exists"](name):
            cmd.append("--modify")
        else:
            cmd.append("--add")

        # pylint: disable-next=undefined-variable
        result = __salt__["cmd.run_all"](cmd)

        if result["retcode"] != 0:
            ret["result"] = False
            # pylint: disable-next=consider-using-f-string
            ret["comment"] = "Failed to set the user. {}".format(
                result["stderr"] or result["stdout"]
            )
            return ret

        # pylint: disable-next=consider-using-f-string
        ret["comment"] = "User {} with password set".format(name)
        return ret

    # pylint: disable-next=broad-exception-caught
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
        # pylint: disable-next=undefined-variable
        "result": True if not __opts__["test"] else None,
        "comment": "",
    }

    # pylint: disable-next=undefined-variable
    if __opts__["test"]:
        # pylint: disable-next=consider-using-f-string
        ret["comment"] = "User {} with password will be set".format(name)
        return ret

    try:
        # pylint: disable-next=undefined-variable
        if not __salt__["postgres.user_exists"](name):
            # pylint: disable-next=undefined-variable
            if __opts__["test"]:
                # pylint: disable-next=consider-using-f-string
                ret["comment"] = "User {} will be removed".format(name)
                return ret
        else:
            # pylint: disable-next=undefined-variable
            if __opts__["test"]:
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
        # pylint: disable-next=undefined-variable
        result = __salt__["cmd.run_all"](cmd)

        if result["retcode"] != 0:
            ret["result"] = False
            # pylint: disable-next=consider-using-f-string
            ret["comment"] = "Failed to delete the user. {}".format(
                result["stderr"] or result["stdout"]
            )
            return ret

        # pylint: disable-next=consider-using-f-string
        ret["comment"] = "User {} deleted".format(name)
        return ret

    # pylint: disable-next=broad-exception-caught
    except Exception as err:
        ret["result"] = False
        ret["comment"] = str(err)

    return ret
