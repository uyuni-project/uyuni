'''
Author: cbosdonnat@suse.com
'''

from mock import MagicMock, patch, call

from ..states import virthelper
from . import mockery
mockery.setup_environment()

# Mock globals
virthelper.log = MagicMock()
virthelper.__salt__ = {}
virthelper.__states__ = {}

def test_single_vm():
    '''
    Test the virthelper.single_vm() state
    '''
    caps = {
        "host": {
            "topology": {
                "cells": [
                    {
                        "id": 0,
                        "memory": "905532 KiB",
                        "distances": {"0": 10, "1": 20, "2": 20, "3": 20},
                        "cpus": [
                            {"id": 0, "socket_id": 0, "core_id": 0, "siblings": "0-1"},
                            {"id": 1, "socket_id": 0, "core_id": 0, "siblings": "0-1"},
                            {"id": 2, "socket_id": 0, "core_id": 1, "siblings": "2-3"},
                            {"id": 3, "socket_id": 0, "core_id": 1, "siblings": "2-3"}
                        ]
                    },
                    {
                        "id": 1,
                        "memory": "909096 KiB",
                        "distances": {"0": 20, "1": 10, "2": 20, "3": 20},
                        "cpus": [
                            {"id": 4, "socket_id": 1, "core_id": 0, "siblings": "4-5"},
                            {"id": 5, "socket_id": 1, "core_id": 0, "siblings": "4-5"},
                            {"id": 6, "socket_id": 1, "core_id": 1, "siblings": "6-7"},
                            {"id": 7, "socket_id": 1, "core_id": 1, "siblings": "6-7"}
                        ]
                    },
                    {
                        "id": 2,
                        "memory": "908072 KiB",
                        "distances": {"0": 20, "1": 20, "2": 10, "3": 20},
                        "cpus": [
                            {"id": 8, "socket_id": 2, "core_id": 0, "siblings": "8-9"},
                            {"id": 9, "socket_id": 2, "core_id": 0, "siblings": "8-9"},
                            {"id": 10, "socket_id": 2, "core_id": 1, "siblings": "10-11"},
                            {"id": 11, "socket_id": 2, "core_id": 1, "siblings": "10-11"}
                        ]
                    },
                    {
                        "id": 3,
                        "memory": "808460 KiB",
                        "distances": {"0": 20, "1": 20, "2": 20, "3": 10},
                        "cpus": [
                            {"id": 12, "socket_id": 3, "core_id": 0, "siblings": "12-13"},
                            {"id": 13, "socket_id": 3, "core_id": 0, "siblings": "12-13"},
                            {"id": 14, "socket_id": 3, "core_id": 1, "siblings": "14-15"},
                            {"id": 15, "socket_id": 3, "core_id": 1, "siblings": "14-15"}
                        ]
                    }
                ]
            }
        }
    }

    with patch.dict(virthelper.__salt__, {
        'virt.capabilities': MagicMock(return_value=caps),
        'virt.node_info': MagicMock(return_value={'phymemory': 3448}),
    }):
        expected_result = {"name": "testvm", "changes":{}, "result": True}
        defined_mock = MagicMock(return_value=expected_result)
        with patch.dict(virthelper.__states__, {'virt.defined': defined_mock}):
            result = virthelper.single_vm(
                "testvm",
                disks=[{"name": "system", "pool": "default", "source_file": "system_disk.qcow2"}],
                interfaces=[{"name": "eth0", "type": "default", "source": "default"}],
                graphics={"type": "vnc"},
                boot_dev="network hd",
                boot={"efi": True},
                serials=[{"type": "pty"}],
                consoles=[{"type": "pty"}],
                stop_on_reboot=True,
                utc_clock=False,
            )
            assert expected_result == result

            calls = [
                call(
                    name="testvm",
                    cpu={
                        'placement': 'static',
                        'maximum': 16,
                        'topology': {"sockets": 4, "cores": 2, "threads": 2},
                        'mode': 'host-passthrough',
                        'check': 'none',
                        'features': {'rdtscp': 'require', 'invtsc': 'require', 'x2apic': 'require'},
                        'tuning': {
                            'vcpupin': {
                                0: "0-1",
                                1: "0-1",
                                2: "2-3",
                                3: "2-3",
                                4: "4-5",
                                5: "4-5",
                                6: "6-7",
                                7: "6-7",
                                8: "8-9",
                                9: "8-9",
                                10: "10-11",
                                11: "10-11",
                                12: "12-13",
                                13: "12-13",
                                14: "14-15",
                                15: "14-15",
                            },
                        },
                        'numa': {
                            0: {
                                'cpus': '0,1,2,3',
                                'memory': '775.75 MiB',
                                'distances': {"0": 10, "1": 20, "2": 20, "3": 20},
                            },
                            1: {
                                'cpus': '4,5,6,7',
                                'memory': '775.75 MiB',
                                'distances': {"0": 20, "1": 10, "2": 20, "3": 20},
                            },
                            2: {
                                'cpus': '8,9,10,11',
                                'memory': '775.75 MiB',
                                'distances': {"0": 20, "1": 20, "2": 10, "3": 20},
                            },
                            3: {
                                'cpus': '12,13,14,15',
                                'memory': '775.75 MiB',
                                'distances': {"0": 20, "1": 20, "2": 20, "3": 10},
                            },
                        },
                    },
                    numatune={
                        'memory': {
                            'mode': 'strict',
                            'nodeset': '0,1,2,3',
                        },
                        'memnodes': {
                            0: {'mode': 'strict', 'nodeset': 0},
                            1: {'mode': 'strict', 'nodeset': 1},
                            2: {'mode': 'strict', 'nodeset': 2},
                            3: {'mode': 'strict', 'nodeset': 3},
                        },
                    },
                    update=True,
                    hypervisor_features={'kvm-hint-dedicated': True},
                    mem={
                        "boot": "3103 MiB",
                        "current": "3103 MiB",
                        "nosharepages": True,
                        'hugepages': [{'size': '1g'}],
                    },
                    clock={
                        "utc": False,
                        "timers": {
                            "rtc": {"tickpolicy": "catchup"},
                            "pit": {"tickpolicy": "catchup"},
                            "hpet": {"present": False},
                        }
                    },
                    seed=False,
                    disks=[{"name": "system", "pool": "default", "source_file": "system_disk.qcow2"}],
                    interfaces=[{"name": "eth0", "type": "default", "source": "default"}],
                    graphics={"type": "vnc"},
                    boot_dev="network hd",
                    boot={"efi": True},
                    serials=[{"type": "pty"}],
                    consoles=[{"type": "pty"}],
                    stop_on_reboot=True,
                )
            ]
            defined_mock.assert_has_calls(calls)
