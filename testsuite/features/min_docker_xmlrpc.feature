# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: XML-RPC image namespace for containers

  Scenario: Test image.store namespace
    Given I am authorized as "admin" with password "admin"
    Then I run image.store tests via XML-RPC

  Scenario: Scalability tests for image store
    Given I am authorized as "admin" with password "admin"
    Then I create "500" random image stores
    And I follow "Images" in the left menu
    And I follow "Stores" in the left menu
    Then I should see a "Registry" text

  Scenario: Test image.profiles namespace
    Given I am authorized as "admin" with password "admin"
    Then I run image.profiles tests via XML-RPC

  Scenario: Cleanup image namespace tests
    Given I am authorized as "admin" with password "admin"
    Then I delete the random image stores

  Scenario: Create and build multiple random images
    Given I am authorized as "admin" with password "admin"
    Then I create "5" random "suse_real_key" containers
    And I wait for "240" seconds
    And I wait until no Salt job is running on "sle-minion"

  Scenario: Cleanup: remove custom system info key
    Given I am authorized as "admin" with password "admin"
    When I follow "Systems"
    And I follow "Custom System Info"
    And I follow "arancio"
    And I follow "Delete Key"
    And I click on "Delete Key"
