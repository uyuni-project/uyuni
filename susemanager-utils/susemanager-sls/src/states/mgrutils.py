"""
Utility states
"""

from salt.exceptions import CommandExecutionError
from salt.states import file


__virtualname__ = 'mgrutils'


def __virtual__():
    '''
    This module is always enabled while 'file.managed' is available.
    '''
    file.__salt__ = __salt__
    file.__opts__ = __opts__
    file.__pillar__ = __pillar__
    file.__grains__ = __grains__
    file.__context__ = __context__
    file.__utils__ = __utils__
    return __virtualname__


def cmd_dump(name, cmd):
    """
    Dump the output of a command to a file
    """
    ret = {
        "name": name,
        "changes": {},
        "result": True if not __opts__["test"] else None,
        "comment": "",
    }
    try:
        cmd_out = __salt__['cmd.run'](cmd, raise_err=True, python_shell=False)
    except CommandExecutionError:
        ret["result"] = False
        ret["comment"] = "Failed to run command {}".format(cmd)
        return ret

    file_ret = __states__["file.managed"](name, contents=cmd_out)
    file_ret["name"] = name
    return file_ret

