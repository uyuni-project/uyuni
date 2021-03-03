#!/usr/bin/python3
import xml.etree.ElementTree as ET
import os
import logging
import argparse
argparser = argparse.ArgumentParser()
argparser.add_argument("path", help="Path to look into for translation files!")
args = argparser.parse_args()

logging.basicConfig(level=logging.INFO)


class CommentedTreeBuilder(ET.TreeBuilder):
    def comment(self, data):
        self.start(ET.Comment, {})
        self.data(data)
        self.end(ET.Comment)


def delete_trans_units(body_element, trans_units, units_to_remove):
    to_delete = [tr_unit for tr_unit in trans_units if tr_unit.attrib['id'] in units_to_remove]
    for item in to_delete:
        body_element.remove(item)


def add_trans_units(body_element, trans_units, units_to_add):
    to_add = [tr_unit for tr_unit in trans_units if tr_unit.attrib['id'] in units_to_add]
    for item in to_add:
        item.set('{http://www.w3.org/XML/1998/namespace}space', "preserve")
        target_element = ET.Element('target', {'state': 'new'})
        target_element.tail = "\n  "
        item.append(target_element)
        body_element.append(item)


def update_trans_units(body_element, trans_units, id_new_value_dict):
    to_update = [tr_unit for tr_unit in trans_units if tr_unit.attrib['id'] in id_new_value_dict]
    for trans_unit in to_update:
        trans_unit.set('{http://www.w3.org/XML/1998/namespace}space', "preserve")
        source_element = trans_unit.find('d:source', ns)
        source_element.text = id_new_value_dict[trans_unit.attrib['id']]
        target_element = trans_unit.find('d:target', ns)
        if target_element is None:
            target_element = ET.Element('target', {'state': 'needs-adaptation'})
            target_element.tail = "\n      "
            trans_unit.append(target_element)
        else:
            target_element.set('state', 'needs-adaptation')


def process(original_file, translation_file):
    original_file_parser = ET.XMLParser(target=CommentedTreeBuilder())
    xml_tree_original = ET.parse(original_file, parser=original_file_parser)
    translation_file_parser = ET.XMLParser(target=CommentedTreeBuilder())
    xml_tree_translation = ET.parse(translation_file, parser=translation_file_parser)
    original_body_element, orig_trans_units = get_trans_units(xml_tree_original)
    translation_body_element, trans_trans_units = get_trans_units(xml_tree_translation)
    logging.debug(f'{len(orig_trans_units)} trans_units in original file {original_file}')
    logging.debug(f'{len(trans_trans_units)} trans_units in translation file {translation_file}')

    org_trans_units_ids = [tr_unit.attrib['id'] for tr_unit in orig_trans_units]
    trans_units_ids = [tr_unit.attrib['id'] for tr_unit in trans_trans_units]
    logging.debug(f'{org_trans_units_ids} trans_units IDs in original file {original_file}')
    logging.debug(f'{trans_units_ids} trans_units in Ids translation file {translation_file}')

    # Delete the ones which are in translation file but not in original
    to_remove = set(trans_units_ids).difference(set(org_trans_units_ids))
    if to_remove:
      logging.info("########## DELETING ORPHAN TRANS_UNITS ##########")
      logging.info(f'Trans-units with these Ids : {to_remove}, will be deleted from the {translation_file}')
      delete_trans_units(translation_body_element, trans_trans_units, to_remove)
      logging.info("-------------------------------------------------\n")

    # Add the ones which are not in translation file but exist in original
    to_add = set(org_trans_units_ids).difference(set(trans_units_ids))
    if to_add:
      logging.info("########## ADDING MISSING TRANS_UNITS ##########")
      logging.info(f'Trans-units with these Ids : {to_add}, will be added to the {translation_file}')
      add_trans_units(translation_body_element, orig_trans_units, to_add)
      logging.info("-------------------------------------------------\n")

    # Update() those trans_units where source has been changed but id remained same. We will be updating source text and
    # add the 'needs-adaptation' state attribute
    logging.info("########## UPDATE THE CHANGED TRANS_UNITS ##########")
    # Get again so we get the updated list after deletion/addition
    trans_trans_units = list(translation_body_element.findall('.//d:trans-unit', ns))

    logging.debug(f'{len(orig_trans_units)} trans_units in original file {original_file}')
    logging.debug(f'{len(trans_trans_units)} trans_units in original file {original_file}')
    if len(trans_trans_units) == len(orig_trans_units):
        trans_units_srcs = {tr_unit.attrib['id']: tr_unit.find('d:source', ns).text for tr_unit in trans_trans_units}
        org_trans_units_srcs = {tr_unit.attrib['id']: tr_unit.find('d:source', ns).text for tr_unit in orig_trans_units}
        to_update = {k: org_trans_units_srcs[k] for k, _ in
                     set(org_trans_units_srcs.items()) - set(trans_units_srcs.items())}
        logging.info(f' These (id,new_source_value) -> {to_update}, will be updated in the {translation_file} ')
        update_trans_units(translation_body_element, trans_trans_units, to_update)
    else:
        logging.info((
            "Something went wrong, this should not have happend! Count of original units: "
            "%d, count of translation units: %d." % (len(orig_trans_units), len(trans_trans_units))))
        raise Exception("Mismatching orig/trans lengths")
    for t in list(translation_body_element.findall('d:trans-unit', ns)):
        if not t.get('{http://www.w3.org/XML/1998/namespace}space'):
            t.set('xml:space', 'preserve')

    xml_tree_translation.write(translation, encoding='UTF-8', xml_declaration=True)

#    for o in list(original_body_element.findall('d:trans-unit', ns)):
#        if not o.get('{http://www.w3.org/XML/1998/namespace}space'):
#            o.set('xml:space', 'preserve')
#    xml_tree_original.write(original_file, encoding='utf-8', xml_declaration=True)

def get_trans_units(xml_tree):
    root_node = xml_tree.getroot()
    file_tag = root_node.find('d:file', ns)
    body_element = file_tag.find('d:body', ns)
    return body_element, list(body_element.findall('.//d:trans-unit', ns))


ET.register_namespace('', "urn:oasis:names:tc:xliff:document:1.1")
ET.register_namespace('xyz', "urn:appInfo:Items")
ET.register_namespace('xsi', "http://www.w3.org/2001/XMLSchema-instance")
ns = {'d': 'urn:oasis:names:tc:xliff:document:1.1'}

os.chdir(args.path)
files = os.listdir(args.path)
#logging.debug(files)

for translation in files:
    if translation.startswith('StringResource_') and translation.endswith('.xml') \
            and translation != 'StringResource_en_US.xml':
        original = 'StringResource_en_US.xml'
        logging.info('\nprocessing ' + str(translation))
        process(original, translation)
