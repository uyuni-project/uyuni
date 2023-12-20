import logging  #  pylint: disable=missing-module-docstring
import subprocess
import salt.utils.timed_subprocess
from salt.exceptions import CommandExecutionError

try:
    from salt.utils.path import which_bin as _which_bin
except ImportError:
    from salt.utils import which_bin as _which_bin

log = logging.getLogger(__name__)

__virtualname__ = "ssh_agent"

__ssh_agent = "/usr/bin/ssh-agent"  #  pylint: disable=invalid-name
__ssh_add = "/usr/bin/ssh-add"  #  pylint: disable=invalid-name


def __virtual__():  #  pylint: disable=invalid-name
    """
    This module is always enabled while 'ssh-agent' is available.
    """
    return (
        __virtualname__
        if _which_bin(["ssh-agent"])
        else (False, "ssh-agent is not available")
    )


def __call_ssh_tool(ssh_tool, cmd_args="", **kwargs):  #  pylint: disable=invalid-name,unused-argument
    log.debug("Calling ssh-agent: '{} {}'".format(ssh_tool, cmd_args))  #  pylint: disable=logging-format-interpolation,consider-using-f-string
    try:
        ssh_tool_proc = salt.utils.timed_subprocess.TimedProc(
            [ssh_tool] + cmd_args.split(),
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        ssh_tool_proc.run()
    except Exception as exc:
        error_msg = "Unexpected error while calling {}: {}".format(ssh_tool, exc)  #  pylint: disable=consider-using-f-string
        log.error(error_msg)
        raise CommandExecutionError(error_msg)  #  pylint: disable=raise-missing-from

    if ssh_tool_proc.process.returncode != 0:
        error_msg = "Unexpected error {} when calling {} {}: {} {}".format(  #  pylint: disable=consider-using-f-string
            ssh_tool_proc.process.returncode,
            ssh_tool,
            cmd_args,
            salt.utils.stringutils.to_str(ssh_tool_proc.stdout),
            salt.utils.stringutils.to_str(ssh_tool_proc.stderr),
        )
        log.error(error_msg)
        raise CommandExecutionError(error_msg)

    return ssh_tool_proc


def start_agent(**kwargs):  #  pylint: disable=unused-argument
    result = __call_ssh_tool(__ssh_agent)

    stdout = salt.utils.stringutils.to_str(result.stdout)
    ssh_agent_lines = stdout.splitlines()

    variables = dict()
    for line in ssh_agent_lines:
        if line.startswith("SSH"):
            line_content_list = line.split(";")
            var, rest = line_content_list[0], line_content_list[1:]  #  pylint: disable=unused-variable
            key, val = var.strip().split("=", 1)
            variables[key] = val

    __salt__["environ.setenv"](variables)  #  pylint: disable=undefined-variable
    return variables


def list_keys(**kwargs):  #  pylint: disable=unused-argument
    result = __call_ssh_tool(__ssh_add, "-l")
    return salt.utils.stringutils.to_str(result.stdout)


def add_key(ssh_key_file, **kwargs):  #  pylint: disable=unused-argument
    __call_ssh_tool(__ssh_add, ssh_key_file)
    return True


def kill(**kwargs):  #  pylint: disable=unused-argument
    __call_ssh_tool(__ssh_agent, "-k")
    return True
