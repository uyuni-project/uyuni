# Copyright (c) 2015-16 SUSE LLC
# Licensed under the terms of the MIT license.

Feature:  Call spacewalk-debug on the server
 	  Check some generics logs 

  Scenario: call spacewalk-debug
    Given I am root
    Then I execute spacewalk-debug on the server

  Scenario: Check spacewalk upd2date logs on client
    Then I control that up2date logs on client under test contains no Traceback error

  Scenario: Check the tomcat log on manager-server
    Then I check the tomcat logs for errors
    And I check the tomcat logs for NullPointerExceptions
