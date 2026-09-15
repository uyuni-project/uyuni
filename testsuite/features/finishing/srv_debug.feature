# Copyright (c) 2015-2022 SUSE LLC
# SPDX-License-Identifier: MIT

Feature: Debug the server after the testsuite has run

  Scenario: Call spacewalk-debug on server
    When I execute spacewalk-debug on the server

  Scenario: Check the tomcat logs on server
    Then the tomcat logs should not contain errors

  Scenario: Check salt event log for failures on server
    Then the salt event log on server should contain no failures

  Scenario: Check the taskomatic logs on server
    Then the taskomatic logs should not contain errors

  Scenario: Check for out of memory errors
    Then the log messages should not contain out of memory errors
