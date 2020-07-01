import logging
import subprocess
import salt.utils.timed_subprocess
from salt.exceptions import CommandExecutionError
try:
    from salt.utils.path import which_bin as _which_bin
except ImportError:
    from salt.utils import which_bin as _which_bin

log = logging.getLogger(__name__)

__virtualname__ = 'ssh_agent'

__ssh_agent = '/usr/bin/ssh-agent'
__ssh_add = '/usr/bin/ssh-add'

def __virtual__():
    '''
    This module is always enabled while 'ssh-agent' is available.
    '''
    return __virtualname__ if _which_bin(['ssh-agent']) else (False, 'ssh-agent is not available')

def __call_ssh_tool(ssh_tool, cmd_args = "", **kwargs):
    log.debug("Calling ssh-agent: '{} {}'".format(ssh_tool, cmd_args))
    try:
        ssh_tool_proc = salt.utils.timed_subprocess.TimedProc(
            [ssh_tool] + cmd_args.split(),
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )
        ssh_tool_proc.run()
    except Exception as exc:
        error_msg = "Unexpected error while calling {}: {}".format(ssh_tool, exc)
        log.error(error_msg)
        raise CommandExecutionError(error_msg)    

    if ssh_tool_proc.process.returncode != 0:
        error_msg = "Unexpected error {} when calling {} {}: {} {}".format(
                ssh_tool_proc.process.returncode,
                ssh_tool,
                cmd_args,
                salt.utils.stringutils.to_str(ssh_tool_proc.stdout),
                salt.utils.stringutils.to_str(ssh_tool_proc.stderr))
        log.error(error_msg)
        raise CommandExecutionError(error_msg)

    return ssh_tool_proc


def start_agent(**kwargs):
    result = __call_ssh_tool(__ssh_agent)

    stdout = salt.utils.stringutils.to_str(result.stdout)
    ssh_agent_lines = stdout.splitlines()

    variables = dict()
    for line in ssh_agent_lines:
        if line.startswith('SSH'):
            line_content_list = line.split(';')
            var, rest = line_content_list[0], line_content_list[1:]
            key, val = var.strip().split("=", 1)
            variables[key] = val

    __salt__['environ.setenv'](variables)
    return variables


def list_keys(**kwargs):
    result = __call_ssh_tool(__ssh_add, "-l")
    return salt.utils.stringutils.to_str(result.stdout)


def add_key(ssh_key_file, **kwargs):
    __call_ssh_tool(__ssh_add, ssh_key_file)
    return True

def kill(**kwargs):
    __call_ssh_tool(__ssh_agent, "-k")
    return True
