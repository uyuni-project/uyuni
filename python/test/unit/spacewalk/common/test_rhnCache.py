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
import unittest
from spacewalk.common import rhnCache


# pylint: disable-next=missing-class-docstring
class Tests(unittest.TestCase):
    # pylint: disable=R0904
    key = "unit-test/test"
    content = ""
    for i in range(256):
        content = content + chr(i)

    def test_cache_1(self):
        "Tests storing of simple strings"
        content = self.content * 10
        self._test(self.key, content)

    def test_cache_2(self):
        "Tests storing of more complex data structures"
        content = [(1, 2, 3), {"a": 1}, "ab"]
        self._test(self.key, content)

    def test_cache_3(self):
        "Tests storing of raw content"
        content = self.content * 10
        self._test(self.key, content, raw=1)

    def test_cache_4(self):
        "Tests storing of raw content"
        content = self.content * 10
        self._test(self.key, content, raw=1, modified="20041110001122")

    def _test(self, key, content, **modifiers):
        # Blow it away
        rhnCache.CACHEDIR = "/tmp/rhn"
        self._cleanup(key)
        rhnCache.set(key, content, **modifiers)
        self.assertTrue(rhnCache.has_key(key))
        content2 = rhnCache.get(key, **modifiers)
        self.assertEqual(content, content2)

        self._cleanup(key)
        self.assertFalse(rhnCache.has_key(key))
        return (key, content)

    def test_cache_5(self):
        content = self.content * 10
        timestamp = "20041110001122"

        self._cleanup(self.key)
        rhnCache.set(self.key, content, modified=timestamp)

        self.assertTrue(rhnCache.has_key(self.key))
        self.assertTrue(rhnCache.has_key(self.key, modified=timestamp))
        self.assertFalse(rhnCache.has_key(self.key, modified="20001122112233"))
        self._cleanup(self.key)

    def test_missing_1(self):
        "Tests exceptions raised by the code"
        self._cleanup(self.key)
        self.assertEqual(None, rhnCache.get(self.key))

    def test_exception_1(self):
        "Tests raising exceptions"
        self.assertRaises(KeyError, rhnCache.get, self.key, missing_is_null=0)

    def test_opening_uncompressed_data_as_compressed(self):
        "Should return None, opening uncompressed data as compressed"
        rhnCache.set(self.key, self.content, raw=1)

        self.assertEqual(None, rhnCache.get(self.key, compressed=1, raw=1))

        self._cleanup(self.key)

    def test_opening_raw_data_as_pickled(self):
        "Should return None, opening uncompressed data as compressed"
        rhnCache.set(self.key, "12345", raw=1)

        self.assertEqual(None, rhnCache.get(self.key, raw=0))

        self._cleanup(self.key)

    def _cleanup(self, key):
        if rhnCache.has_key(key):
            rhnCache.delete(key)

        self.assertFalse(rhnCache.has_key(key))


if __name__ == "__main__":
    sys.exit(unittest.main() or 0)
