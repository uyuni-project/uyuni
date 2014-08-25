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
from helper import CaptureStdout, ConsoleRecorder, read_data_from_fixture

from spacewalk.susemanager.mgr_sync.cli import get_options
from spacewalk.susemanager.mgr_sync.channel import parse_channels, Channel
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

[A] RHEL i386 AS 4 RES 4 [rhel-i386-as-4]
[I] SLES10-SP4-Pool for x86_64 SUSE Linux Enterprise Server 10 SP4 x86_64 [sles10-sp4-pool-x86_64]
    [A] SLE10-SDK-SP4-Pool for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-pool-x86_64]
    [I] SLE10-SDK-SP4-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-updates-x86_64]"""

        self.assertEqual(expected_output.split("\n"), output)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listChannels",
            self.fake_auth_token)

    def test_list_channels_compact_mode_enabled(self):
        """ Testing list channel output """
        options = get_options("list channel -c".split())
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

[A] rhel-i386-as-4
[I] sles10-sp4-pool-x86_64
    [A] sle10-sdk-sp4-pool-x86_64
    [I] sle10-sdk-sp4-updates-x86_64"""

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

[A] RHEL i386 AS 4 RES 4 [rhel-i386-as-4]
    [A] RES4 AS for i386 RES 4 [res4-as-i386]
[I] SLES10-SP4-Pool for x86_64 SUSE Linux Enterprise Server 10 SP4 x86_64 [sles10-sp4-pool-x86_64]
    [A] SLE10-SDK-SP4-Pool for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-pool-x86_64]
    [I] SLE10-SDK-SP4-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-updates-x86_64]"""

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

[A] RHEL i386 AS 4 RES 4 [rhel-i386-as-4]"""

        self.assertEqual(expected_output.split("\n"), output)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listChannels",
            self.fake_auth_token)

    def test_list_channels_filter_show_parent_when_child_matches(self):
        """ Testing list channel output when a filter is set.  Should show the 
        parent even if it does not match the filter as long as one of his
        children match the filter.
        """

        options = get_options("list channel --filter update".split())
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

[I] SLES10-SP4-Pool for x86_64 SUSE Linux Enterprise Server 10 SP4 x86_64 [sles10-sp4-pool-x86_64]
    [I] SLE10-SDK-SP4-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-updates-x86_64]"""

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

01) [A] RHEL i386 AS 4 RES 4 [rhel-i386-as-4]
    [I] SLES10-SP4-Pool for x86_64 SUSE Linux Enterprise Server 10 SP4 x86_64 [sles10-sp4-pool-x86_64]
    02) [A] SLE10-SDK-SP4-Pool for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-pool-x86_64]
        [I] SLE10-SDK-SP4-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-updates-x86_64]"""

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

    def test_list_products_with_filtering_matches_also_children(self):
        """ Test listing products with filtering should match children even when
        their parent does not.
        """

        options = get_options("list product --filter cloud".split())
        stubbed_xmlrpm_call = MagicMock(return_value=read_data_from_fixture(
            'list_products.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with CaptureStdout() as output:
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
            "Adding '{0}' channel".format(chosen_channel),
            "Scheduling reposync for '{0}' channel".format(chosen_channel)
        ]
        self.assertEqual(expected_output, output)

        self.mgr_sync._list_channels.assert_called_once_with(
            expand=False, filter=None, no_optionals=True,
            show_interactive_numbers=True, compact=False)

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

    def test_add_available_base_channel(self):
        """ Test adding an available base channel"""

        channel = "rhel-i386-as-4"
        options = get_options(
            "add channel {0}".format(channel).split())

        self.mgr_sync._fetch_remote_channels = MagicMock(
            return_value=parse_channels(
                read_data_from_fixture("list_channels.data")))

        stubbed_xmlrpm_call = MagicMock()
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with CaptureStdout() as output:
            with patch('sys.exit') as mock_exit:
                self.mgr_sync.run(options)

        self.assertFalse(mock_exit.mock_calls)

        expected_xmlrpc_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "addChannel",
                                        self.fake_auth_token,
                                        channel),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        channel)
        ]
        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)

        expected_output = [
            "Adding '{0}' channel".format(channel),
            "Scheduling reposync for '{0}' channel".format(channel)
        ]
        self.assertEqual(expected_output, output)

    def test_add_available_channel_with_available_base_channel(self):
        """ Test adding an available channel whose parent is available.

        Should add both of them."""

        base_channel = "rhel-i386-es-4"
        channel = "res4-es-i386"
        options = get_options(
            "add channel {0}".format(channel).split())

        self.mgr_sync._fetch_remote_channels = MagicMock(
            return_value=parse_channels(
                read_data_from_fixture("list_channels.data")))

        stubbed_xmlrpm_call = MagicMock()
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with CaptureStdout() as output:
            with patch('sys.exit') as mock_exit:
                self.mgr_sync.run(options)

        self.assertFalse(mock_exit.mock_calls)

        expected_xmlrpc_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "addChannel",
                                        self.fake_auth_token,
                                        base_channel),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        base_channel),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "addChannel",
                                        self.fake_auth_token,
                                        channel),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        channel)
        ]
        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)

        expected_output = """'res4-es-i386' depends on channel 'rhel-i386-es-4' which has not been added yet
Going to add 'rhel-i386-es-4'
Adding 'rhel-i386-es-4' channel
Scheduling reposync for 'rhel-i386-es-4' channel
Adding 'res4-es-i386' channel
Scheduling reposync for 'res4-es-i386' channel"""
        self.assertEqual(expected_output.split("\n"), output)

    def test_add_already_installed_channel(self):
        """Test adding an already added channel.

        Should only trigger the reposync for the channel"""

        channel = "sles11-sp3-pool-x86_64"
        options = get_options(
            "add channel {0}".format(channel).split())

        self.mgr_sync._fetch_remote_channels = MagicMock(
            return_value=parse_channels(
                read_data_from_fixture("list_channels.data")))

        stubbed_xmlrpm_call = MagicMock()
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with CaptureStdout() as output:
            with patch('sys.exit') as mock_exit:
                self.mgr_sync.run(options)

        self.assertFalse(mock_exit.mock_calls)

        expected_xmlrpc_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        channel)
        ]
        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)

        expected_output = [
            "Channel '{0}' has already been added".format(channel),
            "Scheduling reposync for '{0}' channel".format(channel)
        ]
        self.assertEqual(expected_output, output)

    def test_add_unavailable_base_channel(self):
        """Test adding an unavailable base channel

        Should refuse to perform the operation, print to stderr and exit with
        an error code"""

        options = get_options(
            "add channel sles11-sp3-vmware-pool-i586".split())
        self.mgr_sync._fetch_remote_channels = MagicMock(
            return_value=parse_channels(
                read_data_from_fixture("list_channels.data")))

        with CaptureStdout() as output:
            with patch('sys.exit') as mock_exit:
                self.mgr_sync.run(options)

        mock_exit.assert_called_once_with(1)
        self.assertEqual(
            ["Channel 'sles11-sp3-vmware-pool-i586' is not available, skipping"],
            output)

    def test_add_unavailable_child_channel(self):
        """Test adding an unavailable child channel

        Should refuse to perform the operation, print to stderr and exit with
        an error code"""

        options = get_options(
            "add channel sle10-sdk-sp4-pool-x86_64".split())
        self.mgr_sync._fetch_remote_channels = MagicMock(
            return_value=parse_channels(
                read_data_from_fixture("list_channels.data")))

        with CaptureStdout() as output:
            with patch('sys.exit') as mock_exit:
                self.mgr_sync.run(options)

        mock_exit.assert_called_once_with(1)
        self.assertEqual(
            ["Channel 'sle10-sdk-sp4-pool-x86_64' is not available, skipping"],
            output)

    def test_add_available_child_channel_with_unavailable_parent(self):
        """Test adding an available child channel which has an unavailable parent.

        Should refuse to perform the operation, print to stderr and exit with
        an error code.
        This should never occur.
        """

        channels = parse_channels(
            read_data_from_fixture("list_channels.data"))
        parent = channels['rhel-i386-es-4']
        parent.status = Channel.Status.UNAVAILABLE
        child = 'res4-es-i386'

        options = get_options(
            "add channel {0}".format(child).split())
        self.mgr_sync._fetch_remote_channels = MagicMock(
            return_value=channels)

        with ConsoleRecorder() as recorder:
            with patch('sys.exit') as mock_exit:
                self.mgr_sync.run(options)

        mock_exit.assert_called_once_with(1)

        expected_output = """Error, 'res4-es-i386' depends on channel 'rhel-i386-es-4' which is not available
'res4-es-i386' has not been added"""

        self.assertFalse(recorder.stdout)
        self.assertEqual(expected_output.split("\n"),
                         recorder.stderr)

