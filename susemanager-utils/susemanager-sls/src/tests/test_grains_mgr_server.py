"""
Author: mc@suse.com
"""

# pylint: disable-next=unused-import
import sys
import os

# pylint: disable-next=unused-import
import json

# pylint: disable-next=unused-import
import pytest

# pylint: disable-next=unused-import
from unittest.mock import MagicMock, patch, mock_open
from . import mockery

mockery.setup_environment()

# pylint: disable-next=wrong-import-position
from ..grains import mgr_server


def test_server():
    mgr_server.RHNCONF = os.path.join(os.path.abspath(""), "data", "rhnconf.sample")
    mgr_server.RHNCONFDEF = os.path.join(
        os.path.abspath(""), "data", "rhnconfdef.sample"
    )

    grains = mgr_server.server_grains()
    # pylint: disable-next=unidiomatic-typecheck
    assert type(grains) == dict
    assert "is_mgr_server" in grains
    assert "has_report_db" in grains
    assert "is_uyuni" in grains
    assert grains["is_mgr_server"]
    assert grains["has_report_db"]
    assert grains["report_db_name"] == "reportdb"
    assert grains["report_db_host"] == "localhost"
    assert grains["report_db_port"] == "5432"
    assert grains["is_uyuni"]


def test_server_no_reportdb():
    mgr_server.RHNCONF = os.path.join(os.path.abspath(""), "data", "rhnconf2.sample")
    mgr_server.RHNCONFDEF = os.path.join(
        os.path.abspath(""), "data", "rhnconfdef.sample"
    )

    grains = mgr_server.server_grains()
    # pylint: disable-next=unidiomatic-typecheck
    assert type(grains) == dict
    assert "is_mgr_server" in grains
    assert "has_report_db" in grains
    assert "is_uyuni" in grains
    assert grains["is_mgr_server"]
    assert not grains["has_report_db"]
    assert grains["is_uyuni"]


def test_no_server():
    mgr_server.RHNCONF = "/etc/rhn/rhn.conf"

    grains = mgr_server.server_grains()
    # pylint: disable-next=unidiomatic-typecheck
    assert type(grains) == dict
    assert "is_mgr_server" in grains
    assert "has_report_db" not in grains
    assert "is_uyuni" not in grains
    assert not grains["is_mgr_server"]
