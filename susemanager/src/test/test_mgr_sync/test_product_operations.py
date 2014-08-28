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

import unittest2 as unittest

import os.path
import sys

from mock import MagicMock, call, patch

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from helper import ConsoleRecorder, read_data_from_fixture

from spacewalk.susemanager.mgr_sync.cli import get_options
from spacewalk.susemanager.mgr_sync.channel import parse_channels, Channel
from spacewalk.susemanager.mgr_sync.mgr_sync import MgrSync


class ProductOperationsTest(unittest.TestCase):

    def setUp(self):
        self.mgr_sync = MgrSync()
        self.mgr_sync.conn = MagicMock()
        self.fake_auth_token = "fake_token"
        self.mgr_sync.auth.token = MagicMock(
            return_value=self.fake_auth_token)
        self.mgr_sync.config.write = MagicMock()

    def test_list_emtpy_product(self):
        options = get_options("list product".split())
        stubbed_xmlrpm_call = MagicMock(return_value=[])
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)
        self.assertEqual(recorder.stdout, ["No products found."])

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listProducts",
            self.fake_auth_token)

    def test_list_product(self):
        options = get_options("list product".split())
        stubbed_xmlrpm_call = MagicMock(return_value=[])
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)
        self.assertEqual(recorder.stdout, ["No products found."])

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listProducts",
            self.fake_auth_token)

    def test_list_products(self):
        """ Test listing products """

        options = get_options("list product".split())
        stubbed_xmlrpm_call = MagicMock(return_value=read_data_from_fixture(
            'list_products.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)

        expected_output = """Available Products:


Status:
  - I - channel is installed
  - A - channel is not installed, but is available

[A] RES 4 (i386)
[A] RES 4 (x86_64)
[A] RES 5 (i386)
[A] RES 5 (x86_64)
[A] RES 6 (i386)
[A] RES 6 (x86_64)
[A] SUSE Linux Enterprise Desktop 11 SP2 (i586)
  [A] SUSE Linux Enterprise Desktop 11 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (i586)
[A] SUSE Linux Enterprise Desktop 11 SP2 (x86_64)
  [A] SUSE Linux Enterprise Desktop 11 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
[A] SUSE Linux Enterprise Desktop 11 SP2 HP-BNB-2010 (i586)
  [A] SUSE Linux Enterprise Desktop 11 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (i586)
[A] SUSE Linux Enterprise Desktop 11 SP2 HP-BNB-2010 (x86_64)
  [A] SUSE Linux Enterprise Desktop 11 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
[A] SUSE Linux Enterprise Desktop 11 SP2 HP-CNB (i586)
  [A] SUSE Linux Enterprise Desktop 11 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (i586)
[A] SUSE Linux Enterprise Desktop 11 SP2 HP-CNB (x86_64)
  [A] SUSE Linux Enterprise Desktop 11 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
[A] SUSE Linux Enterprise Desktop 11 SP3 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (i586)
[A] SUSE Linux Enterprise Desktop 11 SP3 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
[A] SUSE Linux Enterprise Desktop 11 SP3 HP-BNB-2013 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (i586)
[A] SUSE Linux Enterprise Desktop 11 SP3 HP-BNB-2013 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
[A] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP1 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP1 (x86_64)
[A] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP2 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
[A] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP3 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
[A] SUSE Linux Enterprise Server 10 SP3 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 10 SP3 (i586)
[A] SUSE Linux Enterprise Server 10 SP3 (ia64)
  [I] SUSE Linux Enterprise Software Development Kit 10 SP3 (ia64)
[A] SUSE Linux Enterprise Server 10 SP3 (ppc)
  [I] SUSE Linux Enterprise Software Development Kit 10 SP3 (ppc)
[A] SUSE Linux Enterprise Server 10 SP3 (s390x)
  [I] SUSE Linux Enterprise Software Development Kit 10 SP3 (s390x)
[A] SUSE Linux Enterprise Server 10 SP3 (x86_64)
  [A] SUSE Linux Enterprise Server 10 SP3 online (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 10 SP3 (x86_64)
[A] SUSE Linux Enterprise Server 10 SP4 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 10 SP4 (i586)
[A] SUSE Linux Enterprise Server 10 SP4 (ia64)
  [I] SUSE Linux Enterprise Software Development Kit 10 SP4 (ia64)
[A] SUSE Linux Enterprise Server 10 SP4 (ppc)
  [I] SUSE Linux Enterprise Software Development Kit 10 SP4 (ppc)
[A] SUSE Linux Enterprise Server 10 SP4 (s390x)
  [I] SUSE Linux Enterprise Software Development Kit 10 SP4 (s390x)
[I] SUSE Linux Enterprise Server 10 SP4 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 10 SP4 (x86_64)
[A] SUSE Linux Enterprise Server 11 SP1 (i586)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP1 (i586)
  [A] SUSE Linux Enterprise Point of Service 11 SP1 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP1 (i586)
[A] SUSE Linux Enterprise Server 11 SP1 (ia64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP1 (ia64)
  [I] SUSE Linux Enterprise Server 11 SP1 VMWare (ia64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP1 (ia64)
[A] SUSE Linux Enterprise Server 11 SP1 (ppc64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP1 (ppc64)
  [I] SUSE Linux Enterprise Server 11 SP1 VMWare (ppc64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP1 (ppc64)
[A] SUSE Linux Enterprise Server 11 SP1 (s390x)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP1 (s390x)
  [I] SUSE Linux Enterprise Server 11 SP1 VMWare (s390x)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP1 (s390x)
[A] SUSE Linux Enterprise Server 11 SP1 (x86_64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP1 (x86_64)
  [A] SUSE Linux Enterprise Point of Service 11 SP1 (x86_64)
  [A] SUSE Linux Enterprise Real Time 11 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP1 (x86_64)
[A] SUSE Linux Enterprise Server 11 SP1 VMWare (i586)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP1 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP1 (i586)
[A] SUSE Linux Enterprise Server 11 SP1 VMWare (x86_64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP1 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP1 (x86_64)
[A] SUSE Linux Enterprise Server 11 SP2 (i586)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP2 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (i586)
[A] SUSE Linux Enterprise Server 11 SP2 (ia64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP2 (ia64)
  [A] SUSE Linux Enterprise Server 11 SP2 VMWare (ia64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (ia64)
[A] SUSE Linux Enterprise Server 11 SP2 (ppc64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP2 (ppc64)
  [A] SUSE Linux Enterprise Server 11 SP2 VMWare (ppc64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (ppc64)
[A] SUSE Linux Enterprise Server 11 SP2 (s390x)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP2 (s390x)
  [A] SUSE Linux Enterprise Server 11 SP2 VMWare (s390x)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (s390x)
[A] SUSE Linux Enterprise Server 11 SP2 (x86_64)
  [A] SUSE Cloud 1.0 (x86_64)
  [A] SUSE Lifecycle Management Server 1.3 (x86_64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP2 (x86_64)
  [A] SUSE Linux Enterprise Real Time 11 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
[A] SUSE Linux Enterprise Server 11 SP2 VMWare (i586)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP2 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (i586)
[A] SUSE Linux Enterprise Server 11 SP2 VMWare (x86_64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP2 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
[A] SUSE Linux Enterprise Server 11 SP3 (i586)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP3 (i586)
  [A] SUSE Linux Enterprise Point of Service 11 SP3 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (i586)
[A] SUSE Linux Enterprise Server 11 SP3 (ia64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP3 (ia64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (ia64)
[A] SUSE Linux Enterprise Server 11 SP3 (ppc64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP3 (ppc64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (ppc64)
[A] SUSE Linux Enterprise Server 11 SP3 (s390x)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP3 (s390x)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (s390x)
[A] SUSE Linux Enterprise Server 11 SP3 (x86_64)
  [A] SUSE Cloud 2.0 (x86_64)
  [A] SUSE Cloud 3 (x86_64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP3 (x86_64)
  [A] SUSE Linux Enterprise Point of Service 11 SP3 (x86_64)
  [A] SUSE Linux Enterprise Real Time 11 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
[A] SUSE Linux Enterprise Server 11 SP3 VMWare (i586)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP3 (i586)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (i586)
[A] SUSE Linux Enterprise Server 11 SP3 VMWare (x86_64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP3 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
[A] SUSE Manager Proxy 1.2 (x86_64)
[A] SUSE Manager Proxy 1.7 (x86_64)
[A] SUSE Manager Proxy 2.1 (x86_64)
[A] SUSE Manager Server 2.1 (s390x)
[A] SUSE Manager Server 2.1 (x86_64)
[A] SUSE Studio OnSite 1.3 (x86_64)"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listProducts",
            self.fake_auth_token)

    def test_list_products_with_filtering(self):
        """ Test listing products with filtering"""

        options = get_options("list product --filter proxy".split())
        stubbed_xmlrpm_call = MagicMock(return_value=read_data_from_fixture(
            'list_products.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)

        expected_output = """Available Products:


Status:
  - I - channel is installed
  - A - channel is not installed, but is available

[A] SUSE Manager Proxy 1.2 (x86_64)
[A] SUSE Manager Proxy 1.7 (x86_64)
[A] SUSE Manager Proxy 2.1 (x86_64)"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listProducts",
            self.fake_auth_token)

    def test_list_products_with_filtering_matches_also_children(self):
        """ Test listing products with filtering should match children even when
        their parent does not.
        """

        options = get_options("list product --filter cloud".split())
        stubbed_xmlrpm_call = MagicMock(return_value=read_data_from_fixture(
            'list_products.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)

        expected_output = """Available Products:


Status:
  - I - channel is installed
  - A - channel is not installed, but is available

[A] SUSE Linux Enterprise Server 11 SP2 (x86_64)
  [A] SUSE Cloud 1.0 (x86_64)
[A] SUSE Linux Enterprise Server 11 SP3 (x86_64)
  [A] SUSE Cloud 2.0 (x86_64)
  [A] SUSE Cloud 3 (x86_64)"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listProducts",
            self.fake_auth_token)

