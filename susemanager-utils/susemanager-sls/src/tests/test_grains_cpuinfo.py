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


def test_cpusockets_lscpu():
    '''
    Test lscpu sub in cpusockets function.

    :return:
    '''
    lscpu_out = """
# The following is the parsable format, which can be fed to other
# programs. Each different item in every column has an unique ID
# starting from zero.
# CPU,Core,Socket,Node,,L1d,L1i,L2,L3
0,0,0,0,,0,0,0,0
1,0,0,0,,0,0,0,0
2,1,0,0,,1,1,1,0
3,1,0,0,,1,1,1,0
    """

    cpuinfo.log = MagicMock()
    with patch('salt.utils.which_bin', MagicMock(return_value="/bogus/path")):
        with patch.dict(cpuinfo.__salt__, {'cmd.run_all': MagicMock(return_value={'retcode': 0, 'stdout': lscpu_out})}):
            out = cpuinfo._lscpu()
            assert type(out) == dict
            assert 'cpusockets' in out
            assert out['cpusockets'] == 1

