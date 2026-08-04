# Copyright 2017-2026 SUSE LLC
# Licensed under the terms of the MIT license.

@long_running
Feature: Synchronize products in the products page of the Setup Wizard

  Scenario: Refresh SCC
    When I refresh SCC

  @susemanager
  Scenario: Synchronize all products
    Given I sync products from JSON file "products-susemanager.json"

  Scenario: Verify all channels are solved
    When I wait until all synchronized channels have solved their dependencies
    Then all channels have been synced without errors

  Scenario: Detect product loading issues from the UI in Build Validation
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "Setup Wizard" text
    And I wait until I do not see "Loading" text
    Then I should not see a "Operation not successful" text
    And I should only see success signs in the product list
