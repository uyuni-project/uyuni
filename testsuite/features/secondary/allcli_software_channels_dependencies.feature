# Copyright (c) 2018-2021 SUSE LLC
# Licensed under the terms of the MIT license.
#
# TODO
# This feature test is currently disabled
# It has to be rewritten using fake packages (orion-dummy etc) instead of real synched packages

@scope_changing_software_channels
Feature: Chanel subscription with recommended/required dependencies

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Play with recommended and required child channels selection for a single system
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    # check that the required channel by the base one is selected and disabled
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP2-Pool for x86_64"
    And I wait until I do not see "Loading..." text
    Then I should see the child channel "SLE-Product-SLES15-SP2-Updates for x86_64" "selected" and "disabled"
    And I should see the toggler "disabled"
    And I should see a "SLE-Module-Basesystem15-SP2-Pool for x86_64" text
    And I should see the child channel "SLE-Module-Basesystem15-SP2-Pool for x86_64" "unselected"
    # check the a child channel selection that requires some channel trigger the selection of it
    When I select the child channel "SLE-Module-Basesystem15-SP2-Updates for x86_64"
    Then I should see the child channel "SLE-Module-Basesystem15-SP2-Pool for x86_64" "selected"
    # check a recommended channel not yet selected is checked  by the recommended toggler
    When I click on the "disabled" toggler
    Then I should see the child channel "SLE-Module-Server-Applications15-SP2-Pool for x86_64" "selected"

  Scenario: Play with recommended and required child channels selection in SSM
    When I am on the System Overview page
    And I check the "sle_minion" client
    And I check the "sle_client" client
    Then I should see "2" systems selected for SSM
    When I am on System Set Manager Overview
    And I follow "channel memberships" in the content area
    Then I should see a "Base Channel" text
    And I should see a "Next" text
    And I should see a table line with "Test-Channel-x86_64", "2"
    When I select "System Default Base Channel" from drop-down in table line with "Test-Channel-x86_64"
    And I click on "Next"
    Then I should see the toggler "disabled"
    And I should see a "SLE-Module-Basesystem15-SP2-Pool for x86_64" text
    And I should see "No change" "selected" for the "SLE-Module-Basesystem15-SP2-Pool for x86_64" channel
    When I click on the "disabled" toggler
    Then I should see "Subscribe" "selected" for the "SLE-Module-Basesystem15-SP2-Pool for x86_64" channel
    And I should see "No change" "unselected" for the "SLE-Module-Basesystem15-SP2-Pool for x86_64" channel

  Scenario: Cleanup: remove remaining systems from SSM after software channel tests
    When I follow "Clear"
