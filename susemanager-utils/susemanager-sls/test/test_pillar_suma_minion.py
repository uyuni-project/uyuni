# -*- coding: utf-8 -*-
'''
:codeauthor:    Michael Calmer <Michael.Calmer@suse.com>
'''

from mock import MagicMock, patch

import sys
sys.path.append("../modules/pillar")
import os
import copy

import suma_minion


def test_virtual():
    '''
    Test virtual returns the module name
    '''
    assert suma_minion.__virtual__() == True

def test_formula_pillars():
    '''
    Test formula ordering
    '''
    suma_minion.FORMULAS_DATA_PATH = os.path.sep.join([os.path.abspath(''), 'data'])
    suma_minion.FORMULA_ORDER_FILE = os.path.sep.join([os.path.abspath(''), 'data', 'formula_order.json'])
    suma_minion.MANAGER_FORMULAS_METADATA_MANAGER_PATH = os.path.sep.join([os.path.abspath(''), 'data', 'formulas', 'metadata'])
    pillar = suma_minion.formula_pillars("suma-refhead-min-sles12sp4.mgr.suse.de", [9])
    assert "formulas" in pillar
    assert pillar["formulas"] == ['branch-network', 'locale', 'tftpd']

