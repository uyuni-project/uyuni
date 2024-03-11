#   rhn-client-tools - RHN support tools and libraries
#
# Copyright (c) 2006--2016 Red Hat, Inc.
#
#   This program is free software; you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation; version 2 of the License.
#
#   This program is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License
#   along with this program; if not, write to the Free Software
#   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
#   02110-1301  USA

import unittest
import settestpath

import testByteRangeRpcServer
import testClientCaps
import testConfig
import testTransactions
import testUp2dateUtils

from unittest import TestSuite

def suite():
    # Append all test suites here:
    return TestSuite((
        testByteRangeRpcServer.suite(),
        testClientCaps.suite(),
        testConfig.suite(),
        testTransactions.suite(),
        testUp2dateUtils.suite(),
    ))

if __name__ == "__main__":
    unittest.main(defaultTest="suite")
