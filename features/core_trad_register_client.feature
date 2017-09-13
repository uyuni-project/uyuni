# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Register a client
  In order to register a client to the spacewalk server
  As the root user
  I want to call rhnreg_ks

  Scenario: Register a client
    When I register using "1-SUSE-DEV-x86_64" key
    Then I should see "sle-client" in spacewalk

  Scenario: Check registration values
    Given I update the profile of this client
    When I am on the Systems overview page of this "sle-client"
    And I click on the css "a#clear-ssm"
    And I wait until I see "Software Updates Available" text, refreshing the page
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

  Scenario: Check tab links "Software" => "Patches"
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    Then I should see a "Relevant Patches" text
    And I should see a "Show" button
