# -*- coding: utf-8 -*-
'''
SUSE Manager custom wrapper for Salt "module.run" state module.

This wrapper determines the syntax to use for calling the Salt "module.run" state
that has changed between different Salt version.

Using this wrapper we ensure all SUSE Manager SLS files are using the same syntax
regardless the actual Salt version installed on the minion.

'''
from __future__ import absolute_import

import logging

log = logging.getLogger(__name__)

__virtualname__ = 'mgrcompat'


def __virtual__():
    '''
    This module is always enabled while 'state.single' is available.
    '''
    return __virtualname__ if 'state.single' in __salt__ else (False, 'state.single is not available')

def module_run(**kwargs):
    '''
    This function execute the Salt "module.run" state passing the arguments
    in the right way according to the supported syntax depending on the Salt
    minion version

    '''

    # The new syntax will be used as the default
    use_new_syntax = True

    if __grains__['saltversioninfo'][0] > 2019:
        # New syntax - any future Salt release
        pass
    elif __grains__['saltversioninfo'][0] == 2019 and __grains__['saltversioninfo'][1] > 2:
        # New syntax - posible future Neon release (not yet determined)
        pass
    elif __grains__['saltversioninfo'][0] > 2016 and 'module.run' in __opts__.get('use_superseded', []):
        # New syntax - explicitely enabled via 'use_superseded' configuration on 2018.3 and 2019.2
        pass
    elif __grains__['saltversioninfo'][0] > 2016 and not 'module.run' in __opts__.get('use_superseded', []):
        # Old syntax - default behavior on 2018.3 and 2019.2
        use_new_syntax = False
    elif __grains__['saltversioninfo'][0] <= 2016:
        # Old syntax - no new syntax available for 2016.11 and 2015.8
        use_new_syntax = False

    if use_new_syntax:
        log.debug("Using the new module.run syntax")
        new_kwargs = kwargs
    else:
        log.debug("Using the old deprecated module.run syntax. Tailoring module.run arguments.")
        kwargs.pop('name')
        new_kwargs = {
            'name': list(kwargs.keys())[0],
        }
        for item in kwargs[new_kwargs['name']] or []:
            new_kwargs.update(item)

    return list(__salt__['state.single']('module.run', **new_kwargs).values())[0]
