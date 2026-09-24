"""
Tests for kiwi_info salt module.
"""

import os
import sys
import pickle
import tempfile
from types import ModuleType
from unittest.mock import MagicMock, patch

from ..modules import kiwi_info


# Global mock classes for pickle integration tests
class Result:
    pass


class XMLState:
    pass


class XMLData:
    pass


class BuildType:
    pass


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
            get_file_str_mock.assert_called_once_with(
                "/test/dest/path/SLES12-SP5-EXAMPLE.x86_64-1.0.20-EXAMPLE-Build.raw.xz.sha256"
            )
            assert ret is not None
            assert isinstance(ret, list)
            assert ret == expected_ret


def test_parse_kiwi_result_file_not_found():
    """
    Test parse_kiwi_result when the kiwi.result file does not exist.
    """
    with patch.dict(
        kiwi_info.__salt__, {"file.file_exists": MagicMock(return_value=False)}
    ):
        ret = kiwi_info.parse_kiwi_result("/nonexistent")
        assert not ret


def test_parse_kiwi_result_invalid_file():
    """
    Test parse_kiwi_result when kiwi.result cannot be unpickled.
    """
    with tempfile.TemporaryDirectory() as tmpdir:
        result_path = os.path.join(tmpdir, "kiwi.result")
        with open(result_path, "wb") as result_file:
            result_file.write(b"not a pickle")

        with patch.dict(
            kiwi_info.__salt__, {"file.file_exists": MagicMock(return_value=True)}
        ):
            ret = kiwi_info.parse_kiwi_result(tmpdir)

    assert not ret


def test_parse_kiwi_result_integration_safe_unpickle():
    """
    Integration test for parse_kiwi_result and KiwiResultUnpickler.
    Generates a mock pickled kiwi.result without KIWI modules installed.
    """
    # pylint: disable=attribute-defined-outside-init
    # Setup mock modules in sys.modules so pickle.dump can locate classes
    temp_modules = [
        "kiwi",
        "kiwi.result",
        "kiwi.xml_state",
        "kiwi.xml_data",
        "kiwi.build_type",
    ]
    original_modules = {}

    for m in temp_modules:
        if m in sys.modules:
            original_modules[m] = sys.modules[m]
        sys.modules[m] = ModuleType(m)

    try:
        sys.modules["kiwi.result"].Result = Result
        sys.modules["kiwi.xml_state"].XMLState = XMLState
        sys.modules["kiwi.xml_data"].XMLData = XMLData
        sys.modules["kiwi.build_type"].BuildType = BuildType

        Result.__module__ = "kiwi.result"
        XMLState.__module__ = "kiwi.xml_state"
        XMLData.__module__ = "kiwi.xml_data"
        BuildType.__module__ = "kiwi.build_type"

        # Construct the structure resembling KIWI's results
        res = Result()
        res.xml_state = XMLState()
        res.xml_state.xml_data = XMLData()
        res.xml_state.xml_data.name = "kiwi-image-test"
        res.xml_state.build_type = BuildType()
        res.xml_state.build_type.image = "kis"
        res.xml_state.build_type.filesystem = "xfs"

        # Write pickled object into a real temporary directory/file
        with tempfile.TemporaryDirectory() as tmpdir:
            result_path = os.path.join(tmpdir, "kiwi.result")
            with open(result_path, "wb") as f:
                pickle.dump(res, f)

            for m in temp_modules:
                sys.modules.pop(m)

            def file_exists_mock(path):
                return path == result_path

            with patch.dict(
                kiwi_info.__salt__,
                {
                    "file.file_exists": MagicMock(side_effect=file_exists_mock),
                },
            ):
                ret = kiwi_info.parse_kiwi_result(tmpdir)

                assert ret is not None
                assert ret.get("name") == "kiwi-image-test"
                assert ret.get("type") == "kis"
                assert ret.get("filesystem") == "xfs"

    finally:
        # Restore sys.modules
        for m in temp_modules:
            if m in original_modules:
                sys.modules[m] = original_modules[m]
            else:
                sys.modules.pop(m, None)
