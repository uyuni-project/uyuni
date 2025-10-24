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
    with patch.dict(
        kiwi_info.__salt__, {"file.file_exists": MagicMock(return_value=True)}
    ), patch.dict(
        kiwi_info.__salt__, {"cp.get_file_str": MagicMock(return_value=example_profile)}
    ):
        ret = kiwi_info.parse_profile("test")
        assert ret is not None
        assert isinstance(ret, dict)
        assert ret.get("kiwi_profiles") == "EXAMPLE"
        assert ret.get("kiwi_revision") == "11aa83f4567a207260b43caf2bedd22f6ca17a3b"
        assert ret.get("kiwi_rootpartuuid") == "729627f3-e827-46f9-84a9-de390c611ddb"
        assert ret.get("kiwi_startsector") == "2048"
        assert (
            ret.get("kiwi_cmdline")
            == "console=ttyS0 multipath=off net.ifnames=0 nvme_core.io_timeout=4294967295 "
            "nvme_core.admin_timeout=4294967295 8250.nr_uarts=4 dis_ucode_ldr"
        )
