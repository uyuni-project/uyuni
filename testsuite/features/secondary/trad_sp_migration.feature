# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Since an advanced setup is needed for doing SP migration we test
# only the alert warning message here for now.

@scope_traditional_client
Feature: Service pack migration

  Scenario: Check the warning message on tab "Software" => "Product Migration"
    Given I am authorized for the "Admin" section
    And I am on the Systems overview page of this "sle_client"
    When I follow "Software" in the content area
    And I follow "SP Migration" in the content area
    Then I should see a "Service Pack Migration - Target" text
