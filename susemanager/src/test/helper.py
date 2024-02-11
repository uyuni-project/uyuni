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
    from cStringIO import StringIO
except ImportError:
    from io import StringIO

try:
    from unittest import mock
except ImportError:
    import mock

import os
import sys


# pylint: disable-next=missing-class-docstring
class ConsoleRecorder(object):
    def __enter__(self):
        self._stdout = sys.stdout
        self._stderr = sys.stderr
        sys.stdout = self._stringio = StringIO()
        sys.stderr = self._stringioerr = StringIO()
        return self

    def __exit__(self, *args):
        self.stdout = self._stringio.getvalue().splitlines()
        self.stderr = self._stringioerr.getvalue().splitlines()
        sys.stdout = self._stdout
        sys.stderr = self._stderr


def fake_user_input(*args):
    for ret_value in args:
        yield ret_value


# pylint: disable-next=missing-class-docstring
class FakeStdin:
    def __init__(self, *fake_input):
        self.fake_input = fake_input

    def __enter__(self):
        if sys.version_info < (3,):
            self.patcher = mock.patch("__builtin__.raw_input")
        else:
            self.patcher = mock.patch("builtins.input")
        self.mock = self.patcher.start()
        self.mock.side_effect = fake_user_input(*self.fake_input)
        return self.mock

    def __exit__(self, *args):
        self.patcher.stop()
        return self

    @property
    def call_count(self):
        return self.mock.call_count


def read_data_from_fixture(filename):
    # pylint: disable-next=unspecified-encoding
    with open(path_to_fixture(filename), "r") as file:
        # pylint: disable-next=eval-used
        data = eval(file.read().replace("\r\n", ""))
        return data


def path_to_fixture(filename):
    return os.path.join(
        os.path.dirname(os.path.realpath(__file__)), "fixtures", filename
    )
