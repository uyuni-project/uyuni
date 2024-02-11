# -*- coding: utf-8 -*-
"""
Test for mgrutils states
"""

from unittest.mock import MagicMock, patch
from . import mockery

mockery.setup_environment()

# pylint: disable-next=wrong-import-position
from ..states import mgrutils


mgrutils.__opts__ = {"test": False}
mgrutils.__grains__ = {}
mgrutils.__salt__ = {}
mgrutils.__states__ = {}


def test_cmd_dump():
    """
    Test cmd_dump()
    """
    mock_managed = MagicMock(return_value={"comment": "dummy"})
    with patch.dict(mgrutils.__states__, {"file.managed": mock_managed}):
        mock_run = MagicMock(return_value="output content")
        with patch.dict(mgrutils.__salt__, {"cmd.run": mock_run}):
            ret = mgrutils.cmd_dump("/path/to/out", "/bin/bar --out xml")
            mock_run.assert_called_once_with(
                "/bin/bar --out xml", raise_err=True, python_shell=False
            )
            mock_managed.assert_called_once_with(
                "/path/to/out", contents="output content"
            )
            assert ret["comment"] == "dummy"
            assert ret["name"] == "/path/to/out"
