# SUSE Manager
# Copyright (c) 2018 SUSE LLC

# runner to collect image from build host

import salt.exceptions
import logging
from mgrutil import move_minion_uploaded_files

log = logging.getLogger(__name__)

os_image_store_dir = '/srv/www/os-image'

def upload_file_from_minion(minion, filetoupload, targetdir):
    grains = __salt__['cache.grains'](tgt=minion)
    fqdn = grains.get('fqdn')
    src = 'root@' + fqdn + ':' + filetoupload
    return __salt__['rsync.rsync'](src, targetdir)

def kiwi_collect_image(minion, filepath):
    if not os.path.exists(os_image_store_dir):
        raise salt.exceptions.SaltRunnerError('Target directory {} does not exists'.format(os_image_store_dir))

    pillars = __salt__['cache.pillar'](tgt=minion)
    if pillars.get('use_salt_trasport'):
      return move_minion_uploaded_files(minion = minion,
                                        dirtomove=filepath,
                                        basepath=os_image_store_dir)

    return upload_file_from_minion(minion, filepath, os_image_store_dir)
