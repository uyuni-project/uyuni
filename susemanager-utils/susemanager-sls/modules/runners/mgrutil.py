import logging
import stat
import grp
import os
import shutil

log = logging.getLogger(__name__)

GROUP_OWNER = 'susemanager'


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

