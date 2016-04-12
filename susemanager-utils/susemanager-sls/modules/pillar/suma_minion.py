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


def ext_pillar(minion_id, pillar, path):
    '''
    Find SUMA-related pillars for the registered minions and return the data.
    '''

    log.debug('Getting pillar data for the minion "{0}"'.format(minion_id))

    ret = dict()
    data_filename = os.path.join(path, 'pillar_{minion_id}.yml'.format(minion_id=minion_id))
    if not os.path.exists(data_filename):
        log.error('Requested pillar data "{pillar_file}" does not exist!'.format(pillar_file=data_filename))
    elif not os.access(data_filename, os.R_OK):
        log.error('Access denied to the requested pillar data "{pillar_file}"!'.format(pillar_file=data_filename))
    else:
        ret = yaml.load(open(data_filename).read())

    return ret
