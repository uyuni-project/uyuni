# -*- coding: utf-8 -*-
#
# Copyright (c) 2014 SUSE
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SUSE trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate SUSE trademarks that are incorporated
# in this software or its documentation.

from enum import Enum


class Channel(object):

    class Status(str, Enum):
        INSTALLED = "INSTALLED"
        AVAILABLE = "AVAILABLE"
        UNAVAILABLE = "UNAVAILABLE"

    def __init__(self, data):
        self.base_channel = data['parent'] == 'BASE'
        self.description = data["description"]
        self.label = data["label"]
        self.name = data["name"]
        self.parent = data["parent"]
        self.status = Channel.Status(data["status"]["value"])
        self.target = data["target"]
        self.url = data["url"]
        self._children = []

    @property
    def short_status(self):
        return "[%s]" % str(self.status)[0]

    def description_or_url(self):
        return (self.description + "").strip() or self.url or "N/A"

    def add_child(self, channel):
        self._children.append(channel)

    @property
    def children(self):
        return sorted(self._children, key=lambda child: child.label)

    def to_table_row(self):
        name = self.name
        if not self.base_channel:
            name = "  \\_ " + name
        return [self.short_status,
                name,
                self.description_or_url()]

    def to_ascii_row(self, compact=False):
        if not compact:
            return "{0} {1} {2} [{3}]".format(
                self.short_status,
                self.name,
                self.description_or_url(),
                self.label)
        else:
            return "{0} {1}".format(self.short_status,  self.label)


def parse_channels(data):
    """
    Parses the data returned by SUSE Manager list channels API.
    Returns a dictionary where keys are the base channels labels and values
    are instances of Channel. Each instance is a base channel with all its
    "child" channels associated.
    """

    channels = {}

    for bc in [entry for entry in data if entry['parent'] == 'BASE']:
        base_channel = Channel(bc)
        channels[base_channel.label] = base_channel

    for entry in data:
        if entry['parent'] == 'BASE':
            continue
        channel = Channel(entry)
        channels[channel.parent].add_child(channel)

    return channels

