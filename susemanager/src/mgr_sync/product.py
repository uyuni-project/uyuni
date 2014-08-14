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

from enum import Enum


class Product(object):

    class Status(str, Enum):
        INSTALLED = "INSTALLED"
        AVAILABLE = "AVAILABLE"

    def __init__(self, data):
        self.arch = data["arch"]
        self.friendly_name = data["friendly_name"]
        self.status = Product.Status(data["status"].upper())
        self.extensions = []
        self._parse_extensions(data["extensions"])

    @property
    def short_status(self):
        # pylint: disable=E1101
        return "[%s]" % str(self.status.value)[0]

    def to_ascii_row(self):
        return "{0} {1} ({2})".format(self.short_status, self.friendly_name,
                                      self.arch)

    def to_stdout(self, indentation_level=0):
        prefix = indentation_level * "  "
        print(prefix + self.to_ascii_row())
        indentation_level += 1
        for ext in self.extensions:
            ext.to_stdout(indentation_level=indentation_level)

    def _parse_extensions(self, data):
        for extension in data:
            self.extensions.append(Product(extension))


def parse_products(data):
    """
    Parses the data returned by SUSE Manager list products API.
    Returns a list of the Products.
    """

    products = []

    for p in data:
        products.append(Product(p))

    return products

