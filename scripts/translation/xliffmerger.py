#!/usr/bin/python
import xml.etree.ElementTree as ET
import os
import logging
import argparse
parser = argparse.ArgumentParser()
parser.add_argument("path", help="Path to look into for translation files!")
args = parser.parse_args()

logging.basicConfig(level=logging.INFO)


def delete_trans_units(body_element, trans_units, units_to_remove):
    to_delete = [tr_unit for tr_unit in trans_units if tr_unit.attrib['id'] in units_to_remove]
    for item in to_delete:
        body_element.remove(item)


def add_trans_units(body_element, trans_units, units_to_add):
    to_add = [tr_unit for tr_unit in trans_units if tr_unit.attrib['id'] in units_to_add]
    for item in to_add:
        item.set('state', 'new')
        body_element.append(item)


def update_trans_units(body_element, trans_units, id_new_value_dict):
    to_update = [tr_unit for tr_unit in trans_units if tr_unit.attrib['id'] in id_new_value_dict]
    for trans_unit in to_update:
        trans_unit.set('state', 'needs-adaptation')
        trans_unit[0].text = id_new_value_dict[trans_unit.attrib['id']]


def process(original_file, translation_file):
    xml_tree_original = ET.parse(original_file)
    xml_tree_translation = ET.parse(translation_file)
    original_body_element, original_trans_units = get_trans_units(xml_tree_original)
    translation_body_element, translation_trans_units = get_trans_units(xml_tree_translation)
    logging.debug(f'{len(original_trans_units)} trans_units in original file {original_file}')
    logging.debug(f'{len(translation_trans_units)} trans_units in translation file {translation_file}')

    org_trans_units_ids = [tr_unit.attrib['id'] for tr_unit in original_trans_units]
    trans_units_ids = [tr_unit.attrib['id'] for tr_unit in translation_trans_units]
    logging.debug(f'{org_trans_units_ids} trans_units IDs in original file {original_file}')
    logging.debug(f'{trans_units_ids} trans_units in Ids translation file {translation_file}')

    # Delete the ones which are in translation file but not in original
    logging.info("########## DELETING ORPHAN TRANS_UNITS ##########")
    to_remove = set(trans_units_ids).difference(set(org_trans_units_ids))
    logging.info(f'Trans-units with these Ids : {to_remove}, will be deleted from the {translation_file}')
    delete_trans_units(translation_body_element, translation_trans_units, to_remove)
    logging.info("-------------------------------------------------\n")

    # Add the ones which are not in translation file but exist in original
    logging.info("########## ADDING MISSING TRANS_UNITS ##########")
    to_add = set(org_trans_units_ids).difference(set(trans_units_ids))
    logging.info(f'Trans-units with these Ids : {to_add}, will be added to the {translation_file}')
    add_trans_units(translation_body_element, original_trans_units, to_add)
    logging.info("-------------------------------------------------\n")

    # Update() those trans_units where source has been changed but id remained same. We will be updating source text and
    # add the 'needs-adaptation' state attribute
    logging.info("########## UPDATE THE CHANGED TRANS_UNITS ##########")
    # Get again so we get the updated list after deletion/addition
    translation_trans_units = list(translation_body_element)

    logging.debug(f'{len(original_trans_units)} trans_units in original file {original_file}')
    logging.debug(f'{len(translation_trans_units)} trans_units in original file {original_file}')
    if len(translation_trans_units) == len(original_trans_units):
        trans_units_sources = {tr_unit.attrib['id']: tr_unit[0].text for tr_unit in translation_trans_units}
        org_trans_units_sources = {tr_unit.attrib['id']: tr_unit[0].text for tr_unit in original_trans_units}
        to_update = {k: org_trans_units_sources[k] for k,_ in set(org_trans_units_sources.items()) - set(trans_units_sources.items())}
        logging.info(f' These (id,new_source_value) -> {to_update}, will be updated in the {translation_file} ')
        update_trans_units(translation_body_element,translation_trans_units,to_update)


    #xml_tree_translation.write(translation, encoding='utf-8', xml_declaration=True)
    logging.info("########## FINISH ##########")


def get_trans_units(xml_tree):
    root_node = xml_tree.getroot()
    # file_tag = root_node[0]
    body_element = root_node[0][0]
    return body_element, list(body_element)


ET.register_namespace('', "urn:oasis:names:tc:xliff:document:1.1")
ET.register_namespace('xyz', "urn:appInfo:Items")
ET.register_namespace('xsi', "http://www.w3.org/2001/XMLSchema-instance")

os.chdir(args.path)
files = os.listdir(args.path)
#logging.debug(files)

for translation in files:
    if translation.startswith('StringResource_') and translation.endswith('.xml') \
            and translation != 'StringResource_en_US.xml':
        original = 'StringResource_en_US.xml'
        logging.info('processing ' + str(translation))
        process(original, translation)
