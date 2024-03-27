#  pylint: disable=missing-module-docstring,unused-import
import salt.exceptions
import logging
import os
import re
import json

log = logging.getLogger(__name__)

# Kiwi version is always in format "MAJOR.MINOR.RELEASE" with numeric values
# Source https://osinside.github.io/kiwi/image_description/elements.html#preferences-version
KIWI_VERSION_REGEX = r"\d+\.\d+\.\d+"
# Taken from Kiwi sources https://github.com/OSInside/kiwi/blob/eb2b1a84bf7/kiwi/schema/kiwi.rng#L81
KIWI_ARCH_REGEX = r"(x86_64|i586|i686|ix86|aarch64|arm64|armv5el|armv5tel|armv6hl|armv6l|armv7hl|armv7l|ppc|ppc64|ppc64le|s390|s390x|riscv64)"
# Taken from Kiwi sources https://github.com/OSInside/kiwi/blob/eb2b1a84bf7/kiwi/schema/kiwi.rng#L26
KIWI_NAME_REGEX = r"[a-zA-Z0-9_\-\.]+"


def parse_profile(chroot):
    ret = {}
    path = os.path.join(chroot, "image", ".profile")
    # pylint: disable-next=undefined-variable
    if __salt__["file.file_exists"](path):
        # pylint: disable-next=undefined-variable
        profile = __salt__["cp.get_file_str"](path)
        pattern = re.compile(r"^(?P<name>.*?)='(?P<val>.*)'")
        for line in profile.splitlines():
            match = pattern.match(line)
            if match:
                ret[match.group("name")] = match.group("val")
    return ret


def parse_buildinfo(dest):
    ret = {}
    path = os.path.join(dest, "kiwi.buildinfo")
    # pylint: disable-next=undefined-variable
    if __salt__["file.file_exists"](path):
        # pylint: disable-next=undefined-variable
        profile = __salt__["cp.get_file_str"](path)
        pattern_group = re.compile(r"^\[(?P<name>.*)\]")
        pattern_val = re.compile(r"^(?P<name>.*?)=(?P<val>.*)")

        group = ret
        for line in profile.splitlines():
            match = pattern_group.match(line)
            if match:
                group = {}
                ret[match.group("name")] = group

            match = pattern_val.match(line)
            if match:
                group[match.group("name")] = match.group("val")
    return ret


# fallback for SLES11 Kiwi and for Kiwi NG that does not create the buildinfo file
def guess_buildinfo(dest):
    ret = {"main": {}}
    # pylint: disable-next=undefined-variable
    files = __salt__["file.readdir"](dest)

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
            ret["main"]["image.basename"] = match.group("basename")

        match = pattern_pxe_initrd.match(f) or pattern_pxe_kiwi_ng_initrd.match(f)
        if match:
            have_initrd = True

        match = pattern_pxe_kernel.match(f) or pattern_pxe_kiwi_ng_kernel.match(f)
        if match:
            have_kernel = True

    if have_kernel and have_initrd:
        ret["main"]["image.type"] = "pxe"
    return ret


# Kiwi NG
_kiwi_result_script = """
import sys
import pickle
import json
ret = {}
with open(sys.argv[1], 'rb') as f:
    result = pickle.load(f)
    ret['arch'] = result.xml_state.host_architecture
    ret['basename'] = result.xml_state.xml_data.name
    ret['type'] = result.xml_state.build_type.image
    ret['filesystem'] = result.xml_state.build_type.filesystem
    ret['initrd_system'] = result.xml_state.build_type.initrd_system
    print(json.dumps(ret))
"""


def parse_kiwi_result(dest):
    path = os.path.join(dest, "kiwi.result")
    ret = {}
    # pylint: disable-next=undefined-variable
    if __salt__["file.file_exists"](path):
        # pickle depends on availability of python kiwi modules
        # which are not under our control so there is certain risk of failure
        # also, the kiwi libraries may not be available in salt bundle
        # -> parse the file via wrapper script using system python3
        #
        # return empty dict on failure
        # the caller should handle all values as optional
        # pylint: disable-next=undefined-variable
        result = __salt__["cmd.exec_code_all"](
            "/usr/bin/python3", _kiwi_result_script, args=[path]
        )
        if result["retcode"] == 0:
            ret = json.loads(result["stdout"])
        # else return empty dict

    return ret


def parse_packages(path):
    ret = []
    # pylint: disable-next=undefined-variable
    if __salt__["file.file_exists"](path):
        # pylint: disable-next=undefined-variable
        packages = __salt__["cp.get_file_str"](path)
        pattern = re.compile(
            r"^(?P<name>.*?)\|(?P<epoch>.*?)\|(?P<version>.*?)\|(?P<release>.*?)\|(?P<arch>.*?)\|(?P<disturl>.*?)(\|(?P<license>.*))?$"
        )
        for line in packages.splitlines():
            match = pattern.match(line)
            if match:
                # translate '(none)' values to ''
                d = match.groupdict()
                for k in list(d.keys()):
                    if d[k] == "(none)":
                        d[k] = ""

                # if arch is '' and name begins gpg-pubkey then skip the package
                if d["arch"] == "" and d["name"].startswith("gpg-pubkey"):
                    continue

                ret.append(d)
    return ret


def get_md5(path):
    res = {}
    # pylint: disable-next=undefined-variable
    if not __salt__["file.file_exists"](path):
        return res

    # pylint: disable-next=undefined-variable
    res["hash"] = "md5:" + __salt__["file.get_hash"](path, form="md5")
    # pylint: disable-next=undefined-variable
    res["size"] = __salt__["file.stats"](path).get("size")
    return res


def parse_kiwi_md5(path, compressed=False):
    res = {}

    # pylint: disable-next=undefined-variable
    if not __salt__["file.file_exists"](path):
        return res

    # pylint: disable-next=undefined-variable
    md5_str = __salt__["cp.get_file_str"](path)
    if md5_str is not None:
        if compressed:
            pattern = re.compile(
                r"^(?P<md5>[0-9a-f]+)\s+(?P<size1>[0-9]+)\s+(?P<size2>[0-9]+)\s+(?P<csize1>[0-9]+)\s+(?P<csize2>[0-9]+)\s*$"
            )
        else:
            pattern = re.compile(
                r"^(?P<md5>[0-9a-f]+)\s+(?P<size1>[0-9]+)\s+(?P<size2>[0-9]+)\s*$"
            )
        match = pattern.match(md5_str)
        if match:
            res["hash"] = "md5:" + match.group("md5")
            res["size"] = int(match.group("size1")) * int(match.group("size2"))
            if compressed:
                res["compressed_size"] = int(match.group("csize1")) * int(
                    match.group("csize2")
                )
    return res


_compression_types = [
    {"suffix": ".gz", "compression": "gzip"},
    {"suffix": ".bz", "compression": "bzip"},
    {"suffix": ".xz", "compression": "xz"},
    {"suffix": ".install.iso", "compression": None},
    {"suffix": ".iso", "compression": None},
    {"suffix": ".qcow2", "compression": None},
    {"suffix": ".ova", "compression": None},
    {"suffix": ".vmdk", "compression": None},
    {"suffix": ".vmx", "compression": None},
    {"suffix": ".vhd", "compression": None},
    {"suffix": ".vhdx", "compression": None},
    {"suffix": ".vdi", "compression": None},
    {"suffix": ".raw", "compression": None},
    {"suffix": ".squashfs", "compression": None},
    {"suffix": "", "compression": None},
]


def image_details(dest, bundle_dest=None):
    res = {}
    buildinfo = parse_buildinfo(dest) or guess_buildinfo(dest)
    kiwiresult = parse_kiwi_result(dest)

    basename = buildinfo.get("main", {}).get("image.basename", "")
    image_type = kiwiresult.get("type") or buildinfo.get("main", {}).get(
        "image.type", "unknown"
    )
    fstype = kiwiresult.get("filesystem")

    pattern = re.compile(
        # pylint: disable-next=consider-using-f-string
        r"^(?P<name>{})\.(?P<arch>{})-(?P<version>{})$".format(
            KIWI_NAME_REGEX, KIWI_ARCH_REGEX, KIWI_VERSION_REGEX
        )
    )
    match = pattern.match(basename)
    if match:
        name = match.group("name")
        arch = match.group("arch")
        version = match.group("version")
    else:
        return None

    filename = None
    filepath = None
    compression = None
    for c in _compression_types:
        path = os.path.join(dest, basename + c["suffix"])
        # pylint: disable-next=undefined-variable
        if __salt__["file.file_exists"](path):
            compression = c["compression"]
            filename = basename + c["suffix"]
            filepath = path
            break

    res["image"] = {
        "basename": basename,
        "name": name,
        "arch": arch,
        "type": image_type,
        "version": version,
        "filename": filename,
        "filepath": filepath,
        "fstype": fstype,
    }
    if compression:
        res["image"].update(
            {
                "compression": compression,
                # pylint: disable-next=undefined-variable
                "compressed_hash": __salt__["hashutil.digest_file"](
                    filepath, checksum="md5"
                ),
            }
        )

    res["image"].update(
        parse_kiwi_md5(os.path.join(dest, basename + ".md5"), compression is not None)
    )

    if bundle_dest is not None:
        res["bundles"] = inspect_bundles(bundle_dest, basename)

    return res


def inspect_image(dest, build_id, bundle_dest=None):
    res = image_details(dest, bundle_dest)
    if not res:
        return None

    res["image"]["build_id"] = build_id

    basename = res["image"]["basename"]
    image_type = res["image"]["type"]

    for fstype in ["ext2", "ext3", "ext4", "btrfs", "xfs"]:
        path = os.path.join(dest, basename + "." + fstype)
        # pylint: disable-next=undefined-variable
        if __salt__["file.file_exists"](path) or __salt__["file.is_link"](path):
            res["image"]["fstype"] = fstype
            break

    res["packages"] = parse_packages(os.path.join(dest, basename + ".packages"))

    if image_type == "pxe":
        res["boot_image"] = inspect_boot_image(dest)

    return res


def inspect_boot_image(dest):
    res = None
    # pylint: disable-next=undefined-variable
    files = __salt__["file.readdir"](dest)

    pattern = re.compile(
        # pylint: disable-next=consider-using-f-string
        r"^(?P<name>{})\.(?P<arch>{})-(?P<version>{})\.kernel\.(?P<kernelversion>.*)\.md5$".format(
            KIWI_NAME_REGEX, KIWI_ARCH_REGEX, KIWI_VERSION_REGEX
        )
    )
    pattern_kiwi_ng = re.compile(
        # pylint: disable-next=consider-using-f-string
        r"^(?P<name>{})\.(?P<arch>{})-(?P<version>{})-(?P<kernelversion>.*)\.kernel$".format(
            KIWI_NAME_REGEX, KIWI_ARCH_REGEX, KIWI_VERSION_REGEX
        )
    )
    for f in files:
        match = pattern.match(f)
        if match:
            basename = (
                match.group("name")
                + "."
                + match.group("arch")
                + "-"
                + match.group("version")
            )
            res = {
                "name": match.group("name"),
                "arch": match.group("arch"),
                "basename": basename,
                "initrd": {"version": match.group("version")},
                "kernel": {"version": match.group("kernelversion")},
                "kiwi_ng": False,
            }
            break
        match = pattern_kiwi_ng.match(f)
        if match:
            basename = (
                match.group("name")
                + "."
                + match.group("arch")
                + "-"
                + match.group("version")
            )
            res = {
                "name": match.group("name"),
                "arch": match.group("arch"),
                "basename": basename,
                "initrd": {"version": match.group("version")},
                "kernel": {"version": match.group("kernelversion")},
                "kiwi_ng": True,
            }
            break

    if res is None:
        return None

    for c in _compression_types:
        if res["kiwi_ng"]:
            file = basename + ".initrd" + c["suffix"]
        else:
            file = basename + c["suffix"]
        filepath = os.path.join(dest, file)
        # pylint: disable-next=undefined-variable
        if __salt__["file.file_exists"](filepath):
            res["initrd"]["filename"] = file
            res["initrd"]["filepath"] = filepath
            if res["kiwi_ng"]:
                res["initrd"].update(get_md5(filepath))
            else:
                res["initrd"].update(
                    parse_kiwi_md5(os.path.join(dest, basename + ".md5"))
                )
            break

    if res["kiwi_ng"]:
        file = basename + "-" + res["kernel"]["version"] + ".kernel"
        filepath = os.path.join(dest, file)
        # pylint: disable-next=undefined-variable
        if __salt__["file.file_exists"](filepath):
            res["kernel"]["filename"] = file
            res["kernel"]["filepath"] = filepath
            res["kernel"].update(get_md5(filepath))
    else:
        file = basename + ".kernel." + res["kernel"]["version"]
        filepath = os.path.join(dest, file)
        # pylint: disable-next=undefined-variable
        if __salt__["file.file_exists"](filepath):
            res["kernel"]["filename"] = file
            res["kernel"]["filepath"] = filepath
            res["kernel"].update(parse_kiwi_md5(filepath + ".md5"))

    return res


def inspect_bundles(dest, basename):
    res = []
    # pylint: disable-next=undefined-variable
    files = __salt__["file.readdir"](dest)

    pattern = re.compile(
        r"^(?P<basename>"
        + re.escape(basename)
        + r")-(?P<id>[^.]*)\.(?P<suffix>.*)\.sha256$"
    )
    for f in files:
        match = pattern.match(f)
        if match:
            res1 = match.groupdict()
            sha256_file = f
            # pylint: disable-next=undefined-variable
            sha256_str = __salt__["cp.get_file_str"](os.path.join(dest, sha256_file))
            pattern2 = re.compile(r"^(?P<hash>[0-9a-f]+)\s+(?P<filename>.*)\s*$")
            match = pattern2.match(sha256_str)
            if match:
                d = match.groupdict()
                # pylint: disable-next=consider-using-f-string
                d["hash"] = "sha256:{0}".format(d["hash"])
                res1.update(d)
                res1["filepath"] = os.path.join(dest, res1["filename"])
            else:
                # only hash without file name
                pattern2 = re.compile(r"^(?P<hash>[0-9a-f]+)$")
                match = pattern2.match(sha256_str)
                if match:
                    # pylint: disable-next=consider-using-f-string
                    res1["hash"] = "sha256:{0}".format(match.groupdict()["hash"])
                    res1["filename"] = sha256_file[0 : -len(".sha256")]
                    res1["filepath"] = os.path.join(dest, res1["filename"])
            res.append(res1)
    return res


def build_info(dest, build_id, bundle_dest=None):
    res = {}
    buildinfo = parse_buildinfo(dest) or guess_buildinfo(dest)
    kiwiresult = parse_kiwi_result(dest)
    basename = buildinfo.get("main", {}).get("image.basename", "")
    image_type = kiwiresult.get("type") or buildinfo.get("main", {}).get(
        "image.type", "unknown"
    )

    pattern = re.compile(
        # pylint: disable-next=consider-using-f-string
        r"^(?P<name>{})\.(?P<arch>{})-(?P<version>{})$".format(
            KIWI_NAME_REGEX, KIWI_ARCH_REGEX, KIWI_VERSION_REGEX
        )
    )
    match = pattern.match(basename)
    if not match:
        return None
    name = match.group("name")
    arch = match.group("arch")
    version = match.group("version")

    image_filepath = None
    image_filename = None
    for c in _compression_types:
        test_name = basename + c["suffix"]
        filepath = os.path.join(dest, test_name)
        # pylint: disable-next=undefined-variable
        if __salt__["file.file_exists"](filepath):
            image_filename = test_name
            image_filepath = filepath
            break

    res["image"] = {
        "name": name,
        "arch": arch,
        "version": version,
        "filepath": image_filepath,
        "filename": image_filename,
        "build_id": build_id,
    }

    # Kiwi creates checksum for filesystem image when image type is PXE(or KIS), however if image is compressed, this
    # checksum is of uncompressed image. Other image types do not have checksum created at all.
    res["image"].update(get_md5(image_filepath))

    if image_type == "pxe":
        r = inspect_boot_image(dest)
        res["boot_image"] = {
            "initrd": {
                "filepath": r["initrd"]["filepath"],
                "filename": r["initrd"]["filename"],
                "hash": r["initrd"]["hash"],
            },
            "kernel": {
                "filepath": r["kernel"]["filepath"],
                "filename": r["kernel"]["filename"],
                "hash": r["kernel"]["hash"],
            },
        }

    if bundle_dest is not None:
        res["bundles"] = inspect_bundles(bundle_dest, basename)

    return res
