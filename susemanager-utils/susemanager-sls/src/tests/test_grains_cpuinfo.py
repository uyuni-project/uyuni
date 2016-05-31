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

