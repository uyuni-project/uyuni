from subprocess import Popen, PIPE
import logging
import os
import copy
import shutil
import yaml
import json

import salt.client
log = logging.getLogger(__name__)

# this documents the actions needed to build and register a kiwi image
#
# The main steps are:
#   build and inspect image on build host
#   upload the image to SUMA
#   move it to salt ot http fileserver
#   add pillar with image info

# generate image pillar based on image_info returned by mages/kiwi-image-inspect
# entries like names or checksums can be used directly in pillar
# this function adjussts pillar structure and adds urls to image locations
# on SUMA and on Branch server
def generate_pillar(dest, image, bundle, boot_image, url_base):
    if type(image) is str:
      image = json.loads(image)
    if type(bundle) is str:
      bundle = json.loads(bundle)
    if type(boot_image) is str:
      boot_image = json.loads(boot_image)

    if image['type'] != 'pxe':
      return {'returncode': 0, 'stdout': 'Pillar generation skipped. Not a PXE image type (image type "{}")'.format(image['type'])}

    pillar = {
        'images': {},
        'boot_images': {}
    }

    # images in pillar are indexed by name and version
    name = image['name']
    version = image['version']

    pillar['images'][name] = {}
    pillar['images'][name][version] = copy.deepcopy(image)

    del pillar['images'][name][version]['version']


    # where to put the image on branch server, under /srv/saltboot/
    image_dir_name = bundle['basename'] + '-' + bundle['id']
    local_path = 'image/' + image_dir_name

    # fill in info for syncing between SUMA and branch server
    pillar['images'][name][version]['sync'] = {
        'bundle_url': url_base + '/' + bundle['filename'],
        'bundle_hash': bundle['hash'],
        'local_path': local_path
    }

    # URL used by terminals to download image from branch server
    # FIXME: protocol should be configurable
    pillar['images'][name][version]['url'] = 'tftp://tftp/' + local_path + '/' + image['filename']

    # mark image as active
    pillar['images'][name][version]['inactive'] = False


    # in general, the boot image names can be independent, but for now we use
    # the same name as system image
    boot_image_name = name + '-' + version

    pillar['images'][name][version]['boot_image'] = boot_image_name
    pillar['boot_images'][boot_image_name] = copy.deepcopy(boot_image)

    # URL used by terminals to download boot image from branch server
    # the files are unpacked from bundle tarball to the same directory as system image
    pillar['boot_images'][boot_image_name]['kernel']['url'] = 'tftp://tftp/boot/' + boot_image_name + '/' + boot_image['kernel']['filename']
    pillar['boot_images'][boot_image_name]['initrd']['url'] = 'tftp://tftp/boot/' + boot_image_name + '/' + boot_image['initrd']['filename']
    pillar['boot_images'][boot_image_name]['sync'] = {
        'local_path': boot_image_name,
        'kernel_link': '../../' + local_path + '/' + boot_image['kernel']['filename'],
        'initrd_link': '../../' + local_path + '/' + boot_image['initrd']['filename']
    }

    # write the pillar file
    with open(dest, 'w') as outfile:
        yaml.safe_dump(pillar, outfile, default_flow_style=False)
    p = Popen(['/bin/true'], stdout=PIPE, stderr=PIPE)

    stdout, stderr = p.communicate()
    return {"returncode": p.returncode, "stdout": salt.utils.to_unicode(stdout), "stderr": salt.utils.to_unicode(stderr)}


