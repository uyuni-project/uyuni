#  pylint: disable=missing-module-docstring,unused-import

# SPDX-FileCopyrightText: 2018-2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

import salt.exceptions
import logging
import os
import pickle
import re

log = logging.getLogger(__name__)

# __salt__ variable is populated with Salt Lazy Loader.
# Define it for testing purposes and to avoid pylint warnings.
__salt__ = {}

# Kiwi version is always in format "MAJOR.MINOR.RELEASE" with numeric values
# Source https://osinside.github.io/kiwi/image_description/elements.html#preferences-version
# Enhanced by allowing also MAJOR.MINOR version, particularly for SL-Micro images
KIWI_VERSION_REGEX = r"\d+\.\d+(\.\d+)?"
# Taken from Kiwi sources https://github.com/OSInside/kiwi/blob/eb2b1a84bf7/kiwi/schema/kiwi.rng#L81
KIWI_ARCH_REGEX = r"(x86_64|i586|i686|ix86|aarch64|arm64|armv5el|armv5tel|armv6hl|armv6l|armv7hl|armv7l|ppc|ppc64|ppc64le|s390|s390x|riscv64)"
# Taken from Kiwi sources https://github.com/OSInside/kiwi/blob/eb2b1a84bf7/kiwi/schema/kiwi.rng#L26
KIWI_NAME_REGEX = r"[a-zA-Z0-9_\-\.]+"


# Kiwi NG does not create the buildinfo file
def guess_buildinfo(dest):
    ret = {}
    files = __salt__["file.readdir"](dest)

    pattern_basename = re.compile(r"^(?P<basename>.*)\.packages$")
    pattern_pxe_initrd = re.compile(r".*\.initrd$")
    pattern_pxe_kernel = re.compile(r".*\.kernel$")
    have_kernel = False
    have_initrd = False

    for f in files:
        match = pattern_basename.match(f)
        if match:
            ret["basename"] = match.group("basename")

        match = pattern_pxe_initrd.match(f)
        if match:
            have_initrd = True

        match = pattern_pxe_kernel.match(f)
        if match:
            have_kernel = True

    if have_kernel and have_initrd:
        ret["type"] = "pxe"
    return ret


class _KiwiResultObject:
    def __new__(cls, *args, **kwargs):
        return object.__new__(cls)

    def __init__(self, *args, **kwargs):
        pass

    def __setstate__(self, state):
        self.__dict__.update(state)


class KiwiResultUnpickler(pickle.Unpickler):
    """Load KIWI result objects without requiring KIWI to be installed."""

    def find_class(self, module, name):
        if module == "kiwi" or module.startswith("kiwi."):
            return _KiwiResultObject
        return super().find_class(module, name)


def parse_kiwi_result(dest):
    path = os.path.join(dest, "kiwi.result")
    ret = {}
    if __salt__["file.file_exists"](path):
        try:
            with open(path, "rb") as result_file:
                result = KiwiResultUnpickler(result_file).load()
            ret = {
                "name": getattr(result.xml_state.xml_data, "name", None),
                "type": getattr(result.xml_state.build_type, "image", None),
                "filesystem": getattr(result.xml_state.build_type, "filesystem", None),
            }
        except Exception:  # pylint: disable=broad-exception-caught
            # kiwi.result is optional, and an unreadable result must not fail inspection.
            log.exception("Loading kiwi.result")

    return ret


def parse_packages(path):
    ret = []
    if __salt__["file.file_exists"](path):
        packages = __salt__["cp.get_file_str"](path)
        fields = ["name", "epoch", "version", "release", "arch", "disturl", "license"]
        for line in packages.splitlines():
            line_data = line.split("|")
            if len(line_data) == len(fields):
                # translate '(none)' values to ''
                d = dict(zip(fields, line_data))
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
    if not __salt__["file.file_exists"](path):
        return res

    res["hash"] = "md5:" + __salt__["file.get_hash"](path, form="md5")
    res["size"] = __salt__["file.stats"](path).get("size")
    return res


def get_decompressed_md5(path, decompress_cmd):
    res = {}
    # pylint: disable-next=undefined-variable
    if not __salt__["file.file_exists"](path):
        return res

    cmd = decompress_cmd + " '" + path + "' | md5sum"
    # pylint: disable-next=undefined-variable
    shell_result = __salt__["cmd.run_all"](cmd, python_shell=True)

    if shell_result["retcode"] == 0:
        hash_val = shell_result["stdout"].split()[0].strip()
        res["hash"] = "md5:" + hash_val

    return res


def parse_kiwi_hash(path, compressed=False, hash_type="md5"):
    res = {}

    if not __salt__["file.file_exists"](path):
        return res

    hash_str = __salt__["cp.get_file_str"](path)
    if hash_str is not None:
        if compressed:
            pattern = re.compile(
                r"^(?P<hash>[0-9a-f]+)\s+(?P<size1>[0-9]+)\s+(?P<size2>[0-9]+)\s+(?P<csize1>[0-9]+)\s+(?P<csize2>[0-9]+)\s*$"
            )
        else:
            pattern = re.compile(
                r"^(?P<hash>[0-9a-f]+)\s+(?P<size1>[0-9]+)\s+(?P<size2>[0-9]+)\s*$"
            )
        match = pattern.match(hash_str)
        if match:
            res["hash"] = hash_type + ":" + match.group("hash")
            res["size"] = int(match.group("size1")) * int(match.group("size2"))
            if compressed:
                res["compressed_size"] = int(match.group("csize1")) * int(
                    match.group("csize2")
                )
    return res


_compression_types = {
    ".gz": "gzip",
    ".bz": "bzip",
    ".xz": "xz",
    "": None,
}


_decompress_cmd = {
    "gzip": "gzip -dc",
    "bzip": "bzip2 -dc",
    "xz": "xz -dc",
}


# suffixes for pxe/kis image type
_pxe_image_types = [
    ".gz",
    ".bz",
    ".xz",
    "",
]


def _known_image_types():
    formats = _disk_format_types()
    formats.extend(_compressed_format_types())
    formats.extend(_iso_format_types())
    formats.extend(_raw_format_types())
    return formats


# from https://github.com/OSInside/kiwi/blob/main/kiwi/defaults.py#L1501
def _disk_format_types():
    return [
        ".gce",
        ".qcow2",
        ".vmdk",
        ".ova",
        ".vmx",
        ".vhd",
        ".vhdx",
        ".vhdfixed",
        ".vdi",
        ".vagrant.libvirt.box",
        ".vagrant.virtualbox.box",
    ]


def _compressed_format_types():
    return [".gz", ".bz", ".xz", ".tar.xz"]


def _iso_format_types():
    return [".install.iso", ".iso"]


def _raw_format_types():
    return [".raw", ".squashfs"]


def image_details(dest, bundle_dest=None):
    """
    Gather detailed information about system image.
    """
    res = {}
    buildinfo = guess_buildinfo(dest)
    kiwiresult = parse_kiwi_result(dest)

    basename = buildinfo.get("basename", "")
    image_type = kiwiresult.get("type") or buildinfo.get("type", "unknown")
    fstype = kiwiresult.get("filesystem")

    pattern = re.compile(
        # pylint: disable-next=consider-using-f-string
        r"^(?P<name>{})\.(?P<arch>{})-(?P<version>{})$".format(
            KIWI_NAME_REGEX, KIWI_ARCH_REGEX, KIWI_VERSION_REGEX
        )
    )
    match = pattern.match(basename)
    if not match:
        log.error("Unable to match Kiwi results")
        return None

    name = match.group("name")
    arch = match.group("arch")
    version = match.group("version")

    filename = None
    filepath = None
    compression = None
    image_types = _known_image_types()
    if image_type == "pxe" or image_type == "kis":
        image_types = _pxe_image_types

    for c in image_types:
        path = os.path.join(dest, basename + c)
        if __salt__["file.file_exists"](path):
            filename = basename + c
            filepath = path
            compression = _compression_types.get(c, None)
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
                "compressed_hash": __salt__["hashutil.digest_file"](
                    filepath, checksum="md5"
                ),
            }
        )

    res["image"].update(
        parse_kiwi_hash(
            os.path.join(dest, basename + ".sha256"), compression is not None, "sha256"
        )
    )
    res["image"].update(
        parse_kiwi_hash(
            os.path.join(dest, basename + ".md5"), compression is not None, "md5"
        )
    )

    # remove this when saltboot supports sha256:
    if "hash" not in res["image"] or not res["image"]["hash"].startswith("md5:"):
        if compression:
            decompress_cmd = _decompress_cmd[compression]
            res["image"].update(get_decompressed_md5(filepath, decompress_cmd))
        else:
            res["image"].update(get_md5(filepath))

    if bundle_dest is not None:
        res["bundles"] = inspect_bundles(bundle_dest, basename)

    return res


def inspect_image(dest, build_id, bundle_dest=None):
    """
    Image inspection stage entrypoint.
    Provides detailed information about image and packages it contains.
    """
    res = image_details(dest, bundle_dest)
    if not res:
        return None

    res["image"]["build_id"] = build_id

    basename = res["image"]["basename"]
    image_type = res["image"]["type"]

    res["packages"] = parse_packages(os.path.join(dest, basename + ".packages"))

    if image_type == "pxe" or image_type == "kis":
        res["boot_image"] = inspect_boot_image(dest)

    return res


def inspect_boot_image(dest):
    """
    Gather information about boot image (kernel and initrd).
    Only valid for PXE/KIS image type.
    """
    res = None
    files = __salt__["file.readdir"](dest)

    pattern_kernel = re.compile(
        rf"^(?P<name>{KIWI_NAME_REGEX})\.(?P<arch>{KIWI_ARCH_REGEX})-(?P<version>{KIWI_VERSION_REGEX})-(?P<kernelversion>.*)\.kernel$"
    )
    for f in files:
        match = pattern_kernel.match(f)
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
            }
            break

    if res is None:
        return None

    for c in _compression_types:
        file = basename + ".initrd" + c
        filepath = os.path.join(dest, file)
        if __salt__["file.file_exists"](filepath):
            res["initrd"]["filename"] = file
            res["initrd"]["filepath"] = filepath
            res["initrd"].update(get_md5(filepath))
            break

    file = basename + "-" + res["kernel"]["version"] + ".kernel"
    filepath = os.path.join(dest, file)
    if __salt__["file.file_exists"](filepath):
        res["kernel"]["filename"] = file
        res["kernel"]["filepath"] = filepath
        res["kernel"].update(get_md5(filepath))
    return res


def inspect_bundles(dest, basename):
    """
    Gather details about image bundle.
    Image bundle is a compressed tarball of all image results with custom naming.

    Not used by default, not compatible with containerized saltboot workflow.
    """
    res = []
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
            sha256_str = __salt__["cp.get_file_str"](os.path.join(dest, sha256_file))
            pattern2 = re.compile(r"^(?P<hash>[0-9a-f]+)\s+(?P<filename>\S.*)$")
            match = pattern2.match(sha256_str)
            if match:
                d = match.groupdict()
                d["filename"] = d["filename"].strip()
                d["hash"] = f'sha256:{d["hash"]}'
                res1.update(d)
                res1["filepath"] = os.path.join(dest, res1["filename"])
            else:
                # only hash without file name
                pattern2 = re.compile(r"^(?P<hash>[0-9a-f]+)$")
                match = pattern2.match(sha256_str)
                if match:
                    res1["hash"] = f'sha256:{match.groupdict()["hash"]}'
                    res1["filename"] = sha256_file[0 : -len(".sha256")]
                    res1["filepath"] = os.path.join(dest, res1["filename"])
            res.append(res1)
    return res


def build_info(dest, build_id, bundle_dest=None):
    """
    Generates basic build info for image collection. Skips package inspection.
    """
    res = {}
    buildinfo = guess_buildinfo(dest)
    kiwiresult = parse_kiwi_result(dest)
    basename = buildinfo.get("basename", "")
    image_type = kiwiresult.get("type") or buildinfo.get("type", "unknown")

    pattern = re.compile(
        # pylint: disable-next=consider-using-f-string
        r"^(?P<name>{})\.(?P<arch>{})-(?P<version>{})$".format(
            KIWI_NAME_REGEX, KIWI_ARCH_REGEX, KIWI_VERSION_REGEX
        )
    )
    match = pattern.match(basename)
    if not match:
        log.error("Unable to match Kiwi results")
        return None

    name = match.group("name")
    arch = match.group("arch")
    version = match.group("version")

    image_filepath = None
    image_filename = None
    image_types = _known_image_types()

    if image_type == "pxe" or image_type == "kis":
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
        image_types = _pxe_image_types

    for c in image_types:
        test_name = basename + c
        filepath = os.path.join(dest, test_name)
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

    if bundle_dest is not None:
        res["bundles"] = inspect_bundles(bundle_dest, basename)

    return res
