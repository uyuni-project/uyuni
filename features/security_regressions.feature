# Copyright (c) 2014 Novell, Inc.
#
# Licensed under the terms of the MIT license
#

Feature: Check that there are no security regressions

  Scenario: Security issues found via ZAP
    When the testsuite was run through ZAP as proxy
    And an active attack was performed
    Then there are not security issues


