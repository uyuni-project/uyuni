# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license

Feature: Check tomcat logs for errors
    
  Scenario: Check the tomcat log
    Then I check the tomcat logs for errors
    And I check the tomcat logs for NullPointerExceptions
