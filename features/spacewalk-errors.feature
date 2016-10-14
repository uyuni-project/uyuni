# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license

Feature: Check suse-managager logs for errors
   
  Scenario: Check spacewalk upd2date logs on client
    Then I control that up2date logs on client under test contains no Traceback error

  Scenario: Check the tomcat log on manager-server
    Then I check the tomcat logs for errors
    And I check the tomcat logs for NullPointerExceptions

