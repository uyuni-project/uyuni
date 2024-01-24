#  pylint: disable=missing-module-docstring
from unittest.mock import MagicMock, patch, call
from . import mockery

mockery.setup_environment()
# pylint: disable-next=wrong-import-position
import pytest

# pylint: disable-next=wrong-import-position
from ..states import virt_utils

# Mock globals
virt_utils.log = MagicMock()
virt_utils.__salt__ = {}
virt_utils.__grains__ = {}
virt_utils.__opts__ = {}

TEST_NETS = {
    "net0": {"active": True},
    "net1": {"active": True},
    "net2": {"active": False},
    "net3": {"active": False},
}

TEST_POOLS = {
    "pool0": {"state": "running"},
    "pool1": {"state": "running"},
    "pool2": {"state": "stopped"},
    "pool3": {"state": "stopped"},
}


@pytest.mark.parametrize("test", [False, True])
def test_network_running(test):
    """
    test the network_running function with only one name
    """
    with patch.dict(virt_utils.__opts__, {"test": test}):
        start_mock = MagicMock(return_value=True)
        with patch.dict(
            virt_utils.__salt__,
            {
                "virt.network_info": MagicMock(return_value=TEST_NETS),
                "virt.network_start": start_mock,
            },
        ):
            ret = virt_utils.network_running(name="net2")
            if test:
                assert ret["result"] is None
                start_mock.assert_not_called()
            else:
                assert ret["result"]
                start_mock.assert_called_with("net2")
            assert ret["comment"] == "net2 network has been started"


@pytest.mark.parametrize("test", [False, True])
def test_network_multiple(test):
    """
    test the network_running function with several names
    """
    with patch.dict(virt_utils.__opts__, {"test": test}):
        start_mock = MagicMock(return_value=True)
        with patch.dict(
            virt_utils.__salt__,
            {
                "virt.network_info": MagicMock(return_value=TEST_NETS),
                "virt.network_start": start_mock,
            },
        ):
            ret = virt_utils.network_running(
                name="the-state-id", networks=["net0", "net1", "net2", "net3"]
            )
            if test:
                assert ret["result"] is None
                start_mock.assert_not_called()
            else:
                assert ret["result"]
                assert start_mock.mock_calls == [
                    call("net2"),
                    call("net3"),
                ]
            assert ret["comment"] == "net2, net3 networks have been started"
            assert ret["changes"] == {"net2": "started", "net3": "started"}


def test_network_missing():
    """
    test the network_running function with names of missing networks
    """
    with patch.dict(virt_utils.__opts__, {"test": True}):
        start_mock = MagicMock(return_value=True)
        with patch.dict(
            virt_utils.__salt__,
            {
                "virt.network_info": MagicMock(return_value=TEST_NETS),
                "virt.network_start": start_mock,
            },
        ):
            ret = virt_utils.network_running(
                name="the-state-id", networks=["net0", "net1", "net2", "net5"]
            )
            assert not ret["result"]
            start_mock.assert_not_called()
            assert ret["comment"] == "net5 network is not defined"
            # pylint: disable-next=use-implicit-booleaness-not-comparison
            assert ret["changes"] == {}


@pytest.mark.parametrize("test", [False, True])
def test_pool_running(test):
    """
    test the pool_running function with only one name
    """
    with patch.dict(virt_utils.__opts__, {"test": test}):
        start_mock = MagicMock(return_value=True)
        refresh_mock = MagicMock(return_value=True)
        with patch.dict(
            virt_utils.__salt__,
            {
                "virt.pool_info": MagicMock(return_value=TEST_POOLS),
                "virt.pool_start": start_mock,
                "virt.pool_refresh": refresh_mock,
            },
        ):
            ret = virt_utils.pool_running(name="pool2")
            if test:
                assert ret["result"] is None
                start_mock.assert_not_called()
            else:
                assert ret["result"]
                start_mock.assert_called_with("pool2")
            refresh_mock.assert_not_called()
            assert ret["comment"] == "pool2 pool has been started"
            assert ret["changes"] == {"pool2": "started"}


@pytest.mark.parametrize("test", [False, True])
def test_pool_multiple(test):
    """
    test the pool_running function with several names
    """
    with patch.dict(virt_utils.__opts__, {"test": test}):
        start_mock = MagicMock(return_value=True)
        refresh_mock = MagicMock(return_value=True)
        with patch.dict(
            virt_utils.__salt__,
            {
                "virt.pool_info": MagicMock(return_value=TEST_POOLS),
                "virt.pool_start": start_mock,
                "virt.pool_refresh": refresh_mock,
            },
        ):
            ret = virt_utils.pool_running(
                name="the-state-id", pools=["pool0", "pool1", "pool2", "pool3"]
            )
            if test:
                assert ret["result"] is None
                start_mock.assert_not_called()
            else:
                assert ret["result"]
                assert start_mock.mock_calls == [
                    call("pool2"),
                    call("pool3"),
                ]
                assert refresh_mock.mock_calls == [
                    call("pool0"),
                    call("pool1"),
                ]
            assert ret["comment"] == "pool2, pool3 pools have been started"
            assert ret["changes"] == {
                "pool0": "refreshed",
                "pool1": "refreshed",
                "pool2": "started",
                "pool3": "started",
            }


def test_pool_missing():
    """
    test the pool_running function with names of undefined pools
    """
    with patch.dict(virt_utils.__opts__, {"test": True}):
        start_mock = MagicMock(return_value=True)
        with patch.dict(
            virt_utils.__salt__,
            {
                "virt.pool_info": MagicMock(return_value=TEST_POOLS),
                "virt.pool_start": start_mock,
            },
        ):
            ret = virt_utils.pool_running(
                name="the-state-id", pools=["pool0", "pool1", "pool2", "pool5"]
            )
            assert not ret["result"]
            start_mock.assert_not_called()
            assert ret["comment"] == "pool5 pool is not defined"
            # pylint: disable-next=use-implicit-booleaness-not-comparison
            assert ret["changes"] == {}


def test_vm_resources_running():
    """
    test the vm_resources_running function
    """
    with patch.dict(virt_utils.__opts__, {"test": False}):
        start_net_mock = MagicMock(return_value=True)
        start_pool_mock = MagicMock(return_value=True)
        refresh_pool_mock = MagicMock(return_value=True)
        test_vm_info = {
            "vm1": {
                "nics": {
                    "nic0": {"type": "network", "source": {"network": "net0"}},
                    "nic1": {"type": "network", "source": {"network": "net3"}},
                },
                "disks": {
                    "disk0": {"file": "pool0/system"},
                    "disk1": {"file": "pool2/data"},
                    "disk2": {"file": "/foo/bar.qcow2"},
                },
            },
        }
        with patch.dict(
            virt_utils.__salt__,
            {
                "virt.network_info": MagicMock(return_value=TEST_NETS),
                "virt.pool_info": MagicMock(return_value=TEST_POOLS),
                "virt.network_start": start_net_mock,
                "virt.pool_start": start_pool_mock,
                "virt.pool_refresh": refresh_pool_mock,
                "virt.vm_info": MagicMock(return_value=test_vm_info),
            },
        ):
            ret = virt_utils.vm_resources_running("vm1")
            assert ret["result"]
            assert ret["changes"] == {
                "networks": {"net3": "started"},
                "pools": {"pool0": "refreshed", "pool2": "started"},
            }
            assert start_net_mock.mock_calls == [call("net3")]
            assert start_pool_mock.mock_calls == [call("pool2")]
            assert refresh_pool_mock.mock_calls == [call("pool0")]
