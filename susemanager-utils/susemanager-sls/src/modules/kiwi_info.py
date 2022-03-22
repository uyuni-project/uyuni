import salt.exceptions
import logging
import os
import re
import pickle

log = logging.getLogger(__name__)

def parse_profile(chroot):
    ret = {}
    path = os.path.join(chroot, 'image', '.profile')
    if __salt__['file.file_exists'](path):
        profile = __salt__['cp.get_file_str'](path)
        pattern = re.compile(r"^(?P<name>.*?)='(?P<val>.*)'")
        for line in profile.splitlines():
            match = pattern.match(line)
            if match:
                ret[match.group('name')] = match.group('val')
    return ret

def parse_buildinfo(dest):
    ret = {}
    path = os.path.join(dest, 'kiwi.buildinfo')
    if __salt__['file.file_exists'](path):
        profile = __salt__['cp.get_file_str'](path)
        pattern_group = re.compile(r"^\[(?P<name>.*)\]")
        pattern_val = re.compile(r"^(?P<name>.*?)=(?P<val>.*)")

        group = ret
        for line in profile.splitlines():
            match = pattern_group.match(line)
            if match:
                group = {}
                ret[match.group('name')] = group

            match = pattern_val.match(line)
            if match:
                group[match.group('name')] = match.group('val')
    return ret

# fallback for SLES11 Kiwi and for Kiwi NG that does not create the buildinfo file
def guess_buildinfo(dest):
    ret = {'main': {}}
    files = __salt__['file.readdir'](dest)

    pattern_basename = re.compile(r"^(?P<basename>.*)\.packages$")
    pattern_pxe_initrd = re.compile(r"^initrd-netboot.*")
    pattern_pxe_kiwi_ng_initrd = re.compile(r".*\.initrd\..*")
    pattern_pxe_kernel = re.compile(r".*\.kernel\..*")
    pattern_pxe_kiwi_ng_kernel = re.compile(r".*\.kernel$")
    have_kernel = False
    have_initrd = False

    for f in files:
        match = pattern_basename.match(f)
        if match:
            ret['main']['image.basename'] = match.group('basename')

        match = pattern_pxe_initrd.match(f) or pattern_pxe_kiwi_ng_initrd.match(f)
        if match:
            have_initrd = True

        match = pattern_pxe_kernel.match(f) or pattern_pxe_kiwi_ng_kernel.match(f)
        if match:
            have_kernel = True

    if have_kernel and have_initrd:
        ret['main']['image.type'] = 'pxe'
    return ret

# Kiwi NG
def parse_kiwi_result(dest):
    path = os.path.join(dest, 'kiwi.result')
    ret = {}
    if __salt__['file.file_exists'](path):
        try:
            # pickle depends on availability of python kiwi modules
            # which are not under our control so there is certain risk of failure
            # return empty dict in such case
            # the caller should handle all values as optional
            with open(path, 'rb') as f:
                result = pickle.load(f)
                ret['arch'] = result.xml_state.host_architecture
                ret['basename'] = result.xml_state.xml_data.name
                ret['type'] = result.xml_state.build_type.image
                ret['filesystem'] = result.xml_state.build_type.filesystem
                ret['initrd_system'] = result.xml_state.build_type.initrd_system
        except:
            log.exception("Loading kiwi.result")
            # continue with empty dict
    return ret

def parse_packages(path):
    ret = []
    if __salt__['file.file_exists'](path):
        packages = __salt__['cp.get_file_str'](path)
        pattern = re.compile(r"^(?P<name>.*?)\|(?P<epoch>.*?)\|(?P<version>.*?)\|(?P<release>.*?)\|(?P<arch>.*?)\|(?P<disturl>.*?)(\|(?P<license>.*))?$")
        for line in packages.splitlines():
            match = pattern.match(line)
            if match:
                # translate '(none)' values to ''
                d = match.groupdict()
                for k in list(d.keys()):
                    if d[k] == '(none)':
                        d[k] = ''

                # if arch is '' and name begins gpg-pubkey then skip the package
                if d['arch'] == '' and d['name'].startswith('gpg-pubkey'):
                    continue

                ret.append(d)
    return ret

def get_md5(path):
    res = {}
    if not __salt__['file.file_exists'](path):
        return res

    res['hash'] = "md5:" + __salt__['file.get_hash'](path, form='md5')
    res['size'] = __salt__['file.stats'](path).get('size')
    return res

def parse_kiwi_md5(path, compressed = False):
    res = {}

    if not __salt__['file.file_exists'](path):
        return res

    md5_str = __salt__['cp.get_file_str'](path)
    if md5_str is not None:
        if compressed:
            pattern = re.compile(r"^(?P<md5>[0-9a-f]+)\s+(?P<size1>[0-9]+)\s+(?P<size2>[0-9]+)\s+(?P<csize1>[0-9]+)\s+(?P<csize2>[0-9]+)\s*$")
        else:
            pattern = re.compile(r"^(?P<md5>[0-9a-f]+)\s+(?P<size1>[0-9]+)\s+(?P<size2>[0-9]+)\s*$")
        match = pattern.match(md5_str)
        if match:
            res['hash'] = "md5:" + match.group('md5')
            res['size'] = int(match.group('size1')) * int(match.group('size2'))
            if compressed:
                res['compressed_size'] = int(match.group('csize1')) * int(match.group('csize2'))
    return res

_compression_types = [
    { 'suffix': '.gz', 'compression': 'gzip' },
    { 'suffix': '.bz', 'compression': 'bzip' },
    { 'suffix': '.xz', 'compression': 'xz' },
    { 'suffix': '.install.iso',    'compression': None },
    { 'suffix': '.iso',            'compression': None },
    { 'suffix': '.raw',            'compression': None },
    { 'suffix': '',    'compression': None }
    ]

def image_details(dest: str) -> dict:
    res = {}
    buildinfo = parse_buildinfo(dest) or guess_buildinfo(dest)
    kiwiresult = parse_kiwi_result(dest)

    basename = buildinfo.get('main', {}).get('image.basename', '')
    image_type = kiwiresult.get('type') or buildinfo.get('main', {}).get('image.type', 'unknown')
    fstype = kiwiresult.get('filesystem')

    pattern = re.compile(r"^(?P<name>.*)\.(?P<arch>.*)-(?P<version>.*)$")
    match = pattern.match(basename)
    if match:
        name = match.group('name')
        arch = match.group('arch')
        version = match.group('version')
    else:
        return None

    filename = None
    filepath = None
    compression = None
    for c in _compression_types:
        path = os.path.join(dest, basename + c['suffix'])
        if __salt__['file.file_exists'](path):
            compression = c['compression']
            filename = basename + c['suffix']
            filepath = path
            break

    res['image'] = {
        'basename': basename,
        'name': name,
        'arch': arch,
        'type': image_type,
        'version': version,
        'filename': filename,
        'filepath': filepath,
        'fstype': fstype
    }
    if compression:
        res['image'].update({
            'compression': compression,
            'compressed_hash': __salt__['hashutil.digest_file'](filepath, checksum='md5')
        })

    res['image'].update(parse_kiwi_md5(os.path.join(dest, basename + '.md5'), compression is not None))

    return res

def inspect_image(dest: str, build_id: str) -> dict:
    res = image_details(dest)
    if not res:
      return None

    res['image']['build_id'] = build_id

    basename = res['image']['basename']
    image_type = res['image']['type']

    for fstype in ['ext2', 'ext3', 'ext4', 'btrfs', 'xfs']:
        path = os.path.join(dest, basename + '.' + fstype)
        if __salt__['file.file_exists'](path) or __salt__['file.is_link'](path):
            res['image']['fstype'] = fstype
            break

    res['packages'] = parse_packages(os.path.join(dest, basename + '.packages'))

    if image_type == 'pxe':
        res['boot_image'] = inspect_boot_image(dest)

    return res


def inspect_boot_image(dest: str) -> dict:
    res = None
    files = __salt__['file.readdir'](dest)

    pattern = re.compile(r"^(?P<name>.*)\.(?P<arch>.*)-(?P<version>.*)\.kernel\.(?P<kernelversion>.*)\.md5$")
    pattern_kiwi_ng = re.compile(r"^(?P<name>[^-]*)\.(?P<arch>[^-]*)-(?P<version>[^-]*)-(?P<kernelversion>.*)\.kernel$")
    for f in files:
        match = pattern.match(f)
        if match:
            basename = match.group('name') + '.' + match.group('arch') + '-' + match.group('version')
            res = {
                'name': match.group('name'),
                'arch': match.group('arch'),
                'basename': basename,
                'initrd': {
                    'version': match.group('version')
                    },
                'kernel': {
                    'version': match.group('kernelversion')
                    },
                'kiwi_ng': False
            }
            break
        match = pattern_kiwi_ng.match(f)
        if match:
            basename = match.group('name') + '.' + match.group('arch') + '-' + match.group('version')
            res = {
                'name': match.group('name'),
                'arch': match.group('arch'),
                'basename': basename,
                'initrd': {
                    'version': match.group('version')
                    },
                'kernel': {
                    'version': match.group('kernelversion')
                },
                'kiwi_ng': True
            }
            break

    if res is None:
        return None

    for c in _compression_types:
        if res['kiwi_ng']:
            file = basename + '.initrd' + c['suffix']
        else:
            file = basename + c['suffix']
        filepath = os.path.join(dest, file)
        if __salt__['file.file_exists'](filepath):
            res['initrd']['filename'] = file
            res['initrd']['filepath'] = filepath
            if res['kiwi_ng']:
                res['initrd'].update(get_md5(filepath))
            else:
                res['initrd'].update(parse_kiwi_md5(os.path.join(dest, basename + '.md5')))
            break

    if res['kiwi_ng']:
        file = basename + '-' + res['kernel']['version'] + '.kernel'
        filepath = os.path.join(dest, file)
        if __salt__['file.file_exists'](filepath):
            res['kernel']['filename'] = file
            res['kernel']['filepath'] = filepath
            res['kernel'].update(get_md5(filepath))
    else:
        file = basename + '.kernel.' + res['kernel']['version']
        filepath = os.path.join(dest, file)
        if __salt__['file.file_exists'](filepath):
            res['kernel']['filename'] = file
            res['kernel']['filepath'] = filepath
            res['kernel'].update(parse_kiwi_md5(filepath + '.md5'))

    return res

# TODO consider adding checksum to validate upload
def build_info(dest: str, build_id: str) -> dict:
    res = {}
    buildinfo = parse_buildinfo(dest) or guess_buildinfo(dest)
    kiwiresult = parse_kiwi_result(dest)
    basename = buildinfo.get('main', {}).get('image.basename', '')
    image_type = kiwiresult.get('type') or buildinfo.get('main', {}).get('image.type', 'unknown')

    pattern = re.compile(r"^(?P<name>.*)\.(?P<arch>.*)-(?P<version>.*)$")
    match = pattern.match(basename)
    if not match:
        return None
    name = match.group('name')
    arch = match.group('arch')
    version = match.group('version')

    image_filepath = None
    image_filename = None
    for c in _compression_types:
        test_name = basename + c['suffix']
        filepath = os.path.join(dest, test_name)
        if __salt__['file.file_exists'](filepath):
            image_filename = test_name
            image_filepath = filepath
            break

    res['image'] = {
        'name': name,
        'arch': arch,
        'version': version,
        'filepath': image_filepath,
        'filename': image_filename,
        'build_id': build_id
    }

    if image_type == 'pxe':
        r = inspect_boot_image(dest)
        res['boot_image'] = {
            'initrd': {
                'filepath': r['initrd']['filepath'],
                'filename': r['initrd']['filename'],
                'hash': r['initrd']['hash']
            },
            'kernel': {
                'filepath': r['kernel']['filepath'],
                'filename': r['kernel']['filename'],
                'hash': r['kernel']['hash']
            }
        }

    return res
