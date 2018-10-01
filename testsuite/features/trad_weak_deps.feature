# Copyright (c) 2015-17 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Weak dependencies in the package page and in the metadata on the client

  Background:
    Given I am authorized as "admin" with password "admin"
    When I follow "Software" in the left menu
    And I follow "Channel List" in the left menu
    And I follow "Channel List > All" in the left menu

  Scenario: Pre-requisite: remove packages before weak-dependancies test
   When I run "zypper -n in virgo-dummy" on "sle-client" without error control
   And I run "zypper -n in milkyway-dummy" on "sle-client" without error control
   And I run "zypper -n in orion-dummy" on "sle-client" without error control

  Scenario: Show Supplements information
    When I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "virgo-dummy-2.0-1.1.noarch"
    And I follow "Dependencies"
    Then I should see a "Recommends" text
    And I should see a "Suggests" text
    And I should see a "Supplements" text
    And I should see a "packageand(a-blackhole:dummy)" text

  Scenario: Show Recommends information
    When I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "milkyway-dummy-2.0-1.1.x86_64"
    And I follow "Dependencies"
    Then I should see a "Recommends" text
    And I should see a "Suggests" text
    And I should see a "Supplements" text
    And I should see a "filesystem" text

  Scenario: Show Suggests information
    When I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "milkyway-dummy-2.0-1.1.x86_64"
    And I follow "Dependencies"
    Then I should see a "Recommends" text
    And I should see a "Suggests" text
    And I should see a "Supplements" text
    And I should see a "apache2" text

  Scenario: Show Enhances information
    # bsc#846436 - extra packages installed when performing a patch update
    When I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "orion-dummy-1.1-1.1.x86_64"
    And I follow "Dependencies"
    Then I should see a "Enhances" text
    And I should see a "foobar" text

  Scenario: Check local metadata for weak dependencies
    When I refresh the metadata for "sle-client"
    Then I should have 'rpm:recommends.*filesystem.*rpm:recommends' in the metadata for "sle-client"
    And I should have 'rpm:supplements.*packageand.a-blackhole:dummy.*rpm:supplements' in the metadata for "sle-client"
    And I should have 'rpm:suggests.*apache2.*rpm:suggests' in the metadata for "sle-client"
    And I should have 'rpm:enhances.*foobar.*rpm:enhances' in the metadata for "sle-client"

  Scenario: Cleanup: remove packages after weak dependancies tests
   And I run "zypper -n rm virgo-dummy" on "sle-client"
   And I run "zypper -n rm milkyway-dummy" on "sle-client"
   And I run "zypper -n rm orion-dummy" on "sle-client"
