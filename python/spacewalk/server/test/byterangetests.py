#  pylint: disable=missing-module-docstring
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
import unittest


# pylint: disable-next=missing-class-docstring
class ByteRangeTests(unittest.TestCase):
    def testEmptyRange(self):
        try:
            # pylint: disable-next=undefined-variable
            server.byterange.parse_byteranges("")
            self.fail()
        # pylint: disable-next=undefined-variable
        except server.byterange.InvalidByteRangeException:
            # Expected result
            pass

    def testNoRangeGroups(self):
        try:
            # pylint: disable-next=undefined-variable
            server.byterange.parse_byteranges("bytes=")
            self.fail()
        # pylint: disable-next=undefined-variable
        except server.byterange.InvalidByteRangeException:
            # Expected result
            pass

    def testNegativeStart(self):
        try:
            # pylint: disable-next=undefined-variable
            server.byterange.parse_byteranges("bytes=-1-30")
            self.fail()
        # pylint: disable-next=undefined-variable
        except server.byterange.InvalidByteRangeException:
            pass

    def testStartAfterEnd(self):
        try:
            # pylint: disable-next=undefined-variable
            server.byterange.parse_byteranges("bytes=12-3")
            self.fail()
        # pylint: disable-next=undefined-variable
        except server.byterange.InvalidByteRangeException:
            pass

    def testNoStartOrEnd(self):
        try:
            # pylint: disable-next=undefined-variable
            server.byterange.parse_byteranges("bytes=-")
            self.fail()
        # pylint: disable-next=undefined-variable
        except server.byterange.InvalidByteRangeException:
            pass

    def testNoStartInvalidEnd(self):
        try:
            # pylint: disable-next=undefined-variable
            server.byterange.parse_byteranges("bytes=-0")
            self.fail()
        # pylint: disable-next=undefined-variable
        except server.byterange.InvalidByteRangeException:
            pass

    def testBadCharactersInRange(self):
        try:
            # pylint: disable-next=undefined-variable
            server.byterange.parse_byteranges("bytes=2-CB")
            self.fail()
        # pylint: disable-next=undefined-variable
        except server.byterange.InvalidByteRangeException:
            pass

    def testGoodRange(self):
        # pylint: disable-next=undefined-variable
        start, end = server.byterange.parse_byteranges("bytes=0-4")
        self.assertEqual(0, start)
        self.assertEqual(5, end)

    def testStartByteToEnd(self):
        # pylint: disable-next=undefined-variable
        start, end = server.byterange.parse_byteranges("bytes=12-")
        self.assertEqual(12, start)
        self.assertEqual(None, end)

    def testSuffixRange(self):
        # pylint: disable-next=undefined-variable
        start, end = server.byterange.parse_byteranges("bytes=-30")
        self.assertEqual(-30, start)
        self.assertEqual(None, end)

    def testMultipleRanges(self):
        try:
            # pylint: disable-next=undefined-variable
            server.byterange.parse_byteranges("bytes=1-3,9-12")
            self.fail()
        # pylint: disable-next=undefined-variable
        except server.byterange.UnsatisfyableByteRangeException:
            pass

    def testStartWithFileSize(self):
        # pylint: disable-next=undefined-variable
        start, end = server.byterange.parse_byteranges("bytes=23-", 50)
        self.assertEqual(23, start)
        self.assertEqual(50, end)

    def testSuffixWithFileSize(self):
        # pylint: disable-next=undefined-variable
        start, end = server.byterange.parse_byteranges("bytes=-40", 50)
        self.assertEqual(10, start)
        self.assertEqual(50, end)

    def testStartPastFileSize(self):
        try:
            # pylint: disable-next=undefined-variable
            server.byterange.parse_byteranges("bytes=50-60", 50)
            self.fail()
        # pylint: disable-next=undefined-variable
        except server.byterange.UnsatisfyableByteRangeException:
            pass

    def testSuffixLargerThanFileSize(self):
        try:
            # pylint: disable-next=undefined-variable
            server.byterange.parse_byteranges("bytes=-80", 79)
            self.fail()
        # pylint: disable-next=undefined-variable
        except server.byterange.UnsatisfyableByteRangeException:
            pass


unittest.main()
