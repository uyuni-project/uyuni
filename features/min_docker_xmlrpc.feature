# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: XML-RPC image namespace for containers

  Scenario: Turn the SLES minion into a container build host
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "container_build_host" if not checked
    And I click on "Update Properties"

  Scenario: Apply the highstate to ensure container build host is ready
    Given I am on the Systems overview page of this "sle-minion"
    Then I should see a "[Container Build Host]" text
    When I enable Suse container repos, but not for Sles11 systems
    And I enable sles pool and update repo on "sle-minion"
    And I run "zypper -n --gpg-auto-import-keys ref" on "sle-minion"
    And I wait until no Salt job is running on "sle-minion"
    And I apply highstate on "sle-minion"
    Then I wait until "docker" service is up and running on "sle-minion"

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

  Scenario: Cleanup image namespaces tests
    Given I am authorized as "admin" with password "admin"
    Then I delete the random image stores

  Scenario: Create and build multiple random images
    Given I am authorized as "admin" with password "admin"
    Then I create "5" random "suse_real_key" containers

  Scenario: Cleanup: remove custom system info key
    Given I am authorized as "admin" with password "admin"
    When I follow "Systems"
    And I follow "Custom System Info"
    And I follow "arancio"
    And I follow "Delete Key"
    And I click on "Delete Key"
    And I disable Suse container repos, but not for Sles11 systems
    And I disable sles pool and update repo on "sle-minion"
    And I run "zypper -n --gpg-auto-import-keys ref" on "sle-minion"
