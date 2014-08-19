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

import mock
import re
import unittest2 as unittest

from spacewalk.susemanager.mgr_sync.helpers import cli_ask


def fake_user_input(*args):
    for ret_value in args:
        yield ret_value


class HelpersTest(unittest.TestCase):

    def test_cli_ask_without_validator(self):
        message = "how are you"
        response = "I'm fine"

        with mock.patch('__builtin__.raw_input') as mocked_input:
            mocked_input.side_effect = fake_user_input(None, response)
            value = cli_ask(message)

        self.assertEqual(response, value)
        self.assertEqual(2, mocked_input.call_count)

    def test_cli_ask_with_tuple_validator(self):
        message = "how are you?"
        response = "happy"
        validator = ("happy", "blue")

        with mock.patch('__builtin__.raw_input') as mocked_input:
            mocked_input.side_effect = fake_user_input("angry", response)
            value = cli_ask(message, validator=validator)

        self.assertEqual(response, value)
        self.assertEqual(2, mocked_input.call_count)

    def test_cli_ask_with_list_validator(self):
        message = "how are you?"
        response = "happy"
        validator = ["happy", "blue"]

        with mock.patch('__builtin__.raw_input') as mocked_input:
            mocked_input.side_effect = fake_user_input("angry", response)
            value = cli_ask(message, validator=validator)

        self.assertEqual(response, value)
        self.assertEqual(2, mocked_input.call_count)

    def test_cli_ask_with_regexp_validator(self):
        message = "how old are you?"
        response = "18"
        validator = "\d+"

        with mock.patch('__builtin__.raw_input') as mocked_input:
            mocked_input.side_effect = fake_user_input("young", response)
            value = cli_ask(message, validator=validator)

        self.assertEqual(response, value)
        self.assertEqual(2, mocked_input.call_count)

    def test_cli_ask_with_custom_validator(self):
        message = "how old are you?"
        response = "10"
        validator = lambda i: re.search("\d+", i) and int(i) in range(1, 19)

        with mock.patch('__builtin__.raw_input') as mocked_input:
            mocked_input.side_effect = fake_user_input("hi", "0", "40", "10")
            value = cli_ask(message, validator=validator)

        self.assertEqual(response, value)
        self.assertEqual(4, mocked_input.call_count)

