from subprocess import Popen, PIPE
import logging
import stat
import grp
import shlex
import os
import shutil
import salt.utils
import tempfile

import certs.mgr_ssl_cert_setup

log = logging.getLogger(__name__)

GROUP_OWNER = 'susemanager'


def delete_rejected_key(minion):
    '''
    Delete a previously rejected minion key from minions_rejected
    :param minion: the minion id to look for
    :return: map containing retcode and stdout/stderr
    '''
    path_rejected = "/etc/salt/pki/master/minions_rejected/"
    path = os.path.normpath(path_rejected + minion)
    if not path.startswith(path_rejected):
        return {"retcode": -1, "stderr": "Unexpected path: " + path}
    if os.path.isfile(path):
        cmd = ['rm', path]
        return _cmd(cmd)
    return {"retcode": 0}


def ssh_keygen(path=None):
    '''
    Generate SSH keys using the given path.
    :param path: the path. If the None, the keys are generated in a temporary folder, returned, and removed.
    :return: map containing retcode and stdout/stderr. Also contains key and public_key if no path was provided
    '''
    temp_dir = None
    with tempfile.TemporaryDirectory() as temp_dir:
        out_path = os.path.join(temp_dir, "key") if path is None else path
        result = {"retcode": 0}
        if not path or not os.path.isfile(path):
            cmd = ['ssh-keygen', '-N', '', '-f', out_path, '-t', 'rsa', '-q']
            result = _cmd(cmd)
        elif path:
            out_path = path

        if os.path.isfile(out_path) and result["retcode"] == 0:
            with open(out_path, "r") as fd:
                result["key"] = fd.read()
            with open(out_path + ".pub", "r") as fd:
                result["public_key"] = fd.read()

    return result


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
        host_port = hostname.split(":")
        key = clientkey if idx == 0 else proxykey
        opts = " ".join(["-o {}={}".format(opt, val) for opt, val in list(options.items())])
        ssh = "/usr/bin/ssh -p {} -i {} {} -o User={} {}"\
            .format(host_port[1] if len(host_port) > 1 else 22, key, opts, user, host_port[0])
        cmd.extend(shlex.split(ssh))
    cmd.append(command)
    ret = _cmd(cmd)
    if outputfile:
        with open(outputfile, "w") as out:
            out.write(ret["stdout"])
    return ret

def remove_ssh_known_host(user, hostname, port):
    return __salt__['salt.cmd']('ssh.rm_known_host', user, hostname, None, port)


def _cmd(cmd):
    p = Popen(cmd, stdout=PIPE, stderr=PIPE)
    stdout, stderr = p.communicate()
    return {"retcode": p.returncode, "stdout": salt.utils.stringutils.to_unicode(stdout), "stderr": salt.utils.stringutils.to_unicode(stderr)}


def move_minion_uploaded_files(minion=None, dirtomove=None, basepath=None, actionpath=None):
    srcdir = os.path.join(__opts__['cachedir'], "minions", minion, 'files', dirtomove.lstrip('/'))
    scapstorepath = os.path.join(basepath, actionpath)
    susemanager_gid = grp.getgrnam(GROUP_OWNER).gr_gid
    if not os.path.exists(scapstorepath):
        log.debug("Creating action directory: {0}".format(scapstorepath))
        try:
            os.makedirs(scapstorepath)
        except Exception as err:
            log.error('Failed to create dir {0}'.format(scapstorepath), exc_info=True)
            return {False: 'Salt failed to create dir {0}: {1}'.format(scapstorepath, str(err))}
        # change group permissions to rwx and group owner to susemanager
        mode = stat.S_IRWXU | stat.S_IRWXG | stat.S_IROTH | stat.S_IXOTH
        subdirs = actionpath.split('/')
        for idx in range(1, len(subdirs)):
            if subdirs[0: idx] != '':
                # ignore errors. If dir has owner != salt then chmod fails but the dir
                # might still have the correct group owner
                try:
                    os.chmod(os.path.join(basepath, *subdirs[0: idx]), mode)
                except OSError:
                    pass
                try:
                    os.chown(os.path.join(basepath, *subdirs[0: idx]), -1, susemanager_gid)
                except OSError:
                    pass

    try:
        # move the files to the scap store dir
        for fl in os.listdir(srcdir):
            shutil.move(os.path.join(srcdir, fl), scapstorepath)
        # change group owner to susemanager
        for fl in os.listdir(scapstorepath):
            os.chown(os.path.join(scapstorepath, fl), -1, susemanager_gid)
    except Exception as err:
        log.error('Salt failed to move {0} -> {1}'.format(srcdir, scapstorepath), exc_info=True)
        return {False: str(err)}
    return {True: scapstorepath}


def check_ssl_cert(root_ca, server_crt, server_key, intermediate_cas):
    '''
    Check that the provided certificates are valid and return the certificate and key to deploy.
    '''
    try:
        cert = certs.mgr_ssl_cert_setup.getContainersSetup(root_ca, intermediate_cas, server_crt, server_key)
        return {"cert": cert}
    except certs.mgr_ssl_cert_setup.CertCheckError as err:
        return {"error": str(err)}
