# pylint: disable=missing-module-docstring
#   rhn-client-tools - RHN support tools and libraries
#
# Copyright (c) 2006--2016 Red Hat, Inc.
#
# SPDX-License-Identifier: GPL-2.0-only
import unittest

# pylint: disable-next=unused-import
import settestpath

import testConfig
import testTransactions
import testUp2dateUtils

from unittest import TestSuite


def suite():
    # Append all test suites here:
    return TestSuite(
        (
            testConfig.suite(),
            testTransactions.suite(),
            testUp2dateUtils.suite(),
        )
    )


if __name__ == "__main__":
    unittest.main(defaultTest="suite")
