'''
Author: bo@suse.de
'''

from mock import MagicMock, patch, mock_open
from . import mockery
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


def test_cpusockets_dmidecode():
    '''
    Test dmidecode sub in cpusockets function.

    :return:
    '''

    sample = mockery.get_test_data('dmidecode.sample')
    cpuinfo.log = MagicMock()
    with patch('salt.utils.path.which_bin', MagicMock(return_value="/bogus/path")):
        with patch.dict(cpuinfo.__salt__, {'cmd.run_all': MagicMock(return_value={'retcode': 0, 'stdout': sample})}):
            out = cpuinfo._dmidecode([])
            assert type(out) == dict
            assert 'cpusockets' in out
            assert out['cpusockets'] == 1


def test_cpusockets_parse_cpuinfo():
    '''
    Test parse_cpuinfo sub in cpusockets function.

    :return:
    '''
    cpuinfo.log = MagicMock()
    # cpuinfo parser is not applicable for non-Intel architectures, so should return nothing.
    for sample_name in ['cpuinfo.s390.sample', 'cpuinfo.ppc64le.sample']:
        with patch('os.access', MagicMock(return_value=True)):
            with patch.object(cpuinfo, 'open', mock_open(read_data=mockery.get_test_data(sample_name)), create=True):
                assert cpuinfo._parse_cpuinfo([]) is None

    with patch('os.access', MagicMock(return_value=True)):
        with patch.object(cpuinfo, 'open', mock_open(read_data=mockery.get_test_data('cpuinfo.sample')), create=True):
            out = cpuinfo._parse_cpuinfo([])
            assert type(out) == dict
            assert 'cpusockets' in out
            assert out['cpusockets'] == 1


def test_cpusockets_lscpu():
    '''
    Test lscpu sub in cpusockets function.

    :return:
    '''
    for fn_smpl in ['lscpu.ppc64le.sample', 'lscpu.s390.sample', 'lscpu.sample']:
        cpuinfo.log = MagicMock()
        with patch('salt.utils.path.which_bin', MagicMock(return_value="/bogus/path")):
            with patch.dict(cpuinfo.__salt__,
                            {'cmd.run_all': MagicMock(return_value={'retcode': 0,
                                                                    'stdout': mockery.get_test_data(fn_smpl)})}):
                out = cpuinfo._lscpu([])
                assert type(out) == dict
                assert 'cpusockets' in out
                assert out['cpusockets'] == 1

