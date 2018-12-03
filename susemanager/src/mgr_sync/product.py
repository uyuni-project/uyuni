# -*- coding: utf-8 -*-
#
# Copyright (c) 2014 SUSE
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SUSE trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate SUSE trademarks that are incorporated
# in this software or its documentation.
from __future__ import print_function

from enum import Enum

from spacewalk.susemanager.mgr_sync.channel import Channel


class Product(object):

    class Status(str, Enum):  # pylint: disable=too-few-public-methods
        INSTALLED = "INSTALLED"
        AVAILABLE = "AVAILABLE"
        UNAVAILABLE = "UNAVAILABLE"

    def __init__(self, data):
        self.arch = data["arch"]
        self.friendly_name = data["friendly_name"]
        self.status = Product.Status(data["status"].upper())
        self.recommended = data["recommended"]
        self.extensions = []
        self._parse_extensions(data["extensions"])
        self.channels = [Channel(channel) for channel in data['channels']]
        self.isBase = False  # pylint: disable=invalid-name

    def __repr__(self):
        return self.to_ascii_row()

    @property
    def short_status(self):
        # pylint: disable=E1101
        if self.status == Product.Status.AVAILABLE:
            return "[ ]%s" % (self.recommended and " (R)" or "")
        else:
            return "[%s]%s" % (str(self.status.value)[0], self.recommended and " (R)" or "")

    def to_ascii_row(self):
        return "{0} {1}".format(self.short_status, self.friendly_name)

    def to_stdout(self, indentation_level=0, filter=None, expand=False,  # pylint: disable=redefined-builtin
                  interactive_data=None):
        prefix = indentation_level * "  "
        if interactive_data is None:
            interactive_data = {}
        if interactive_data:
            if self.status in (Product.Status.INSTALLED,
                               Product.Status.UNAVAILABLE):
                prefix = "     " + prefix
            else:
                counter = interactive_data['counter']
                prefix = "{0:03}) {1}".format(counter, prefix)
                interactive_data['num_prod'][counter] = self
                interactive_data['counter'] += 1

        if not filter or self.matches_filter(filter):
            print(prefix + self.to_ascii_row())

            if (not expand and self.status is not Product.Status.INSTALLED) or \
               self.status == Product.Status.UNAVAILABLE:
                return

            indentation_level += 1
            for ext in self.extensions:
                ext.to_stdout(indentation_level=indentation_level,
                              expand=expand,
                              filter=filter,
                              interactive_data=interactive_data)

    def _parse_extensions(self, data):
        for extension in data:
            self.extensions.append(Product(extension))

    def matches_filter(self, filter):  # pylint: disable=redefined-builtin
        if not self.extensions:
            return filter in self.to_ascii_row().lower()
        else:
            for ext in self.extensions:
                if ext.matches_filter(filter):
                    return True

        return False


def parse_products(data, log):
    """
    Parses the data returned by SUSE Manager list products API.
    Returns a list of the Products.
    """
    log.info("Parsing products...")

    products = []

    for pdata in data:
        prd = Product(pdata)
        prd.isBase = True
        log.debug("Found product '{0} {1}'".format(prd.friendly_name, prd.arch))
        products.append(prd)

    return products
