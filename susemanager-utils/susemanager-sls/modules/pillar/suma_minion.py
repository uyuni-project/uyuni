# -*- coding: utf-8 -*-
'''
Retrieve SUSE Manager pillar data for a minion_id.
- Adds generated and static SUSE Manager pillar data.
- Adds formula pillar data.

.. code-block:: yaml

    ext_pillar:
      - suma_minion: True

'''

# Import python libs
from __future__ import absolute_import
import os
import logging
import yaml
import json
import sys

# SUSE Manager static pillar paths:
MANAGER_STATIC_PILLAR_DATA_PATH = '/usr/share/susemanager/pillar_data'
MANAGER_PILLAR_DATA_PATH = '/srv/susemanager/pillar_data'

# SUSE Manager formulas paths:
MANAGER_FORMULAS_METADATA_PATH = '/usr/share/susemanager/formulas/metadata'
CUSTOM_FORMULAS_METADATA_PATH = '/srv/formula_metadata'
FORMULAS_DATA_PATH = '/srv/susemanager/formula_data'

# SUSE Manager static pillar data.
MANAGER_STATIC_PILLAR = [
    'gpgkeys',
]

# Set up logging
log = logging.getLogger(__name__)


def __virtual__():
    '''
    Ensure the pillar module name.
    '''
    return True


def ext_pillar(minion_id, *args):
    '''
    Find SUMA-related pillars for the registered minions and return the data.
    '''

    log.debug('Getting pillar data for the minion "{0}"'.format(minion_id))
    ret = {}

    # Including SUSE Manager static pillar data
    for static_pillar in MANAGER_STATIC_PILLAR:
        static_pillar_filename = os.path.join(MANAGER_STATIC_PILLAR_DATA_PATH, static_pillar)
        try:
            ret.update(yaml.load(open('{0}.yml'.format(static_pillar_filename)).read()))
        except Exception as exc:
            log.error('Error accessing "{0}": {1}'.format(static_pillar_filename, exc))

    # Including generated pillar data for this minion
    data_filename = os.path.join(MANAGER_PILLAR_DATA_PATH, 'pillar_{minion_id}.yml'.format(minion_id=minion_id))
    if os.path.exists(data_filename):
        try:
            ret.update(yaml.load(open(data_filename).read()))
        except Exception as error:
            log.error('Error accessing "{pillar_file}": {message}'.format(pillar_file=data_filename, message=str(error)))

    # Including formulas into pillar data
    try:
        ret.update(formula_pillars(minion_id, ret.get("group_ids", [])))
    except Exception as error:
        log.error('Error accessing formula pillar data: {message}'.format(message=str(error)))

    return ret


def load_formulas_from_file(formula_filename):
    formulas = {}
    formula_file = os.path.join(FORMULAS_DATA_PATH, formula_filename)
    if os.path.exists(formula_file):
        try:
            with open(formula_file) as f:
                formulas = json.load(f)
        except Exception as error:
            log.error('Error loading formulas from file: {message}'.format(message=str(error)))
    return formulas


def process_formulas(formula_filename, minion_id, group_ids=[], as_group=False):
    pillar = {}
    out_formulas = []
    data = load_formulas_from_file(formula_filename)

    if as_group:
        for group in group_ids:
            for formula in data.get(str(group), []):
                out_formulas.append(formula.encode('utf-8'))
                pillar.update(load_formula_pillar(minion_id, group, formula))

    else:
        for formula in data.get(str(minion_id), []):
            out_formulas.append(formula.encode('utf-8'))
            pillar.update(load_formula_pillar(minion_id, None, formula))

    return pillar, out_formulas


def formula_pillars(minion_id, group_ids):
    '''
    Find formula pillars for the minion, merge them and return the data.
    '''
    ret = {}

    # Loading group formulas
    group_pillar, group_formulas = process_formulas(
        "group_formulas.json", minion_id, group_ids, as_group=True)
    ret.update(group_pillar)

    # Loading minion formulas
    minion_pillar, minion_formulas = process_formulas(
        "minion_formulas.json", minion_id)
    ret.update(minion_pillar)

    ret["formulas"] = list(set(group_formulas + minion_formulas))
    return ret


def load_formula_pillar(minion_id, group_id, formula_name):
    '''
    Load the data from a specific formula for a minion in a specific group, merge and return it.
    '''
    layout_filename = os.path.join(MANAGER_FORMULAS_METADATA_PATH, formula_name, "form.yml")
    if not os.path.isfile(layout_filename):
        layout_filename = os.path.join(CUSTOM_FORMULAS_METADATA_PATH, formula_name, "form.yml")
        if not os.path.isfile(layout_filename):
            log.error('Error loading data for formula "{formula}": No form.yml found'.format(formula=formula_name))
            return {}

    group_filename = os.path.join(FORMULAS_DATA_PATH, "group_pillar", "{id}_{name}.json".format(id=group_id, name=formula_name)) if group_id is not None else None
    system_filename = os.path.join(FORMULAS_DATA_PATH, "pillar", "{id}_{name}.json".format(id=minion_id, name=formula_name))

    try:
        layout = yaml.load(open(layout_filename).read())
        group_data = json.load(open(group_filename)) if group_filename is not None and os.path.isfile(group_filename) else {}
        system_data = json.load(open(system_filename)) if os.path.isfile(system_filename) else {}
    except Exception as error:
        log.error('Error loading data for formula "{formula}": {message}'.format(formula=formula_name, message=str(error)))
        return {}

    return merge_formula_data(layout, group_data, system_data)

def merge_formula_data(layout, group_data, system_data, scope="system"):
    '''
    Merge the group and system formula data, respecting the scope of a value.
    '''
    ret = {}

    for element_name in layout:
        if element_name.startswith("$"):
            continue

        element = layout[element_name]
        if not isinstance(element, dict):
            continue

        element_scope = element.get("$scope", scope)
        value = None

        if element.get("$type", "text") in ["group", "hidden-group"]:
            value = merge_formula_data(element, group_data.get(element_name, {}), system_data.get(element_name, {}), element_scope)
        elif element_scope == "system":
            value = system_data.get(element_name, group_data.get(element_name, element.get("$default", element.get("$placeholder", ""))))
        elif element_scope == "group":
            value = group_data.get(element_name, element.get("$default", element.get("$placeholder", "")))
        elif element_scope == "readonly":
            value = element.get("$default", element.get("$placeholder", ""))

        if value is not None:
            ret[element_name] = value
    return ret
