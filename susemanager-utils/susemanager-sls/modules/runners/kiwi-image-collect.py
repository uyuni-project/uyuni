# SUSE Manager
# Copyright (c) 2018 SUSE LLC

# runner to collect image from build host

import os
import salt.exceptions
import logging
from mgrutil import move_minion_uploaded_files

log = logging.getLogger(__name__)

os_image_store_dir = '/srv/www/os-image'

def upload_file_from_minion(minion, filetoupload, targetdir):
    grains = list(__salt__['cache.grains'](tgt=minion).values())[0]
    addr4 = None
    for addr in grains.get('ipv4'):
      if addr == '127.0.0.1':
        continue
      else:
        if __salt__['salt.cmd']('network.ping', addr, return_boolean=True):
          addr4 = addr
          break

    if not addr4:
      raise salt.exceptions.SaltRunnerError('Could not find reachable IPv4 address for minion {}'.format(minion))

    src = 'root@' + addr4 + ':' + filetoupload
    return __salt__['salt.cmd'](
      'rsync.rsync',
      src, targetdir,
      rsh='ssh -o IdentityFile=/srv/susemanager/salt/salt_ssh/mgr_ssh_id -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null'
    )

def kiwi_collect_image(minion, filepath):
    if not os.path.exists(os_image_store_dir):
        raise salt.exceptions.SaltRunnerError('Target directory {} does not exists'.format(os_image_store_dir))

    pillars = list(__salt__['cache.pillar'](tgt=minion).values())[0]
    if pillars.get('use_salt_trasport'):
      return move_minion_uploaded_files(minion = minion,
                                        dirtomove=filepath,
                                        basepath=os_image_store_dir)

    return upload_file_from_minion(minion, filepath, os_image_store_dir)
