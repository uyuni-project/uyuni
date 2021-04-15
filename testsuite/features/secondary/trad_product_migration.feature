# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Since an advanced setup is needed for doing SP migration we test
# only the alert warning message here for now.

@scope_traditional_client
@scope_product_migration
Feature: Product migration

  Scenario: Check the warning message on tab "Software" => "Product Migration"
    Given I am authorized for the "Admin" section
    And I am on the Systems overview page of this "sle_client"
    When I follow "Software" in the content area
    And I follow "Product Migration" in the content area
    Then I should see a "Product Migration - Target" text
