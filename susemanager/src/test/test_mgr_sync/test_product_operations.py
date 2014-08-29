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

from mock import MagicMock

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from helper import ConsoleRecorder, read_data_from_fixture

from spacewalk.susemanager.mgr_sync.cli import get_options
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
            'list_products_simplified.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)

        expected_output = """Available Products:


Status:
  - I - channel is installed
  - A - channel is not installed, but is available

[A] RES 4 (x86_64)
[A] RES 4 (x86_64)
[A] RES 5 (x86_64)
[A] RES 6 (x86_64)
[A] SUSE Linux Enterprise Desktop 11 SP2 (x86_64)
  [A] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
[A] SUSE Linux Enterprise Desktop 11 SP3 (x86_64)
  [A] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
[A] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP1 (x86_64)
  [A] SUSE Linux Enterprise Software Development Kit 11 SP1 (x86_64)
[A] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP2 (x86_64)
  [A] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
[A] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP3 (x86_64)
  [A] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
  [A] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
[A] SUSE Linux Enterprise Server 10 SP3 (x86_64)
  [A] SUSE Linux Enterprise Software Development Kit 10 SP3 (x86_64)
[I] SUSE Linux Enterprise Server 10 SP4 (x86_64)
  [A] SUSE Linux Enterprise Software Development Kit 10 SP4 (x86_64)
[A] SUSE Linux Enterprise Server 11 SP1 (x86_64)
  [A] Novell Open Enterprise Server 2 11 (x86_64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP1 (x86_64)
  [A] SUSE Linux Enterprise Point of Service 11 SP1 (x86_64)
  [A] SUSE Linux Enterprise Real Time 11 (x86_64)
  [A] SUSE Linux Enterprise Software Development Kit 11 SP1 (x86_64)
  [A] SUSE Linux Enterprise Subscription Management Tool 11 (x86_64)
[A] SUSE Linux Enterprise Server 11 SP2 (x86_64)
  [A] Novell Open Enterprise Server 2 11.1 (x86_64)
  [A] SUSE Cloud 1.0 (x86_64)
  [A] SUSE Lifecycle Management Server 1.3 (x86_64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP2 (x86_64)
  [A] SUSE Linux Enterprise Real Time 11 (x86_64)
  [A] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
  [A] SUSE Linux Enterprise Subscription Management Tool 11 SP2 (x86_64)
  [A] SUSE WebYaST 1.3 (x86_64)
[I] SUSE Linux Enterprise Server 11 SP3 (x86_64)
  [A] Novell Open Enterprise Server 2 11.2 (x86_64)
  [A] SUSE Cloud 2.0 (x86_64)
  [A] SUSE Cloud 3 (x86_64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP3 (x86_64)
  [A] SUSE Linux Enterprise Point of Service 11 SP3 (x86_64)
  [A] SUSE Linux Enterprise Real Time 11 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
  [A] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
  [A] SUSE WebYaST 1.3 (x86_64)
[A] SUSE Linux Enterprise Server 11 SP3 VMWare (x86_64)
  [A] SUSE Linux Enterprise High Availability Extension 11 SP3 (x86_64)
  [A] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
  [A] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
  [A] SUSE WebYaST 1.3 (x86_64)
[A] SUSE Manager Proxy 1.2 (x86_64)
[A] SUSE Manager Proxy 1.7 (x86_64)
[A] SUSE Manager Proxy 2.1 (x86_64)
[A] SUSE Manager Server 2.1 (x86_64)"""

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
[I] SUSE Linux Enterprise Server 11 SP3 (x86_64)
  [A] SUSE Cloud 2.0 (x86_64)
  [A] SUSE Cloud 3 (x86_64)"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listProducts",
            self.fake_auth_token)

    def test_list_products_with_interactive_mode_enabled(self):
        """ Test listing products """

        stubbed_xmlrpm_call = MagicMock(return_value=read_data_from_fixture(
            'list_products_simplified.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync._list_products(
                filter=None, show_interactive_numbers=True)

        expected_output = """Available Products:


Status:
  - I - channel is installed
  - A - channel is not installed, but is available

001) [A] RES 4 (x86_64)
002) [A] RES 4 (x86_64)
003) [A] RES 5 (x86_64)
004) [A] RES 6 (x86_64)
005) [A] SUSE Linux Enterprise Desktop 11 SP2 (x86_64)
006)   [A] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
007) [A] SUSE Linux Enterprise Desktop 11 SP3 (x86_64)
008)   [A] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
009) [A] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP1 (x86_64)
010)   [A] SUSE Linux Enterprise Software Development Kit 11 SP1 (x86_64)
011) [A] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP2 (x86_64)
012)   [A] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
013) [A] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP3 (x86_64)
014)   [A] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
015)   [A] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
016) [A] SUSE Linux Enterprise Server 10 SP3 (x86_64)
017)   [A] SUSE Linux Enterprise Software Development Kit 10 SP3 (x86_64)
     [I] SUSE Linux Enterprise Server 10 SP4 (x86_64)
018)   [A] SUSE Linux Enterprise Software Development Kit 10 SP4 (x86_64)
019) [A] SUSE Linux Enterprise Server 11 SP1 (x86_64)
020)   [A] Novell Open Enterprise Server 2 11 (x86_64)
021)   [A] SUSE Linux Enterprise High Availability Extension 11 SP1 (x86_64)
022)   [A] SUSE Linux Enterprise Point of Service 11 SP1 (x86_64)
023)   [A] SUSE Linux Enterprise Real Time 11 (x86_64)
024)   [A] SUSE Linux Enterprise Software Development Kit 11 SP1 (x86_64)
025)   [A] SUSE Linux Enterprise Subscription Management Tool 11 (x86_64)
026) [A] SUSE Linux Enterprise Server 11 SP2 (x86_64)
027)   [A] Novell Open Enterprise Server 2 11.1 (x86_64)
028)   [A] SUSE Cloud 1.0 (x86_64)
029)   [A] SUSE Lifecycle Management Server 1.3 (x86_64)
030)   [A] SUSE Linux Enterprise High Availability Extension 11 SP2 (x86_64)
031)   [A] SUSE Linux Enterprise Real Time 11 (x86_64)
032)   [A] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
033)   [A] SUSE Linux Enterprise Subscription Management Tool 11 SP2 (x86_64)
034)   [A] SUSE WebYaST 1.3 (x86_64)
     [I] SUSE Linux Enterprise Server 11 SP3 (x86_64)
035)   [A] Novell Open Enterprise Server 2 11.2 (x86_64)
036)   [A] SUSE Cloud 2.0 (x86_64)
037)   [A] SUSE Cloud 3 (x86_64)
038)   [A] SUSE Linux Enterprise High Availability Extension 11 SP3 (x86_64)
039)   [A] SUSE Linux Enterprise Point of Service 11 SP3 (x86_64)
040)   [A] SUSE Linux Enterprise Real Time 11 (x86_64)
       [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
041)   [A] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
042)   [A] SUSE WebYaST 1.3 (x86_64)
043) [A] SUSE Linux Enterprise Server 11 SP3 VMWare (x86_64)
044)   [A] SUSE Linux Enterprise High Availability Extension 11 SP3 (x86_64)
045)   [A] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
046)   [A] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
047)   [A] SUSE WebYaST 1.3 (x86_64)
048) [A] SUSE Manager Proxy 1.2 (x86_64)
049) [A] SUSE Manager Proxy 1.7 (x86_64)
050) [A] SUSE Manager Proxy 2.1 (x86_64)
051) [A] SUSE Manager Server 2.1 (x86_64)"""

        self.assertEqual(recorder.stdout, expected_output.split("\n"))

