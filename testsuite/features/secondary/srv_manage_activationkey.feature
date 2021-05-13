# Copyright (c) 2010-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Manipulate activation keys
  In order to register systems to the spacewalk server
  As the testing user
  I want to create and edit activation keys

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create an activation key for i586
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE Test Key i586" as "description"
    And I enter "SUSE-TEST-i586" as "key"
    And I check "virtualization_host"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SUSE Test Key i586 has been created." text

  Scenario: Change limit of the i586 activation key
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key i586"
    And I enter "20" as "usageLimit"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key i586 has been modified." text
    And I should see "20" in field "usageLimit"

  Scenario: Change the base channel of the i586 activation key
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key i586"
    And I select "Test-Channel-i586" from "selectedBaseChannel"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key i586 has been modified." text

  Scenario: Delete the i586 activation key
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key i586" in the content area
    And I follow "Delete Key"
    And I click on "Delete Activation Key"
    Then I should see a "Activation key SUSE Test Key i586 has been deleted." text

  Scenario: Create an activation key with a channel and a package list for i586
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE Test PKG Key i586" as "description"
    And I enter "SUSE-TEST-2-i586" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-i586" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    And I follow "Packages"
    And I enter "sed" as "packages"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test PKG Key i586 has been modified." text

  Scenario: Delete the i586 activation key with packages
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test PKG Key i586" in the content area
    And I follow "Delete Key"
    And I click on "Delete Activation Key"
    Then I should see a "Activation key SUSE Test PKG Key i586 has been deleted." text

  Scenario: Create an activation key with a channel and a package list for x86_64
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE Test PKG Key x86_64" as "description"
    And I enter "SUSE-TEST-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-x86_64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    And I follow "Packages"
    And I enter "sed" as "packages"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test PKG Key x86_64 has been modified." text

  Scenario: Delete the x86_64 activation key with packages
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test PKG Key x86_64" in the content area
    And I follow "Delete Key"
    And I click on "Delete Activation Key"
    Then I should see a "Activation key SUSE Test PKG Key x86_64 has been deleted." text
