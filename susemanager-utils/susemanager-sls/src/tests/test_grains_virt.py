#  pylint: disable=missing-module-docstring
import pytest

# pylint: disable-next=unused-import
from unittest.mock import MagicMock, patch, Mock
from . import mockery

mockery.setup_environment()

# pylint: disable-next=wrong-import-position
from ..grains import virt


@pytest.mark.parametrize("network", [True, False])
def test_features_network(network):
    """
    test the network part of the features function
    """
    virt_funcs = {}
    if network:
        virt_funcs["network_update"] = MagicMock(return_value=True)
    with patch.dict(virt.salt.modules.virt.__dict__, virt_funcs):
        assert virt.features()["virt_features"]["enhanced_network"] == network


@pytest.mark.parametrize(
    "cluster, start_resources", [(True, True), (True, False), (False, False)]
)
def test_features_cluster(cluster, start_resources):
    """
    test the cluster part of the features function
    """
    param = (
        ""
        if not start_resources
        else """
      <parameter name="start_resources">
        <content type="boolean" default="false"/>
      </parameter>"""
    )
    # pylint: disable-next=consider-using-f-string
    crm_resources = """<?xml version="1.0"?>
<!DOCTYPE resource-agent SYSTEM "ra-api-1.dtd">
<resource-agent name="VirtualDomain">
  <parameters>  
    {}
  </parameters>
</resource-agent>
""".format(
        param
    )
    popen_mock = MagicMock(side_effect=OSError())
    check_call_mock = MagicMock(side_effect=OSError())
    if cluster:
        popen_mock = MagicMock()
        popen_mock.return_value.communicate.side_effect = [
            (crm_resources, None),
            (b"libvirtd (libvirt) 5.1.0\n", None),
        ]
        check_call_mock = MagicMock(return_value=0)

    with patch.object(virt.subprocess, "check_call", check_call_mock):
        with patch.object(virt.subprocess, "Popen", popen_mock):
            actual = virt.features()["virt_features"]
            assert actual["cluster"] == cluster
            assert actual["resource_agent_start_resources"] == start_resources


@pytest.mark.parametrize("version, expected", [("5.1.0", False), ("7.3.0", True)])
def test_features_efi(version, expected):
    """
    Test the uefi auto discovery feature
    """
    popen_mock = MagicMock()
    popen_mock.return_value.communicate.return_value = (
        # pylint: disable-next=consider-using-f-string
        "libvirtd (libvirt) {}\n".format(version).encode(),
        None,
    )

    with patch.object(virt.subprocess, "Popen", popen_mock):
        assert virt.features()["virt_features"]["uefi_auto_loader"] == expected
