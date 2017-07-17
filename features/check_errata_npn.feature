# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check patches
  In Order to check if the patches import was successfull
  As the testing user
  I want to see the patches in the web page including the packages

  Scenario: check all patches exists
    Given I am on the patches page
    When I follow "Relevant" in the left menu
    Then I should see an update in the list
    And I should see a "virgo-dummy-3456" link

  Scenario: check sles-release-6789 patches
    Given I am on the patches page
    And I follow "andromeda-dummy-6789"
    Then I should see a "andromeda-dummy-6789 - Bug Fix Advisory" text
    And I should see a "Test update for andromeda-dummy" text
    And I should see a "Test-Channel-x86_64" link
    And I should see a "Test-Channel-i586" link
    And I should see a "reboot_suggested" text

  Scenario: check sles-release-6789 patches packages
    Given I am on the patches page
    And I follow "andromeda-dummy-6789"
    And I follow "Packages"
    Then I should see a "Test-Channel-x86_64" link
    And I should see a "Test-Channel-i586" link
    And I should see a "sha256:3bb3a56e6654f14300ab815c3f6e2af848874c829541b4e1b342262bb2f72d30" text
    And I should see a "andromeda-dummy-2.0-1.1-noarch" link

  Scenario: check relevant patches for this client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    Then I should see a "Relevant Patches" text
    And I should see a "Test update for virgo-dummy" text

  Scenario: regenerate search index for later tests
    Then I clean the search index on the server
