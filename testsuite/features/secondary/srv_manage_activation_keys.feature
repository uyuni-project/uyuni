# Copyright (c) 2010-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to manipulate activation keys
  In order to register systems to the spacewalk server
  As the testing user
  I want to create and edit activation keys

  Scenario: Create an activation key for i586
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE Test Key i586" as "description"
    And I enter "SUSE-DEV-i586" as "key"
    And I check "virtualization_host"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SUSE Test Key i586 has been created." text
    And I should see a "Details" link
    And I should see a "Packages" link
    And I should see a "Configuration" link in the content area
    And I should see a "Groups" link
    And I should see a "Activated Systems" link

  Scenario: Create an activation key with a channel and a package list for i586
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE Test PKG Key i586" as "description"
    And I enter "SUSE-PKG-i586" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-i586" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    And I follow "Packages"
    And I enter "sed" as "packages"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test PKG Key i586 has been modified." text
    And I should see a "Details" link
    And I should see a "Packages" link
    And I should see a "Configuration" link in the content area
    And I should see a "Groups" link
    And I should see a "Activated Systems" link

  Scenario: Change limit of the activation key
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key i586"
    And I enter "20" as "usageLimit"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key i586 has been modified." text
    And I should see "20" in field "usageLimit"

  Scenario: Change the base channel of the activation key
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key i586"
    And I select "Test-Channel-i586" from "selectedBaseChannel"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key i586 has been modified." text
