#!/usr/bin/python
#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2008--2015 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
#

import sys
import locale
import unittest
from unittest.mock import MagicMock, patch
import time
from uyuni.common import rhnLib

TIMEZONE_SHIFT = time.timezone / 3600


# pylint: disable-next=missing-class-docstring
class Tests(unittest.TestCase):
    # pylint: disable=R0904

    ###########################################################################
    # Tests for rhnLib.rfc822time()
    ###########################################################################

    @patch(
        "uyuni.common.rhnLib.time.gmtime",
        MagicMock(return_value=(2006, 1, 27, 14, 12, 5, 4, 27, 0)),
    )
    def test_rfc822time_normal_tuple(self):
        "rfc822time: Simple call using a valid tuple argument."
        test_arg = (2006, 1, 27, int(14 - TIMEZONE_SHIFT), 12, 5, 4, 27, -1)
        target = "Fri, 27 Jan 2006 14:12:05 GMT"
        result = rhnLib.rfc822time(test_arg)
        self.assertEqual(result, target, result + " != " + target)

    @patch(
        "uyuni.common.rhnLib.time.gmtime",
        MagicMock(return_value=(2006, 1, 27, 14, 12, 5, 4, 27, 0)),
    )
    def test_rfc822time_normal_list(self):
        "rfc822time: Simple call using a valid list argument."
        test_arg = [2006, 1, 27, int(14 - TIMEZONE_SHIFT), 12, 5, 4, 27, -1]
        target = "Fri, 27 Jan 2006 14:12:05 GMT"
        result = rhnLib.rfc822time(test_arg)
        self.assertEqual(result, target, result + " != " + target)

    def test_rfc822time_normal_float(self):
        "rfc822time: Simple call using a valid float argument."
        test_arg = 1138371125
        target = "Fri, 27 Jan 2006 14:12:05 GMT"
        result = rhnLib.rfc822time(test_arg)
        self.assertEqual(result, target, result + " != " + target)

    def test_rfc822time_japan_locale(self):
        "rfc822time: Test result in ja_JP locale."
        test_arg = 1138371125
        target = "Fri, 27 Jan 2006 14:12:05 GMT"
        old_locale = locale.getlocale(locale.LC_TIME)
        locale.setlocale(locale.LC_TIME, "ja_JP")
        result = rhnLib.rfc822time(test_arg)
        locale.setlocale(locale.LC_TIME, old_locale)
        self.assertEqual(result, target, result + " != " + target)

    def testParseUrl(self):
        self.assertEqual(("", "", "", "", "", ""), rhnLib.parseUrl(""))
        self.assertEqual(
            ("", "somehostname", "", "", "", ""), rhnLib.parseUrl("somehostname")
        )
        self.assertEqual(
            ("http", "somehostname", "", "", "", ""),
            rhnLib.parseUrl("http://somehostname"),
        )
        self.assertEqual(
            ("https", "somehostname", "", "", "", ""),
            rhnLib.parseUrl("https://somehostname"),
        )
        self.assertEqual(
            ("https", "somehostname:123", "", "", "", ""),
            rhnLib.parseUrl("https://somehostname:123"),
        )
        self.assertEqual(
            ("https", "somehostname:123", "/ABCDE", "", "", ""),
            rhnLib.parseUrl("https://somehostname:123/ABCDE"),
        )


if __name__ == "__main__":
    sys.exit(unittest.main() or 0)
