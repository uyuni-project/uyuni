"""
Unit tests for the virt_utils module
"""
from mock import MagicMock, patch, mock_open
from xml.etree import ElementTree
import pytest

from ..modules import virt_utils
from . import mockery

mockery.setup_environment()

virt_utils.__salt__ = {}


CRM_CONFIG_XML = b"""<?xml version="1.0" ?>
<cib>
  <configuration>
    <nodes>
      <node id="1084783225" uname="demo-kvm1"/>
      <node id="1084783226" uname="demo-kvm2"/>
    </nodes>
    <resources>
      <clone id="c-clusterfs">
        <meta_attributes id="c-clusterfs-meta_attributes">
          <nvpair name="interleave" value="true" id="c-clusterfs-meta_attributes-interleave"/>
          <nvpair name="clone-max" value="8" id="c-clusterfs-meta_attributes-clone-max"/>
          <nvpair id="c-clusterfs-meta_attributes-target-role" name="target-role" value="Started"/>
        </meta_attributes>
        <primitive id="clusterfs" class="ocf" provider="heartbeat" type="Filesystem">
          <instance_attributes id="clusterfs-instance_attributes">
            <nvpair name="directory" value="/srv/clusterfs" id="clusterfs-instance_attributes-directory"/>
            <nvpair name="fstype" value="ocfs2" id="clusterfs-instance_attributes-fstype"/>
            <nvpair name="device" value="/dev/vdc" id="clusterfs-instance_attributes-device"/>
          </instance_attributes>
        </primitive>
      </clone>
      <primitive id="vm01" class="ocf" provider="heartbeat" type="VirtualDomain">
        <instance_attributes id="vm01-instance_attributes">
          <nvpair name="config" value="/srv/clusterfs/vm01.xml" id="vm01-instance_attributes-config"/>
        </instance_attributes>
      </primitive>
      <primitive id="vm03" class="ocf" provider="heartbeat" type="VirtualDomain">
        <instance_attributes id="vm03-instance_attributes">
          <nvpair name="config" value="/srv/clusterfs/vm03.xml" id="vm03-instance_attributes-config"/>
        </instance_attributes>
      </primitive>
    </resources>
  </configuration>
</cib>
"""


@pytest.mark.parametrize(
    "path,expected",
    [
        ("/srv/clusterfs/vms/", "c-clusterfs"),
        ("/srv/clusterfs", "c-clusterfs"),
        ("/foo/bar", None),
    ],
)
def test_get_cluster_filesystem(path, expected):
    """
    test the get_cluster_filesystem() function in normal cases
    """
    with patch.object(virt_utils, "Path", MagicMock(wraps=virt_utils.Path)) as path_mock:
        path_mock.return_value.resolve.return_value = virt_utils.Path(path)
        with patch.object(virt_utils.subprocess, "Popen", MagicMock()) as popen_mock:
            popen_mock.return_value.communicate.return_value = (CRM_CONFIG_XML, None)
            assert virt_utils.get_cluster_filesystem(path) == expected


def test_get_cluster_filesystem_nocrm():
    """
    test the get_cluster_filesystem() function when crm is not installed
    """
    with patch.object(virt_utils, "Path", MagicMock(wraps=virt_utils.Path)) as path_mock:
        path_mock.return_value.resolve.return_value = virt_utils.Path("/srv/clusterfs/xml")
        with patch.object(virt_utils.subprocess, "Popen", MagicMock()) as popen_mock:
            popen_mock.return_value.communicate.side_effect = FileNotFoundError("No such file or directory: 'crm'")
            assert virt_utils.get_cluster_filesystem("/srv/clusterfs/xml") == None


@pytest.mark.parametrize("no_graphics", [True, False])
def test_vm_info_no_cluster(no_graphics):
    """
    Test the vm_info() function for a VM which isn't in a cluster
    """
    fake_vminfo = {} if no_graphics else {
        "graphics": {
            "type": "spice",
        }
    }
    vminfo_mock = MagicMock(return_value={"vm": fake_vminfo})
    with patch.dict(virt_utils.__salt__, {"virt.vm_info": vminfo_mock}):
        info = virt_utils.vm_info("vm")
        assert info["vm"].get("cluster_primitive") is None
        assert info["vm"].get("graphics_type") == (None if no_graphics else "spice")


def test_vminfo_cluster():
    """
    Test the vm_info() function for VMs in a cluster
    """
    vm_xml_template = """<domain type='kvm'>
  <name>{}</name>
  <uuid>{}</uuid>
  <memory unit='KiB'>524288</memory>
  <currentMemory unit='KiB'>524288</currentMemory>
  <vcpu placement='static'>1</vcpu>
  <devices>
    <graphics type='vnc' port='-1' autoport='yes' listen='0.0.0.0'>
      <listen type='address' address='0.0.0.0'/>
    </graphics>
  </devices>
</domain>"""

    vms = [
        ("vm01", "15c09f1f-6ac7-43b5-83e9-96a63c40fb14"),
        ("vm03", "c4596ec0-4e0e-4a1d-aa43-88ba442d5085")
    ]
    vms_xml = [ElementTree.fromstring(vm_xml_template.format(vm[0], vm[1])) for vm in vms]

    vminfo_mock = MagicMock(return_value={"vm01": {"graphics": {"type": "vnc"}}})

    popen_mock = MagicMock()
    popen_mock.return_value.communicate.return_value = (CRM_CONFIG_XML, None)

    with patch.dict(virt_utils.__salt__, {"virt.vm_info": vminfo_mock}):
        with patch.object(virt_utils.subprocess, "Popen", popen_mock):
            with patch.object(virt_utils.ElementTree, "parse", MagicMock(side_effect=vms_xml)):
                info = virt_utils.vm_info()
                assert info["vm01"].get("cluster_primitive") == "vm01"
                assert info["vm01"].get("graphics_type") =="vnc"
                assert info["vm01"].get("definition_path") == "/srv/clusterfs/vm01.xml"


def test_host_info():
    """
    Test the host_info() function
    """
    popen_mock = MagicMock()
    crm_conf_node = b"""<?xml version="1.0" ?>
<cib>
  <configuration>
    <crm_config/>
    <nodes>
      <node id="1084783225" uname="demo-kvm1"/>
      <node id="1084783226" uname="demo-kvm2"/>
      <node id="1084783227" uname="demo-kvm3"/>
    </nodes>
    <resources/>
    <constraints/>
  </configuration>
</cib>"""
    popen_mock.return_value.communicate.side_effect = [(b"demo-kvm1", None), (crm_conf_node, None)]
    with patch.object(virt_utils.subprocess, "Popen", popen_mock):
        with patch.dict(virt_utils.__salt__, {"virt.get_hypervisor": MagicMock(return_value="kvm")}):
            info = virt_utils.host_info()
            assert info["hypervisor"] == "kvm"
            assert info["cluster_other_nodes"] == ["demo-kvm2", "demo-kvm3"]


def test_vm_definition():
    """
    test the vm_definition() function with a regular VM
    """
    with patch.object(virt_utils.libvirt, "open", MagicMock()) as mock_conn:
        mock_conn.return_value.lookupByUUIDString.return_value.name.return_value = "vm01"
        vm_xml = """<domain type='kvm'>
  <name>vm01</name>
  <uuid>15c09f1f-6ac7-43b5-83e9-96a63c40fb14</uuid>
  <memory unit='KiB'>524288</memory>
  <currentMemory unit='KiB'>524288</currentMemory>
  <vcpu placement='static'>1</vcpu>
</domain>"""
        vm_info = {
            "uuid": "15c09f1f-6ac7-43b5-83e9-96a63c40fb14",
            "cpu": 1,
        }

        with patch.dict(virt_utils.__salt__, {
            "virt.get_xml": MagicMock(return_value=vm_xml),
            "virt.vm_info": MagicMock(return_value={"vm01": vm_info})
        }):
            actual = virt_utils.vm_definition("15c09f1f-6ac7-43b5-83e9-96a63c40fb14")
            assert actual["definition"] == vm_xml
            assert actual["info"] == vm_info


def test_vm_definition_cluster():
    """
    test the vm_definition() function with a stopped VM defined on a cluster
    """
    vm_xml = """<domain type='kvm'>
  <name>vm01</name>
  <uuid>15c09f1f-6ac7-43b5-83e9-96a63c40fb14</uuid>
  <memory unit='KiB'>524288</memory>
  <currentMemory unit='KiB'>524288</currentMemory>
  <vcpu placement='static'>1</vcpu>
  <devices>
    <graphics type='vnc' port='-1' autoport='yes' listen='0.0.0.0'>
      <listen type='address' address='0.0.0.0'/>
    </graphics>
  </devices>
</domain>"""

    with patch.object(virt_utils.libvirt, "open", MagicMock()) as mock_conn:
        with patch.object(virt_utils.libvirt, "libvirtError", Exception) as mock_error:
            mock_conn.return_value.lookupByUUIDString.side_effect = mock_error
            with patch.object(virt_utils.subprocess, "Popen", MagicMock()) as popen_mock:
                popen_mock.return_value.communicate.return_value = (CRM_CONFIG_XML, None)
                with patch("builtins.open", mock_open(read_data=vm_xml)):
                    actual = virt_utils.vm_definition("15c09f1f-6ac7-43b5-83e9-96a63c40fb14")
                    assert actual["definition"] == vm_xml
                    assert actual.get("info") is None
