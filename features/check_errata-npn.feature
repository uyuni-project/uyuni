# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check errata
  In Order to check if the errata import was successfull
  As the testing user
  I want to see the erratums in the web page including the packages

  Scenario: check all errata exists
    Given I am on the errata page
    When I follow "Relevant" in the left menu
    Then I should see an update in the list
    And I should see a "virgo-dummy-3456" link

  Scenario: check sles-release-6789 errata
    Given I am on the errata page
    When I follow "All" in the left menu
    When I follow "andromeda-dummy-6789"
    Then I should see a "andromeda-dummy-6789 - Bug Fix Advisory" text
    And I should see a "Test update for andromeda-dummy" text
    And I should see a "SLES11-SP3-Updates x86_64 Channel" link
    And I should see a "SLES11-SP3-Updates i586 Channel" link
    And I should see a "reboot_suggested" text

  Scenario: check sles-release-6789 errata packages
    Given I am on the errata page
    When I follow "All" in the left menu
    When I follow "andromeda-dummy-6789"
    When I follow "Packages"
    Then I should see a "SLES11-SP3-Updates x86_64 Channel" link
    And I should see a "SLES11-SP3-Updates i586 Channel" link
    And I should see a "sha256:3bb3a56e6654f14300ab815c3f6e2af848874c829541b4e1b342262bb2f72d30" text
    And I should see a "andromeda-dummy-2.0-1.1-noarch" link

  Scenario: check relevant errata for this client
    Given I am on the Systems overview page of this client
    When I follow "Software" in the content area
    When I follow "Errata" in the content area
    Then I should see a "Relevant Errata" text
    And I should see a "Test update for virgo-dummy" text

  Scenario: regenerate search index for later tests
    Given I am root
    Then I clean the search index on the server
