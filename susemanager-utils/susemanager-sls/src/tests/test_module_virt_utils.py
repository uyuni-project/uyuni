"""
Unit tests for the virt_utils module
"""
from mock import MagicMock, patch
from xml.etree import ElementTree
import pytest

from ..modules import virt_utils
from . import mockery

mockery.setup_environment()


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
