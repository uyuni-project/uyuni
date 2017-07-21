# Copyright (c) 2015-17 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test weak dependencies
  In Order to validate the new added weak dependencies feature
  As an authorized user
  I want to see the weak deps in the package page and in the metadata on the client

  Background:
    Given I am authorized as "admin" with password "admin"
    When I follow "Software" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Channels > All" in the left menu

  Scenario: Check Package metadata displayed in WebUI (Supplements)
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "virgo-dummy-2.0-1.1.noarch"
    And I follow "Dependencies"
    Then I should see a "Recommends" text
    And I should see a "Suggests" text
    And I should see a "Supplements" text
    And I should see a "packageand(a-blackhole:dummy)" text

  Scenario: Check Package metadata displayed in WebUI (Recommends)
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "milkyway-dummy-2.0-1.1.x86_64"
    And I follow "Dependencies"
    Then I should see a "Recommends" text
    And I should see a "Suggests" text
    And I should see a "Supplements" text
    And I should see a "filesystem" text

  Scenario: Check Package metadata displayed in WebUI (Suggests)
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "milkyway-dummy-2.0-1.1.x86_64"
    And I follow "Dependencies"
    Then I should see a "Recommends" text
    And I should see a "Suggests" text
    And I should see a "Supplements" text
    And I should see a "apache2" text

  Scenario: Check Package metadata displayed in WebUI (Enhances) bnc 846436
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "orion-dummy-1.1-1.1.x86_64"
    And I follow "Dependencies"
    Then I should see a "Enhances" text
    And I should see a "foobar" text

  Scenario: Check local metdata for weak deps
    When I refresh the metadata for "sle-client"
    Then I should have 'rpm:recommends.*filesystem.*rpm:recommends' in the metadata for "sle-client"
    And I should have 'rpm:supplements.*packageand.a-blackhole:dummy.*rpm:supplements' in the metadata for "sle-client"
    And I should have 'rpm:suggests.*apache2.*rpm:suggests' in the metadata for "sle-client"
    And I should have 'rpm:enhances.*foobar.*rpm:enhances' in the metadata for "sle-client"
