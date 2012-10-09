# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

# features/create_activationkey.feature
Feature: Create an activation key
  In Order register a system to the spacewalk server
  As the testing user
  I want to create an activation key

  Scenario: fail to create an activation key
    Given I am on the Systems page
      And I follow "Activation Keys" in the left menu
      And I follow "create new key"
    When I enter "SUSE Test Key" as "description"
     And I enter "SUSE-DEV" as "key"
     And I check "monitoring_entitled"
     And I check "provisioning_entitled"
     And I check "virtualization_host"
     And I check "virtualization_host_platform"
     And I click on "Create Activation Key"
   Then I should see a "A system can have either the Virtualization or Virtualization Platform entitlement but not both." text

  Scenario: create an activation key
    Given I am on the Systems page
      And I follow "Activation Keys" in the left menu
      And I follow "create new key"
    When I enter "SUSE Test Key i586" as "description"
     And I enter "SUSE-DEV-i586" as "key"
     And I check "monitoring_entitled"
     And I check "provisioning_entitled"
     And I check "virtualization_host"
     And I click on "Create Activation Key"
   Then I should see a "Activation key SUSE Test Key i586 has been created." text
    And I should see a "Details" link
    And I should see a "Child Channels" link
    And I should see a "Packages" link
    And I should see a "Configuration" link
    And I should see a "Groups" link
    And I should see a "Activated Systems" link

  Scenario: Change limit of the activation key
    Given I am on the Systems page
      And I follow "Activation Keys" in the left menu
      And I follow "SUSE Test Key i586"
    When I enter "20" as "usageLimit"
     And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key i586 has been modified." text
     And I should see "20" in field "usageLimit"

  Scenario: Change Base Channel of the activation key
    Given I am on the Systems page
      And I follow "Activation Keys" in the left menu
      And I follow "SUSE Test Key i586"
    When I select "SLES11-SP2-Updates i586 Channel" from "selectedChannel"
     And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key i586 has been modified." text

  Scenario: create an activation key with Channel
    Given I am on the Systems page
      And I follow "Activation Keys" in the left menu
      And I follow "create new key"
    When I enter "SUSE Test Key x86_64" as "description"
     And I enter "SUSE-DEV-x86_64" as "key"
     And I check "monitoring_entitled"
     And I check "provisioning_entitled"
     And I check "virtualization_host"
     And I enter "20" as "usageLimit"
     And I select "SLES11-SP2-Updates x86_64 Channel" from "selectedChannel"
     And I click on "Create Activation Key"
   Then I should see a "Activation key SUSE Test Key x86_64 has been created" text
    And I should see a "Details" link
    And I should see a "Child Channels" link
    And I should see a "Packages" link
    And I should see a "Configuration" link
    And I should see a "Groups" link
    And I should see a "Activated Systems" link

  Scenario: create an activation key with Channel and package list
    Given I am on the Systems page
      And I follow "Activation Keys" in the left menu
      And I follow "create new key"
    When I enter "SUSE Test PKG Key x86_64" as "description"
     And I enter "SUSE-PKG-x86_64" as "key"
     And I check "monitoring_entitled"
     And I check "provisioning_entitled"
     And I enter "20" as "usageLimit"
     And I select "SLES11-SP2-Updates x86_64 Channel" from "selectedChannel"
     And I click on "Create Activation Key"
     And I follow "Packages"
     And I enter "man" as "packages"
     And I click on "Update Key"
   Then I should see a "Activation key SUSE Test PKG Key x86_64 has been modified." text
    And I should see a "Details" link
    And I should see a "Child Channels" link
    And I should see a "Packages" link
    And I should see a "Configuration" link
    And I should see a "Groups" link
    And I should see a "Activated Systems" link

  Scenario: create an activation key with Channel and package list
    Given I am on the Systems page
      And I follow "Activation Keys" in the left menu
      And I follow "create new key"
    When I enter "SUSE Test PKG Key i586" as "description"
     And I enter "SUSE-PKG-i586" as "key"
     And I check "monitoring_entitled"
     And I check "provisioning_entitled"
     And I enter "20" as "usageLimit"
     And I select "SLES11-SP2-Updates i586 Channel" from "selectedChannel"
     And I click on "Create Activation Key"
     And I follow "Packages"
     And I enter "man" as "packages"
     And I click on "Update Key"
   Then I should see a "Activation key SUSE Test PKG Key i586 has been modified." text
    And I should see a "Details" link
    And I should see a "Child Channels" link
    And I should see a "Packages" link
    And I should see a "Configuration" link
    And I should see a "Groups" link
    And I should see a "Activated Systems" link

