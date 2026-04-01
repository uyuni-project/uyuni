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


@pytest.mark.parametrize(
    "arch, lparcfg_content, expected_count",
    [("ppc64le", "partition_active_processors=8\n", 8), ("x86_64", None, 4)],
)
def test_total_num_cpus(arch, lparcfg_content, expected_count):
    """
    Test that total_num_cpus returns the correct count based on architecture.
    """
    os_listdir = ["cpu0", "cpu1", "cpu2", "cpu3"]

    with patch("os.path.exists", return_value=True), patch(
        "os.listdir", return_value=os_listdir
    ), patch("src.grains.cpuinfo._get_architecture", return_value=arch), patch(
        "src.grains.cpuinfo._read_file", return_value=lparcfg_content
    ):

        cpus = cpuinfo.total_num_cpus()
        assert cpus["total_num_cpus"] == expected_count


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


def test_arch_specs_unknown():
    with patch("src.grains.cpuinfo._get_architecture", return_value="unknown"), patch(
        "src.grains.cpuinfo._add_ppc64_extras"
    ) as mock_ppc64, patch("src.grains.cpuinfo._add_arm64_extras") as mock_arm64, patch(
        "src.grains.cpuinfo._add_z_systems_extras"
    ) as mock_z:

        specs = cpuinfo.arch_specs()

        assert mock_ppc64.call_count == 0
        assert mock_arm64.call_count == 0
        assert mock_z.call_count == 0
        assert specs == {"cpu_arch_specs": {}}


def test_arch_specs_ppc64():
    """
    Test that arch_specs extracts all relevant PPC64 LPAR and device tree fields.
    """
    # pylint: disable-next=protected-access
    cpuinfo._get_architecture = MagicMock(return_value="ppc64")
    # pylint: disable-next=protected-access
    cpuinfo._read_file = MagicMock(
        side_effect=lambda path: (
            "shared_processor_mode = 1\n"
            "partition_active_processors = 4\n"
            "partition_entitled_capacity = 2\n"
            "capped = 0\n"
            if path == "/proc/ppc64/lparcfg"
            else "device tree content"
        )
    )
    specs = cpuinfo.arch_specs()
    assert specs["cpu_arch_specs"] == {
        "lpar_mode": "shared",
        "total_virtual_processors": 4,
        "entitled_capacity": 2,
        "capping_mode": "uncapped",
        "device_tree": "device tree content",
    }


def test_arch_specs_arm64():
    # pylint: disable-next=protected-access
    cpuinfo._get_architecture = MagicMock(return_value="arm64")
    # pylint: disable-next=protected-access
    cpuinfo._read_file = MagicMock(return_value="")

    with patch.dict(cpuinfo.__salt__, {"cmd.run_all": MagicMock()}), patch.object(
        cpuinfo, "_which_bin"
    ) as mock_which_bin:
        mock_which_bin.return_value = "/usr/bin/dmidecode"
        dmi_output = "Family: test_family\nManufacturer: test_manufacturer\nSignature: test_signature"
        cpuinfo.__salt__["cmd.run_all"].return_value = {
            "retcode": 0,
            "stdout": dmi_output,
        }

        specs = cpuinfo.arch_specs()

    assert specs == {
        "cpu_arch_specs": {
            "family": "test_family",
            "manufacturer": "test_manufacturer",
            "signature": "test_signature",
        }
    }


@pytest.mark.parametrize(
    "output, expected_specs",
    [
        (
            "VM00 Type: test_type\nType Name: test_model\nVM00 Name: test_layer\nSockets: test_sockets",
            {
                "type": "test_type",
                "type_name": "test_model",
                "layer_type": "test_layer",
            },
        ),
        (
            "LPAR Type: lpar_type\nType Name: lpar_model\nLPAR Name: lpar_layer\nSockets: lpar_sockets",
            {
                "type": "lpar_type",
                "type_name": "lpar_model",
                "layer_type": "lpar_layer",
            },
        ),
    ],
)
def test_add_z_systems_extras(output, expected_specs):
    specs = {}
    with patch.dict(cpuinfo.__salt__, {"cmd.run_all": MagicMock()}), patch.object(
        cpuinfo, "_which_bin"
    ) as mock_which_bin:
        mock_which_bin.return_value = "/usr/bin/read_values"
        cpuinfo.__salt__["cmd.run_all"].return_value = {
            "retcode": 0,
            "stdout": output,
        }
        # pylint: disable-next=protected-access
        cpuinfo._add_z_systems_extras(specs)
    assert specs == expected_specs


def test_exact_string_match():
    text = "Family: test_family\nManufacturer: test_manufacturer\nSignature: test_signature"
    # pylint: disable-next=protected-access
    result = cpuinfo._exact_string_match("Family", text)
    assert result == "test_family"


def test_read_file_failure():
    # pylint: disable-next=protected-access
    cpuinfo._read_file = MagicMock(return_value="")
    # pylint: disable-next=protected-access
    result = cpuinfo._read_file("/path/to/nonexistent/file")
    assert result == ""
