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
import unittest2 as unittest

from spacewalk.susemanager.mgr_sync.channel import parse_channels


class ChannelTest(unittest.TestCase):

    def test_parse_channels(self):
        expected_channels, expected_hierarchy = self._parse_mgr_ncc_output()
        channels = parse_channels(self._read_data_from_fixture("list_channels.data"))

        self.assertEqual(sorted(channels.keys()),
                         sorted(expected_hierarchy.keys()))
        for label, bc in channels.items():
            self.assertEqual(label, bc.label)
            self.assertEqual(bc.short_status, expected_channels[bc.label])

            if bc.children:
                if bc.status in ("INSTALLED", "AVAILABLE"):
                    children = sorted([c.label for c in bc.children])
                    self.assertEqual(children,
                                     sorted(expected_hierarchy[bc.label]))
                else:
                    # mgr-ncc-sync does not expand UNAVAILABLE nodes
                    pass
            else:
                self.assertEqual(0, len(expected_hierarchy[bc.label]))

    def _read_data_from_fixture(self, filename):
        with open(self._path_to_fixture(filename), 'r') as file:
            data = eval(file.read())
            return data

    def _path_to_fixture(self, filename):
        return os.path.join(os.path.dirname(os.path.realpath(__file__)),
                            "..", "fixtures",
                            filename)

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

        with open(self._path_to_fixture("mgr-ncc-sync.output"), "r") as file:
            latest_base_product = None
            for line in file.readlines():
                base_product = line.startswith("[")
                status, label = filter(None, line.strip().split(" "))

                # status is something like [X]
                status = status[1]

                if base_product:
                    latest_base_product = label
                    channels_hierarcy[label] = []
                else:
                    channels_hierarcy[latest_base_product].append(label)

                channels[label] = status

        return (channels, channels_hierarcy)
