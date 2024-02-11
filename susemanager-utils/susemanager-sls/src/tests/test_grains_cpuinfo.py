"""
Author: bo@suse.de
"""

import json
import pytest
from unittest.mock import MagicMock, patch, mock_open
from . import mockery

mockery.setup_environment()

# pylint: disable-next=wrong-import-position
from ..grains import cpuinfo


def test_total_num_cpus():
    """
    Test total_num_cpus function.

    :return:
    """
    os_listdir = [
        "cpu0",
        "cpu1",
        "cpu2",
        "cpu3",
        "cpufreq",
        "cpuidle",
        "power",
        "modalias",
        "kernel_max",
        "possible",
        "online",
        "offline",
        "isolated",
        "uevent",
        "intel_pstate",
        "microcode",
        "present",
    ]

    with patch("os.path.exists", MagicMock(return_value=True)):
        with patch("os.listdir", MagicMock(return_value=os_listdir)):
            cpus = cpuinfo.total_num_cpus()
            # pylint: disable-next=unidiomatic-typecheck
            assert type(cpus) == dict
            assert "total_num_cpus" in cpus
            assert cpus["total_num_cpus"] == 4


def test_cpusockets_dmidecode_count_sockets():
    """
    Test _dmidecode_count_sockets sub in cpusockets function.

    :return:
    """

    sample = mockery.get_test_data("dmidecode.sample")
    cpuinfo.log = MagicMock()
    with patch.dict(
        cpuinfo.__salt__,
        {"cmd.run_all": MagicMock(return_value={"retcode": 0, "stdout": sample})},
    ):
        # pylint: disable-next=protected-access
        out = cpuinfo._dmidecode_count_sockets([])
        # pylint: disable-next=unidiomatic-typecheck
        assert type(out) == dict
        assert "cpusockets" in out
        assert out["cpusockets"] == 1


def test_cpusockets_cpuinfo_count_sockets():
    """
    Test _cpuinfo_count_sockets sub in cpusockets function.

    :return:
    """
    cpuinfo.log = MagicMock()
    # cpuinfo parser is not applicable for non-Intel architectures, so should return nothing.
    for sample_name in ["cpuinfo.s390.sample", "cpuinfo.ppc64le.sample"]:
        with patch("os.access", MagicMock(return_value=True)):
            with patch.object(
                cpuinfo,
                "open",
                mock_open(read_data=mockery.get_test_data(sample_name)),
                create=True,
            ):
                # pylint: disable-next=protected-access
                assert cpuinfo._cpuinfo_count_sockets([]) is None

    with patch("os.access", MagicMock(return_value=True)):
        with patch.object(
            cpuinfo,
            "open",
            mock_open(read_data=mockery.get_test_data("cpuinfo.sample")),
            create=True,
        ):
            # pylint: disable-next=protected-access
            out = cpuinfo._cpuinfo_count_sockets([])
            # pylint: disable-next=unidiomatic-typecheck
            assert type(out) == dict
            assert "cpusockets" in out
            assert out["cpusockets"] == 1


@pytest.mark.parametrize("arch", ["ppc64le", "s390", "x86_64"])
def test_cpusockets_lscpu_count_sockets(arch):
    """
    Test _lscpu_count_sockets sub in cpusockets function.

    :return:
    """
    # pylint: disable-next=consider-using-f-string
    fn_smpl = "lscpu.{}.sample".format(arch)
    cpuinfo.log = MagicMock()
    with patch.dict(
        cpuinfo.__salt__,
        {
            "cmd.run_all": MagicMock(
                return_value={"retcode": 0, "stdout": mockery.get_test_data(fn_smpl)}
            )
        },
    ):
        # pylint: disable-next=protected-access
        out = cpuinfo._lscpu_count_sockets([])
        # pylint: disable-next=unidiomatic-typecheck
        assert type(out) == dict
        assert "cpusockets" in out
        assert out["cpusockets"] == 1


@pytest.mark.parametrize("arch", ["x86_64", "aarch64", "s390", "ppc64"])
def test_cpusockets_cpu_data(arch):
    """
    Test lscpu -J data extraction function.

    :return:
    """
    cpuinfo.log = MagicMock()
    # pylint: disable-next=consider-using-f-string
    sample_data = mockery.get_test_data("lscpu-json.{}.sample".format(arch))
    with patch.dict(
        cpuinfo.__salt__,
        {"cmd.run_all": MagicMock(return_value={"retcode": 0, "stdout": sample_data})},
    ):
        out = cpuinfo.cpu_data()
        # pylint: disable-next=unidiomatic-typecheck
        assert type(out) == dict
        # pylint: disable-next=consider-using-f-string
        expected = json.loads(mockery.get_test_data("lscpu-json.{}.out".format(arch)))
        assert out == expected
