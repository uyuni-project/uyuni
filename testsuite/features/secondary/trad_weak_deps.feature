# Copyright (c) 2015-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
Feature: Weak dependencies in the package page and in the metadata on the client

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: remove packages before weak-dependancies test
   When I install package "virgo-dummy" on this "sle_client" without error control
   And I install package "milkyway-dummy" on this "sle_client" without error control
   And I install package "orion-dummy" on this "sle_client" without error control

  Scenario: Show Supplements information
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "virgo-dummy-2.0-1.1.noarch"
    And I follow "Dependencies"
    Then I should see a "Recommends" text
    And I should see a "Suggests" text
    And I should see a "Supplements" text
    And I should see a "packageand(a-blackhole:dummy)" text

  Scenario: Show Recommends information
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "milkyway-dummy-2.0-1.1.x86_64"
    And I follow "Dependencies"
    Then I should see a "Recommends" text
    And I should see a "Suggests" text
    And I should see a "Supplements" text
    And I should see a "filesystem" text

  Scenario: Show Suggests information
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "milkyway-dummy-2.0-1.1.x86_64"
    And I follow "Dependencies"
    Then I should see a "Recommends" text
    And I should see a "Suggests" text
    And I should see a "Supplements" text
    And I should see a "apache2" text

  Scenario: Show Enhances information
    # bsc#846436 - extra packages installed when performing a patch update
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "orion-dummy-1.1-1.1.x86_64"
    And I follow "Dependencies"
    Then I should see a "Enhances" text
    And I should see a "foobar" text

  Scenario: Check local metadata for weak dependencies
    When I refresh the metadata for "sle_client"
    Then I should have 'rpm:recommends.*filesystem.*rpm:recommends' in the metadata for "sle_client"
    And I should have 'rpm:supplements.*packageand.a-blackhole:dummy.*rpm:supplements' in the metadata for "sle_client"
    And I should have 'rpm:suggests.*apache2.*rpm:suggests' in the metadata for "sle_client"
    And I should have 'rpm:enhances.*foobar.*rpm:enhances' in the metadata for "sle_client"

  Scenario: Cleanup: remove packages after weak dependancies tests
   And I remove package "virgo-dummy" from this "sle_client"
   And I remove package "milkyway-dummy" from this "sle_client"
   And I remove package "orion-dummy" from this "sle_client"
