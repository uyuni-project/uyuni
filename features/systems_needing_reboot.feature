# COPYRIGHT (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Explore the main landing page
  In Order to avoid systems with different running/installed kernel
  As a authorized user
  I want to see systems that need a reboot

  Scenario: Feature should be accessible
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    Then I should see a "All" link in the left menu
    And I should see a "Requiring Reboot" link in the left menu

  Scenario: No reboot notice if no need to reboot
    Given I am on the Systems overview page of this client
    Then I should not see a "The system requires a reboot" text

  Scenario: Display Reboot Required after installing an Errata
    Given I am on the Systems overview page of this client
    And I follow "Software" in the content area
    And I follow "Errata" in the content area
    When I check "andromeda-dummy-6789" in the list
    And I click on "Apply Errata"
    And I click on "Confirm"
    And I run rhn_check on this client
    And I follow "Systems" in the left menu
    And I follow this client link
    Then I should see a "The system requires a reboot" text
    When I follow "Systems" in the left menu
    And I follow "Requiring Reboot" in the left menu
    Then I should see this client as link
