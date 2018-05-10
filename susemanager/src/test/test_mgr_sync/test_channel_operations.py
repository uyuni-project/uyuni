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

from mock import MagicMock, call, patch

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from helper import ConsoleRecorder, read_data_from_fixture

from spacewalk.susemanager.mgr_sync.cli import get_options
from spacewalk.susemanager.mgr_sync.channel import parse_channels, Channel
from spacewalk.susemanager.mgr_sync.mgr_sync import MgrSync
from spacewalk.susemanager.mgr_sync import logger


class ChannelOperationsTest(unittest.TestCase):

    def setUp(self):
        self.mgr_sync = MgrSync()
        self.mgr_sync.log = self.mgr_sync.__init__logger = MagicMock(
            return_value=logger.Logger(3, "tmp.log"))
        self.mgr_sync.conn = MagicMock()
        self.fake_auth_token = "fake_token"
        self.mgr_sync.auth.token = MagicMock(
            return_value=self.fake_auth_token)
        self.mgr_sync.config.write = MagicMock()
        self.mgr_sync.conn.sync.master.hasMaster = MagicMock(return_value=False)

    def tearDown(self):
        if os.path.exists("tmp.log"):
            os.unlink("tmp.log")

    def _mock_iterator(self):
        '''
        Mock *called* iterator.

        :return:
        '''
        mocked_iter = MagicMock()
        for dummy_element in mocked_iter():
            pass
        return mocked_iter.mock_calls[-1]

    def test_list_channels_no_channels(self):
        options = get_options("list channels".split())
        stubbed_xmlrpm_call = MagicMock(return_value=[])
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)
        self.assertEqual(recorder.stdout, ["No channels found."])

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listChannels",
            self.fake_auth_token)

    def test_list_channels(self):
        """ Testing list channel output """
        options = get_options("list channel".split())
        stubbed_xmlrpm_call = MagicMock(
            return_value=read_data_from_fixture(
                'list_channels_simplified.data'))
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)
        expected_output = """Available Channels:


Status:
  - [I] - channel is installed
  - [ ] - channel is not installed, but is available
  - [U] - channel is unavailable

[ ] RHEL i386 AS 4 RES 4 [rhel-i386-as-4]
[ ] RHEL x86_64 AS 4 RES 4 [rhel-x86_64-as-4]
[I] SLES10-SP4-Pool for x86_64 SUSE Linux Enterprise Server 10 SP4 x86_64 [sles10-sp4-pool-x86_64]
    [ ] SLE10-SDK-SP4-Pool for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-pool-x86_64]
    [I] SLE10-SDK-SP4-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-updates-x86_64]"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

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
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)
        expected_output = """Available Channels:


Status:
  - [I] - channel is installed
  - [ ] - channel is not installed, but is available
  - [U] - channel is unavailable

[ ] rhel-i386-as-4
[ ] rhel-x86_64-as-4
[I] sles10-sp4-pool-x86_64
    [ ] sle10-sdk-sp4-pool-x86_64
    [I] sle10-sdk-sp4-updates-x86_64"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

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
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)
        expected_output = """Available Channels (full):


Status:
  - [I] - channel is installed
  - [ ] - channel is not installed, but is available
  - [U] - channel is unavailable

[ ] RHEL i386 AS 4 RES 4 [rhel-i386-as-4]
    [ ] RES4 AS for i386 RES 4 [res4-as-i386]
[ ] RHEL x86_64 AS 4 RES 4 [rhel-x86_64-as-4]
    [ ] RES4 AS SUSE-Manager-Tools x86_64 SUSE-Manager-Tools [res4-as-suse-manager-tools-x86_64]
    [ ] RES4 AS for x86_64 RES 4 [res4-as-x86_64]
[I] SLES10-SP4-Pool for x86_64 SUSE Linux Enterprise Server 10 SP4 x86_64 [sles10-sp4-pool-x86_64]
    [ ] SLE10-SDK-SP4-Pool for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-pool-x86_64]
    [I] SLE10-SDK-SP4-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-updates-x86_64]"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

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
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)
        expected_output = """Available Channels:


Status:
  - [I] - channel is installed
  - [ ] - channel is not installed, but is available
  - [U] - channel is unavailable

[ ] RHEL i386 AS 4 RES 4 [rhel-i386-as-4]
[ ] RHEL x86_64 AS 4 RES 4 [rhel-x86_64-as-4]"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

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
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)
        expected_output = """Available Channels:


Status:
  - [I] - channel is installed
  - [ ] - channel is not installed, but is available
  - [U] - channel is unavailable

[I] SLES10-SP4-Pool for x86_64 SUSE Linux Enterprise Server 10 SP4 x86_64 [sles10-sp4-pool-x86_64]
    [I] SLE10-SDK-SP4-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-updates-x86_64]"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

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
        with ConsoleRecorder() as recorder:
            available_channels = self.mgr_sync._list_channels(
                expand=False,
                filter=None,
                no_optionals=True,
                show_interactive_numbers=True)
        expected_output = """Available Channels:


Status:
  - [I] - channel is installed
  - [ ] - channel is not installed, but is available
  - [U] - channel is unavailable

01) [ ] RHEL i386 AS 4 RES 4 [rhel-i386-as-4]
02) [ ] RHEL x86_64 AS 4 RES 4 [rhel-x86_64-as-4]
    [I] SLES10-SP4-Pool for x86_64 SUSE Linux Enterprise Server 10 SP4 x86_64 [sles10-sp4-pool-x86_64]
    03) [ ] SLE10-SDK-SP4-Pool for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-pool-x86_64]
        [I] SLE10-SDK-SP4-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 10 SP4 Software Development Kit [sle10-sdk-sp4-updates-x86_64]"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "listChannels",
            self.fake_auth_token)

        self.assertEqual(['rhel-i386-as-4', 'rhel-x86_64-as-4', 'sle10-sdk-sp4-pool-x86_64'],
                         available_channels)

    def test_add_available_base_channel_with_mirror(self):
        """ Test adding an available base channel"""
        mirror_url = "http://smt.suse.de"
        channel = "rhel-i386-as-4"
        options = get_options(
            "add channel {0} --from-mirror {1}".format(channel, mirror_url).split())

        self.mgr_sync._fetch_remote_channels = MagicMock(
            return_value=parse_channels(
                read_data_from_fixture("list_channels.data"), self.mgr_sync.log))

        stubbed_xmlrpm_call = MagicMock()
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with ConsoleRecorder() as recorder:
            self.assertEqual(0, self.mgr_sync.run(options))

        expected_xmlrpc_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "addChannels",
                                        self.fake_auth_token,
                                        channel,
                                        mirror_url),
            self._mock_iterator(),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        [channel])
        ]
        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)

        expected_output = [
            "Adding '{0}' channel".format(channel),
            "Scheduling reposync for '{0}' channel".format(channel)
        ]

    def test_add_available_base_channel(self):
        """ Test adding an available base channel"""

        channel = "rhel-i386-as-4"
        options = get_options(
            "add channel {0}".format(channel).split())

        self.mgr_sync._fetch_remote_channels = MagicMock(
        return_value=parse_channels(
            read_data_from_fixture("list_channels.data"), self.mgr_sync.log))
	stubbed_xmlrpm_call = MagicMock()
	stubbed_xmlrpm_call.side_effect = xmlrpc_sideeffect
	self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
	with ConsoleRecorder() as recorder:
	    self.assertEqual(0, self.mgr_sync.run(options))
	expected_xmlrpc_calls = [
	    call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
		                        "addChannels",
		                        self.fake_auth_token,
		                        channel,
		                        ''),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        [channel])
        ]
        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)

        expected_output = [
            "Added '{0}' channel".format(channel),
            "Scheduling reposync for following channels:",
            "- {0}".format(channel)
        ]
        self.assertEqual(expected_output, recorder.stdout)

    def test_add_available_channel_with_available_base_channel(self):
        """ Test adding an available channel whose parent is available.

        Should add both of them."""

        base_channel = "rhel-i386-es-4"
        channel = "res4-es-i386"
        options = get_options(
            "add channel {0}".format(channel).split())

        self.mgr_sync._fetch_remote_channels = MagicMock(
            return_value=parse_channels(
                read_data_from_fixture("list_channels.data"), self.mgr_sync.log))

        stubbed_xmlrpm_call = MagicMock()
        stubbed_xmlrpm_call.side_effect = xmlrpc_sideeffect
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with ConsoleRecorder() as recorder:
            self.assertEqual(0, self.mgr_sync.run(options))

        expected_xmlrpc_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "addChannels",
                                        self.fake_auth_token,
                                        base_channel,
                                        ''),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        [base_channel]),
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "addChannels",
                                        self.fake_auth_token,
                                        channel,
                                        ''),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        [channel])
        ]
        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)

        expected_output = """'res4-es-i386' depends on channel 'rhel-i386-es-4' which has not been added yet
Going to add 'rhel-i386-es-4'
Added 'rhel-i386-es-4' channel
Scheduling reposync for following channels:
- rhel-i386-es-4
Added 'res4-es-i386' channel
Scheduling reposync for following channels:
- res4-es-i386"""
        self.assertEqual(expected_output.split("\n"), recorder.stdout)

    def test_add_already_installed_channel(self):
        """Test adding an already added channel.

        Should only trigger the reposync for the channel"""

        channel = "sles11-sp3-pool-x86_64"
        options = get_options(
            "add channel {0}".format(channel).split())

        self.mgr_sync._fetch_remote_channels = MagicMock(
            return_value=parse_channels(
                read_data_from_fixture("list_channels.data"), self.mgr_sync.log))

        stubbed_xmlrpm_call = MagicMock()
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with ConsoleRecorder() as recorder:
            self.assertEqual(0, self.mgr_sync.run(options))

        expected_xmlrpc_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        [channel])
        ]
        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)

        expected_output = [
            "Channel '{0}' has already been added".format(channel),
            "Scheduling reposync for following channels:",
            "- {0}".format(channel)
        ]
        self.assertEqual(expected_output, recorder.stdout)

    def test_add_unavailable_base_channel(self):
        """Test adding an unavailable base channel

        Should refuse to perform the operation, print to stderr and exit with
        an error code"""

        options = get_options(
            "add channel sles11-sp3-vmware-pool-i586".split())
        self.mgr_sync._fetch_remote_channels = MagicMock(
            return_value=parse_channels(
                read_data_from_fixture("list_channels.data"), self.mgr_sync.log))

        with ConsoleRecorder() as recorder:
            self.assertEqual(1, self.mgr_sync.run(options))

        self.assertEqual(
            ["Channel 'sles11-sp3-vmware-pool-i586' is not available, skipping"],
            recorder.stdout)

    def test_add_unavailable_child_channel(self):
        """Test adding an unavailable child channel

        Should refuse to perform the operation, print to stderr and exit with
        an error code"""

        options = get_options(
            "add channel sle10-sdk-sp4-pool-x86_64".split())
        self.mgr_sync._fetch_remote_channels = MagicMock(
            return_value=parse_channels(
                read_data_from_fixture("list_channels.data"), self.mgr_sync.log))

        with ConsoleRecorder() as recorder:
            self.assertEqual(1, self.mgr_sync.run(options))

        self.assertEqual(
            ["Channel 'sle10-sdk-sp4-pool-x86_64' is not available, skipping"],
            recorder.stdout)

    def test_add_available_child_channel_with_unavailable_parent(self):
        """Test adding an available child channel which has an unavailable parent.

        Should refuse to perform the operation, print to stderr and exit with
        an error code.
        This should never occur.
        """

        channels = parse_channels(
            read_data_from_fixture("list_channels.data"), self.mgr_sync.log)
        parent = channels['rhel-i386-es-4']
        parent.status = Channel.Status.UNAVAILABLE
        child = 'res4-es-i386'

        options = get_options(
            "add channel {0}".format(child).split())
        self.mgr_sync._fetch_remote_channels = MagicMock(
            return_value=channels)

        with ConsoleRecorder() as recorder:
            self.assertEqual(1, self.mgr_sync.run(options))

        expected_output = """Error, 'res4-es-i386' depends on channel 'rhel-i386-es-4' which is not available
'res4-es-i386' has not been added"""

        self.assertFalse(recorder.stdout)
        self.assertEqual(expected_output.split("\n"),
                         recorder.stderr)


    def test_add_channels_interactive(self):
        options = get_options("add channel".split())
        available_channels = ['ch1', 'ch2']
        chosen_channel = available_channels[0]
        self.mgr_sync._list_channels = MagicMock(
            return_value=available_channels)
        stubbed_xmlrpm_call = MagicMock()
        stubbed_xmlrpm_call.side_effect = xmlrpc_sideeffect
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with patch('spacewalk.susemanager.mgr_sync.mgr_sync.cli_ask') as mock:
            mock.return_value = str(
                available_channels.index(chosen_channel) + 1)
            with ConsoleRecorder() as recorder:
                self.mgr_sync.run(options)

        expected_output = [
            "Added '{0}' channel".format(chosen_channel),
            "Scheduling reposync for following channels:",
            "- {0}".format(chosen_channel)
        ]
        self.assertEqual(expected_output, recorder.stdout)

        self.mgr_sync._list_channels.assert_called_once_with(
            expand=False, filter=None, no_optionals=False,
            show_interactive_numbers=True, compact=False)

        expected_xmlrpc_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "addChannels",
                                        self.fake_auth_token,
                                        chosen_channel,
                                        ''),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        [chosen_channel])
        ]

        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)

    def test_add_channels_interactive_no_optional(self):
        options = get_options("add channel --no-optional".split())
        available_channels = ['ch1', 'ch2']
        chosen_channel = available_channels[0]
        self.mgr_sync._list_channels = MagicMock(
            return_value=available_channels)
        stubbed_xmlrpm_call = MagicMock()
        stubbed_xmlrpm_call.side_effect = xmlrpc_sideeffect
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with patch('spacewalk.susemanager.mgr_sync.mgr_sync.cli_ask') as mock:
            mock.return_value = str(
                available_channels.index(chosen_channel) + 1)
            with ConsoleRecorder() as recorder:
                self.mgr_sync.run(options)

        expected_output = [
            "Added '{0}' channel".format(chosen_channel),
            "Scheduling reposync for following channels:",
            "- {0}".format(chosen_channel)
        ]
        self.assertEqual(expected_output, recorder.stdout)

        self.mgr_sync._list_channels.assert_called_once_with(
            expand=False, filter=None, no_optionals=True,
            show_interactive_numbers=True, compact=False)

        expected_xmlrpc_calls = [
            call._execute_xmlrpc_method(self.mgr_sync.conn.sync.content,
                                        "addChannels",
                                        self.fake_auth_token,
                                        chosen_channel,
                                        ''),
            call._execute_xmlrpc_method(self.mgr_sync.conn.channel.software,
                                        "syncRepo",
                                        self.fake_auth_token,
                                        [chosen_channel])
        ]

        stubbed_xmlrpm_call.assert_has_calls(expected_xmlrpc_calls)

def xmlrpc_sideeffect(*args, **kwargs):
    if args[1] == "addChannels":
        return [args[3]]
    return read_data_from_fixture('list_channels.data')
