import logging
import os
import copy
import shutil
import yaml

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
def generate_pillar(dest, image_info, url_base):
    pillar = {
        'images': {},
        'boot_images': {}
    }

    # images in pillar are indexed by name and version
    name = image_info['image']['name']
    version = image_info['image']['version']

    pillar['images'][name] = {}
    pillar['images'][name][version] = copy.deepcopy(image_info['image'])

    del pillar['images'][name][version]['version']


    # where to put the image on branch server, under /srv/saltboot/
    image_dir_name = image_info['bundle']['basename'] + '-' + image_info['bundle']['id']
    local_path = 'image/' + image_dir_name

    # fill in info for syncing between SUMA and branch server
    pillar['images'][name][version]['sync'] = {
        'bundle_url': url_base + '/' + image_info['bundle']['filename'],
        'bundle_hash': image_info['bundle']['hash'],
        'local_path': local_path
    }

    # URL used by terminals to download image from branch server
    # FIXME: protocol should be configurable
    pillar['images'][name][version]['url'] = 'tftp://tftp/' + local_path + '/' + image_info['image']['filename']

    # mark image as active
    pillar['images'][name][version]['inactive'] = False


    # in general, the boot image names can be independent, but for now we use
    # the same name as system image
    boot_image_name = name + '-' + version

    pillar['images'][name][version]['boot_image'] = boot_image_name
    pillar['boot_images'][boot_image_name] = copy.deepcopy(image_info['boot_image'])

    # URL used by terminals to download boot image from branch server
    # the files are unpacked from bundle tarball to the same directory as system image
    pillar['boot_images'][boot_image_name]['kernel']['url'] = 'tftp://tftp/boot/' + boot_image_name + '/' + image_info['boot_image']['kernel']['filename']
    pillar['boot_images'][boot_image_name]['initrd']['url'] = 'tftp://tftp/boot/' + boot_image_name + '/' + image_info['boot_image']['initrd']['filename']
    pillar['boot_images'][boot_image_name]['sync'] = {
        'local_path': boot_image_name,
        'kernel_link': '../../' + local_path + '/' + image_info['boot_image']['kernel']['filename'],
        'initrd_link': '../../' + local_path + '/' + image_info['boot_image']['initrd']['filename']
    }

    # write the pillar file
    with open(dest, 'w') as outfile:
        yaml.dump(pillar, outfile, default_flow_style=False)


# move the uploaded image to salt (or http) fileserver
def move_image_bundle(image_info, build_host):
    src = os.path.join('/var/cache/salt/master/minions', build_host, 
                       'files/var/lib/Kiwi/images', image_info['bundle']['id'],
                       image_info['bundle']['filename'])

    shutil.move(src, '/srv/susemanager/salt/images')
    #shutil.move(src, '/srv/www/htdocs/images')


# Complete workflow: build and inspect image, move it to fileserver and generate pillar
#
# Example args:
# source = '/usr/share/kiwi/image/jeos-6.0.0', build_id = 'jeosbuild-6.0.0', build_host = 'dhcp204',
# kiwi_repositories = [
#   'http://smt.suse.cz/repo/SUSE/Products/SLE-SERVER/12-SP3/x86_64/product/',
#   'http://smt.suse.cz/repo/SUSE/Updates/SLE-SERVER/12-SP3/x86_64/update/',
#   'http://smt.suse.cz/repo/SUSE/Products/SLE-Manager-Tools/12/x86_64/product/',
#   'http://smt.suse.cz/repo/SUSE/Updates/SLE-Manager-Tools/12/x86_64/update/'
# ]

def build_and_register(source, build_id, build_host, kiwi_repositories = []):
    client = salt.client.get_local_client()

    pillar = {
        'source': source,
        'build_id': build_id,
        'kiwi_repositories': kiwi_repositories
    }

    res = client.cmd(build_host, 'state.apply', ['images/kiwi-image-build'], kwarg={'pillar': pillar})

    res = client.cmd(build_host, 'state.apply', ['images/kiwi-image-inspect'], kwarg={'pillar': pillar})

    image_info = res[build_host]['module_|-mgr_inspect_kiwi_image_|-kiwi_info.inspect_image_|-run']['changes']['ret']

    generate_pillar(("/srv/pillar/images/image-" + image_info['bundle']['basename'] + '-' + image_info['bundle']['id']).replace('.', '-') + '.sls', image_info, "salt://images")

    move_image_bundle(image_info, build_host)

    return image_info
