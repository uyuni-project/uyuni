# SUSE Manager
# Copyright (c) 2018--2020 SUSE LLC

# runner to collect image from build host

import os
import logging

log = logging.getLogger(__name__)

def upload_file_from_minion(minion, minion_ip, filetoupload, targetdir):
    fqdn = __salt__['cache.grains'](tgt=minion).get(minion, {}).get('fqdn')
    log.info('Collecting image "{}" from minion {} (FQDN: {}, IP: {})'.format(filetoupload, minion, fqdn, minion_ip))
    if not fqdn or fqdn == 'localhost':
        fqdn = minion_ip
    src = 'root@{}:{}'.format(fqdn, filetoupload)
    return __salt__['salt.cmd'](
      'rsync.rsync',
      src, targetdir,
      rsh='ssh -o IdentityFile=/srv/susemanager/salt/salt_ssh/mgr_ssh_id -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null'
    )

def move_file_from_minion_cache(minion, filetomove, targetdir):
    src = os.path.join(__opts__['cachedir'], 'minions', minion, 'files', filetomove.lstrip('/'))
    log.info('Collecting image from minion cache "{}"'.format(src))
    # file.move throws an exception in case of error
    return __salt__['salt.cmd']('file.move', src, targetdir)

def kiwi_collect_image(minion, minion_ip, filepath, image_store_dir):
    __salt__['salt.cmd']('file.mkdir', image_store_dir)

    use_salt_transport = __salt__['cache.pillar'](tgt=minion).get(minion, {}).get('use_salt_transport')
    if use_salt_transport:
        return move_file_from_minion_cache(minion, filepath, image_store_dir)

    return upload_file_from_minion(minion, minion_ip, filepath, image_store_dir)
