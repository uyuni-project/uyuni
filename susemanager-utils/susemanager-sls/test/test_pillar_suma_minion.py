# -*- coding: utf-8 -*-
'''
:codeauthor:    Michael Calmer <Michael.Calmer@suse.com>
'''

from mock import MagicMock, patch

import pytest
import sys
sys.path.append("../modules/pillar")
import os

import suma_minion

TEST_FORMULA_ORDER = [
    "branch-network",
    "cpu-mitigations",
    "dhcpd",
    "grafana",
    "image-synchronize",
    "locale",
    "prometheus",
    "prometheus-exporters",
    "pxe",
    "saltboot",
    "tftpd",
    "virtualization-host",
    "vsftpd",
    "bind"
]

@pytest.fixture(autouse=True)
def data_paths():
    '''
    Set the test data paths
    '''
    suma_minion.FORMULAS_DATA_PATH = os.path.sep.join([os.path.abspath(''), 'data'])
    suma_minion.FORMULA_ORDER_FILE = os.path.sep.join([os.path.abspath(''), 'data', 'formula_order.json'])
    suma_minion.MANAGER_FORMULAS_METADATA_MANAGER_PATH = os.path.sep.join([os.path.abspath(''), 'data', 'formulas', 'metadata'])


@pytest.mark.parametrize("has_psycopg2", [True, False])
def test_virtual(has_psycopg2):
    '''
    Test virtual returns the module name
    '''
    with patch('suma_minion.HAS_POSTGRES', has_psycopg2):
        assert suma_minion.__virtual__() == has_psycopg2


def test_formula_pillars_db():
    '''
    Test getting the formulas from the database
    '''
    minion_id = "suma-refhead-min-sles12sp4.mgr.suse.de"
    pillar = {"group_ids": [9]}

    cursor = MagicMock()
    cursor.fetchall.return_value = [({"formula_order": TEST_FORMULA_ORDER},)]
    pillar = suma_minion.load_global_pillars(cursor, pillar)

    cursor.fetchall.return_value = [("formula-locale", {}), ("formula-tftpd", {})]
    group_formulas, pillar = suma_minion.load_group_pillars(minion_id, cursor, pillar)

    cursor.fetchall.return_value = [("formula-branch-network", {})]
    system_formulas, pillar = suma_minion.load_system_pillars(minion_id, cursor, pillar)

    pillar = suma_minion.formula_pillars(system_formulas, group_formulas, pillar)
    assert "formulas" in pillar
    assert pillar["formulas"] == ['branch-network', 'locale', 'tftpd']
