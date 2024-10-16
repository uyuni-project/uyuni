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
    mgradm = False
    postgres = False

    # pylint: disable-next=undefined-variable
    if not __grains__.get("is_mgr_server"):
        return (False, "Minion is not a mgr server")
    # pylint: disable-next=undefined-variable
    if "postgres.user_exists" in __salt__:
        postgres = True
    if os.path.exists("/usr/bin/mgradm") and os.path.exists("/usr/bin/mgrctl"):
        mgradm = True
    if not postgres and not mgradm:
        return (
            False,
            "Unable to load postgres module.  Make sure `postgres.bins_dir` is set.",
        )
    return __virtualname__


def _user_exists(dbuser):
    # pylint: disable-next=undefined-variable
    if "postgres.user_exists" in __salt__:
        # pylint: disable-next=undefined-variable
        if __salt__["postgres.user_exists"](dbuser):
            return True
        else:
            return False
    elif os.path.exists("/usr/bin/mgradm"):
        cmd = ["mgradm", "support", "sql", "-d", "reportdb", "--logLevel", "error"]
        # pylint: disable-next=undefined-variable
        result = __salt__["cmd.run_all"](
            cmd, stdin="SELECT pg_roles.rolname as name FROM pg_roles;"
        )

        if result["retcode"] != 0:
            raise CommandExecutionError(result["stderr"])

        for line in (line.strip() for line in result["stdout"].splitlines()):
            if line and line == dbuser.lower():
                return True
    return False


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

        if _user_exists(name):
            cmd.append("--modify")
        else:
            cmd.append("--add")

        result = {}
        if os.path.exists("/usr/bin/mgrctl"):
            command = ["mgrctl", "exec", " ".join(cmd)]
            # pylint: disable-next=undefined-variable
            result = __salt__["cmd.run_all"](command)
        else:
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
        if not _user_exists(name):
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

        result = {}
        if os.path.exists("/usr/bin/mgrctl"):
            command = ["mgrctl", "exec", " ".join(cmd)]
            # pylint: disable-next=undefined-variable
            result = __salt__["cmd.run_all"](command)
        else:
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
