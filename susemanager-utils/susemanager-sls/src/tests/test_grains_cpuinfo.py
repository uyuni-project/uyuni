'''
Author: bo@suse.de
'''

from mock import MagicMock, patch
import mockery
mockery.setup_environment()

from ..grains import cpuinfo


def test_total_num_cpus():
    '''
    Test total_num_cpus function.

    :return:
    '''
    os_listdir = ['cpu0', 'cpu1', 'cpu2', 'cpu3', 'cpufreq', 'cpuidle', 'power', 'modalias',
                  'kernel_max', 'possible', 'online', 'offline', 'isolated', 'uevent',
                  'intel_pstate', 'microcode', 'present']

    with patch('os.path.exists', MagicMock(return_value=True)):
        with patch('os.listdir', MagicMock(return_value=os_listdir)):
            cpus = cpuinfo.total_num_cpus()
            assert type(cpus) == dict
            assert 'total_num_cpus' in cpus
            assert cpus['total_num_cpus'] == 4


def test_cpusockets_parse_cpuinfo():
    '''
    Test parse_cpuinfo sub in cpusockets function.

    :return:
    '''
    sample = mockery.get_test_data('cpuinfo.sample')

def test_cpusockets_lscpu():
    '''
    Test lscpu sub in cpusockets function.

    :return:
    '''
    sample = mockery.get_test_data('lscpu.sample')
    cpuinfo.log = MagicMock()
    with patch('salt.utils.which_bin', MagicMock(return_value="/bogus/path")):
        with patch.dict(cpuinfo.__salt__, {'cmd.run_all': MagicMock(return_value={'retcode': 0, 'stdout': sample})}):
            out = cpuinfo._lscpu()
            assert type(out) == dict
            assert 'cpusockets' in out
            assert out['cpusockets'] == 1

