'''
Author: Michael Calmer <mc@suse.com>
'''
import sys
import os
import shutil
from mock import MagicMock, patch
sys.modules['libvirt'] = MagicMock()
from ..beacons import virtpoller

virtpoller.__context__ = dict()

CACHE_FILE = '/tmp/virt_state-test.cache'

def test_virtual():
    '''
    Test virtual function.
    '''
    #with patch(virtpoller.HAS_LIBVIRT, True):
    assert virtpoller.__virtual__() == virtpoller.__virtualname__


def test_validate():
    '''
    Test validate() function
    '''
    res, msg = virtpoller.validate({'cache_file': '/bogus/path',
                                    'expire_time': 2})
    assert res is True
    assert msg == 'Configuration validated'


def test_beacon():
    '''
    Test beacon functionality.
    First run without cache file. All systems are "new" and should be reported.
    '''
    domain = MagicMock()
    domain.info = MagicMock(name='info')
    domain.info.return_value = [0, 1024, 1024, 2, 30]
    domain.name = MagicMock(name='name')
    domain.name.return_value = 'testvm'

    conn = MagicMock()
    conn.listAllDomains = MagicMock(name='listAllDomains')
    conn.listAllDomains.return_value = [domain]

    if os.path.exists(CACHE_FILE):
        os.unlink(CACHE_FILE)

    with patch.object(virtpoller, 'libvirt', MagicMock(return_value=True)):
        with patch.object(virtpoller.libvirt, 'openReadOnly', MagicMock(return_value=conn)):
            with patch.object(virtpoller.binascii, 'hexlify', MagicMock(return_value=5)):
                ret = virtpoller.beacon({'cache_file': CACHE_FILE,
                                         'expire_time': 2})
    assert isinstance(ret, list)
    assert isinstance(ret[0], dict)
    assert sorted(ret[0].keys()) == ['plan']
    assert ret[0]['plan'][0]['event_type'] == 'exists'
    assert 'guest_properties' in ret[0]['plan'][0]
    data = ret[0]['plan'][0]['guest_properties']
    assert data['name'] == 'testvm'
    assert data['virt_type'] == 'para_virtualized'
    assert data['state'] == 'running'
    assert data['vcpus'] == 2
    assert data['memory_size'] == '1024'
    assert data['uuid'] == 5

def test_beacon_update():
    '''
    Test beacon functionality. Second run with cache file.
    Nothing has changed so the return value of the function
    Should be an empty list
    '''
    domain = MagicMock()
    domain.info = MagicMock(name='info')
    domain.info.return_value = [0, 1024, 1024, 2, 30]
    domain.name = MagicMock(name='name')
    domain.name.return_value = 'testvm'

    conn = MagicMock()
    conn.listAllDomains = MagicMock(name='listAllDomains')
    conn.listAllDomains.return_value = [domain]

    if os.path.exists(CACHE_FILE):
        os.unlink(CACHE_FILE)
    shutil.copyfile(os.path.sep.join([os.path.abspath(''), 'data', 'virt_state-test.initcache']), CACHE_FILE)

    with patch.object(virtpoller, 'libvirt', MagicMock(return_value=True)):
        with patch.object(virtpoller.libvirt, 'openReadOnly', MagicMock(return_value=conn)):
            with patch.object(virtpoller.binascii, 'hexlify', MagicMock(return_value=5)):
                ret = virtpoller.beacon({'cache_file': CACHE_FILE,
                                         'expire_time': 2})
    assert isinstance(ret, list)
    print("%s" % ret)
    assert len(ret) == 0

def test_beacon_change():
    '''
    Test beacon functionality. Another run with cache file.
    There are changes so it should report the new values.
    '''
    domain = MagicMock()
    domain.info = MagicMock(name='info')
    domain.info.return_value = [4, 1024, 2048, 2, 30]
    domain.name = MagicMock(name='name')
    domain.name.return_value = 'testvm'

    conn = MagicMock()
    conn.listAllDomains = MagicMock(name='listAllDomains')
    conn.listAllDomains.return_value = [domain]

    if os.path.exists(CACHE_FILE):
        os.unlink(CACHE_FILE)
    shutil.copyfile(os.path.sep.join([os.path.abspath(''), 'data', 'virt_state-test.initcache']), CACHE_FILE)

    with patch.object(virtpoller, 'libvirt', MagicMock(return_value=True)):
        with patch.object(virtpoller.libvirt, 'openReadOnly', MagicMock(return_value=conn)):
            with patch.object(virtpoller.binascii, 'hexlify', MagicMock(return_value=5)):
                ret = virtpoller.beacon({'cache_file': CACHE_FILE,
                                         'expire_time': 2})
    assert isinstance(ret, list)
    assert isinstance(ret[0], dict)
    assert sorted(ret[0].keys()) == ['plan']
    assert ret[0]['plan'][0]['event_type'] == 'exists'
    assert 'guest_properties' in ret[0]['plan'][0]
    data = ret[0]['plan'][0]['guest_properties']
    assert data['name'] == 'testvm'
    assert data['virt_type'] == 'para_virtualized'
    assert data['state'] == 'stopped'
    assert data['vcpus'] == 2
    assert data['memory_size'] == '2048'
    assert data['uuid'] == 5

def test_beacon_remove():
    '''
    Test beacon functionality. Another run with cache file.
    The former host is not available anymore. Report should
    say "removed"
    '''

    conn = MagicMock()
    conn.listAllDomains = MagicMock(name='listAllDomains')
    conn.listAllDomains.return_value = []

    if os.path.exists(CACHE_FILE):
        os.unlink(CACHE_FILE)
    shutil.copyfile(os.path.sep.join([os.path.abspath(''), 'data', 'virt_state-test.initcache']), CACHE_FILE)

    with patch.object(virtpoller, 'libvirt', MagicMock(return_value=True)):
        with patch.object(virtpoller.libvirt, 'openReadOnly', MagicMock(return_value=conn)):
            with patch.object(virtpoller.binascii, 'hexlify', MagicMock(return_value=5)):
                ret = virtpoller.beacon({'cache_file': CACHE_FILE,
                                         'expire_time': 2})
    assert isinstance(ret, list)
    assert isinstance(ret[0], dict)
    assert sorted(ret[0].keys()) == ['plan']
    assert ret[0]['plan'][0]['event_type'] == 'removed'
    assert 'guest_properties' in ret[0]['plan'][0]
    data = ret[0]['plan'][0]['guest_properties']
    assert data['name'] == 'testvm'
    assert data['virt_type'] == 'para_virtualized'
    assert data['state'] == 'running'
    assert data['vcpus'] == 2
    assert data['memory_size'] == '1024'
    assert data['uuid'] == 5

