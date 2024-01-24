#!/usr/bin/env python
# pylint: disable=missing-module-docstring
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

try:
    # pylint: disable-next=unused-import
    from unittest import mock
except ImportError:
    import mock

import os
import re
import sys

from spacewalk.susemanager.helpers import cli_ask

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
# pylint: disable-next=wrong-import-position
from .helper import FakeStdin


class HelpersTest(unittest.TestCase):
    def test_cli_ask_without_validator(self):
        message = "how are you"
        response = "I'm fine"

        with FakeStdin(None, response) as mocked_input:
            value = cli_ask(message)
            self.assertEqual(response, value)
            self.assertEqual(2, mocked_input.call_count)

    def test_cli_ask_with_tuple_validator(self):
        message = "how are you?"
        response = "happy"
        validator = ("happy", "blue")

        with FakeStdin("angry", response) as mocked_input:
            value = cli_ask(message, validator=validator)
            self.assertEqual(response, value)
            self.assertEqual(2, mocked_input.call_count)

    def test_cli_ask_with_list_validator(self):
        message = "how are you?"
        response = "happy"
        validator = ["happy", "blue"]

        with FakeStdin("angry", response) as mocked_input:
            value = cli_ask(message, validator=validator)
            self.assertEqual(response, value)
            self.assertEqual(2, mocked_input.call_count)

    def test_cli_ask_with_regexp_validator(self):
        message = "how old are you?"
        response = "18"
        # pylint: disable-next=anomalous-backslash-in-string
        validator = "\d+"

        with FakeStdin("young", response) as mocked_input:
            value = cli_ask(message, validator=validator)
            self.assertEqual(response, value)
            self.assertEqual(2, mocked_input.call_count)

    def test_cli_ask_with_custom_validator(self):
        message = "how old are you?"
        response = "10"
        # pylint: disable-next=anomalous-backslash-in-string,unnecessary-lambda-assignment
        validator = lambda i: re.search("\d+", i) and int(i) in range(1, 19)

        with FakeStdin("young", "0", "40", "10") as mocked_input:
            value = cli_ask(message, validator=validator)
            self.assertEqual(response, value)
            self.assertEqual(4, mocked_input.call_count)
