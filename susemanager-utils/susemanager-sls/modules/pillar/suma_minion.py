# -*- coding: utf-8 -*-
'''
Retrieve SUSE Manager pillar data by the ID. File format is: <prefix>_<minion_id>.sls

Parameters:
    prefix - Any string, used as a prefix for the SLS filenames to match.
             Default "minion".

    file_extension - Extension to the pillar file.
                     Default "sls"

    environment - Used nvironment for pillar_roots
                  Default first met from the configuration.

    path - List of paths where to look for SLS pillar data.
           Default is a list of "pillar_roots".

    overwrite - Files will be processed in the order of the directories.
                if there are few pillar files where inside values are interfering,
                "overwrite: true" will replace an existing key with the new,
                otherwise skip it.
                Default: False

.. code-block:: yaml

    ext_pillar:
      - suma_minion:
          prefix: server
          file_extension: sls
          overwrite: true
          environment: base
          pillar_roots:
            - /path/with/the/pillar/files
            - /another/path/with/the/pillar/files

'''

# Import python libs
from __future__ import absolute_import
import os
import logging
import yaml

# Set up logging
log = logging.getLogger(__name__)


def __virtual__():
    '''
    Ensure the pillar module name.
    '''
    return True


def _merge(data, update, overwrite=False):
    '''
    Merge YAML data

    :param data:
    :param update:
    :return:
    '''
    if isinstance(data, dict) and isinstance(update, dict):
        for k, v in update.items():
            if overwrite or k not in data:
                data[k] = v
            else:
                data[k] = _merge(data[k], v)
    return data


def ext_pillar(minion_id,
               pillar,  # pylint: disable=W0613
               prefix="minion",
               file_extension="sls",
               environment=None,
               overwrite=False,
               pillar_roots=None):
    '''
    Find SUMA-related pillars for the registered minions and return the data.
    '''

    log.debug('Finding Pillar data for the minion "{0}"'.format(minion_id))

    pillar_roots = __opts__.get('pillar_roots', {}).get(environment or 'base', list()) + (pillar_roots or list())

    ret = dict()
    for pillar_path in pillar_roots:
        if not os.path.exists(pillar_path):
            log.error('Ignoring path "{0}" - does not exist'.format(pillar_path))
            continue
        if not os.access(pillar_path, os.R_OK):
            log.error('Ignoring path "{0}" - access denied.'.format(pillar_path))
            continue

        data_filename = os.path.join(pillar_path,
                                     "{prefix}_{minion_id}.{extension}".format(prefix=prefix,
                                                                               minion_id=minion_id,
                                                                               extension=file_extension))
        if os.path.exists(data_filename):
            ret = _merge(ret, yaml.load(open(data_filename).read()), overwrite=overwrite)

    return ret
