# -*- coding: utf-8 -*-
'''
Salt only merges top.sls contained in different salt environments, but
SUSE Manager provides the feature of define different top.sls files in
different `pillar_roots` directories of the `base` saltenv.

This module merge top.sls files contained in the different `pillar_roots`
defined for the `base` saltenv and returns a the entire pillar information
contained in the differents top.sls files.

.. code-block:: yaml

    ext_pillar:
        - susemanager_pillartops: True
'''

# Import python libs
from __future__ import absolute_import
import logging
import os
import copy

# Import salt
import salt

# Define the module's virtual name
__virtualname__ = 'susemanager_pillartops'

log = logging.getLogger(__name__)


def __virtual__():
    '''
    Ensure the module name.
    '''
    return __virtualname__


def ext_pillar(*args, **kwargs):
    '''
    Returns pillar information for a minion based on merged top.sls files
    of `base` salt environment in `pillar_roots`.
    '''
    if __opts__["environment"] not in [None, "base"]:
        log.debug('Omitting merge of top.sls files outside "base" saltenv: %s',
                  __opts__['environment'])
        return {}

    log.debug(
        'Merging top.sls pillar files in: %s',
        __opts__['pillar_roots']['base']
    )

    top_tree = {}
    opts = copy.deepcopy(__opts__)
    for pillar_root in __opts__['pillar_roots']['base']:
        if os.path.isfile(os.path.join(pillar_root, "top.sls")):
            opts['file_roots'] = {"base": [pillar_root]}
            opts['pillar_roots'] = {"base": [pillar_root]}
            opts['ext_pillar'] = []

            pillar_ = salt.pillar.Pillar(opts,
                                         __grains__,
                                         opts['id'],
                                         opts['environment']).compile_pillar()

            top_tree.update(pillar_)
    return top_tree
