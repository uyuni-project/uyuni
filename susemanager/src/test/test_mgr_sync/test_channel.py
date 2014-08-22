#!/usr/bin/env python
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

import os
import sys
import unittest2 as unittest

from spacewalk.susemanager.mgr_sync.channel import parse_channels, \
    find_channel_by_label, Channel

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from helper import read_data_from_fixture, path_to_fixture


class ChannelTest(unittest.TestCase):

    def test_parse_channels(self):
        expected_channels, expected_hierarchy = self._parse_mgr_ncc_output()
        channels = parse_channels(read_data_from_fixture("list_channels.data"))

        self.assertEqual(sorted(channels.keys()),
                         sorted(expected_hierarchy.keys()))
        for label, bc in channels.items():
            self.assertEqual(label, bc.label)
            self.assertEqual(
                bc.status,
                self._mgr_ncc_status_to_new_status(
                    expected_channels[bc.label]))

            if bc.children and bc.status == Channel.Status.INSTALLED:
                children = sorted([c.label for c in bc.children])
                self.assertEqual(children,
                                 sorted(expected_hierarchy[bc.label]))
            else:
                self.assertEqual(0, len(expected_hierarchy[bc.label]))

    def _mgr_ncc_status_to_new_status(self, status):
        status = status[1]
        if status == 'P':
            return Channel.Status.INSTALLED
        elif status == 'X':
            return Channel.Status.UNAVAILABLE
        elif status == '.':
            return Channel.Status.AVAILABLE
        else:
            raise Exception('Type unknown')

    def _parse_mgr_ncc_output(self):
        """
        Parse the output of mgr-ncc-sync and returns a tuple with
        two dictionaries.

        The first one contains all the channels and has the following
        structure:
            * key: channel label
            * value: status

        The second one contains only the base channels and has the following
        structure:
            * key: base channel labels
            * value: list of children labels
        """

        channels = {}
        channels_hierarcy = {}

        with open(path_to_fixture("mgr-ncc-sync.output"), "r") as file:
            latest_base_product = None
            for line in file.readlines():
                base_product = line.startswith("[")
                status, label = filter(None, line.strip().split(" "))

                if base_product:
                    latest_base_product = label
                    channels_hierarcy[label] = []
                else:
                    channels_hierarcy[latest_base_product].append(label)

                channels[label] = status

        return (channels, channels_hierarcy)

    def test_find_channel_by_label(self):
        channels = parse_channels(
            read_data_from_fixture("list_channels.data"))

        for label in ['rhel-x86_64-es-4',
                      'sles11-sp2-updates-i586']:
            bc = find_channel_by_label(label, channels)
            self.assertIsNotNone(bc)
            self.assertEqual(label, bc.label)

        self.assertIsNone(find_channel_by_label('foobar', channels))

