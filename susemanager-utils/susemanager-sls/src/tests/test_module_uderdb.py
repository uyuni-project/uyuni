'''
Author: Bo Maryniuk <bo@suse.de>
'''

import sys
from mock import MagicMock, patch

# Mock the imports for the udevdb
sys.modules['salt'] = MagicMock()
sys.modules['salt.utils'] = MagicMock()
sys.modules['salt.modules'] = MagicMock()
sys.modules['salt.modules.cmdmod'] = MagicMock()
sys.modules['salt.exceptions'] = MagicMock()

from ..modules import udevdb


def test_virtual():
    '''
    Test virtual returns True if 'udevadm' is around in the environment.

    :return:
    '''
    with patch('salt.utils.which_bin', MagicMock(return_value=None)):
        assert udevdb.__virtual__() is False

    with patch('salt.utils.which_bin', MagicMock(return_value="/bogus/path")):
        assert udevdb.__virtual__() is True


def test_normalize():
    '''
    Test if udevdb.normalize does not returns nested lists that contains only one item.

    :return:
    '''
    data = {'key': ['value', 'here'], 'foo': ['bar'], 'some': 'data'}
    udevdb.normalize(data)
    assert data == {'foo': 'bar', 'some': 'data', 'key': ['value', 'here']}
