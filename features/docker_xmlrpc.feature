# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature:  Container Image namespace tests

  Scenario: Assign to the sles-minion the property container build host
    Given I am on the Systems overview page of this "sle-minion"
    And I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "container_build_host" if not checked
    When I click on "Update Properties"

  Scenario: Apply the highstate to ensure container buid host is ready
    Given I am on the Systems overview page of this "sle-minion"
    Then I should see a "[Container Build Host]" text
    And I run "zypper mr -e `grep -h SLE-Manager-Tools-12-x86_64] /etc/zypp/repos.d/* | sed 's/\[//' | sed 's/\]//'`" on "sle-minion"
    And I run "zypper mr -e SLE-Module-Containers-SLE-12-x86_64-Pool" on "sle-minion"
    And I run "zypper mr -e SLE-Module-Containers-SLE-12-x86_64-Update" on "sle-minion"
    And I run "zypper mr -e SLE-12-SP2-x86_64-Pool" on "sle-minion"
    And I run "zypper mr -e SLE-12-SP2-x86_64-Update" on "sle-minion"
    And I run "zypper -n --gpg-auto-import-keys ref" on "sle-minion"
    And I wait until no Salt job is running on "sle-minion"
    And I apply highstate on "sle-minion"
    Then I wait until "docker" service is up and running on "sle-minion"

  Scenario: Test image.store Namespace
    Given I am authorized as "admin" with password "admin"
    Then I run image.store tests via xmlrpc

  Scenario: Scalability tests for image store
    Given I am authorized as "admin" with password "admin"
    Then I create "500" random image stores
    And I follow "Images" in the left menu
    And I follow "Stores" in the left menu
    Then I should see a "Registry" text

  Scenario: Test image.profiles Namespace
    Given I am authorized as "admin" with password "admin"
    Then I run image.profiles tests via xmlrpc

  Scenario: Cleanup image namespaces tests
    Given I am authorized as "admin" with password "admin"
    Then I delete the random image stores

  Scenario: Create and build multiples random images
    Given I am authorized as "admin" with password "admin"
    Then I create "5" random "suse_real_key" containers

  Scenario: CLEANUP: Remove Custom System Info key
    Given I am authorized as "admin" with password "admin"
    When I follow "Systems"
    And I follow "Custom System Info"
    And I follow "arancio"
    And I follow "Delete Key"
    And I click on "Delete Key"
