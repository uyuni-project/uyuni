# Copyright (c) 2015-17 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Debug SUSE Manager after the testsuite has run

  Scenario: Call spacewalk-debug on server
    Then I execute spacewalk-debug on the server

  Scenario: Check spacewalk upd2date logs on client
    Then I control that up2date logs on client under test contains no Traceback error

  Scenario: Check the tomcat logs on server
    Then I check the tomcat logs for errors

  Scenario: Check salt event log for failures on server
    Then I control that salt event log on server contains no failures
