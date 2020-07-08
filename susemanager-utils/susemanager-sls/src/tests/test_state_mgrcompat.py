#-*- coding: utf-8 -*-
'''
Test custom wrapper for "module.run" state module.

Author: Pablo Suárez Herńandez <psuarezhernandez@suse.com>
'''

from mock import MagicMock, patch

from ..states import mgrcompat

TAILORED_MODULE_RUN_KWARGS = {'service.running': [{'text': 'superseded', 'name': 'salt-minion'}]}
MGRCOMPAT_MODULE_RUN_KWARGS = {'name': 'service.running', 'text': 'superseded', 'm_name': 'salt-minion'}

mgrcompat.log = MagicMock()
mgrcompat.module = MagicMock()
mgrcompat.__salt__ = {'module.run': True}
mgrcompat.__opts__ = {}
mgrcompat.__pillar__ = {}
mgrcompat.__grains__ = {}
mgrcompat.__context__ = {}
mgrcompat.__utils__ = {}


def test_module_run_on_sodium():
    mock = MagicMock(return_value={'changes': {'service.running': 'foobar'}})
    mgrcompat.module.run = mock
    with patch.dict(mgrcompat.__grains__, {'saltversioninfo': [2020, 1, 1, 1]}):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**TAILORED_MODULE_RUN_KWARGS)

def test_module_run_on_neon():
    mock = MagicMock(return_value={'changes': {'service.running': 'foobar'}})
    mgrcompat.module.run = mock
    with patch.dict(mgrcompat.__grains__, {'saltversioninfo': [2019, 10, 1, 1]}):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**MGRCOMPAT_MODULE_RUN_KWARGS)

def test_module_run_on_neon_use_superseded():
    mock = MagicMock(return_value={'changes': {'service.running': 'foobar'}})
    mgrcompat.module.run = mock
    with patch.dict(mgrcompat.__grains__, {'saltversioninfo': [2019, 10, 1, 1]}):
        with patch.dict(mgrcompat.__opts__, {'use_superseded': ['module.run']}):
            mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
            mock.assert_called_once_with(**TAILORED_MODULE_RUN_KWARGS)

def test_module_run_on_2019_2_0_use_superseded():
    mock = MagicMock(return_value={'changes': {'service.running': 'foobar'}})
    mgrcompat.module.run = mock
    with patch.dict(mgrcompat.__grains__, {'saltversioninfo': [2019, 2, 0, 0]}):
        with patch.dict(mgrcompat.__opts__, {'use_superseded': ['module.run']}):
            mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
            mock.assert_called_once_with(**TAILORED_MODULE_RUN_KWARGS)

def test_module_run_on_2019_2_0_without_use_superseded():
    mock = MagicMock(return_value={'changes': {'service.running': 'foobar'}})
    mgrcompat.module.run = mock
    with patch.dict(mgrcompat.__grains__, {'saltversioninfo': [2019, 2, 0, 0]}):
        with patch.dict(mgrcompat.__opts__, {}):
            mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
            mock.assert_called_once_with(**MGRCOMPAT_MODULE_RUN_KWARGS)

def test_module_run_on_2016_11_4():
    mock = MagicMock(return_value={'changes': {'service.running': 'foobar'}})
    mgrcompat.module.run = mock
    with patch.dict(mgrcompat.__grains__, {'saltversioninfo': [2016, 11, 4, 0]}):
       mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
       mock.assert_called_once_with(**MGRCOMPAT_MODULE_RUN_KWARGS)
