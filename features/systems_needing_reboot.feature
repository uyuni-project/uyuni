# COPYRIGHT (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Reboot required after patch
  In Order to avoid systems with different running/installed kernel
  As a authorized user
  I want to see systems that need a reboot

  Scenario: Check requiring Rebot on the webui
    Given I am authorized
    And I follow "Home" in the left menu
    And I follow "Systems" in the left menu
    And I follow "Overview" in the left menu
    When I click Systems, under Systems node
    Then I should see a "All" link in the left menu
    And  I follow "All" in the left menu
    Then I should see a "Requiring Reboot" link in the left menu

  Scenario: No reboot notice if no need to reboot
    Given I am on the Systems overview page of this "sle-client"
    Then I should not see a "The system requires a reboot" text

  Scenario: Display Reboot Required after installing an Errata
    Given I am on the Systems overview page of this "sle-client"
    And I follow "Software" in the content area
    And I follow "Errata" in the content area
    When I check "andromeda-dummy-6789" in the list
    And I click on "Apply Errata"
    And I click on "Confirm"
    And I run rhn_check on this client
    And I follow "Overview" in the left menu
    And I click Systems, under Systems node
    And I follow "All" in the left menu
    And I follow this "sle-client" link
    Then I should see a "The system requires a reboot" text
    And I follow "Overview" in the left menu
    And I click Systems, under Systems node
    And I follow "Requiring Reboot" in the left menu
    Then I should see "sle-client" as link
