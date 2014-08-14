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

from mock import MagicMock, PropertyMock, call

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from helper import CaptureStdout, read_data_from_fixture

from spacewalk.susemanager.mgr_sync.cli import get_options
from spacewalk.susemanager.mgr_sync.mgr_sync import MgrSync


class MgrSyncTest(unittest.TestCase):

    def setUp(self):
        self.mgr_sync = MgrSync()
        self.fake_auth_token = "fake_token"
        type(self.mgr_sync.auth).token = PropertyMock(return_value=self.fake_auth_token)

    def test_list_emtpy_channel(self):
        options = get_options("list channel".split())
        stubbed_xmlrpm_call = MagicMock(return_value=[])
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with CaptureStdout() as output:
            self.mgr_sync.run(options)
        self.assertEqual(output, ["No channels found."])

        stubbed_xmlrpm_call.assert_called_once_with("listChannels",
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
            call._execute_xmlrpc_method("synchronizeChannels", self.fake_auth_token),
            call._execute_xmlrpc_method("synchronizeChannelFamilies", self.fake_auth_token),
            call._execute_xmlrpc_method("synchronizeProducts", self.fake_auth_token),
            call._execute_xmlrpc_method("synchronizeProductChannels", self.fake_auth_token),
            call._execute_xmlrpc_method("synchronizeSubscriptions", self.fake_auth_token),
            call._execute_xmlrpc_method("synchronizeUpgradePaths", self.fake_auth_token)
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

        stubbed_xmlrpm_call.assert_called_once_with("listChannels",
                                                    self.fake_auth_token)


    def test_list_channels_expand_enabled(self):
        """ Testing list channel output when expand option is toggled """
        options = get_options("list channel -e".split())
        stubbed_xmlrpm_call = MagicMock(return_value=read_data_from_fixture('list_channels_simplified.data'))
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

        stubbed_xmlrpm_call.assert_called_once_with("listChannels",
                                                    self.fake_auth_token)


    def test_list_channels_filter_set(self):
        """ Testing list channel output when a filter is set """
        options = get_options("list channel --filter rhel".split())
        stubbed_xmlrpm_call = MagicMock(return_value=read_data_from_fixture('list_channels_simplified.data'))
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

        stubbed_xmlrpm_call.assert_called_once_with("listChannels",
                                                    self.fake_auth_token)



    def test_list_channels_interactive(self):
        """ Test listing channels when interactive more is set """
        stubbed_xmlrpm_call = MagicMock(return_value=read_data_from_fixture('list_channels_simplified.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        available_channels = []
        with CaptureStdout() as output:
            available_channels = self.mgr_sync._list_channels(expand=False,
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

        stubbed_xmlrpm_call.assert_called_once_with("listChannels",
                                                    self.fake_auth_token)

        self.assertEqual(['rhel-i386-as-4', 'sle10-sdk-sp4-pool-x86_64'],
                         available_channels)

