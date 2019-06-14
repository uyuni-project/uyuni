# -*- coding: utf-8 -*-
'''
SUSE Manager custom wrapper for Salt "module.run" state module.

This wrapper determines the syntax to use for calling the Salt "module.run" state
that has changed between different Salt version.

Using this wrapper we ensure all SUSE Manager SLS files are using the same syntax
regardless the actual Salt version installed on the minion.

'''
from __future__ import absolute_import

# Import salt libs
from salt.utils.odict import OrderedDict
from salt.states import module

import logging

log = logging.getLogger(__name__)

__virtualname__ = 'mgrcompat'


def __virtual__():
    '''
    This module is always enabled while 'module.run' is available.
    '''
    module.__salt__ = __salt__
    module.__opts__ = __opts__
    module.__pillar__ = __pillar__
    module.__grains__ = __grains__
    module.__context__ = __context__
    module.__utils__ = __utils__
    return __virtualname__ if 'module.run' in __salt__ else (False, 'module.run is not available')

def _tailor_kwargs_to_new_syntax(name, **kwargs):
    # Remove "m_" from the kwargs parameters key
    nkwargs = {}
    for k, v in kwargs.items():
        if k.startswith("m_"):
            nkwargs[k[2:]] = v
        else:
            nkwargs[k] = v
    return {name: [OrderedDict(nkwargs)]}

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
    elif __grains__['saltversioninfo'][0] == 2019 and __grains__['saltversioninfo'][1] > 2 and __opts__.get('use_superseded', []):
        # New syntax - posible future Neon release (not yet determined)
        pass
    elif __grains__['saltversioninfo'][0] == 2019 and __grains__['saltversioninfo'][1] > 2 and not 'module.run' in __opts__.get('use_superseded', []):
        # New syntax - posible future Neon release (not yet determined)
        use_new_syntax = False
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
        old_name = kwargs.pop('name')
        new_kwargs = _tailor_kwargs_to_new_syntax(old_name, **kwargs)
    else:
        new_kwargs = kwargs

    ret = module.run(**new_kwargs)
    if use_new_syntax:
        if ret['changes']:
            changes = ret['changes'].pop(old_name)
            ret['changes']['ret'] = changes
        ret['name'] = old_name
    return ret
