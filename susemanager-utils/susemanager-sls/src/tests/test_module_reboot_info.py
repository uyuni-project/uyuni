
from ..modules import reboot_info

from unittest.mock import MagicMock, patch
import pytest

@pytest.mark.parametrize(
    "os_family, expected_result",
    [
        ("Debian", True),
        ("Suse", True),
        ("RedHat", True),
        ("Windows", False),
    ],
)
def test_virtual(os_family, expected_result):
  reboot_info.__grains__ = {
    "os_family": os_family
  }
  assert reboot_info.__virtual__() == expected_result


@pytest.mark.parametrize(
    "exit_code_to_check, real_exit_code, result",
    [
        (0, 0, True),
        (0, 1, False)
    ],
)
def test_check_cmd_exit_code(exit_code_to_check, real_exit_code, result):
    mock_run_all = MagicMock(return_value={"stderr": None, "retcode": real_exit_code})
    with patch.dict(reboot_info.__salt__, {"cmd.run_all": mock_run_all}):
        assert reboot_info._check_cmd_exit_code("fake command", exit_code_to_check) == result

@pytest.mark.parametrize(
    "file_exists, result",
    [
        (True, True),
        (False, False)
    ],
)
def test_reboot_required_debian(file_exists, result):
  reboot_info.__grains__["os_family"] = "Debian"
  with patch("os.path.exists", return_value=file_exists):
      assert reboot_info.reboot_required()["reboot_required"] == result

@pytest.mark.parametrize(
    "os_major_release, file_exists, result",
    [
        (15, True, True),
        (15, False, False),
        (11, True, True),
        (11, False, False),
    ],
)
def test_reboot_required_suse(os_major_release, file_exists, result):
  reboot_info.__grains__["os_family"] = "Suse"
  reboot_info.__grains__["osmajorrelease"] = os_major_release
  with patch("os.path.exists", return_value=file_exists):
    assert reboot_info.reboot_required()["reboot_required"] == result

@pytest.mark.parametrize(
    "os_major_release, cmd, exit_code, result",
    [
        (7, "needs-restarting -r", 1, True),
        (7, "needs-restarting -r", 0, False),
        (8, "dnf -q needs-restarting -r", 1, True),
        (8, "dnf -q needs-restarting -r", 99, False),
    ],
)
def test_reboot_required_redhat(os_major_release, cmd, exit_code, result):
  reboot_info.__grains__["os_family"] = "RedHat"
  reboot_info.__grains__["osmajorrelease"] = os_major_release
  reboot_info._check_cmd_exit_code = MagicMock(return_value=exit_code == 1)
  assert reboot_info.reboot_required()["reboot_required"] == result
  reboot_info._check_cmd_exit_code.assert_called_once_with(cmd, 1)
    

@pytest.mark.parametrize(
    "pending_transaction, result",
    [
        (True, True),
        (False, False)
    ],
)
def test_reboot_required_transactional(pending_transaction, result):
  reboot_info.__grains__["transactional"] = True
  mock_pending_transactions = MagicMock(return_value=pending_transaction)
  with patch.dict(reboot_info.__salt__, {"transactional_update.pending_transaction": mock_pending_transactions}):
    assert reboot_info.reboot_required()["reboot_required"] == result
