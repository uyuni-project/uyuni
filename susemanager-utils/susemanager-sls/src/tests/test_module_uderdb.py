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

