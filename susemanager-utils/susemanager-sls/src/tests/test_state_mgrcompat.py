# -*- coding: utf-8 -*-
"""
Test custom wrapper for "module.run" state module.

Author: Pablo Suárez Herńandez <psuarezhernandez@suse.com>
"""

from unittest.mock import MagicMock, patch
from . import mockery

mockery.setup_environment()

# pylint: disable-next=wrong-import-position
from ..states import mgrcompat

TAILORED_MODULE_RUN_KWARGS = {
    "service.running": [{"text": "superseded", "name": "salt-minion"}, {"foo": "bar"}]
}
MGRCOMPAT_MODULE_RUN_KWARGS = {
    "name": "service.running",
    "text": "superseded",
    "m_name": "salt-minion",
    "kwargs": {"foo": "bar"},
}

mgrcompat.log = MagicMock()
mgrcompat.OrderedDict = dict
mgrcompat.__opts__ = {}
mgrcompat.__grains__ = {}
mgrcompat.__states__ = {}


def test_module_run_on_phosphorous():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [3005, None, None, None]}
    ), patch.dict(mgrcompat.__states__, {"module.run": mock}):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**MGRCOMPAT_MODULE_RUN_KWARGS)


def test_module_run_on_silicon():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [3004, None, None, None]}
    ), patch.dict(mgrcompat.__states__, {"module.run": mock}):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**MGRCOMPAT_MODULE_RUN_KWARGS)


def test_module_run_on_silicon_use_superseded():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [3004, None, None, None]}
    ), patch.dict(mgrcompat.__opts__, {"use_superseded": ["module.run"]}), patch.dict(
        mgrcompat.__states__, {"module.run": mock}
    ):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**TAILORED_MODULE_RUN_KWARGS)


def test_module_run_on_aluminum():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [3003, None, None, None]}
    ), patch.dict(mgrcompat.__states__, {"module.run": mock}):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**MGRCOMPAT_MODULE_RUN_KWARGS)


def test_module_run_on_aluminum_use_superseded():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [3003, None, None, None]}
    ), patch.dict(mgrcompat.__opts__, {"use_superseded": ["module.run"]}), patch.dict(
        mgrcompat.__states__, {"module.run": mock}
    ):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**TAILORED_MODULE_RUN_KWARGS)


def test_module_run_on_magnesium():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [3002, None, None, None]}
    ), patch.dict(mgrcompat.__states__, {"module.run": mock}):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**MGRCOMPAT_MODULE_RUN_KWARGS)


def test_module_run_on_magnesium_use_superseded():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [3002, None, None, None]}
    ), patch.dict(mgrcompat.__opts__, {"use_superseded": ["module.run"]}), patch.dict(
        mgrcompat.__states__, {"module.run": mock}
    ):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**TAILORED_MODULE_RUN_KWARGS)


def test_module_run_on_sodium():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [3001, None, None, None]}
    ), patch.dict(mgrcompat.__states__, {"module.run": mock}):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**MGRCOMPAT_MODULE_RUN_KWARGS)


def test_module_run_on_sodium_use_superseded():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [3001, None, None, None]}
    ), patch.dict(mgrcompat.__opts__, {"use_superseded": ["module.run"]}), patch.dict(
        mgrcompat.__states__, {"module.run": mock}
    ):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**TAILORED_MODULE_RUN_KWARGS)


def test_module_run_on_neon():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [3000, None, None, None]}
    ), patch.dict(mgrcompat.__states__, {"module.run": mock}):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**MGRCOMPAT_MODULE_RUN_KWARGS)


def test_module_run_on_neon_use_superseded():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [3000, None, None, None]}
    ), patch.dict(mgrcompat.__opts__, {"use_superseded": ["module.run"]}), patch.dict(
        mgrcompat.__states__, {"module.run": mock}
    ):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**TAILORED_MODULE_RUN_KWARGS)


def test_module_run_on_2019_2_0_use_superseded():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [2019, 2, 0, 0]}
    ), patch.dict(mgrcompat.__opts__, {"use_superseded": ["module.run"]}), patch.dict(
        mgrcompat.__states__, {"module.run": mock}
    ):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**TAILORED_MODULE_RUN_KWARGS)


def test_module_run_on_2019_2_0_without_use_superseded():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [2019, 2, 0, 0]}
    ), patch.dict(mgrcompat.__states__, {"module.run": mock}):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**MGRCOMPAT_MODULE_RUN_KWARGS)


def test_module_run_on_2016_11_4():
    mock = MagicMock(return_value={"changes": {"service.running": "foobar"}})
    with patch.dict(
        mgrcompat.__grains__, {"saltversioninfo": [2016, 11, 4, 0]}
    ), patch.dict(mgrcompat.__states__, {"module.run": mock}):
        mgrcompat.module_run(**MGRCOMPAT_MODULE_RUN_KWARGS)
        mock.assert_called_once_with(**MGRCOMPAT_MODULE_RUN_KWARGS)
