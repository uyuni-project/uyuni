import pytest
from unittest.mock import MagicMock, patch
import subprocess
from ..modules import cpuinfo

@pytest.fixture
def mock_subprocess():
    with patch("subprocess.check_output") as mock:
        yield mock

@pytest.fixture
def mock_read_file():
    with patch("builtins.open", MagicMock(return_value=MagicMock(read=MagicMock(return_value="test content")))):
        yield

def test_get_architecture_success(mock_subprocess):
    mock_subprocess.return_value = "x86_64".encode()
    result = cpuinfo._get_architecture()
    assert result == "x86_64"

def test_get_architecture_failure(mock_subprocess):
    mock_subprocess.side_effect = subprocess.CalledProcessError(1, "uname")
    result = cpuinfo._get_architecture()
    assert result == "unknown"

def test_arch_specs_unknown(mock_subprocess):
    mock_subprocess.return_value = "unknown".encode()
    specs = cpuinfo.arch_specs()
    assert specs == {}

def test_arch_specs_ppc64(mock_subprocess):
    mock_subprocess.return_value = "ppc64".encode()
    cpuinfo._read_file = MagicMock(
        side_effect=lambda path: "shared_processor_mode = 1"
        if path == "/proc/ppc64/lparcfg"
        else "device tree content"
    )
    specs = cpuinfo.arch_specs()
    assert specs == {"lpar_mode": "shared", "device_tree": "device tree content"}


def test_arch_specs_arm64(mock_subprocess):
    cpuinfo._get_architecture = MagicMock(return_value="arm64")
    cpuinfo._read_file = MagicMock(return_value="device tree file content")
    mock_subprocess.return_value = "Family: test_family\nManufacturer: test_manufacturer\nSignature: test_signature".encode()
    specs = cpuinfo.arch_specs()
    assert specs == {
        "family": "test_family",
        "manufacturer": "test_manufacturer",
        "signature": "test_signature",
        "device_tree": "device tree file content"
    }

@pytest.mark.parametrize(
    "output, expected_specs",
    [
        ("VM00 Type: test_type\nVM00 Name: test_layer\nSockets: test_sockets", 
         {"hypervisor": "zvm", "type": "test_type", "layer_type": "test_layer", "sockets": "test_sockets"}),
    ]
)
def test_add_z_systems_extras(output, expected_specs, mock_subprocess):
    mock_subprocess.return_value = output.encode()
    specs = {}
    cpuinfo._add_z_systems_extras(specs)
    assert specs == expected_specs

def test_exact_string_match():
    text = "Family: test_family\nManufacturer: test_manufacturer\nSignature: test_signature"
    result = cpuinfo._exact_string_match("Family", text)
    assert result == "test_family"

def test_read_file_failure():
    cpuinfo._read_file = MagicMock(return_value="")
    result = cpuinfo._read_file("/path/to/nonexistent/file")
    assert result == ""
