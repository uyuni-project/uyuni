"""
Author: mc@suse.com
"""

from ..modules import supportdata

from unittest.mock import MagicMock, patch
from . import mockery

mockery.setup_environment()


def test_supportdata_suse():
    """
    Test getting supportdata on a standard SUSE system

    :return:
    """
    supportdata.__grains__["os_family"] = "Suse"

    with patch.dict(
        supportdata.__salt__,
        {"cmd.run_all": MagicMock(return_value={"retcode": 0, "stdout": "sample"})},
    ):
        with patch.dict(
            supportdata.__salt__,
            {"cp.push_dir": MagicMock(return_value=False)},
        ):
            with patch(
                "src.modules.supportdata._get_supportdata_dir",
                MagicMock(return_value="/var/log/supportdata"),
            ):
                with patch(
                    "os.path.exists", MagicMock(side_effect=[False, False, True])
                ), patch("os.makedirs", MagicMock(return_value=True)):
                    out = supportdata.get()
                    assert isinstance(out, dict)
                    assert "success" in out
                    assert out["success"] is True
                    assert out["supportdata_dir"] == "/var/log/supportdata"

                    supportdata.__salt__["cmd.run_all"].assert_called_once_with(
                        ["/sbin/supportconfig", "-R", "/var/log/supportdata"],
                        python_shell=False,
                    )

def test_supportdata_suse_extra_args():
    """
    Test getting supportdata on a standard SUSE system

    :return:
    """
    supportdata.__grains__["os_family"] = "Suse"

    with patch.dict(
        supportdata.__salt__,
        {"cmd.run_all": MagicMock(return_value={"retcode": 0, "stdout": "sample"})},
    ):
        with patch.dict(
            supportdata.__salt__,
            {"cp.push_dir": MagicMock(return_value=False)},
        ):
            with patch(
                "src.modules.supportdata._get_supportdata_dir",
                MagicMock(return_value="/var/log/supportdata"),
            ):
                with patch(
                    "os.path.exists", MagicMock(side_effect=[False, False, True])
                ), patch("os.makedirs", MagicMock(return_value=True)):
                    out = supportdata.get("-o X,WEB -l 10000")
                    assert isinstance(out, dict)
                    assert "success" in out
                    assert out["success"] is True
                    assert out["supportdata_dir"] == "/var/log/supportdata"

                    supportdata.__salt__["cmd.run_all"].assert_called_once_with(
                        ["/sbin/supportconfig", "-R", "/var/log/supportdata", "-o", "X,WEB", "-l", "10000"],
                        python_shell=False,
                    )


def test_supportdata_mlm_proxy():
    """
    Test getting supportdata on a MLM Proxy

    :return:
    """
    supportdata.__grains__["os_family"] = "Suse"

    with patch.dict(
        supportdata.__salt__,
        {"cmd.run_all": MagicMock(return_value={"retcode": 0, "stdout": "sample"})},
    ):
        with patch.dict(
            supportdata.__salt__,
            {"cp.push_dir": MagicMock(return_value=False)},
        ):
            with patch(
                "src.modules.supportdata._get_supportdata_dir",
                MagicMock(return_value="/var/log/supportdata"),
            ):
                with patch(
                    "os.path.exists", MagicMock(side_effect=[False, True, True])
                ), patch("os.makedirs", MagicMock(return_value=True)):
                    out = supportdata.get()
                    assert isinstance(out, dict)
                    assert "success" in out
                    assert out["success"] is True
                    assert out["supportdata_dir"] == "/var/log/supportdata"

                    supportdata.__salt__["cmd.run_all"].assert_called_once_with(
                        [
                            "/usr/bin/mgrpxy",
                            "support",
                            "config",
                            "--output",
                            "/var/log/supportdata",
                        ],
                        python_shell=False,
                    )


def test_supportdata_mlm_server():
    """
    Test getting supportdata on a MLM Server

    :return:
    """
    supportdata.__grains__["os_family"] = "Suse"

    with patch.dict(
        supportdata.__salt__,
        {"cmd.run_all": MagicMock(return_value={"retcode": 0, "stdout": "sample"})},
    ):
        with patch.dict(
            supportdata.__salt__,
            {"cp.push_dir": MagicMock(return_value=False)},
        ):
            with patch(
                "src.modules.supportdata._get_supportdata_dir",
                MagicMock(return_value="/var/log/supportdata"),
            ):
                with patch(
                    "os.path.exists", MagicMock(side_effect=[True, False, True])
                ), patch("os.makedirs", MagicMock(return_value=True)):
                    out = supportdata.get()
                    assert isinstance(out, dict)
                    assert "success" in out
                    assert out["success"] is True
                    assert out["supportdata_dir"] == "/var/log/supportdata"

                    supportdata.__salt__["cmd.run_all"].assert_called_once_with(
                        [
                            "/usr/bin/mgradm",
                            "support",
                            "config",
                            "--output",
                            "/var/log/supportdata",
                        ],
                        python_shell=False,
                    )


def test_supportdata_redhat():
    """
    Test getting supportdata on a RedHat Server

    :return:
    """
    supportdata.__grains__["os_family"] = "RedHat"

    with patch.dict(
        supportdata.__salt__,
        {"cmd.run_all": MagicMock(return_value={"retcode": 0, "stdout": "sample"})},
    ):
        with patch.dict(
            supportdata.__salt__,
            {"cp.push_dir": MagicMock(return_value=False)},
        ):
            with patch(
                "src.modules.supportdata._get_supportdata_dir",
                MagicMock(return_value="/var/log/supportdata"),
            ):
                with patch("os.path.exists", MagicMock(side_effect=[True])), patch(
                    "os.makedirs", MagicMock(return_value=True)
                ):
                    out = supportdata.get()
                    assert isinstance(out, dict)
                    assert "success" in out
                    assert out["success"] is True
                    assert out["supportdata_dir"] == "/var/log/supportdata"

                    supportdata.__salt__["cmd.run_all"].assert_called_once_with(
                        [
                            "/usr/sbin/sosreport",
                            "--batch",
                            "--tmp-dir",
                            "/var/log/supportdata",
                        ],
                        python_shell=False,
                    )


def test_supportdata_debian():
    """
    Test getting supportdata on a Debian Server

    :return:
    """
    supportdata.__grains__["os_family"] = "Debian"
    supportdata.__grains__["os"] = "Debian 12"

    with patch.dict(
        supportdata.__salt__,
        {"cmd.run_all": MagicMock(return_value={"retcode": 0, "stdout": "sample"})},
    ):
        with patch.dict(
            supportdata.__salt__,
            {"cp.push_dir": MagicMock(return_value=False)},
        ):
            with patch(
                "src.modules.supportdata._get_supportdata_dir",
                MagicMock(return_value="/var/log/supportdata"),
            ):
                with patch("os.path.exists", MagicMock(side_effect=[True])), patch(
                    "os.makedirs", MagicMock(return_value=True)
                ):
                    out = supportdata.get()
                    assert isinstance(out, dict)
                    assert "success" in out
                    assert out["success"] is False
                    assert out["supportdata_dir"] == ""
                    assert (
                        out["error"]
                        == "Getting supportdata not supported for Debian 12"
                    )
