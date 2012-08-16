# Copyright (c) 2010-2012 Novell, Inc.
# Licensed under the terms of the MIT license.

# features/running.feature
Feature: The host is running
  In order to see if the host is running
  As an unauthorized user
  I want to see the Login page and unprotected contents

  Scenario: Accessing the Login page
    Given I am not authorized
    When I go to the home page
    Then I should see something

  Scenario: Accessing the About page
    Given I am not authorized
    When I go to the home page
    And I follow "About"
    Then I should see a "About SUSE Manager" text

  Scenario: Accessing the Copyright Notice
    Given I am not authorized
    When I go to the home page
    And I follow "Copyright Notice"
    Then I should see a "Copyright (c) 2011 Novell, Inc" text

  Scenario: Accessing the EULA
    Given I am not authorized
    When I go to the home page
    And I follow "Copyright Notice"
    And I follow "SUSE MANAGER LICENSE AGREEMENT"
    Then I should see a "SUSE Manager License Agreement" text
