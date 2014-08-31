#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2014 Novell, Inc.
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

import os
import sys
import unittest2 as unittest

from spacewalk.susemanager.mgr_sync.product import parse_products, Product

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from helper import read_data_from_fixture, ConsoleRecorder


class ProductTest(unittest.TestCase):

    def setUp(self):
        self.products = parse_products(read_data_from_fixture("list_products.data"))


    def test_parse_products(self):

        res4 = next(p for p in self.products if p.friendly_name == 'RES 4' and p.arch == 'i386')
        self.assertEqual([], res4.extensions)
        self.assertEqual(Product.Status.AVAILABLE, res4.status)

        sles11sp3_s390x = next(p for p in self.products if p.friendly_name == 'SUSE Linux Enterprise Server 11 SP3' and p.arch == 's390x')
        self.assertEqual(2, len(sles11sp3_s390x.extensions))
        self.assertEqual(Product.Status.AVAILABLE, sles11sp3_s390x.status)

        sle_ha = next(p for p in sles11sp3_s390x.extensions if p.friendly_name == 'SUSE Linux Enterprise High Availability Extension 11 SP3')
        self.assertEqual([], sle_ha.extensions)
        self.assertEqual(Product.Status.AVAILABLE, sle_ha.status)
        self.assertEqual(sle_ha.arch, sles11sp3_s390x.arch)

        sles_sdk = next(p for p in sles11sp3_s390x.extensions if p.friendly_name == 'SUSE Linux Enterprise Software Development Kit 11 SP3')
        self.assertEqual([], sles_sdk.extensions)
        self.assertEqual(Product.Status.INSTALLED, sles_sdk.status)
        self.assertEqual(sles_sdk.arch, sles11sp3_s390x.arch)

    def test_to_stdout(self):
        sles11sp3_s390x = next(p for p in self.products if p.friendly_name == 'SUSE Linux Enterprise Server 11 SP3' and p.arch == 's390x')
        with ConsoleRecorder() as recorder:
            sles11sp3_s390x.to_stdout()

        expected_output = """[ ] SUSE Linux Enterprise Server 11 SP3 (s390x)
  [ ] SUSE Linux Enterprise High Availability Extension 11 SP3 (s390x)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (s390x)"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        res4 = next(p for p in self.products if p.friendly_name == 'RES 4' and p.arch == 'i386')
        with ConsoleRecorder() as recorder:
            res4.to_stdout()

        expected_output = ["[ ] RES 4 (i386)"]
        self.assertEqual(expected_output, recorder.stdout)

