# -*- coding: utf-8 -*-
#
# Copyright (C) 2009, 2010, 2011, 2012 Novell, Inc.
#   This library is free software; you can redistribute it and/or modify
# it only under the terms of version 2.1 of the GNU Lesser General Public
# License as published by the Free Software Foundation.
#
#   This library is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
# details.
#
#   You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA

import xml.etree.ElementTree as etree

def create_product_ident(product_id, name, arch, base_channel):
    ident = "%s-%s-%s-%s" % (name.lower().replace(" ", "-"), arch, product_id, base_channel)
    return ident

class SimpleProduct:
    def __init__(self, ident, product_id, name, arch, base_channel=None):
        self.ident = ident
        self.product_id = product_id
        self.name = name
        self.arch = arch
        self.base_channel = base_channel
        self.mandatory_channels = dict()
        self.optional_channels = dict()
        self.parent_product = None

    def add_mandatory_channel(self, label, status):
        self.mandatory_channels[label] = status

    def add_optional_channel(self, label, status):
        self.optional_channels[label] = status

    def is_base(self):
        if self.base_channel in self.mandatory_channels and self.parent_product is None:
            return True
        return False

    def set_parent_product(self, ident):
        self.parent_product = ident

    def status(self):
        for st in self.mandatory_channels.values():
            if st == ".":
                return "."
        return "P"

    def display_name(self):
        return "%s [%s]" % (self.name, self.arch)

    def to_xml(self, root):
        product_elem = etree.SubElement(root, 'product', attrib={
            'ident': self.ident,
            'name': self.name,
            'arch': self.arch})
        if self.parent_product:
            product_elem.set('parent_product', self.parent_product)
        else:
            product_elem.set('parent_product', "")
        mand_channel_elem = etree.SubElement(product_elem, "mandatory_channels")
        for label in self.mandatory_channels:
            c_elem = etree.SubElement(mand_channel_elem, "channel", attrib={'label':label, 'status':self.mandatory_channels[label]})
        opt_channel_elem = etree.SubElement(product_elem, "optional_channels")
        for label in self.optional_channels:
            c_elem = etree.SubElement(opt_channel_elem, "channel", attrib={'label':label, 'status':self.optional_channels[label]})

