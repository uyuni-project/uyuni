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


class MgrSyncTest(unittest.TestCase):

    def setUp(self):
        self.mgr_sync = MgrSync()
        self.mgr_sync.conn = MagicMock()
        self.fake_auth_token = "fake_token"
        self.mgr_sync.auth.token = MagicMock(
            return_value=self.fake_auth_token)
        self.mgr_sync.config.write = MagicMock()

        patcher = patch('spacewalk.susemanager.mgr_sync.mgr_sync.current_cc_backend')
        self.mock_current_backend = patcher.start()

    def test_should_exit_when_ncc_is_active(self):
        """ Should exit with an error when NCC backend is active """

        self.mock_current_backend.return_value = BackendType.NCC
        options = get_options("refresh".split())
        try:
            with ConsoleRecorder() as recorder:
                self.mgr_sync.run(options)
        except SystemExit, ex:
            self.assertEqual(1, ex.code)

        expected_stderr = """Error: the Novell Customer Center (NCC) backend is currently in use.
mgr-sync requires the SUSE Customer Center (SCC) backend to be activated.

This can be done using the following commmand:
    mgr-sync enable scc

Note: there is no way to revert the migration from Novell Customer Center (NCC) to SUSE Customer Center (SCC)."""

        self.assertEqual(expected_stderr.split("\n"), recorder.stderr)
        self.assertFalse(recorder.stdout)

    def test_should_allow_migration_to_scc(self):
        """ Should allow the execution of the enable scc action even when
        NCC is active """

        self.mock_current_backend.return_value = BackendType.NCC
        mock_enable_scc = MagicMock()
        self.mgr_sync._enable_scc = mock_enable_scc

        options = get_options("enable scc".split())
        with ConsoleRecorder():
            self.mgr_sync.run(options)

        mock_enable_scc.assert_called_once()

    def test_should_not_migrate_to_scc_more_than_once(self):
        """ Should not migrate to SCC when the SCC backend is already active"""

        self.mock_current_backend.return_value = BackendType.SCC
        mock_enable_scc = MagicMock()
        self.mgr_sync._enable_scc = mock_enable_scc

        options = get_options("enable scc".split())
        with ConsoleRecorder():
            self.mgr_sync.run(options)

        self.assertFalse(mock_enable_scc.mock_calls)

