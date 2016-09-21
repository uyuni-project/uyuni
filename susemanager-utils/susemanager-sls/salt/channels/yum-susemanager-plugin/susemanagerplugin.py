from yum.plugins import TYPE_CORE
from yum import config
import os
import hashlib

CK_PATH = "/var/cache/salt/minion/rpmdb.cookie"
RPM_PATH = "/var/lib/rpm/Packages"

requires_api_version = '2.5'
plugin_type = TYPE_CORE


def _get_mtime():
    '''
    Get the modified time of the RPM Database.
    Returns:
        Unix ticks
    '''
    return os.path.exists(RPM_PATH) and int(os.path.getmtime(RPM_PATH)) or 0

def _get_checksum():
    '''
    Get the checksum of the RPM Database.
    Returns:
        hexdigest
    '''
    digest = hashlib.md5()
    with open(RPM_PATH, "rb") as rpm_db_fh:
        while True:
            buff = rpm_db_fh.read(0x1000)
            if not buff:
                break
            digest.update(buff)
    return digest.hexdigest()

def config_hook(conduit):
    config.RepoConf.susemanager_token = config.Option()

def prereposetup_hook(conduit):
    for repo in conduit.getRepos().listEnabled():
       susemanager_token = getattr(repo, 'susemanager_token', None)
       if susemanager_token:
          repo.http_headers['X-Mgr-Auth'] = susemanager_token

def posttrans_hook(conduit):
    if 'SALT_RUNNING' not in os.environ:
        with open(CK_PATH, 'w') as ck_fh:
            ck_fh.write('{chksum} {mtime}\n'.format(chksum=_get_checksum(), mtime=_get_mtime()))
