'''
Author: mc@suse.com
'''

from mock import MagicMock, patch
from . import mockery
mockery.setup_environment()

from ..modules import sumautil


def test_livepatching_kernelliveversion():
    '''
    Test kernel_live_version.

    :return:
    '''

    sumautil.log = MagicMock()
    with patch('salt.utils.path.which_bin', MagicMock(return_value="/bogus/path")):
        mock = MagicMock(side_effect=[{ 'retcode': 0, 'stdout': 'ready' },
                                      { 'retcode': 0, 'stdout': mockery.get_test_data('livepatching-1.sample')}
                                     ]);
        with patch.dict(sumautil.__salt__, {'cmd.run_all': mock}):
            out = sumautil.get_kernel_live_version()
            assert type(out) == dict
            assert 'mgr_kernel_live_version' in out
            assert out['mgr_kernel_live_version'] == 'kgraft_patch_1_2_2'

        mock = MagicMock(side_effect=[{ 'retcode': 0, 'stdout': 'ready' },
                                      { 'retcode': 0, 'stdout': mockery.get_test_data('livepatching-2.sample') }
                                     ]);
        with patch.dict(sumautil.__salt__, {'cmd.run_all': mock}):
            out = sumautil.get_kernel_live_version()
            assert type(out) == dict
            assert 'mgr_kernel_live_version' in out
            assert out['mgr_kernel_live_version'] == 'kgraft_patch_2_2_1'

    with patch('salt.utils.path.which_bin', MagicMock(return_value=None)):
        out = sumautil.get_kernel_live_version()
        assert out is None
