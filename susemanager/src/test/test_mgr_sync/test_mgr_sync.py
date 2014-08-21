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

from mock import MagicMock, PropertyMock, call, patch

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from helper import CaptureStdout, FakeStdin, read_data_from_fixture

from spacewalk.susemanager.mgr_sync.cli import get_options
from spacewalk.susemanager.mgr_sync.mgr_sync import MgrSync


class MgrSyncTest(unittest.TestCase):

    def setUp(self):
        self.mgr_sync = MgrSync()
        self.mgr_sync.conn = MagicMock()
        self.fake_auth_token = "fake_token"
        type(self.mgr_sync.auth).token = PropertyMock(
            return_value=self.fake_auth_token)

    def test_list_emtpy_channel(self):
        options = get_options("list channel".split())
        stubbed_xmlrpm_call = MagicMock(return_value=[])
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with CaptureStdout() as output:
            self.mgr_sync.run(options)
        self.assertEqual(output, ["No channels found."])

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listChannels",
            self.fake_auth_token)

    def test_list_channels(self):
        options = get_options("list channels".split())
        stubbed_xmlrpm_call = MagicMock(return_value=[])
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with CaptureStdout() as output:
            self.mgr_sync.run(options)
        self.assertEqual(output, ["No channels found."])

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listChannels",
            self.fake_auth_token)

    def test_refresh(self):
        """ Test the refresh action """

        options = get_options("refresh".split())
        stubbed_xmlrpm_call = MagicMock(return_value=True)
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with CaptureStdout() as output:
            self.mgr_sync.run(options)

        expected_output = """Refreshing Channels                            [DONE]
Refreshing Channel families                    [DONE]
Refreshing SUSE products                       [DONE]
Refreshing SUSE Product channels               [DONE]
Refreshing Subscriptions                       [DONE]
Refreshing Upgrade paths                       [DONE]"""

        self.assertEqual(expected_output.split("\n"), output)

        expected_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeChannels",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeChannelFamilies",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeProducts",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeProductChannels",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeSubscriptions",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeUpgradePaths",
                                        self.fake_auth_token)
        ]
        stubbed_xmlrpm_call.assert_has_calls(expected_calls)

    def test_list_channels(self):
        """ Testing list channel output """
        options = get_options("list channel".split())
        stubbed_xmlrpm_call = MagicMock(return_value=read_data_from_fixture('list_channels_simplified.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with CaptureStdout() as output:
            self.mgr_sync.run(options)
        expected_output = """Available Channels:


Status:
  - I - channel is installed
  - A - channel is not installed, but is available
  - U - channel is unavailable

[A] RHEL i386 AS 4 RES 4
[I] SLES10-SP4-Pool for x86_64 SUSE Linux Enterprise Server 10 SP4 x86_64
    [A] SLE10-SDK-SP4-Pool for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit
    [I] SLE10-SDK-SP4-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit"""

        self.assertEqual(expected_output.split("\n"), output)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listChannels",
            self.fake_auth_token)

    def test_list_channels_expand_enabled(self):
        """ Testing list channel output when expand option is toggled """
        options = get_options("list channel -e".split())
        stubbed_xmlrpm_call = MagicMock(
            return_value=read_data_from_fixture(
                'list_channels_simplified.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with CaptureStdout() as output:
            self.mgr_sync.run(options)
        expected_output = """Available Channels (full):


Status:
  - I - channel is installed
  - A - channel is not installed, but is available
  - U - channel is unavailable

[A] RHEL i386 AS 4 RES 4
    [A] RES4 AS for i386 RES 4
[I] SLES10-SP4-Pool for x86_64 SUSE Linux Enterprise Server 10 SP4 x86_64
    [A] SLE10-SDK-SP4-Pool for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit
    [I] SLE10-SDK-SP4-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit"""

        self.assertEqual(expected_output.split("\n"), output)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listChannels",
            self.fake_auth_token)

    def test_list_channels_filter_set(self):
        """ Testing list channel output when a filter is set """
        options = get_options("list channel --filter rhel".split())
        stubbed_xmlrpm_call = MagicMock(
            return_value=read_data_from_fixture(
                'list_channels_simplified.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with CaptureStdout() as output:
            self.mgr_sync.run(options)
        expected_output = """Available Channels:


Status:
  - I - channel is installed
  - A - channel is not installed, but is available
  - U - channel is unavailable

[A] RHEL i386 AS 4 RES 4"""

        self.assertEqual(expected_output.split("\n"), output)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listChannels",
            self.fake_auth_token)

    def test_list_channels_interactive(self):
        """ Test listing channels when interactive more is set """
        stubbed_xmlrpm_call = MagicMock(
            return_value=read_data_from_fixture(
                'list_channels_simplified.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        available_channels = []
        with CaptureStdout() as output:
            available_channels = self.mgr_sync._list_channels(
                expand=False,
                filter=None,
                no_optionals=True,
                show_interactive_numbers=True)
        expected_output = """Available Channels:


Status:
  - I - channel is installed
  - A - channel is not installed, but is available
  - U - channel is unavailable

01) [A] RHEL i386 AS 4 RES 4
    [I] SLES10-SP4-Pool for x86_64 SUSE Linux Enterprise Server 10 SP4 x86_64
    02) [A] SLE10-SDK-SP4-Pool for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit
        [I] SLE10-SDK-SP4-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit"""

        self.assertEqual(expected_output.split("\n"), output)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listChannels",
            self.fake_auth_token)

        self.assertEqual(['rhel-i386-as-4', 'sle10-sdk-sp4-pool-x86_64'],
                         available_channels)

    def test_list_emtpy_product(self):
        options = get_options("list product".split())
        stubbed_xmlrpm_call = MagicMock(return_value=[])
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with CaptureStdout() as output:
            self.mgr_sync.run(options)
        self.assertEqual(output, ["No products found."])

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listProducts",
            self.fake_auth_token)

    def test_list_product(self):
        options = get_options("list product".split())
        stubbed_xmlrpm_call = MagicMock(return_value=[])
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with CaptureStdout() as output:
            self.mgr_sync.run(options)
        self.assertEqual(output, ["No products found."])

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
        with CaptureStdout() as output:
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

        self.assertEqual(expected_output.split("\n"), output)

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
        with CaptureStdout() as output:
            self.mgr_sync.run(options)

        expected_output = """Available Products:


Status:
  - I - channel is installed
  - A - channel is not installed, but is available

[A] SUSE Manager Proxy 1.2 (x86_64)
[A] SUSE Manager Proxy 1.7 (x86_64)
[A] SUSE Manager Proxy 2.1 (x86_64)"""

        self.assertEqual(expected_output.split("\n"), output)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listProducts",
            self.fake_auth_token)

    def test_add_channels_interactive(self):
        options = get_options("add channel".split())
        available_channels = ['ch1', 'ch2']
        chosen_channel = available_channels[0]
        self.mgr_sync._list_channels = MagicMock(
            return_value=available_channels)
        stubbed_xmlrpm_call = MagicMock(return_value=read_data_from_fixture(
            'list_products.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with patch('spacewalk.susemanager.mgr_sync.mgr_sync.cli_ask') as mock:
            mock.return_value = str(
                available_channels.index(chosen_channel) + 1)
            with CaptureStdout() as output:
                self.mgr_sync.run(options)

        expected_output = [
            "Adding {0} channel".format(chosen_channel),
            "Scheduling reposync for {0} channel".format(chosen_channel)
        ]
        self.assertEqual(expected_output, output)

        self.mgr_sync._list_channels.assert_called_once_with(
            expand=False, filter=None, no_optionals=True,
            show_interactive_numbers=True)

        expected_xmlrpc_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "addChannel",
                                        self.fake_auth_token,
                                        chosen_channel),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        chosen_channel)
        ]

        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)
