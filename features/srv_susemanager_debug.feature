# Copyright (c) 2015-17 SUSE LLC
# Licensed under the terms of the MIT license.

Feature:  Debug Suse-Manager server after the testsuite has run

  Scenario: call spacewalk-debug
    Then I execute spacewalk-debug on the server

  Scenario: Check spacewalk upd2date logs on client
    Then I control that up2date logs on client under test contains no Traceback error

  Scenario: Check the tomcat log on manager-server
    Then I check the tomcat logs for errors

  Scenario: Check that no scheduled events have failed on manager server
     Then there should be no failed scheduled actions
