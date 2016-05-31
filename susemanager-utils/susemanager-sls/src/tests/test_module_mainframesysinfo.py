'''
Author: Bo Maryniuk <bo@suse.de>
'''

import sys
from mock import MagicMock, patch

sys.modules['salt'] = MagicMock()
sys.modules['salt.utils'] = MagicMock()
sys.modules['salt.modules'] = MagicMock()
sys.modules['salt.modules.cmdmod'] = MagicMock()
sys.modules['salt.exceptions'] = MagicMock()

from ..modules import mainframesysinfo


def test_virtual():
    '''
    Test virtual returns True if setup os.access returns positive, and otherwise.

    :return:
    '''

    with patch('os.access', MagicMock(return_value=True)):
        assert mainframesysinfo.__virtual__() is True

    with patch('os.access', MagicMock(return_value=False)):
        assert mainframesysinfo.__virtual__() is False
