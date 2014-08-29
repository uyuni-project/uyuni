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
        self.summary = data["summary"]
        self.label = data["label"]
        self.name = data["name"]
        self.parent = data["parent"]
        self.status = data["status"]
        self.url = data["source_url"]
        self.optional = data["optional"]
        self._children = []

    @property
    def short_status(self):
        return "[%s]" % str(self.status[0])

    def summary_or_url(self):
        return (self.summary + "").strip() or self.url or "N/A"

    def add_child(self, channel):
        self._children.append(channel)

    @property
    def children(self):
        return sorted(self._children, key=lambda child: child.label)

    def to_ascii_row(self, compact=False):
        if not compact:
            return "{0} {1} {2} [{3}]".format(
                self.short_status,
                self.name,
                self.summary_or_url(),
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


def find_channel_by_label(label, channels):
    """ Looks for channel with label
    :param channels: a data structure returned by `parse_channels`
    :param label: the label to search
    :return: None if the channel is not found, a Channel instance otherwise
    """

    if label in channels.keys():
        return channels[label]

    for bc in channels.values():
        matches = [c for c in bc.children if c.label == label]
        if len(matches) == 1:
            return matches[0]

    return None

