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

from spacewalk.common.suseLib import BackendType
from spacewalk.susemanager.mgr_sync.cli import get_options
from spacewalk.susemanager.mgr_sync.mgr_sync import MgrSync
from spacewalk.susemanager.mgr_sync.channel import Channel
from spacewalk.susemanager.mgr_sync.product import parse_products, Product


class ProductOperationsTest(unittest.TestCase):

    def setUp(self):
        self.mgr_sync = MgrSync()
        self.mgr_sync.conn = MagicMock()
        self.fake_auth_token = "fake_token"
        self.mgr_sync.auth.token = MagicMock(
            return_value=self.fake_auth_token)
        self.mgr_sync.config.write = MagicMock()

        patcher = patch('spacewalk.susemanager.mgr_sync.mgr_sync.current_cc_backend')
        mock = patcher.start()
        mock.return_value = BackendType.SCC

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

    def test_list_products_with_expand_enabled(self):
        """ Test listing products with expand enabled """

        options = get_options("list product -e".split())
        stubbed_xmlrpm_call = MagicMock(return_value=read_data_from_fixture(
            'list_products_simplified.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)

        expected_output = """Available Products:


Status:
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

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
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

[ ] RES 4 (x86_64)
[ ] RES 4 (x86_64)
[ ] RES 5 (x86_64)
[ ] RES 6 (x86_64)
[ ] SUSE Linux Enterprise Desktop 11 SP2 (x86_64)
[ ] SUSE Linux Enterprise Desktop 11 SP3 (x86_64)
[ ] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP1 (x86_64)
[ ] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP2 (x86_64)
[ ] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP3 (x86_64)
[ ] SUSE Linux Enterprise Server 10 SP3 (x86_64)
[I] SUSE Linux Enterprise Server 10 SP4 (x86_64)
  [ ] SUSE Linux Enterprise Software Development Kit 10 SP4 (x86_64)
[ ] SUSE Linux Enterprise Server 11 SP1 (x86_64)
[ ] SUSE Linux Enterprise Server 11 SP2 (x86_64)
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
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

[ ] SUSE Manager Proxy 1.2 (x86_64)
[ ] SUSE Manager Proxy 1.7 (x86_64)
[ ] SUSE Manager Proxy 2.1 (x86_64)"""

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
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

[ ] SUSE Manager Proxy 1.2 (x86_64)
[ ] SUSE Manager Proxy 1.7 (x86_64)
[ ] SUSE Manager Proxy 2.1 (x86_64)"""

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
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

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
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

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
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

001) [ ] RES 4 (x86_64)
002) [ ] RES 4 (x86_64)
003) [ ] RES 5 (x86_64)
004) [ ] RES 6 (x86_64)
005) [ ] SUSE Linux Enterprise Desktop 11 SP2 (x86_64)
006) [ ] SUSE Linux Enterprise Desktop 11 SP3 (x86_64)
007) [ ] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP1 (x86_64)
008) [ ] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP2 (x86_64)
009) [ ] SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP3 (x86_64)
010) [ ] SUSE Linux Enterprise Server 10 SP3 (x86_64)
     [I] SUSE Linux Enterprise Server 10 SP4 (x86_64)
011)   [ ] SUSE Linux Enterprise Software Development Kit 10 SP4 (x86_64)
012) [ ] SUSE Linux Enterprise Server 11 SP1 (x86_64)
013) [ ] SUSE Linux Enterprise Server 11 SP2 (x86_64)
     [I] SUSE Linux Enterprise Server 11 SP3 (x86_64)
014)   [ ] Novell Open Enterprise Server 2 11.2 (x86_64)
015)   [ ] SUSE Cloud 2.0 (x86_64)
016)   [ ] SUSE Cloud 3 (x86_64)
017)   [ ] SUSE Linux Enterprise High Availability Extension 11 SP3 (x86_64)
018)   [ ] SUSE Linux Enterprise Point of Service 11 SP3 (x86_64)
019)   [ ] SUSE Linux Enterprise Real Time 11 (x86_64)
       [I] SUSE Linux Enterprise Software Development Kit 11 SP3 (x86_64)
020)   [ ] SUSE Linux Enterprise Subscription Management Tool 11 SP3 (x86_64)
021)   [ ] SUSE WebYaST 1.3 (x86_64)
022) [ ] SUSE Linux Enterprise Server 11 SP3 VMWare (x86_64)
023) [ ] SUSE Manager Proxy 1.2 (x86_64)
024) [ ] SUSE Manager Proxy 1.7 (x86_64)
025) [ ] SUSE Manager Proxy 2.1 (x86_64)
026) [ ] SUSE Manager Server 2.1 (x86_64)"""

        self.assertEqual(recorder.stdout, expected_output.split("\n"))

    def test_add_products_interactive_with_mirror(self):
        """ Test adding a product with all the required channels available. """
        mirror_url = "http://smt.suse.de"
        products = read_data_from_fixture('list_products_simplified.data')
        res4 = next(p for p in products
                    if p['friendly_name'] == 'RES 4' and p['arch'] == 'x86_64')
        options = get_options("add product --from-mirror {0}".format(mirror_url).split())
        available_products = parse_products([res4])
        chosen_product = available_products[0]
        self.mgr_sync._fetch_remote_products = MagicMock(
            return_value=available_products)
        stubbed_xmlrpm_call = MagicMock()
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with patch('spacewalk.susemanager.mgr_sync.mgr_sync.cli_ask') as mock:
            mock.return_value = str(
                available_products.index(chosen_product) + 1)
            with ConsoleRecorder() as recorder:
                self.assertEqual(0, self.mgr_sync.run(options))

        expected_output = """Available Products:


Status:
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

001) [ ] RES 4 (x86_64)
Adding channels required by 'RES 4' product
Adding 'res4-as-suse-manager-tools-x86_64' channel
Scheduling reposync for 'res4-as-suse-manager-tools-x86_64' channel
Adding 'rhel-x86_64-as-4' channel
Scheduling reposync for 'rhel-x86_64-as-4' channel
Adding 'res4-as-x86_64' channel
Scheduling reposync for 'res4-as-x86_64' channel
Product successfully added"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        expected_xmlrpc_calls = []
        mandatory_channels = [c for c in chosen_product.channels
                              if not c.optional]
        for channel in mandatory_channels:
            expected_xmlrpc_calls.append(
                call._execute_xmlrpc_method(
                    self.mgr_sync.conn.sync.content,
                    "addChannel",
                    self.fake_auth_token,
                    channel.label,
		    mirror_url))
            expected_xmlrpc_calls.append(
                call._execute_xmlrpc_method(
                    self.mgr_sync.conn.channel.software,
                    "syncRepo",
                    self.fake_auth_token,
                    channel.label))

    def test_add_products_interactive(self):
        """ Test adding a product with all the required channels available. """

        products = read_data_from_fixture('list_products_simplified.data')
        res4 = next(p for p in products
                    if p['friendly_name'] == 'RES 4' and p['arch'] == 'x86_64')
        options = get_options("add product".split())
        available_products = parse_products([res4])
        chosen_product = available_products[0]
        self.mgr_sync._fetch_remote_products = MagicMock(
            return_value=available_products)
        stubbed_xmlrpm_call = MagicMock()
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with patch('spacewalk.susemanager.mgr_sync.mgr_sync.cli_ask') as mock:
            mock.return_value = str(
                available_products.index(chosen_product) + 1)
            with ConsoleRecorder() as recorder:
                self.assertEqual(0, self.mgr_sync.run(options))

        expected_output = """Available Products:


Status:
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

001) [ ] RES 4 (x86_64)
Adding channels required by 'RES 4' product
Adding 'res4-as-suse-manager-tools-x86_64' channel
Scheduling reposync for 'res4-as-suse-manager-tools-x86_64' channel
Adding 'rhel-x86_64-as-4' channel
Scheduling reposync for 'rhel-x86_64-as-4' channel
Adding 'res4-as-x86_64' channel
Scheduling reposync for 'res4-as-x86_64' channel
Product successfully added"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        expected_xmlrpc_calls = []
        mandatory_channels = [c for c in chosen_product.channels
                              if not c.optional]
        for channel in mandatory_channels:
            expected_xmlrpc_calls.append(
                call._execute_xmlrpc_method(
                    self.mgr_sync.conn.sync.content,
                    "addChannel",
                    self.fake_auth_token,
                    channel.label,
                    None))
            expected_xmlrpc_calls.append(
                call._execute_xmlrpc_method(
                    self.mgr_sync.conn.channel.software,
                    "syncRepo",
                    self.fake_auth_token,
                    channel.label))

        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)

    def test_add_products_interactive_with_a_channel_already_installed(self):
        """ Test adding a product with one of the required channels
        already installed """

        products = read_data_from_fixture('list_products_simplified.data')
        res4 = next(p for p in products
                    if p['friendly_name'] == 'RES 4' and p['arch'] == 'x86_64')
        options = get_options("add product".split())
        available_products = parse_products([res4])
        chosen_product = available_products[0]
        self.mgr_sync._fetch_remote_products = MagicMock(
            return_value=available_products)
        stubbed_xmlrpm_call = MagicMock()
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        # set the 1st required channel as already installed
        chosen_product.channels[0].status = Channel.Status.INSTALLED
        channel_to_not_add = chosen_product.channels[0]

        with patch('spacewalk.susemanager.mgr_sync.mgr_sync.cli_ask') as mock:
            mock.return_value = str(
                available_products.index(chosen_product) + 1)
            with ConsoleRecorder() as recorder:
                self.assertEqual(0, self.mgr_sync.run(options))

        expected_output = """Available Products:


Status:
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

001) [ ] RES 4 (x86_64)
Adding channels required by 'RES 4' product
Adding 'res4-as-suse-manager-tools-x86_64' channel
Scheduling reposync for 'res4-as-suse-manager-tools-x86_64' channel
Adding 'rhel-x86_64-as-4' channel
Scheduling reposync for 'rhel-x86_64-as-4' channel
Adding 'res4-as-x86_64' channel
Scheduling reposync for 'res4-as-x86_64' channel
Product successfully added"""
        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        expected_xmlrpc_calls = []
        mandatory_channels = [c for c in chosen_product.channels
                              if not c.optional]
        for channel in mandatory_channels:
            if channel is not channel_to_not_add:
                expected_xmlrpc_calls.append(
                    call._execute_xmlrpc_method(
                        self.mgr_sync.conn.sync.content,
                        "addChannel",
                        self.fake_auth_token,
                        channel.label,
                        None))
            expected_xmlrpc_calls.append(
                call._execute_xmlrpc_method(
                    self.mgr_sync.conn.channel.software,
                    "syncRepo",
                    self.fake_auth_token,
                    channel.label))

        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)

    def test_add_products_interactive_with_a_required_channel_unavailable(self):
        """ Test should not be able to select an unavailable product """

        products = read_data_from_fixture('list_products_simplified.data')
        res4 = next(p for p in products
                    if p['friendly_name'] == 'RES 4' and p['arch'] == 'x86_64')
        options = get_options("add product".split())
        available_products = parse_products([res4])
        chosen_product = available_products[0]
        self.mgr_sync._fetch_remote_products = MagicMock(
            return_value=available_products)
        stubbed_xmlrpm_call = MagicMock()
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        # set the 1st required channel as already installed
        chosen_product.status = Product.Status.UNAVAILABLE

        with patch('spacewalk.susemanager.mgr_sync.mgr_sync.cli_ask') as mock_cli_ask:
            with ConsoleRecorder() as recorder:
                self.assertEqual(0, self.mgr_sync.run(options))
            self.assertFalse(mock_cli_ask.mock_calls)

        expected_output = """Available Products:


Status:
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

     [U] RES 4 (x86_64)
All the available products have already been installed, nothing to do"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        self.assertFalse(stubbed_xmlrpm_call.mock_calls)

    def test_all_available_products_are_already_installed(self):
        """ Test all the available products are already installed"""

        products = read_data_from_fixture('list_products_simplified.data')
        res4 = next(p for p in products
                    if p['friendly_name'] == 'RES 4' and p['arch'] == 'x86_64')
        options = get_options("add product".split())
        available_products = parse_products([res4])
        chosen_product = available_products[0]
        self.mgr_sync._fetch_remote_products = MagicMock(
            return_value=available_products)
        stubbed_xmlrpm_call = MagicMock()
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        # set the product as already installed
        chosen_product.status = Product.Status.INSTALLED

        with patch('spacewalk.susemanager.mgr_sync.mgr_sync.cli_ask') as mock:
            mock.return_value = str(
                available_products.index(chosen_product) + 1)
            with ConsoleRecorder() as recorder:
                try:
                    self.mgr_sync.run(options)
                except SystemExit, ex:
                    self.assertEqual(0, ex.code)

        expected_output = """Available Products:


Status:
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

     [I] RES 4 (x86_64)
All the available products have already been installed, nothing to do"""
        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        self.assertFalse(stubbed_xmlrpm_call.mock_calls)

    def test_add_products_with_an_optional_channel_unavailable(self):
        """ Test adding a product with an optional channel unavailable. """

        products = read_data_from_fixture('list_products_simplified.data')
        res4 = next(p for p in products
                    if p['friendly_name'] == 'RES 4' and p['arch'] == 'x86_64')
        options = get_options("add product".split())
        available_products = parse_products([res4])
        chosen_product = available_products[0]
        self.mgr_sync._fetch_remote_products = MagicMock(
            return_value=available_products)
        stubbed_xmlrpm_call = MagicMock()
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        # set the 1st required channel as optional and unavailable
        chosen_product.channels[0].optional = True
        chosen_product.channels[0].status = Channel.Status.UNAVAILABLE

        with patch('spacewalk.susemanager.mgr_sync.mgr_sync.cli_ask') as mock:
            mock.return_value = str(
                available_products.index(chosen_product) + 1)
            with ConsoleRecorder() as recorder:
                self.assertEqual(0, self.mgr_sync.run(options))

        expected_output = """Available Products:


Status:
  - [I] - product is installed
  - [ ] - product is not installed, but is available
  - [U] - product is unavailable

001) [ ] RES 4 (x86_64)
Adding channels required by 'RES 4' product
Adding 'rhel-x86_64-as-4' channel
Scheduling reposync for 'rhel-x86_64-as-4' channel
Adding 'res4-as-x86_64' channel
Scheduling reposync for 'res4-as-x86_64' channel
Product successfully added"""
        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        expected_xmlrpc_calls = []
        mandatory_channels = [c for c in chosen_product.channels
                              if not c.optional]
        for channel in mandatory_channels:
            expected_xmlrpc_calls.append(
                call._execute_xmlrpc_method(
                    self.mgr_sync.conn.sync.content,
                    "addChannel",
                    self.fake_auth_token,
                    channel.label,
                    None))
            expected_xmlrpc_calls.append(
                call._execute_xmlrpc_method(
                    self.mgr_sync.conn.channel.software,
                    "syncRepo",
                    self.fake_auth_token,
                    channel.label))

        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)

