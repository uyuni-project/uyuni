#!/usr/bin/env python
# pylint: disable=missing-module-docstring
# -*- coding: utf-8 -*-
#
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: LGPL-2.1-only
import os
import json
import sys

try:
    import unittest2 as unittest
except ImportError:
    import unittest

try:
    from unittest.mock import MagicMock
except ImportError:
    from mock import MagicMock

from spacewalk.susemanager.mgr_sync.channel import (
    parse_channels,
    find_channel_by_label,
    Channel,
)
from spacewalk.susemanager.mgr_sync.mgr_sync import MgrSync
from spacewalk.susemanager.mgr_sync import logger

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
# pylint: disable-next=wrong-import-position
from helper import read_data_from_fixture, path_to_fixture


class ChannelTest(unittest.TestCase):
    def setUp(self):
        self.mgr_sync = MgrSync()
        self.mgr_sync.log = self.mgr_sync.__init__logger = MagicMock(
            return_value=logger.Logger(3, "tmp.log")
        )

    def tearDown(self):
        if os.path.exists("tmp.log"):
            os.unlink("tmp.log")

    def test_parse_channels(self):
        # pylint: disable-next=unspecified-encoding
        with open(path_to_fixture("expected_channels.json"), "r") as file:
            expected_channels = json.load(file)

        # pylint: disable-next=unspecified-encoding
        with open(path_to_fixture("expected_hierarchy.json"), "r") as file:
            expected_hierarchy = json.load(file)

        channels = parse_channels(
            read_data_from_fixture("list_channels.data"), self.mgr_sync.log
        )

        self.assertEqual(sorted(channels.keys()), sorted(expected_hierarchy.keys()))
        for label, bc in list(channels.items()):
            self.assertEqual(label, bc.label)
            self.assertEqual(bc.status, expected_channels[bc.label])

            if bc.children and bc.status == Channel.Status.INSTALLED:
                children = sorted([c.label for c in bc.children])
                self.assertEqual(children, sorted(expected_hierarchy[bc.label]))
            else:
                self.assertEqual(0, len(expected_hierarchy[bc.label]))

    def test_find_channel_by_label(self):
        channels = parse_channels(
            read_data_from_fixture("list_channels.data"), self.mgr_sync.log
        )

        for label in ["rhel-x86_64-es-4", "sles11-sp2-updates-i586"]:
            bc = find_channel_by_label(label, channels, self.mgr_sync.log)
            self.assertIsNotNone(bc)
            self.assertEqual(label, bc.label)

        self.assertIsNone(find_channel_by_label("foobar", channels, self.mgr_sync.log))
