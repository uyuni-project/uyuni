'''
Virtualization helper states
'''

import logging
import itertools

log = logging.getLogger(__name__)

__virtualname__ = 'virthelper'

def __virtual__():
    if "virt.node_info" not in __salt__:
        return (False, "virt module could not be loaded")
    return __virtualname__

def single_vm(name,
              disks=None,
              interfaces=None,
              graphics=None,
              boot_dev=None,
              boot=None,
              serials=None,
              consoles=None,
              stop_on_reboot=False,
              utc_clock=True):
    '''
    Create a big VM with guests CPU mapped to the host CPU topology.

    name
        the name of the virtual machine
    '''
    caps = __salt__['virt.capabilities']()
    cells = caps['host']['topology']['cells']
    cpus = [cell['cpus'] for cell in cells]
    cpus = [cpu for sublist in cpus for cpu in sublist]

    cpu_topology = {
        'sockets': len({c['socket_id'] for c in cpus}),
        'cores': len({c['core_id'] for c in cpus}),
        'threads': {
            len(list(g))
            for k, g
            in itertools.groupby(cpus, lambda c: '{}-{}'.format(c['socket_id'], c['core_id']))
        }.pop()
    }

    node_info = __salt__['virt.node_info']()
    vm_memory = int(node_info['phymemory'] * 0.9)

    numa = {
        c['id']: {
            'cpus': ','.join([str(cpu['id']) for cpu in c['cpus']]),
            'memory': c['memory'],
            'distances': c['distances'],
        }
        for c
        in cells
    }
    log.debug(numa)

    # Call the virt.defined state
    return __states__['virt.defined'](
        name=name,
        cpu={
            'placement': 'static',
            'maximum': cpu_topology['sockets'] * cpu_topology["cores"] * cpu_topology['threads'],
            'topology': cpu_topology,
            'mode': 'host-passthrough',
            'check': 'none',
            'features': {
                'rdtscp': 'require',
                'invtsc': 'require',
                'x2apic': 'require',
            },
            'tuning': {
                'vcpupin': {cpu['id']: cpu['siblings'] for cpu in cpus},
            },
            'numa': {
                c['id']: {
                    'cpus': ','.join([str(cpu['id']) for cpu in c['cpus']]),
                    'memory': str(vm_memory / len(cells)) + ' MiB',
                    'distances': c['distances'],
                }
                for c
                in cells
            },
        },
        numatune={
            'memory': {
                'mode': 'strict',
                'nodeset': ','.join([str(cell['id']) for cell in cells]),
            },
            'memnodes': {
                cell['id']: {'mode': 'strict', 'nodeset': cell['id']}
                for cell
                in cells
            },
        },
        mem={
            'boot': str(vm_memory) + ' MiB',
            'current': str(vm_memory) + ' MiB',
            'nosharepages': True,
            'hugepages': [{'size': '1g'}],
        },
        update=True,
        hypervisor_features={'kvm-hint-dedicated': True},
        clock={
            'utc': utc_clock,
            'timers': {
                'rtc': {'tickpolicy': 'catchup'},
                'pit': {'tickpolicy': 'catchup'},
                'hpet': {'present': False},
            },
        },
        seed=False,
        disks=disks,
        interfaces=interfaces,
        graphics=graphics,
        boot_dev=boot_dev,
        boot=boot,
        serials=serials,
        consoles=consoles,
        stop_on_reboot=stop_on_reboot,
    )
