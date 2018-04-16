# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Assign channels with dependencies

  Scenario: Check recommended child channels selection
    Given I am on the Systems overview page of this "sle-minion"
    And the "sle-minion" is a SLE-"15" client
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I check radio button "SLE-Product-SLES15-Pool for x86_64"
    And I should see the recommended toggler "disabled"
    Then I should see a text like "SLE-Module-Basesystem15-Pool for x86_64"
    And I should see the child channel "SLE-Module-Basesystem15-Pool for x86_64" "unselected"
    Then I click on the "disabled" recommended toggler
    And I should see the child channel "SLE-Module-Basesystem15-Pool for x86_64" "selected"