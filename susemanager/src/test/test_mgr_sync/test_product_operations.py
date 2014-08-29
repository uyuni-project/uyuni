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
  - [I] - channel is installed
  - [ ] - channel is not installed, but is available

[ ] RES 4 (x86_64)
[ ] RES 4 (x86_64)
[ ] RES 5 (x86_64)
[ ] RES 6 (x86_64)
[ ] SUSE Linux Enterprise Desktop 11 SP2 (x86_64)
  [ ] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
[ ] SUSE Linux Enterprise Desktop 11 SP3 (x86_64)
  [ ] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
[ ] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP1 (x86_64)
  [ ] SUSE Linux Enterprise Software Development Kit 11 SP1 (x86_64)
[ ] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP2 (x86_64)
  [ ] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
[ ] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP3 (x86_64)
  [ ] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
  [ ] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
[ ] SUSE Linux Enterprise Server 10 SP3 (x86_64)
  [ ] SUSE Linux Enterprise Software Development Kit 10 SP3 (x86_64)
[I] SUSE Linux Enterprise Server 10 SP4 (x86_64)
  [ ] SUSE Linux Enterprise Software Development Kit 10 SP4 (x86_64)
[ ] SUSE Linux Enterprise Server 11 SP1 (x86_64)
  [ ] Novell Open Enterprise Server 2 11 (x86_64)
  [ ] SUSE Linux Enterprise High Availability Extension 11 SP1 (x86_64)
  [ ] SUSE Linux Enterprise Point of Service 11 SP1 (x86_64)
  [ ] SUSE Linux Enterprise Real Time 11 (x86_64)
  [ ] SUSE Linux Enterprise Software Development Kit 11 SP1 (x86_64)
  [ ] SUSE Linux Enterprise Subscription Management Tool 11 (x86_64)
[ ] SUSE Linux Enterprise Server 11 SP2 (x86_64)
  [ ] Novell Open Enterprise Server 2 11.1 (x86_64)
  [ ] SUSE Cloud 1.0 (x86_64)
  [ ] SUSE Lifecycle Management Server 1.3 (x86_64)
  [ ] SUSE Linux Enterprise High Availability Extension 11 SP2 (x86_64)
  [ ] SUSE Linux Enterprise Real Time 11 (x86_64)
  [ ] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
  [ ] SUSE Linux Enterprise Subscription Management Tool 11 SP2 (x86_64)
  [ ] SUSE WebYaST 1.3 (x86_64)
[I] SUSE Linux Enterprise Server 11 SP3 (x86_64)
  [ ] Novell Open Enterprise Server 2 11.2 (x86_64)
  [ ] SUSE Cloud 2.0 (x86_64)
  [ ] SUSE Cloud 3 (x86_64)
  [ ] SUSE Linux Enterprise High Availability Extension 11 SP3 (x86_64)
  [ ] SUSE Linux Enterprise Point of Service 11 SP3 (x86_64)
  [ ] SUSE Linux Enterprise Real Time 11 (x86_64)
  [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
  [ ] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
  [ ] SUSE WebYaST 1.3 (x86_64)
[ ] SUSE Linux Enterprise Server 11 SP3 VMWare (x86_64)
  [ ] SUSE Linux Enterprise High Availability Extension 11 SP3 (x86_64)
  [ ] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
  [ ] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
  [ ] SUSE WebYaST 1.3 (x86_64)
[ ] SUSE Manager Proxy 1.2 (x86_64)
[ ] SUSE Manager Proxy 1.7 (x86_64)
[ ] SUSE Manager Proxy 2.1 (x86_64)
[ ] SUSE Manager Server 2.1 (x86_64)"""

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
  - [I] - channel is installed
  - [ ] - channel is not installed, but is available

[ ] SUSE Manager Proxy 1.2 (x86_64)
[ ] SUSE Manager Proxy 1.7 (x86_64)
[ ] SUSE Manager Proxy 2.1 (x86_64)"""

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
  - [I] - channel is installed
  - [ ] - channel is not installed, but is available

[ ] SUSE Linux Enterprise Server 11 SP2 (x86_64)
  [ ] SUSE Cloud 1.0 (x86_64)
[I] SUSE Linux Enterprise Server 11 SP3 (x86_64)
  [ ] SUSE Cloud 2.0 (x86_64)
  [ ] SUSE Cloud 3 (x86_64)"""

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
  - [I] - channel is installed
  - [ ] - channel is not installed, but is available

001) [ ] RES 4 (x86_64)
002) [ ] RES 4 (x86_64)
003) [ ] RES 5 (x86_64)
004) [ ] RES 6 (x86_64)
005) [ ] SUSE Linux Enterprise Desktop 11 SP2 (x86_64)
006)   [ ] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
007) [ ] SUSE Linux Enterprise Desktop 11 SP3 (x86_64)
008)   [ ] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
009) [ ] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP1 (x86_64)
010)   [ ] SUSE Linux Enterprise Software Development Kit 11 SP1 (x86_64)
011) [ ] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP2 (x86_64)
012)   [ ] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
013) [ ] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP3 (x86_64)
014)   [ ] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
015)   [ ] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
016) [ ] SUSE Linux Enterprise Server 10 SP3 (x86_64)
017)   [ ] SUSE Linux Enterprise Software Development Kit 10 SP3 (x86_64)
     [I] SUSE Linux Enterprise Server 10 SP4 (x86_64)
018)   [ ] SUSE Linux Enterprise Software Development Kit 10 SP4 (x86_64)
019) [ ] SUSE Linux Enterprise Server 11 SP1 (x86_64)
020)   [ ] Novell Open Enterprise Server 2 11 (x86_64)
021)   [ ] SUSE Linux Enterprise High Availability Extension 11 SP1 (x86_64)
022)   [ ] SUSE Linux Enterprise Point of Service 11 SP1 (x86_64)
023)   [ ] SUSE Linux Enterprise Real Time 11 (x86_64)
024)   [ ] SUSE Linux Enterprise Software Development Kit 11 SP1 (x86_64)
025)   [ ] SUSE Linux Enterprise Subscription Management Tool 11 (x86_64)
026) [ ] SUSE Linux Enterprise Server 11 SP2 (x86_64)
027)   [ ] Novell Open Enterprise Server 2 11.1 (x86_64)
028)   [ ] SUSE Cloud 1.0 (x86_64)
029)   [ ] SUSE Lifecycle Management Server 1.3 (x86_64)
030)   [ ] SUSE Linux Enterprise High Availability Extension 11 SP2 (x86_64)
031)   [ ] SUSE Linux Enterprise Real Time 11 (x86_64)
032)   [ ] SUSE Linux Enterprise Software Development Kit 11 SP2 (x86_64)
033)   [ ] SUSE Linux Enterprise Subscription Management Tool 11 SP2 (x86_64)
034)   [ ] SUSE WebYaST 1.3 (x86_64)
     [I] SUSE Linux Enterprise Server 11 SP3 (x86_64)
035)   [ ] Novell Open Enterprise Server 2 11.2 (x86_64)
036)   [ ] SUSE Cloud 2.0 (x86_64)
037)   [ ] SUSE Cloud 3 (x86_64)
038)   [ ] SUSE Linux Enterprise High Availability Extension 11 SP3 (x86_64)
039)   [ ] SUSE Linux Enterprise Point of Service 11 SP3 (x86_64)
040)   [ ] SUSE Linux Enterprise Real Time 11 (x86_64)
       [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
041)   [ ] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
042)   [ ] SUSE WebYaST 1.3 (x86_64)
043) [ ] SUSE Linux Enterprise Server 11 SP3 VMWare (x86_64)
044)   [ ] SUSE Linux Enterprise High Availability Extension 11 SP3 (x86_64)
045)   [ ] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
046)   [ ] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
047)   [ ] SUSE WebYaST 1.3 (x86_64)
048) [ ] SUSE Manager Proxy 1.2 (x86_64)
049) [ ] SUSE Manager Proxy 1.7 (x86_64)
050) [ ] SUSE Manager Proxy 2.1 (x86_64)
051) [ ] SUSE Manager Server 2.1 (x86_64)"""

        self.assertEqual(recorder.stdout, expected_output.split("\n"))

