# -*- coding: utf-8 -*-
'''
SUSE Manager master_tops module
-------------------------------

This module provides the base states top information from SUSE Manager.

The top information returned by this module is merged by Salt with the 
user custom data provided in /srv/salt/top.sls file.

.. code-block:: yaml

    master_tops:
        - susemanager_mastertops: True
'''

# Import python libs
from __future__ import absolute_import
import copy
import logging

# Define the module's virtual name
__virtualname__ = 'susemanager_mastertops'

log = logging.getLogger(__name__)

SUSEMANAGER_BASE_STATES = [
    "channels",
    "certs",
    "packages",
    "custom",
    "custom_groups",
    "custom_org"
]


def __virtual__():
    '''
    Ensure the module name.
    '''
    return __virtualname__


def top(**kwargs):
    '''
    Returns state information for a minion based on merged top.sls files
    of `base` salt environment in `file_roots`.
    '''
    minion_states = []

    if kwargs['opts']['environment'] in [None, "base"]:
        log.debug('Loading SUSE Manager base states')

        minion_states = copy.deepcopy(SUSEMANAGER_BASE_STATES)

        # Fix: channels are not available for RHEL
        if kwargs['grains']['os_family'] != "Suse":
            minion_states.remove("channels")

    return {"base": minion_states} if minion_states else None
