# SUSE Manager pylint: disable=missing-module-docstring,invalid-name
# Copyright (c) 2018--2022 SUSE LLC

# runner to collect image from build host

import os
import logging

log = logging.getLogger(__name__)


def upload_file_from_minion(minion, minion_ip, filetoupload, targetdir):
    fqdn = __salt__["cache.grains"](tgt=minion).get(minion, {}).get("fqdn")  #  pylint: disable=undefined-variable
    ssh_port = __salt__["cache.grains"](tgt=minion).get(minion, {}).get("ssh_port", 22)  #  pylint: disable=undefined-variable
    log.info(
        'Collecting image "{}" from minion {} (FQDN: {}, IP: {}, SSH PORT: {})'.format(  #  pylint: disable=logging-format-interpolation,consider-using-f-string
            filetoupload, minion, fqdn, minion_ip, ssh_port
        )
    )
    if not fqdn or fqdn == "localhost":
        fqdn = minion_ip
    src = "root@{}:{}".format(fqdn, filetoupload)  #  pylint: disable=consider-using-f-string
    tries = 3
    res = None
    while tries > 0:
        res = __salt__["salt.cmd"](  #  pylint: disable=undefined-variable
            "rsync.rsync",
            src,
            targetdir,
            rsh="ssh -o IdentityFile=/srv/susemanager/salt/salt_ssh/mgr_ssh_id -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p {}".format(  #  pylint: disable=line-too-long,consider-using-f-string
                ssh_port
            ),
        )
        # In case of unexplained error, try again (can be dns failure, networking failure, ...)  #  pylint: disable=line-too-long
        if res.get("retcode", 0) != 255:
            break
        tries -= 1
    if res.get("retcode") == 0:
        filename = os.path.basename(filetoupload)
        # Check and set correct permission for uploaded file. We need it world readable
        __salt__["salt.cmd"](  #  pylint: disable=undefined-variable
            "file.check_perms",
            os.path.join(targetdir, filename),
            None,
            "salt",
            "salt",
            644,
        )
    return res


def move_file_from_minion_cache(minion, filetomove, targetdir):
    src = os.path.join(
        __opts__["cachedir"], "minions", minion, "files", filetomove.lstrip("/")  #  pylint: disable=undefined-variable
    )
    log.info('Collecting image from minion cache "{}"'.format(src))  #  pylint: disable=logging-format-interpolation,consider-using-f-string
    # file.move throws an exception in case of error
    return __salt__["salt.cmd"]("file.move", src, targetdir)  #  pylint: disable=undefined-variable


def kiwi_collect_image(minion, minion_ip, filepath, image_store_dir):
    try:
        __salt__["salt.cmd"]("file.mkdir", image_store_dir)  #  pylint: disable=undefined-variable
    except PermissionError:
        log.error(
            "kiwi_collect_image: Unable to create image directory {}".format(  #  pylint: disable=logging-format-interpolation,consider-using-f-string
                image_store_dir
            )
        )
        return {
            "retcode": 13,
            "comment": "Unable to create image directory {}".format(image_store_dir),  #  pylint: disable=consider-using-f-string
        }

    use_salt_transport = (
        __salt__["cache.pillar"](tgt=minion).get(minion, {}).get("use_salt_transport")  #  pylint: disable=undefined-variable
    )
    if use_salt_transport:
        return move_file_from_minion_cache(minion, filepath, image_store_dir)

    return upload_file_from_minion(minion, minion_ip, filepath, image_store_dir)
