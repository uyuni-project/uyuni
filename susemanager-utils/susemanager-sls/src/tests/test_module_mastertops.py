# -*- coding: utf-8 -*-
'''
:codeauthor:    Pablo Suárez Hernández <psuarezhernandez@suse.de>
'''

from mock import MagicMock, patch
import mockery
mockery.setup_environment()

import sys
sys.path.append("../../modules/tops")

import mgr_mastertops

TEST_MANAGER_STATIC_TOP = {
    "base": [
        "channels",
        "certs",
        "packages",
        "custom",
        "custom_groups",
        "custom_org"
    ]
}


def test_virtual():
    '''
    Test virtual returns the module name
    '''
    assert mgr_mastertops.__virtual__() == "mgr_mastertops"


def test_top_default_saltenv():
    '''
    Test if top function is returning the static SUSE Manager top state
    for base environment when no environment has been specified.
    '''
    kwargs = {'opts': {'environment': None}}
    assert mgr_mastertops.top(**kwargs) == TEST_MANAGER_STATIC_TOP


def test_top_base_saltenv():
    '''
    Test if top function is returning the static SUSE Manager top state
    for base environment when environment is set to "base".
    '''
    kwargs = {'opts': {'environment': 'base'}}
    assert mgr_mastertops.top(**kwargs) == TEST_MANAGER_STATIC_TOP


def test_top_unknown_saltenv():
    '''
    Test if top function is returning None for unknown salt environments.
    '''
    kwargs = {'opts': {'environment': 'otherenv'}}
    assert mgr_mastertops.top(**kwargs) == None
