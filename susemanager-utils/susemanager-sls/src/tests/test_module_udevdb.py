'''
Author: Bo Maryniuk <bo@suse.de>
'''

from mock import MagicMock, patch
from . import mockery
mockery.setup_environment()

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
    assert udevdb.normalize(data) == {'foo': 'bar', 'some': 'data', 'key': ['value', 'here']}


def test_exportdb():
    '''
    Test udevdb.exportdb method.

    :return:
    '''
    udev_data = mockery.get_test_data('udev.sample')
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
            'N': 'input/event2'},
           {'P': '/devices/pci0000:00/0000:00:01.1/ata1/host0/target0:0:0/0:0:0:0',
            'E': {'MODALIAS': 'scsi:t-0x00',
                  'SUBSYSTEM': 'scsi',
                  'DEVTYPE': 'scsi_device',
                  'DRIVER': 'sd',
                  'DEVPATH': '/devices/pci0000:00/0000:00:01.1/ata1/host0/target0:0:0/0:0:0:0'
                  },
            'X-Mgr': {'SCSI_SYS_TYPE': '0'}},
           ]

    with patch.dict(udevdb.__salt__, {'cmd.run_all': MagicMock(side_effect=[{'retcode': 0, 'stdout': udev_data},
                                                                            {'retcode': 0, 'stdout': '0'}])}):
        data = udevdb.exportdb()
        assert data == filter(None, data)

        for d_idx, d_section in enumerate(data):
            assert out[d_idx]['P'] == d_section['P']
            assert out[d_idx].get('N') == d_section.get('N')
            assert out[d_idx].get('X-Mgr') == d_section.get('X-Mgr')
            for key, value in d_section['E'].items():
                assert out[d_idx]['E'][key] == value
