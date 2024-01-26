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
# pylint: disable-next=wrong-import-position,unused-import
from helper import ConsoleRecorder, read_data_from_fixture

# pylint: disable-next=wrong-import-position
from spacewalk.susemanager.authenticator import MaximumNumberOfAuthenticationFailures

# pylint: disable-next=wrong-import-position
from spacewalk.susemanager.mgr_sync.cli import get_options

# pylint: disable-next=wrong-import-position
from spacewalk.susemanager.mgr_sync.mgr_sync import MgrSync

# pylint: disable-next=wrong-import-position
from spacewalk.susemanager.mgr_sync import logger


class MgrSyncTest(unittest.TestCase):
    def setUp(self):
        self.mgr_sync = MgrSync()
        self.mgr_sync.conn = MagicMock()

        self.fake_auth_token = "fake_token"
        mock_connection = MagicMock()
        mock_auth = MagicMock()
        mock_connection.auth = mock_auth
        mock_auth.login = MagicMock(return_value=self.fake_auth_token)
        self.mgr_sync.auth.connection = mock_connection

        self.mgr_sync.config.write = MagicMock()
        # pylint: disable-next=protected-access
        self.mgr_sync.__init__logger = MagicMock(
            return_value=logger.Logger(3, "tmp.log")
        )
        self.mgr_sync.conn.sync.master.hasMaster = MagicMock(return_value=False)

    def tearDown(self):
        if os.path.exists("tmp.log"):
            os.unlink("tmp.log")

    def test_should_write_a_logfile(self):
        """Should write a logfile when debugging is enabled"""

        self.assertTrue(os.path.isfile("tmp.log"))

    def test_should_handle_max_number_of_authentication_failures(self):
        def raise_maximum_number_of_authentication_failures(options):
            raise MaximumNumberOfAuthenticationFailures

        # pylint: disable-next=protected-access
        self.mgr_sync._process_user_request = MagicMock()
        # pylint: disable-next=protected-access
        self.mgr_sync._process_user_request.side_effect = (
            raise_maximum_number_of_authentication_failures
        )

        options = get_options("list channels".split())
        with ConsoleRecorder() as recorder:
            self.assertEqual(1, self.mgr_sync.run(options))
        self.assertEqual(["mgr-sync: Authentication failure"], recorder.stderr)

    def test_should_always_write_the_session_token_to_the_local_configuration(self):
        self.mgr_sync.config.token = "old token"
        self.mgr_sync.auth.user = "admin"
        self.mgr_sync.auth.password = "test"
        # pylint: disable-next=protected-access
        self.mgr_sync._execute_xmlrpc_method = MagicMock(return_value=[])

        options = get_options("list channels".split())
        with ConsoleRecorder():
            self.assertEqual(0, self.mgr_sync.run(options))
        self.assertEqual(self.fake_auth_token, self.mgr_sync.config.token)

        self.mgr_sync.config.write.assert_called_once_with()
