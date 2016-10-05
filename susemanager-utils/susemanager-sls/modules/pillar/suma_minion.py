# -*- coding: utf-8 -*-
'''
Retrieve SUSE Manager pillar data by the ID. File format is: <prefix>_<minion_id>.sls
Adds formula pillar data.

Parameters:
    path - Path where SUSE Manager stores pillar data
    formula_layout_path - Path were SUSE Manager stores formula layouts
    formula_data_path - Path were SUSE Manager stores formula data

.. code-block:: yaml

    ext_pillar:
      - suma_minion:
        - /another/path/with/the/pillar/files
        - /path/to/official/formula/layout/files
        - /path/to/custom/formula/layout/files
        - /path/to/formula/data/files

'''

# Import python libs
from __future__ import absolute_import
import os
import logging
import yaml
import json

# Set up logging
log = logging.getLogger(__name__)


def __virtual__():
    '''
    Ensure the pillar module name.
    '''
    return True


def ext_pillar(minion_id, pillar, path, official_form_path, custom_form_path, formula_data_path):
    '''
    Find SUMA-related pillars for the registered minions and return the data.
    '''

    log.debug('Getting pillar data for the minion "{0}"'.format(minion_id))

    ret = {}
    data_filename = os.path.join(path, 'pillar_{minion_id}.yml'.format(minion_id=minion_id))
    if not os.path.exists(data_filename):
        # during onboarding the file do not exist which is ok
        return ret
    try:
        ret = yaml.load(open(data_filename).read())
    except Exception as error:
        log.error('Error accessing "{pillar_file}": {message}'.format(pillar_file=data_filename, message=str(error)))

    try:
        ret.update(formula_pillars(minion_id, ret.get("group_ids", []), official_form_path, custom_form_path, formula_data_path))
    except Exception as error:
        log.error('Error accessing formula pillar data: {message}'.format(message=str(error)))

    return ret

def formula_pillars(minion_id, group_ids, official_form_path, custom_form_path, formula_data_path):
    '''
    Find formula pillars for the minion, merge them and return the data.
    '''
    ret = {}
    formulas_by_group = {}
    formulas = []

    # Loading group formulas
    try:
        with open(os.path.join(formula_data_path, "group_formulas.json")) as f:
            group_formulas = json.load(f)
            for group in group_ids:
                formulas_by_group[group] = group_formulas.get(unicode(group), [])
    except Exception as error:
        log.error('Error loading group formulas: {message}'.format(message=str(error)))

    for group in formulas_by_group:
        for formula in formulas_by_group[group]:
            formulas.append(formula)
            ret.update(load_formula_pillar(minion_id, group, formula, official_form_path, custom_form_path, formula_data_path))

    # Loading minion formulas
    try:
        with open(os.path.join(formula_data_path, "minion_formulas.json")) as f:
            minion_formulas_data = json.load(f)
            minion_formulas = minion_formulas_data.get(minion_id, [])
            for formula in minion_formulas:
                if formula in formulas:
                    continue
                formulas.append(formula)
                ret.update(load_formula_pillar(minion_id, None, formula, official_form_path, custom_form_path, formula_data_path))
    except Exception as error:
        log.error('Error loading minion formulas: {message}'.format(message=str(error)))

    ret["formulas"] = formulas
    return ret

def load_formula_pillar(minion_id, group_id, formula_name, official_form_path, custom_form_path, formula_data_path):
    '''
    Load the data from a specific formula for a minion in a specific group, merge and return it.
    '''
    layout_filename = os.path.join(official_form_path, formula_name, "form.yml")
    if not os.path.isfile(layout_filename):
        layout_filename = os.path.join(custom_form_path, formula_name, "form.yml")
        if not os.path.isfile(layout_filename):
            log.error('Error loading data for formula "{formula}": No form.yml found'.format(formula=formula_name))
            return {}

    group_filename = os.path.join(formula_data_path, "group_pillar", "{id}_{name}.json".format(id=group_id, name=formula_name)) if group_id is not None else None
    system_filename = os.path.join(formula_data_path, "pillar", "{id}_{name}.json".format(id=minion_id, name=formula_name))

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
