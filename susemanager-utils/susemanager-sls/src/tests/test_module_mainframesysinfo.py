'''
Author: Bo Maryniuk <bo@suse.de>
'''

import pytest
from mock import MagicMock, patch
from . import mockery
mockery.setup_environment()

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


def test_read_values():
    '''
    Test the read_values method.

    :return:
    '''
    bogus_data = "bogus data"
    run_all = {'stdout': bogus_data, 'retcode': 0, 'stderr': ''}
    with patch.dict(mainframesysinfo.__salt__, {'cmd.run_all': MagicMock(return_value=run_all)}):
        assert mainframesysinfo.read_values() == bogus_data

    run_all['retcode'] = 1
    run_all['stderr'] = 'error here'
    with patch.dict(mainframesysinfo.__salt__, {'cmd.run_all': MagicMock(return_value=run_all)}):
        with pytest.raises(Exception) as x:
            mainframesysinfo.read_values()
        assert str(x.value) == run_all['stderr']
