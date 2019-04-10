import salt.exceptions
import logging
import os
import re

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

# fallback for SLES11 Kiwi that does not create the buildinfo file
def guess_buildinfo(dest):
    ret = {'main': {}}
    files = __salt__['file.readdir'](dest)

    pattern_basename = re.compile(r"^(?P<basename>.*)\.packages$")
    pattern_pxe = re.compile(r"^initrd-netboot")
    for f in files:
        match = pattern_basename.match(f)
        if match:
            ret['main']['image.basename'] = match.group('basename')

        match = pattern_pxe.match(f)
        if match:
            ret['main']['image.type'] = 'pxe'
    return ret

def parse_packages(path):
    ret = []
    if __salt__['file.file_exists'](path):
        packages = __salt__['cp.get_file_str'](path)
        pattern = re.compile(r"^(?P<name>.*)\|(?P<epoch>.*)\|(?P<version>.*)\|(?P<release>.*)\|(?P<arch>.*)\|(?P<disturl>.*)$")
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
            res['hash'] = match.group('md5')
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

def image_details(dest, bundle_dest = None):
    res = {}
    buildinfo = parse_buildinfo(dest) or guess_buildinfo(dest)

    basename = buildinfo.get('main', {}).get('image.basename', '')
    image_type = buildinfo.get('main', {}).get('image.type', 'unknown')

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
        'compression': compression,
        'filename': filename,
        'filepath': filepath
    }

    res['image'].update(parse_kiwi_md5(os.path.join(dest, basename + '.md5'), compression is not None))

    if bundle_dest is not None:
      res['bundle'] = inspect_bundle(bundle_dest, basename)

    return res

def inspect_image(dest, bundle_dest = None):
    res = image_details(dest, bundle_dest)
    if not res:
      return None

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


def inspect_boot_image(dest):
    res = None
    files = __salt__['file.readdir'](dest)

    pattern = re.compile(r"^(?P<name>.*)\.(?P<arch>.*)-(?P<version>.*)\.kernel\.(?P<kernelversion>.*)\.md5$")
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
                }
            }
            break

    if res is None:
        return None

    for c in _compression_types:
        path = os.path.join(dest, basename + c['suffix'])
        if __salt__['file.file_exists'](path):
            res['initrd']['filename'] = basename + c['suffix']
            break

    res['initrd'].update(parse_kiwi_md5(os.path.join(dest, basename + '.md5')))

    path = os.path.join(dest, basename + '.kernel.' + res['kernel']['version'])
    if __salt__['file.file_exists'](path):
        res['kernel']['filename'] = basename + '.kernel.' + res['kernel']['version']
        res['kernel'].update(parse_kiwi_md5(path + '.md5'))

    return res

def inspect_bundle(dest, basename):
    res = None
    files = __salt__['file.readdir'](dest)

    pattern = re.compile(r"^(?P<basename>" + re.escape(basename) + r")-(?P<id>.*)\.(?P<suffix>[^.]*)\.sha256$")
    for f in files:
        match = pattern.match(f)
        if match:
            res = match.groupdict()
            sha256_file = f
            break
    if res is None:
        return None

    sha256_str = __salt__['cp.get_file_str'](os.path.join(dest, sha256_file))
    pattern = re.compile(r"^(?P<hash>[0-9a-f]+)\s+(?P<filename>.*)\s*$")
    match = pattern.match(sha256_str)
    if match:
        d = match.groupdict()
        d['hash'] = 'sha256:{0}'.format(d['hash'])
        res.update(d)
        res['filepath'] = os.path.join(dest, res['filename'])

    return res
