"""
Utility states
"""

from salt.exceptions import CommandExecutionError
from salt.states import file


__virtualname__ = "mgrutils"


# pylint: disable-next=invalid-name
def __virtual__():
    """
    This module is always enabled while 'file.managed' is available.
    """
    # pylint: disable-next=undefined-variable
    file.__salt__ = __salt__
    # pylint: disable-next=undefined-variable
    file.__opts__ = __opts__
    # pylint: disable-next=undefined-variable
    file.__pillar__ = __pillar__
    # pylint: disable-next=undefined-variable
    file.__grains__ = __grains__
    # pylint: disable-next=undefined-variable
    file.__context__ = __context__
    # pylint: disable-next=undefined-variable
    file.__utils__ = __utils__
    return __virtualname__


def cmd_dump(name, cmd):
    """
    Dump the output of a command to a file
    """
    ret = {
        "name": name,
        "changes": {},
        # pylint: disable-next=undefined-variable
        "result": True if not __opts__["test"] else None,
        "comment": "",
    }
    try:
        # pylint: disable-next=undefined-variable
        cmd_out = __salt__["cmd.run"](cmd, raise_err=True, python_shell=False)
    except CommandExecutionError:
        ret["result"] = False
        # pylint: disable-next=consider-using-f-string
        ret["comment"] = "Failed to run command {}".format(cmd)
        return ret

    # pylint: disable-next=undefined-variable
    file_ret = __states__["file.managed"](name, contents=cmd_out)
    file_ret["name"] = name
    return file_ret
