# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Register a client
  In Order register a client to the spacewalk server
  As the root user
  I want to call rhnreg_ks

  Scenario: Register a client
    When I register using an activation key
    Then I should see "sle-client" in spacewalk

  Scenario: check registration values
    Given I update the profile of this client
    When I am on the Systems overview page of this "sle-client"
    And I click on the css "a#clear-ssm"
    And I wait for the data update
    Then I should see a "System Status" text
    And I should see a "Software Updates Available" text
    And I should see a "Critical:" link
    And I should see a "Non-Critical:" link
    And I should see a "Packages:" link
    And I should see a "Edit These Properties" link
    And I should see a "[Management]" text
    And I should see a "Add to SSM" link
    And I should see a "Delete System" link
    And I should see a "Initial Registration Parameters:" text
    And I should see a "OS: sles-release" text
    And I should see a "Release: 12.2" text

  Scenario: check tab links "Software" => "Errata"
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Errata" in the content area
    Then I should see a "Relevant Errata" text
    And I should see a "Show" button
    And I should see a "Test update for virgo-dummy" text
    And I should see an update in the list
    And I should see a "andromeda-dummy-6789" link
