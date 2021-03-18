import pytest
from mock import MagicMock, patch
from . import mockery
mockery.setup_environment()

from ..grains import virt

@pytest.mark.parametrize("network", [True, False])
def test_features(network):
    """
    test the features function
    """
    virt_funcs = {}
    if network:
        virt_funcs["network_update"] = MagicMock(return_value = True)
    with patch.dict(virt.salt.modules.virt.__dict__, virt_funcs):
        assert virt.features()["virt_features"]["enhanced_network"] == network
