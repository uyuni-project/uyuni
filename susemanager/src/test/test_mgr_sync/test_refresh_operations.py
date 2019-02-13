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

try:
    import unittest2 as unittest
except ImportError:
    import unittest

import os.path
import sys

try:
    from unittest.mock import MagicMock, call, patch
except ImportError:
    from mock import MagicMock, call, patch

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from helper import ConsoleRecorder, read_data_from_fixture

from spacewalk.susemanager.mgr_sync.cli import get_options
from spacewalk.susemanager.mgr_sync.mgr_sync import MgrSync


class RefreshOperationsTest(unittest.TestCase):

    def setUp(self):
        self.mgr_sync = MgrSync()
        self.mgr_sync.conn = MagicMock()
        self.fake_auth_token = "fake_token"
        self.mgr_sync.auth.token = MagicMock(
            return_value=self.fake_auth_token)
        self.mgr_sync.config.write = MagicMock()
        self.mgr_sync.conn.sync.master.hasMaster = MagicMock(return_value=False)

    def test_refresh_from_mirror(self):
        """ Test the refresh action """
        mirror_url = "http://smt.suse.de"
        options = get_options("refresh --from-mirror {0}".format(mirror_url).split())
        stubbed_xmlrpm_call = MagicMock(return_value=True)
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        stubbed_reposync = MagicMock()
        self.mgr_sync._schedule_channel_reposync = stubbed_reposync
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)

        expected_output = """Refreshing Channel families                    [DONE]
Refreshing SUSE products                       [DONE]
Refreshing SUSE repositories                   [DONE]
Refreshing Subscriptions                       [DONE]"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        expected_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeChannelFamilies",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeProducts",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeRepositories",
                                        self.fake_auth_token, mirror_url),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeSubscriptions",
                                        self.fake_auth_token)
        ]
        stubbed_xmlrpm_call.assert_has_calls(expected_calls)
        self.assertFalse(stubbed_reposync.mock_calls)

    def test_refresh(self):
        """ Test the refresh action """

        options = get_options("refresh".split())
        stubbed_xmlrpm_call = MagicMock(return_value=True)
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        stubbed_reposync = MagicMock()
        self.mgr_sync._schedule_channel_reposync = stubbed_reposync
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)

        expected_output = """Refreshing Channel families                    [DONE]
Refreshing SUSE products                       [DONE]
Refreshing SUSE repositories                   [DONE]
Refreshing Subscriptions                       [DONE]"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        expected_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeChannelFamilies",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeProducts",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeRepositories",
                                        self.fake_auth_token, ''),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeSubscriptions",
                                        self.fake_auth_token)
        ]
        stubbed_xmlrpm_call.assert_has_calls(expected_calls)
        self.assertFalse(stubbed_reposync.mock_calls)

    def test_refresh_enable_reposync(self):
        """ Test the refresh action """

        options = get_options("refresh --refresh-channels".split())
        stubbed_xmlrpm_call = MagicMock(
            return_value=read_data_from_fixture(
                'list_channels_simplified.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)

        expected_output = """Refreshing Channel families                    [DONE]
Refreshing SUSE products                       [DONE]
Refreshing SUSE repositories                   [DONE]
Refreshing Subscriptions                       [DONE]

Scheduling refresh of all the available channels
Scheduling reposync for following channels:
- sles10-sp4-pool-x86_64
- sle10-sdk-sp4-updates-x86_64"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        expected_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeChannelFamilies",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeProducts",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeRepositories",
                                        self.fake_auth_token, ''),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeSubscriptions",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "listChannels",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        ["sles10-sp4-pool-x86_64", "sle10-sdk-sp4-updates-x86_64"]),
        ]
        stubbed_xmlrpm_call.assert_has_calls(expected_calls)

    def test_refresh_never_ask_credentials_when_schedule_option_is_set(self):
        """ Refresh with the 'schedule' option should just schedule the
            operation. User credentials must not be asked.
        """

        options = get_options("refresh --schedule".split())
        mock_execute_xmlrpc = MagicMock()
        self.mgr_sync._execute_xmlrpc_method = mock_execute_xmlrpc
        mock_reposync = MagicMock()
        self.mgr_sync._schedule_channel_reposync = mock_reposync
        mock_schedule_taskomatic_refresh = MagicMock()
        self.mgr_sync._schedule_taskomatic_refresh = mock_schedule_taskomatic_refresh

        with ConsoleRecorder() as recorder:
            self.assertEqual(0, self.mgr_sync.run(options))

        self.assertEqual(['Refresh successfully scheduled'], recorder.stdout)

        self.assertTrue(mock_schedule_taskomatic_refresh.mock_calls)
        self.assertFalse(mock_execute_xmlrpc.mock_calls)
        self.assertFalse(mock_reposync.mock_calls)

    def test_refresh_should_not_trigger_reposync_when_there_is_an_error(self):
        """ The refresh action should not trigger a reposync when something
            went wrong during one of the refresh steps.
        """

        options = get_options("refresh --refresh-channels".split())
        stubbed_xmlrpm_call = MagicMock(
            side_effect=Exception("Boom baby!"))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        mock_reposync = MagicMock()
        self.mgr_sync._schedule_channel_reposync = mock_reposync

        with ConsoleRecorder() as recorder:
            self.assertEqual(1, self.mgr_sync.run(options))

        self.assertTrue(recorder.stderr)
        self.assertFalse(mock_reposync.mock_calls)
