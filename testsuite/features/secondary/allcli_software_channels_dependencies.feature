# Copyright (c) 2018-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_changing_software_channels
@scc_credentials
Feature: Channel subscription with recommended or required dependencies

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Pre-requisite: remove remaining systems from SSM after software channel tests
    When I click on the clear SSM button

@susemanager
  Scenario: Play with recommended and required child channels selection for a single system
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    # check that the required channel by the base one is selected and disabled
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP4-Pool for x86_64"
    And I wait until I do not see "Loading..." text
    Then I should see the child channel "SLE-Product-SLES15-SP4-Updates for x86_64" "selected" and "disabled"
    When I exclude the recommended child channels
    Then I should see the toggler "disabled"
    And I should see a "SLE-Module-Containers15-SP4-Pool for x86_64" text
    And I should see the child channel "SLE-Module-Containers15-SP4-Pool for x86_64" "unselected"
    # check the a child channel selection that requires some channel trigger the selection of it
    When I select the child channel "SLE-Module-Containers15-SP4-Pool for x86_64"
    Then I should see the child channel "SLE-Module-Containers15-SP4-Pool for x86_64" "selected"
    # check a recommended channel not yet selected is checked  by the recommended toggler
    When I click on the "disabled" toggler
    Then I should see the child channel "SLE-Module-Server-Applications15-SP4-Pool for x86_64" "selected"

# These tests do not test if recommended channesl are shown correctly due to the fact that we kill the reposync
# for openSUSE Leap. With this caveat, no child channels are selected when selection openSUSE as parent.
@uyuni
  Scenario: Play with recommended and required child channels selection for a single system
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait for child channels to appear
    And I check radio button "(none, disable service)"
    And I wait for child channels to appear
    And I check radio button "openSUSE Leap 15.5 (x86_64)"
    Then I should see the child channel "Uyuni Proxy Devel for openSUSE Leap 15.5 (x86_64) (Development)" "unselected" and "disabled"
    Then I should see the child channel "openSUSE 15.5 non oss (x86_64)" "selected"
    When I select the child channel "Uyuni Proxy Devel for openSUSE Leap 15.5 (x86_64) (Development)"
    Then I should see the child channel "Uyuni Proxy Devel for openSUSE Leap 15.5 (x86_64) (Development)" "selected"

@susemanager
  Scenario: Play with recommended and required child channels selection in SSM
    When I follow the left menu "Systems > System List > All"
    And I check the "sle_minion" client
    Then I should see "1" systems selected for SSM
    When I follow the left menu "Systems > System Set Manager > Overview"
    And I follow "channel memberships" in the content area
    Then I should see a "Base Channel" text
    And I should see a "Next" text
    And I should see a table line with "SLE-Product-SLES15-SP4-Pool for x86_64", "1"
    When I select "System Default Base Channel" from drop-down in table line with "SLE-Product-SLES15-SP4-Pool for x86_64"
    And I click on "Next"
    Then I should see the toggler "disabled"
    And I should see a "SLE-Module-Basesystem15-SP4-Pool for x86_64" text
    And I should see "No change" "selected" for the "SLE-Module-Basesystem15-SP4-Pool for x86_64" channel
    When I click on the "disabled" toggler
    Then I should see "Subscribe" "selected" for the "SLE-Module-Basesystem15-SP4-Pool for x86_64" channel
    And I should see "No change" "unselected" for the "SLE-Module-Basesystem15-SP4-Pool for x86_64" channel

# These tests do not test if recommended channesl are shown correctly due to the fact that we kill the reposync
# for openSUSE Leap. With this caveat, no child channels are selected when selection openSUSE as parent.
@uyuni
  Scenario: Play with recommended and required child channels selection in SSM
    When I follow the left menu "Systems > System List > All"
    And I check the "sle_minion" client
    Then I should see "1" systems selected for SSM
    When I follow the left menu "Systems > System Set Manager > Overview"
    And I follow "channel memberships" in the content area
    Then I should see a "Base Channel" text
    And I should see a "Next" text
    And I should see a table line with "openSUSE Leap 15.5 (x86_64)", "1"
    When I select "No Change" from drop-down in table line with "openSUSE Leap 15.5 (x86_64)"
    And I click on "Next"
    Then I should see the toggler "disabled"
    And I should see a "openSUSE 15.5 non oss (x86_64)" text
    And I should see "No change" "selected" for the "openSUSE 15.5 non oss (x86_64)" channel

  Scenario: Cleanup: remove remaining systems from SSM after software channel tests
    When I click on the clear SSM button
