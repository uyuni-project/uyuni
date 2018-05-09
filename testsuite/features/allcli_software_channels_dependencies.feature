# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Chanel subscription with recommended/required dependencies

@SLE15_MINION
  Scenario: Play with recommended and required child channels selection for a single system
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I check radio button "SLE-Product-SLES15-Pool for x86_64"
    And I should see the recommended toggler "disabled"
    Then I should see a text like "SLE-Module-Basesystem15-Pool for x86_64"
    And I should see the child channel "SLE-Module-Basesystem15-Pool for x86_64" "unselected"
    Then I select the child channel "SLE-Module-Basesystem15-Updates for x86_64"
    And I should see the child channel "SLE-Product-SLES15-Updates for x86_64" "selected"
    Then I click on the "disabled" recommended toggler
    And I should see the child channel "SLE-Module-Basesystem15-Pool for x86_64" "selected"

@SLE15_MINION
  Scenario: Play with recommended and required child channels selection in SSM
    Given I am authorized as "admin" with password "admin"
    When I am on the System Overview page
    And I check the "sle-minion" client
    And I check the "sle-client" client
    And I should see "2" systems selected for SSM
    And I am on System Set Manager Overview
    And I follow "channel memberships" in the content area
    Then I should see a "Base Channel" text
    And I should see a "Next" text
    And I should see a table line with "Test-Channel-x86_64", "2"
    When I select "System Default Base Channel" from drop-down in table line with "Test-Channel-x86_64"
    And I click on "Next"
    And I should see the recommended toggler "disabled"
    And I should see a text like "SLE-Module-Basesystem15-Pool for x86_64"
    And I should see "No change" "selected" for the "SLE-Module-Basesystem15-Pool for x86_64" channel
    Then I click on the "disabled" recommended toggler
    And I should see "Subscribe" "selected" for the "SLE-Module-Basesystem15-Pool for x86_64" channel
    And I should see "No change" "unselected" for the "SLE-Module-Basesystem15-Pool for x86_64" channel
