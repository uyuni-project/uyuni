# pylint: disable=missing-module-docstring
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
import unittest


class ByteRangeTests(unittest.TestCase):  #  pylint: disable=missing-class-docstring
    def testEmptyRange(self):
        try:
            server.byterange.parse_byteranges("")  #  pylint: disable=undefined-variable
            self.fail()
        except server.byterange.InvalidByteRangeException:  #  pylint: disable=undefined-variable
            # Expected result
            pass

    def testNoRangeGroups(self):
        try:
            server.byterange.parse_byteranges("bytes=")  #  pylint: disable=undefined-variable
            self.fail()
        except server.byterange.InvalidByteRangeException:  #  pylint: disable=undefined-variable
            # Expected result
            pass

    def testNegativeStart(self):
        try:
            server.byterange.parse_byteranges("bytes=-1-30")  #  pylint: disable=undefined-variable
            self.fail()
        except server.byterange.InvalidByteRangeException:  #  pylint: disable=undefined-variable
            pass

    def testStartAfterEnd(self):
        try:
            server.byterange.parse_byteranges("bytes=12-3")  #  pylint: disable=undefined-variable
            self.fail()
        except server.byterange.InvalidByteRangeException:  #  pylint: disable=undefined-variable
            pass

    def testNoStartOrEnd(self):
        try:
            server.byterange.parse_byteranges("bytes=-")  #  pylint: disable=undefined-variable
            self.fail()
        except server.byterange.InvalidByteRangeException:  #  pylint: disable=undefined-variable
            pass

    def testNoStartInvalidEnd(self):
        try:
            server.byterange.parse_byteranges("bytes=-0")  #  pylint: disable=undefined-variable
            self.fail()
        except server.byterange.InvalidByteRangeException:  #  pylint: disable=undefined-variable
            pass

    def testBadCharactersInRange(self):
        try:
            server.byterange.parse_byteranges("bytes=2-CB")  #  pylint: disable=undefined-variable
            self.fail()
        except server.byterange.InvalidByteRangeException:  #  pylint: disable=undefined-variable
            pass

    def testGoodRange(self):
        start, end = server.byterange.parse_byteranges("bytes=0-4")  #  pylint: disable=undefined-variable
        self.assertEqual(0, start)
        self.assertEqual(5, end)

    def testStartByteToEnd(self):
        start, end = server.byterange.parse_byteranges("bytes=12-")  #  pylint: disable=undefined-variable
        self.assertEqual(12, start)
        self.assertEqual(None, end)

    def testSuffixRange(self):
        start, end = server.byterange.parse_byteranges("bytes=-30")  #  pylint: disable=undefined-variable
        self.assertEqual(-30, start)
        self.assertEqual(None, end)

    def testMultipleRanges(self):
        try:
            server.byterange.parse_byteranges("bytes=1-3,9-12")  #  pylint: disable=undefined-variable
            self.fail()
        except server.byterange.UnsatisfyableByteRangeException:  #  pylint: disable=undefined-variable
            pass

    def testStartWithFileSize(self):
        start, end = server.byterange.parse_byteranges("bytes=23-", 50)  #  pylint: disable=undefined-variable
        self.assertEqual(23, start)
        self.assertEqual(50, end)

    def testSuffixWithFileSize(self):
        start, end = server.byterange.parse_byteranges("bytes=-40", 50)  #  pylint: disable=undefined-variable
        self.assertEqual(10, start)
        self.assertEqual(50, end)

    def testStartPastFileSize(self):
        try:
            server.byterange.parse_byteranges("bytes=50-60", 50)  #  pylint: disable=undefined-variable
            self.fail()
        except server.byterange.UnsatisfyableByteRangeException:  #  pylint: disable=undefined-variable
            pass

    def testSuffixLargerThanFileSize(self):
        try:
            server.byterange.parse_byteranges("bytes=-80", 79)  #  pylint: disable=undefined-variable
            self.fail()
        except server.byterange.UnsatisfyableByteRangeException:  #  pylint: disable=undefined-variable
            pass


unittest.main()
