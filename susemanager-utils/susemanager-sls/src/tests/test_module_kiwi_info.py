"""
Tests for kiwi_info salt module.
"""

from ..modules import kiwi_info

from unittest.mock import MagicMock, patch


def test_parse_profile():
    example_profile = """kiwi_align='1048576'
kiwi_boot_timeout='1'
kiwi_bootloader='grub2'
kiwi_cmdline='console=ttyS0 multipath=off net.ifnames=0 nvme_core.io_timeout=4294967295 nvme_core.admin_timeout=4294967295 8250.nr_uarts=4 dis_ucode_ldr'
kiwi_devicepersistency='by-label'
kiwi_displayname='SLES 12 SP5'
kiwi_firmware='uefi'
kiwi_iname='SLES12-SP5'
kiwi_initrd_system='dracut'
kiwi_iversion='1.0.20'
kiwi_keytable='us.map.gz'
kiwi_language='en_US'
kiwi_profiles='EXAMPLE'
kiwi_revision='11aa83f4567a207260b43caf2bedd22f6ca17a3b'
kiwi_rootpartuuid='729627f3-e827-46f9-84a9-de390c611ddb'
kiwi_sectorsize='512'
kiwi_startsector='2048'
kiwi_timezone='UTC'
kiwi_type='vmx'
"""
    expected_ret = {
        "kiwi_align": "1048576",
        "kiwi_boot_timeout": "1",
        "kiwi_bootloader": "grub2",
        "kiwi_cmdline": "console=ttyS0 multipath=off net.ifnames=0 "
        "nvme_core.io_timeout=4294967295 nvme_core.admin_timeout=4294967295 "
        "8250.nr_uarts=4 dis_ucode_ldr",
        "kiwi_devicepersistency": "by-label",
        "kiwi_displayname": "SLES 12 SP5",
        "kiwi_firmware": "uefi",
        "kiwi_iname": "SLES12-SP5",
        "kiwi_initrd_system": "dracut",
        "kiwi_iversion": "1.0.20",
        "kiwi_keytable": "us.map.gz",
        "kiwi_language": "en_US",
        "kiwi_profiles": "EXAMPLE",
        "kiwi_revision": "11aa83f4567a207260b43caf2bedd22f6ca17a3b",
        "kiwi_rootpartuuid": "729627f3-e827-46f9-84a9-de390c611ddb",
        "kiwi_sectorsize": "512",
        "kiwi_startsector": "2048",
        "kiwi_timezone": "UTC",
        "kiwi_type": "vmx",
    }
    with patch.dict(
        kiwi_info.__salt__, {"file.file_exists": MagicMock(return_value=True)}
    ), patch.dict(
        kiwi_info.__salt__, {"cp.get_file_str": MagicMock(return_value=example_profile)}
    ):
        ret = kiwi_info.parse_profile("test")
        assert ret is not None
        assert isinstance(ret, dict)
        assert ret == expected_ret


def test_parse_packages():
    example_packages = """bash|(none)|4.3|83.23.1|x86_64|obs://build.suse.de/SUSE:Maintenance:10724/SUSE_SLE-12-SP2_Update/b5cfe46def8616297059178c4336d762-bash.SUSE_SLE-12-SP2_Update|GPL-3.0-or-later
kernel-default-base|(none)|4.12.14|120.1|x86_64|obs://build.suse.de/SUSE:SLE-12-SP5:GA/standard/a0ea4b03e8d7e6568f24d720f0153afd-kernel-default|GPL-2.0
filesystem|(none)|13.1|14.15|x86_64|obs://build.suse.de/SUSE:SLE-12-SP3:GA/standard/ae15081902ea27fd632375aeb0ce0da6-filesystem|MIT
less|(none)|458|5.13|x86_64|obs://build.suse.de/SUSE:SLE-12:GA/standard/827c24a5ea5849b1fc081138c6b394bf-less|GPL-3.0+ or BSD-2-Clause
openssl|(none)|1.0.2p|1.13|noarch|obs://build.suse.de/SUSE:SLE-12-SP4:GA/standard/15a6602b5249eac363d3adec4a6b7f4e-openssl|OpenSSL
gpg-pubkey|(none)|39db7c82|5847eb1f|(none)|(none)|pubkey
gpg-pubkey|(none)|50a3dd1c|50f35137|(none)|(none)|pubkey
"""
    expected_ret = [
        {
            "name": "bash",
            "epoch": "",
            "version": "4.3",
            "release": "83.23.1",
            "arch": "x86_64",
            "disturl": "obs://build.suse.de/SUSE:Maintenance:10724/SUSE_SLE-12-SP2_Update/b5cfe46def8616297059178c4336d762-bash.SUSE_SLE-12-SP2_Update",
            "license": "GPL-3.0-or-later",
        },
        {
            "name": "kernel-default-base",
            "epoch": "",
            "version": "4.12.14",
            "release": "120.1",
            "arch": "x86_64",
            "disturl": "obs://build.suse.de/SUSE:SLE-12-SP5:GA/standard/a0ea4b03e8d7e6568f24d720f0153afd-kernel-default",
            "license": "GPL-2.0",
        },
        {
            "name": "filesystem",
            "epoch": "",
            "version": "13.1",
            "release": "14.15",
            "arch": "x86_64",
            "disturl": "obs://build.suse.de/SUSE:SLE-12-SP3:GA/standard/ae15081902ea27fd632375aeb0ce0da6-filesystem",
            "license": "MIT",
        },
        {
            "name": "less",
            "epoch": "",
            "version": "458",
            "release": "5.13",
            "arch": "x86_64",
            "disturl": "obs://build.suse.de/SUSE:SLE-12:GA/standard/827c24a5ea5849b1fc081138c6b394bf-less",
            "license": "GPL-3.0+ or BSD-2-Clause",
        },
        {
            "name": "openssl",
            "epoch": "",
            "version": "1.0.2p",
            "release": "1.13",
            "arch": "noarch",
            "disturl": "obs://build.suse.de/SUSE:SLE-12-SP4:GA/standard/15a6602b5249eac363d3adec4a6b7f4e-openssl",
            "license": "OpenSSL",
        },
    ]
    with patch.dict(
        kiwi_info.__salt__, {"file.file_exists": MagicMock(return_value=True)}
    ), patch.dict(
        kiwi_info.__salt__,
        {"cp.get_file_str": MagicMock(return_value=example_packages)},
    ):
        ret = kiwi_info.parse_packages("test")
        assert ret is not None
        assert isinstance(ret, list)
        assert ret == expected_ret


def test_inspect_bundles():
    example_dest_files = [
        "SLES12-SP5-EXAMPLE.x86_64-1.0.20-EXAMPLE-Build.cdx.json",
        "SLES12-SP5-EXAMPLE.x86_64-1.0.20-EXAMPLE-Build.packages",
        "SLES12-SP5-EXAMPLE.x86_64-1.0.20-EXAMPLE-Build.raw.xz",
        "SLES12-SP5-EXAMPLE.x86_64-1.0.20-EXAMPLE-Build.raw.xz.sha256",
        "SLES12-SP5-EXAMPLE.x86_64-1.0.20-EXAMPLE-Build.spdx.json",
        "SLES12-SP5-EXAMPLE.x86_64-1.0.20-EXAMPLE-Build.verified",
    ]
    example_sha256_files = [
        "bea584c97a591cc1098410292ba4f16960848d925037292227e4b4260f38e05d  "
        "SLES12-SP5-EXAMPLE.x86_64-1.0.20-EXAMPLE-Build.raw.xz\n",
        "bea584c97a591cc1098410292ba4f16960848d925037292227e4b4260f38e05d\n",
    ]
    expected_ret = [
        {
            "basename": "SLES12-SP5-EXAMPLE.x86_64-1.0.20",
            "filename": "SLES12-SP5-EXAMPLE.x86_64-1.0.20-EXAMPLE-Build.raw.xz",
            "filepath": "/test/dest/path/SLES12-SP5-EXAMPLE.x86_64-1.0.20-EXAMPLE-Build.raw.xz",
            "hash": "sha256:bea584c97a591cc1098410292ba4f16960848d925037292227e4b4260f38e05d",
            "id": "EXAMPLE-Build",
            "suffix": "raw.xz",
        }
    ]
    for example_sha256_file in example_sha256_files:
        get_file_str_mock = MagicMock(return_value=example_sha256_file)
        with patch.dict(
            kiwi_info.__salt__,
            {"file.readdir": MagicMock(return_value=example_dest_files)},
        ), patch.dict(
            kiwi_info.__salt__,
            {"cp.get_file_str": get_file_str_mock},
        ):
            ret = kiwi_info.inspect_bundles(
                "/test/dest/path/",
                "SLES12-SP5-EXAMPLE.x86_64-1.0.20",
            )
            assert get_file_str_mock.called_once_with(
                "/test/dest/path/SLES12-SP5-EXAMPLE.x86_64-1.0.20-EXAMPLE-Build.raw.xz.sha256"
            )
            assert ret is not None
            assert isinstance(ret, list)
            assert ret == expected_ret
