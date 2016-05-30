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


def test_exportdb():
    '''
    Test udevdb.exportdb method.

    :return:
    '''
    udev_data = """
P: /devices/LNXSYSTM:00/LNXPWRBN:00
E: DEVPATH=/devices/LNXSYSTM:00/LNXPWRBN:00
E: DRIVER=button
E: MODALIAS=acpi:LNXPWRBN:
E: SUBSYSTEM=acpi

P: /devices/LNXSYSTM:00/LNXPWRBN:00/input/input2
E: DEVPATH=/devices/LNXSYSTM:00/LNXPWRBN:00/input/input2
E: EV=3
E: ID_FOR_SEAT=input-acpi-LNXPWRBN_00
E: ID_INPUT=1
E: ID_INPUT_KEY=1
E: ID_PATH=acpi-LNXPWRBN:00
E: ID_PATH_TAG=acpi-LNXPWRBN_00
E: KEY=10000000000000 0
E: MODALIAS=input:b0019v0000p0001e0000-e0,1,k74,ramlsfw
E: NAME="Power Button"
E: PHYS="LNXPWRBN/button/input0"
E: PRODUCT=19/0/1/0
E: PROP=0
E: SUBSYSTEM=input
E: TAGS=:seat:
E: USEC_INITIALIZED=2010022

P: /devices/LNXSYSTM:00/LNXPWRBN:00/input/input2/event2
N: input/event2
E: BACKSPACE=guess
E: DEVNAME=/dev/input/event2
E: DEVPATH=/devices/LNXSYSTM:00/LNXPWRBN:00/input/input2/event2
E: ID_INPUT=1
E: ID_INPUT_KEY=1
E: ID_PATH=acpi-LNXPWRBN:00
E: ID_PATH_TAG=acpi-LNXPWRBN_00
E: MAJOR=13
E: MINOR=66
E: SUBSYSTEM=input
E: TAGS=:power-switch:
E: USEC_INITIALIZED=2076101
E: XKBLAYOUT=us
E: XKBMODEL=pc105
    """
    out = [{'P': '/devices/LNXSYSTM:00/LNXPWRBN:00',
            'E': {'MODALIAS': 'acpi:LNXPWRBN:',
                  'SUBSYSTEM': 'acpi',
                  'DRIVER': 'button',
                  'DEVPATH': '/devices/LNXSYSTM:00/LNXPWRBN:00'}},
           {'P': '/devices/LNXSYSTM:00/LNXPWRBN:00/input/input2',
            'E': {'SUBSYSTEM': 'input',
                  'PRODUCT': '19/0/1/0',
                  'PHYS': '"LNXPWRBN/button/input0"',
                  'NAME': '"Power Button"',
                  'ID_INPUT': 1,
                  'DEVPATH': '/devices/LNXSYSTM:00/LNXPWRBN:00/input/input2',
                  'MODALIAS': 'input:b0019v0000p0001e0000-e0,1,k74,ramlsfw',
                  'ID_PATH_TAG': 'acpi-LNXPWRBN_00',
                  'TAGS': ':seat:',
                  'PROP': 0,
                  'ID_FOR_SEAT': 'input-acpi-LNXPWRBN_00',
                  'KEY': '10000000000000 0',
                  'USEC_INITIALIZED': 2010022,
                  'ID_PATH': 'acpi-LNXPWRBN:00',
                  'EV': 3,
                  'ID_INPUT_KEY': 1}},
           {'P': '/devices/LNXSYSTM:00/LNXPWRBN:00/input/input2/event2',
            'E': {'SUBSYSTEM': 'input',
                  'XKBLAYOUT': 'us',
                  'MAJOR': 13,
                  'ID_INPUT': 1,
                  'DEVPATH': '/devices/LNXSYSTM:00/LNXPWRBN:00/input/input2/event2',
                  'ID_PATH_TAG': 'acpi-LNXPWRBN_00',
                  'DEVNAME': '/dev/input/event2',
                  'TAGS': ':power-switch:',
                  'BACKSPACE': 'guess',
                  'MINOR': 66,
                  'USEC_INITIALIZED': 2076101,
                  'ID_PATH': 'acpi-LNXPWRBN:00',
                  'XKBMODEL': 'pc105',
                  'ID_INPUT_KEY': 1},
            'N': 'input/event2'}]

    with patch.dict(udevdb.__salt__, {'cmd.run_all': MagicMock(return_value={'retcode': 0, 'stdout': udev_data})}):
        data = udevdb.exportdb()
        assert data == filter(None, data)

        for d_idx, d_section in enumerate(out):
            assert out[d_idx]['P'] == d_section['P']
            assert out[d_idx].get('N') == d_section.get('N')
            for key, value in d_section['E'].items():
                assert out[d_idx]['E'][key] == value
