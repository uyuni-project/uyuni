#!/usr/bin/python3
# pylint: disable=missing-module-docstring,invalid-name
import xml.etree.ElementTree as ET
import os
import logging
import argparse

argparser = argparse.ArgumentParser()
argparser.add_argument("path", help="Path to look into for translation files!")
args = argparser.parse_args()

logging.basicConfig(level=logging.INFO)


class CommentedTreeBuilder(ET.TreeBuilder):
    # pylint: disable-next=arguments-renamed
    def comment(self, data):
        self.start(ET.Comment, {})
        self.data(data)
        self.end(ET.Comment)


def get_groups(xml_tree):
    root_node = xml_tree.getroot()
    file_tag = root_node.find("d:file", ns)
    body_element = file_tag.find("d:body", ns)
    groups = list(body_element.findall("d:group", ns))
    # pylint: disable-next=logging-format-interpolation,consider-using-f-string
    logging.info("'{0}' <group> elements are found".format(len(groups)))
    return body_element, groups


def extract_trans_units(org_file):
    parser = ET.XMLParser(target=CommentedTreeBuilder())
    tree = ET.parse(file, parser=parser)
    body_element, groups = get_groups(tree)
    for group in groups:
        all_child_elements_of_body = list(body_element)
        context_group = group.find("d:context-group", ns)
        group_trans_units = group.findall("d:trans-unit", ns)
        index = all_child_elements_of_body.index(group)
        group_trans_units.reverse()
        for group_trans_unit in group_trans_units:
            if context_group is not None:
                group_trans_unit.append(context_group)
            body_element.insert(index, group_trans_unit)
        body_element.remove(group)

    tree.write(org_file, encoding="utf-8", xml_declaration=True)


ET.register_namespace("", "urn:oasis:names:tc:xliff:document:1.1")
ET.register_namespace("xyz", "urn:appInfo:Items")
ET.register_namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")
ns = {"d": "urn:oasis:names:tc:xliff:document:1.1"}

os.chdir(args.path)
files = os.listdir(args.path)
logging.debug(files)

for file in files:
    if file.startswith("StringResource_") and file.endswith(".xml"):
        # pylint: disable-next=logging-not-lazy
        logging.info("processing " + str(file))
        extract_trans_units(file)
