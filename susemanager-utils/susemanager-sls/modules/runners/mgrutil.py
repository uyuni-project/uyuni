from subprocess import Popen, PIPE
import os


def ssh_keygen(path):
    '''
    Generate SSH keys using the give path
    :param path: the path
    :return: map containing returncode and stdout/stderr
    '''
    if os.path.isfile(path):
        return {"returncode": -1, "stderr": "Key file already exists"}
    cmd = ['ssh-keygen', '-N', '', '-f', path, '-t', 'rsa', '-q']
    # if not os.path.isdir(os.path.dirname(path)):
    #     os.makedirs(os.path.dirname(path))
    p = Popen(cmd, stdout=PIPE, stderr=PIPE)
    stdout, stderr = p.communicate()
    if p.returncode != 0:
        return {"returncode": p.returncode, "stderr": stderr}
    return {"returncode": p.returncode, "stdout": stdout}
