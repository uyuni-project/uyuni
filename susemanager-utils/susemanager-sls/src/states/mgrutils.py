"""
Utility states
"""

from salt.exceptions import CommandExecutionError
from salt.states import file


__virtualname__ = "mgrutils"


def __virtual__():  #  pylint: disable=invalid-name
    """
    This module is always enabled while 'file.managed' is available.
    """
    file.__salt__ = __salt__  #  pylint: disable=undefined-variable
    file.__opts__ = __opts__  #  pylint: disable=undefined-variable
    file.__pillar__ = __pillar__  #  pylint: disable=undefined-variable
    file.__grains__ = __grains__  #  pylint: disable=undefined-variable
    file.__context__ = __context__  #  pylint: disable=undefined-variable
    file.__utils__ = __utils__  #  pylint: disable=undefined-variable
    return __virtualname__


def cmd_dump(name, cmd):
    """
    Dump the output of a command to a file
    """
    ret = {
        "name": name,
        "changes": {},
        "result": True if not __opts__["test"] else None,  #  pylint: disable=undefined-variable
        "comment": "",
    }
    try:
        cmd_out = __salt__["cmd.run"](cmd, raise_err=True, python_shell=False)  #  pylint: disable=undefined-variable
    except CommandExecutionError:
        ret["result"] = False
        ret["comment"] = "Failed to run command {}".format(cmd)  #  pylint: disable=consider-using-f-string
        return ret

    file_ret = __states__["file.managed"](name, contents=cmd_out)  #  pylint: disable=undefined-variable
    file_ret["name"] = name
    return file_ret
