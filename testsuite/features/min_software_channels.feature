# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Assign channels with dependencies

  Scenario: Check recommended child channels selection
    Given I am on the Systems overview page of this "sle-minion"
    And the "sle-minion" is a SLE-"15" client
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    Then I should see a "Basesystem Module 15" text
    And I should see the recommended toggler "enabled"
    And I should see the child channel "Basesystem Module 15" "selected"
    Then I click on the "enabled" recommended toggler
    And I should see the child channel "Basesystem Module 15" "unselected"