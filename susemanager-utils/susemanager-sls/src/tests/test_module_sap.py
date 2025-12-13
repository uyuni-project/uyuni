"""
Unit tests for the sap module
"""

from ..modules import sap

from unittest.mock import patch, MagicMock
import pytest


@pytest.mark.parametrize(
    "dir_struct, expected_result",
    [
        (
            [
                ("F1B", ("ASCS00", "DVEBMGS00", "AZCC00")),
                ("F3C", ("HBA00",)),
                ("F2C", ("HDB00",)),
            ],
            [
                {"system_id": "F1B", "instance_type": "ASCS"},
                {"system_id": "F1B", "instance_type": "AZCC"},
                {"system_id": "F1B", "instance_type": "DVEBMGS"},
                {"system_id": "F2C", "instance_type": "HDB"},
                {"system_id": "F3C", "instance_type": "HBA"},
            ],
        ),
        (
            [
                ("F2C", ("HDB00",)),
            ],
            [
                {"system_id": "F2C", "instance_type": "HDB"},
            ],
        ),
        ([], []),
    ],
)
def test_get_workloads(tmpdir, dir_struct, expected_result):
    for sap_dir1, sap_dirs2 in dir_struct:
        tmp_sap_dir1 = tmpdir.mkdir(sap_dir1)
        for sap_dir2 in sap_dirs2:
            tmp_sap_dir1.mkdir(sap_dir2)

    orig_sap_regex = sap.SAP_REGEX

    tmpdir_path = str(tmpdir)

    def mock_match(s):
        if s.startswith(tmpdir_path):
            s = "/usr/sap" + s[len(tmpdir_path) :]
        return orig_sap_regex.match(s)

    mock_sap_regex = MagicMock()
    mock_sap_regex.match = mock_match

    with patch.object(sap, "SAP_BASE_PATH", tmpdir), patch.object(
        sap, "SAP_REGEX", mock_sap_regex
    ):
        assert sap.get_workloads() == expected_result


def test_no_sap_directory():
    with patch("os.path.exists", return_value=False):
        result = sap.get_workloads()
        # pylint: disable-next=singleton-comparison
        assert (len(result) > 0) == False
