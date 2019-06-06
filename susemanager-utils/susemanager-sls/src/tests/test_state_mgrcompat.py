#-*- coding: utf-8 -*-
'''
Test custom wrapper for "module.run" state module.

Author: Pablo Suárez Herńandez <psuarezhernandez@suse.com>
'''

from mock import MagicMock, patch
from . import mockery
mockery.setup_environment()

from ..states import mgrcompat

MGRCOMPAT_MODULE_RUN_KWARGS = {'name': 'new_syntax', 'test.echo': [{'text': 'superseded'}]}
TAILORED_MODULE_RUN_KWARGS = {'name': 'test.echo', 'text': 'superseded'}

mgrcompat.log = MagicMock()
mgrcompat.__salt__ = {}
mgrcompat.__grains__ = {}
mgrcompat.__opts__ = {}


def test_module_run_on_2020_1_1():
    mock = MagicMock(return_value={'foo': 'bar'})
    with patch.dict(mgrcompat.__salt__, {'state.single': mock}):
        with patch.dict(mgrcompat.__grains__, {'saltversioninfo': [2020, 1, 1, 1]}):
            mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
            mock.assert_called_once_with('module.run', **MGRCOMPAT_MODULE_RUN_KWARGS)

def test_module_run_on_2019_10_1():
    mock = MagicMock(return_value={'foo': 'bar'})
    with patch.dict(mgrcompat.__salt__, {'state.single': mock}):
        with patch.dict(mgrcompat.__grains__, {'saltversioninfo': [2019, 10, 1, 1]}):
            mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
            mock.assert_called_once_with('module.run', **MGRCOMPAT_MODULE_RUN_KWARGS)

def test_module_run_on_2019_2_0_use_superseded():
    mock = MagicMock(return_value={'foo': 'bar'})
    with patch.dict(mgrcompat.__salt__, {'state.single': mock}):
        with patch.dict(mgrcompat.__grains__, {'saltversioninfo': [2019, 2, 0, 0]}):
            with patch.dict(mgrcompat.__opts__, {'use_superseded': ['module.run']}):
                mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
                mock.assert_called_once_with('module.run', **MGRCOMPAT_MODULE_RUN_KWARGS)

def test_module_run_on_2019_2_0_without_use_superseded():
    mock = MagicMock(return_value={'foo': 'bar'})
    with patch.dict(mgrcompat.__salt__, {'state.single': mock}):
        with patch.dict(mgrcompat.__grains__, {'saltversioninfo': [2019, 2, 0, 0]}):
            with patch.dict(mgrcompat.__opts__, {}):
                mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
                mock.assert_called_once_with('module.run', **TAILORED_MODULE_RUN_KWARGS)

def test_module_run_on_2016_11_4():
    mock = MagicMock(return_value={'foo': 'bar'})
    with patch.dict(mgrcompat.__salt__, {'state.single': mock}):
        with patch.dict(mgrcompat.__grains__, {'saltversioninfo': [2016, 11, 4, 0]}):
           mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
           mock.assert_called_once_with('module.run', **TAILORED_MODULE_RUN_KWARGS)
