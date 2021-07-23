import pytest
from mock import MagicMock, patch, Mock
from . import mockery
mockery.setup_environment()

from ..grains import virt

@pytest.fixture
def libvirt():
    if not hasattr(virt, "libvirt"):
        virt.libvirt = Mock()
    return virt.libvirt


@pytest.mark.parametrize("network", [True, False])
def test_features_network(network):
    """
    test the network part of the features function
    """
    virt_funcs = {}
    if network:
        virt_funcs["network_update"] = MagicMock(return_value = True)
    with patch.dict(virt.salt.modules.virt.__dict__, virt_funcs):
        assert virt.features()["virt_features"]["enhanced_network"] == network


@pytest.mark.parametrize("cluster, start_resources", [(True, True), (True, False), (False, False)])
def test_features_cluster(cluster, start_resources):
    """
    test the cluster part of the features function
    """
    param = "" if not start_resources else """
      <parameter name="start_resources">
        <content type="boolean" default="false"/>
      </parameter>"""
    crm_resources = """<?xml version="1.0"?>
<!DOCTYPE resource-agent SYSTEM "ra-api-1.dtd">
<resource-agent name="VirtualDomain">
  <parameters>  
    {}
  </parameters>
</resource-agent>
""".format(param)
    popen_mock = MagicMock(side_effect=FileNotFoundError())
    check_call_mock = MagicMock(side_effect=FileNotFoundError())
    if cluster:
        popen_mock = MagicMock()
        popen_mock.return_value.communicate.return_value = (crm_resources, None)
        check_call_mock = MagicMock(return_value = 0)

    with patch.object(virt.subprocess, "check_call", check_call_mock):
        with patch.object(virt.subprocess, "Popen", popen_mock):
            assert virt.features()["virt_features"]["cluster"] == cluster
            assert virt.features()["virt_features"]["resource_agent_start_resources"] == start_resources


@pytest.mark.parametrize("version, expected", [(5001000, False), (7003000, True)])
def test_features_efi(version, expected, libvirt):
    """
    Test the uefi auto discovery feature
    """
    with patch.object(libvirt, "open", MagicMock()) as mock_conn:
        mock_conn.return_value.getLibVersion.return_value = version
        assert virt.features()["virt_features"]["uefi_auto_loader"] == expected
