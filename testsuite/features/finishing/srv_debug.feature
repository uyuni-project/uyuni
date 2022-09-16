# Copyright (c) 2015-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Debug the server after the testsuite has run

  Scenario: Call spacewalk-debug on server
    When I execute spacewalk-debug on the server

  Scenario: Check the tomcat logs on server
    Then the tomcat logs should not contain errors

  Scenario: Check salt event log for failures on server
    Then the salt event log on server should contain no failures
