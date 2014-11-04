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


class RefreshOperationsTest(unittest.TestCase):

    def setUp(self):
        self.mgr_sync = MgrSync()
        self.mgr_sync.conn = MagicMock()
        self.mgr_sync._is_scc_allowed = MagicMock(return_value=True)
        self.fake_auth_token = "fake_token"
        self.mgr_sync.auth.token = MagicMock(
            return_value=self.fake_auth_token)
        self.mgr_sync.config.write = MagicMock()

        patcher = patch('spacewalk.susemanager.mgr_sync.mgr_sync.current_cc_backend')
        mock = patcher.start()
        mock.return_value = BackendType.SCC

        patcher = patch('spacewalk.susemanager.mgr_sync.mgr_sync.hasISSMaster')
        mock = patcher.start()
        mock.return_value = False

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

        expected_output = """Refreshing Channels                            [DONE]
Refreshing Channel families                    [DONE]
Refreshing SUSE products                       [DONE]
Refreshing SUSE Product channels               [DONE]
Refreshing Subscriptions                       [DONE]
Refreshing Upgrade paths                       [DONE]"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        expected_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeChannels",
                                        self.fake_auth_token, mirror_url),
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

        expected_output = """Refreshing Channels                            [DONE]
Refreshing Channel families                    [DONE]
Refreshing SUSE products                       [DONE]
Refreshing SUSE Product channels               [DONE]
Refreshing Subscriptions                       [DONE]
Refreshing Upgrade paths                       [DONE]"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        expected_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeChannels",
                                        self.fake_auth_token, ''),
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

        expected_output = """Refreshing Channels                            [DONE]
Refreshing Channel families                    [DONE]
Refreshing SUSE products                       [DONE]
Refreshing SUSE Product channels               [DONE]
Refreshing Subscriptions                       [DONE]
Refreshing Upgrade paths                       [DONE]

Scheduling refresh of all the available channels
Scheduling reposync for 'sles10-sp4-pool-x86_64' channel
Scheduling reposync for 'sle10-sdk-sp4-updates-x86_64' channel"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        expected_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "synchronizeChannels",
                                        self.fake_auth_token, ''),
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
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "listChannels",
                                        self.fake_auth_token),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        "sles10-sp4-pool-x86_64"),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        "sle10-sdk-sp4-updates-x86_64"),
        ]
        stubbed_xmlrpm_call.assert_has_calls(expected_calls)

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
