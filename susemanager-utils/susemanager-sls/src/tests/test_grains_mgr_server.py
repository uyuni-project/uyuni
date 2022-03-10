'''
Author: mc@suse.com
'''

import sys
import os
import json
import pytest
from unittest.mock import MagicMock, patch, mock_open
from . import mockery
mockery.setup_environment()

from ..grains import mgr_server

def test_server():
    mgr_server.RHNCONF = os.path.sep.join([os.path.abspath(''), 'data', 'rhnconf.sample'])

    grains = mgr_server.server_grains()
    assert type(grains) == dict
    assert 'is_mgr_server' in grains
    assert 'has_report_db' in grains
    assert grains['is_mgr_server'] == True
    assert grains['has_report_db'] == True
    assert grains['report_db_name'] == 'reportdb'
    assert grains['report_db_host'] == 'localhost'
    assert grains['report_db_port'] == '5432'


def test_server_no_reportdb():
    mgr_server.RHNCONF = os.path.sep.join([os.path.abspath(''), 'data', 'rhnconf2.sample'])

    grains = mgr_server.server_grains()
    assert type(grains) == dict
    assert 'is_mgr_server' in grains
    assert 'has_report_db' in grains
    assert grains['is_mgr_server'] == True
    assert grains['has_report_db'] == False


def test_no_server():
    mgr_server.RHNCONF = '/etc/rhn/rhn.conf'

    grains = mgr_server.server_grains()
    assert type(grains) == dict
    assert 'is_mgr_server' in grains
    assert 'has_report_db' in grains
    assert grains['is_mgr_server'] == False
    assert grains['has_report_db'] == False
