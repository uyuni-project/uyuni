#!/usr/bin/env python
# pylint: disable=missing-module-docstring
# -*- coding: utf-8 -*-
#
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: LGPL-2.1-only
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
