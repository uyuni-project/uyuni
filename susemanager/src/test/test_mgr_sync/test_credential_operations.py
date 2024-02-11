#  pylint: disable=missing-module-docstring
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
    # pylint: disable-next=unused-import
    from unittest.mock import MagicMock, call, patch
except ImportError:
    from mock import MagicMock, call, patch

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
# pylint: disable-next=wrong-import-position
from helper import ConsoleRecorder, read_data_from_fixture

# pylint: disable-next=wrong-import-position
from spacewalk.susemanager.mgr_sync.cli import get_options

# pylint: disable-next=wrong-import-position
from spacewalk.susemanager.mgr_sync.mgr_sync import MgrSync

# pylint: disable-next=wrong-import-position
from spacewalk.susemanager.mgr_sync import logger


class CredentialOperationsTest(unittest.TestCase):
    def setUp(self):
        self.mgr_sync = MgrSync()
        self.mgr_sync.conn = MagicMock()
        self.mgr_sync.log = self.mgr_sync.__init__logger = MagicMock(
            return_value=logger.Logger(3, "tmp.log")
        )
        self.fake_auth_token = "fake_token"
        self.mgr_sync.auth.token = MagicMock(return_value=self.fake_auth_token)
        self.mgr_sync.config.write = MagicMock()
        self.mgr_sync.conn.sync.master.hasMaster = MagicMock(return_value=False)

    def tearDown(self):
        if os.path.exists("tmp.log"):
            os.unlink("tmp.log")

    def test_list_credentials_no_credentials(self):
        """Test listing credentials with none present"""
        options = get_options("list credentials".split())
        stubbed_xmlrpm_call = MagicMock(return_value=[])
        # pylint: disable-next=protected-access
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)

        self.assertEqual(recorder.stdout, ["No credentials found"])

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content, "listCredentials", self.fake_auth_token
        )

    def test_list_credentials(self):
        """Test listing credentials"""
        options = get_options("list credentials".split())
        stubbed_xmlrpm_call = MagicMock(
            return_value=read_data_from_fixture("list_credentials.data")
        )
        # pylint: disable-next=protected-access
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        with ConsoleRecorder() as recorder:
            self.mgr_sync.run(options)
        expected_output = """Credentials:
foo (primary)
bar"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content, "listCredentials", self.fake_auth_token
        )

    def test_list_credentials_interactive(self):
        """Test listing credentials when interactive mode is set"""
        stubbed_xmlrpm_call = MagicMock(
            return_value=read_data_from_fixture("list_credentials.data")
        )
        # pylint: disable-next=protected-access
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call
        # pylint: disable-next=unused-variable
        credentials = []

        with ConsoleRecorder() as recorder:
            # pylint: disable-next=protected-access
            credentials = self.mgr_sync._list_credentials(show_interactive_numbers=True)
        expected_output = """Credentials:
01) foo (primary)
02) bar"""

        self.assertEqual(expected_output.split("\n"), recorder.stdout)

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content, "listCredentials", self.fake_auth_token
        )

    def test_add_credentials_interactive(self):
        """Test adding credentials interactively"""
        options = get_options("add credentials".split())
        # pylint: disable-next=protected-access
        self.mgr_sync._fetch_credentials = MagicMock(
            return_value=read_data_from_fixture("list_credentials.data")
        )

        stubbed_xmlrpm_call = MagicMock()
        # pylint: disable-next=protected-access
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with patch("spacewalk.susemanager.mgr_sync.mgr_sync.cli_ask") as mock:
            mock.side_effect = ["foobar", "foo", "foo"]
            with ConsoleRecorder() as recorder:
                self.assertEqual(0, self.mgr_sync.run(options))

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "addCredentials",
            self.fake_auth_token,
            "foobar",
            "foo",
            False,
        )

        self.assertEqual(recorder.stdout, ["Successfully added credentials."])

    def test_add_credentials_non_interactive(self):
        """Test adding credentials non-interactively"""
        options = get_options("add credentials foobar foo".split())
        # pylint: disable-next=protected-access
        self.mgr_sync._fetch_credentials = MagicMock(
            return_value=read_data_from_fixture("list_credentials.data")
        )

        stubbed_xmlrpm_call = MagicMock()
        # pylint: disable-next=protected-access
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        # pylint: disable-next=unused-variable
        with ConsoleRecorder() as recorder:
            self.assertEqual(0, self.mgr_sync.run(options))

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "addCredentials",
            self.fake_auth_token,
            "foobar",
            "foo",
            False,
        )

    def test_delete_credentials_interactive(self):
        """Test deleting credentials interactively"""
        options = get_options("delete credentials".split())
        # pylint: disable-next=protected-access
        self.mgr_sync._fetch_credentials = MagicMock(
            return_value=read_data_from_fixture("list_credentials.data")
        )

        stubbed_xmlrpm_call = MagicMock()
        # pylint: disable-next=protected-access
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with patch("spacewalk.susemanager.mgr_sync.mgr_sync.cli_ask") as mock:
            mock.side_effect = ["1", "y"]
            with ConsoleRecorder() as recorder:
                self.assertEqual(0, self.mgr_sync.run(options))

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "deleteCredentials",
            self.fake_auth_token,
            "foo",
        )

        self.assertEqual(
            [recorder.stdout[-1]], ["Successfully deleted credentials: foo"]
        )

    def test_delete_credentials_non_interactive(self):
        """Test deleting credentials non-interactively"""
        options = get_options("delete credentials foo".split())
        # pylint: disable-next=protected-access
        self.mgr_sync._fetch_credentials = MagicMock(
            return_value=read_data_from_fixture("list_credentials.data")
        )
        stubbed_xmlrpm_call = MagicMock()
        # pylint: disable-next=protected-access
        self.mgr_sync._execute_xmlrpc_method = stubbed_xmlrpm_call

        with ConsoleRecorder() as recorder:
            self.assertEqual(0, self.mgr_sync.run(options))

        stubbed_xmlrpm_call.assert_called_once_with(
            self.mgr_sync.conn.sync.content,
            "deleteCredentials",
            self.fake_auth_token,
            "foo",
        )

        self.assertEqual(recorder.stdout, ["Successfully deleted credentials: foo"])
