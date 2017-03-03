# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature:  Build Container images with SUSE Manager

  Scenario: Assign to the sles-minion the property container build host 
  Given I am on the Systems overview page of this "sle-minion"
  And I follow "Details" in the content area
  And I follow "Properties" in the content area
  And I check "container_build_host"
  When I click on "Update Properties"
  Then I should see a "Container Build Host type has been applied." text 
  And I should see a "Note: This action will not result in state application" text
  And I should see a "To apply the state, either use the states page or run `state.highstate` from the command line." text
  And I should see a "System properties changed" text

  Scenario: Apply the highstate to container buid host
  Given I am on the Systems overview page of this "sle-minion"
  And I should see a "System Types: 	[Salt] [Container Build Host]" text
  And I follow "States" in the content area
  And I follow "Highstate"

  Scenario: Create an Image Store without credentials
  Given I am authorized as "admin" with password "admin"

  Scenario: Create an Image Store with authentication
  Given I am authorized as "admin" with password "admin"

  Scenario: Create an Activation Key to define the used channels during building
  Given I am authorized as "admin" with password "admin"

  Scenario: Create an Image Profile
  Given I am authorized as "admin" with password "admin"

  Scenario: Build a docker Image
  Given I am authorized as "admin" with password "admin"

  Scenario: Check the Event Log of the build host
  Given I am authorized as "admin" with password "admin"
