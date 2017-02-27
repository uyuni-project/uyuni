from subprocess import Popen, PIPE
import logging
import shlex
import os

log = logging.getLogger(__name__)

def ssh_keygen(path):
    '''
    Generate SSH keys using the given path.
    :param path: the path
    :return: map containing returncode and stdout/stderr
    '''
    if os.path.isfile(path):
        return {"returncode": -1, "stderr": "Key file already exists"}
    cmd = ['ssh-keygen', '-N', '', '-f', path, '-t', 'rsa', '-q']
    # if not os.path.isdir(os.path.dirname(path)):
    #     os.makedirs(os.path.dirname(path))
    return _cmd(cmd)


def chain_ssh_cmd(hosts=None, clientkey=None, proxykey=None, user="root", options=None, command=None, outputfile=None):
    '''
    Chain ssh calls over one or more hops to run a command on the last host in the chain.
    :param hosts:
    :param clientkey:
    :param proxykey:
    :param user:
    :param options:
    :param command:
    :param outputfile:
    :return:
    '''
    cmd = []
    for idx, hostname in enumerate(hosts):
        key = clientkey if idx == 0 else proxykey
        opts = " ".join(["-o {}={}".format(opt, val) for opt, val in options.items()])
        ssh = "/usr/bin/ssh -i {} {} -o User={} {}"\
            .format(key, opts, user, hostname)
        cmd.extend(shlex.split(ssh))
    cmd.append(command)
    ret = _cmd(cmd)
    if outputfile:
        with open(outputfile, "w") as out:
            out.write(ret["stdout"])
    return ret


def _cmd(cmd):
    p = Popen(cmd, stdout=PIPE, stderr=PIPE)
    stdout, stderr = p.communicate()
    return {"returncode": p.returncode, "stdout": stdout, "stderr": stderr}

def move_minion_uploaded_files(minion=None, dirtomove=None, scapstorepath=None):
    src = os.path.join(__opts__['cachedir'], "minions", minion, 'files', dirtomove.lstrip('/'))
    try:
        shutil.move(src, scapstorepath)
    except Exception as err:
        log.error('Failed to move {0} -> {1}'.format(src, scapstorepath), exc_info=True)
        return {False: str(err)}
    return {True: scapstorepath}

