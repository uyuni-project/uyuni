# Copyright (c) 2010-2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create activation keys
  In order to register systems to the spacewalk server
  As the testing user
  I want to use activation keys

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Clone the child custom channel including test repositories
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select the custom architecture channel for "sle_minion" as the origin channel
    And I choose "current"
    And I click on "Clone Channel"
    And I enter "SLE-Custom-Channel-x86_64" as "Channel Name"
    And I enter "test-channel-for-sle" as "Channel Label"
    And I select the parent channel for the "sle_minion" from "Parent Channel"
    And I click on "Clone Channel"
    Then I should see a "SLE-Custom-Channel-x86_64" text

  Scenario: Create an activation key with a channel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE Test Key x86_64" as "description"
    And I enter "SUSE-KEY-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "selectedBaseChannel"
    And I click on the "disabled" toggler
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I check "SLE-Custom-Channel-x86_64"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SUSE Test Key x86_64 has been created" text
    And I should see a "Details" link
    And I should see a "Packages" link
    And I should see a "Configuration" link in the content area
    And I should see a "Groups" link
    And I should see a "Activated Systems" link

@rhlike_minion
  Scenario: Create an activation key for RedHat-like minion
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "RedHat like Test Key" as "description"
    And I enter "SUSE-KEY-RH-LIKE" as "key"
    And I select "Test-Channel-x86_64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key RedHat like Test Key has been created" text
    And I should see a "Details" link
    And I should see a "Packages" link
    And I should see a "Configuration" link in the content area
    And I should see a "Groups" link
    And I should see a "Activated Systems" link

@deblike_minion
  Scenario: Create an activation key for Debian-like minion
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "Debian-like Test Key" as "description"
    And I enter "DEBLIKE-KEY" as "key"
    And I select "Test-Channel-Deb-AMD64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key Debian-like Test Key has been created" text
    And I should see a "Details" link
    And I should see a "Packages" link
    And I should see a "Configuration" link in the content area
    And I should see a "Groups" link
    And I should see a "Activated Systems" link

  Scenario: Create an activation key with a channel for salt-ssh
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE SSH Test Key x86_64" as "description"
    And I enter "SUSE-SSH-KEY-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "selectedBaseChannel"
    And I select "Push via SSH" from "contact-method"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SUSE SSH Test Key x86_64 has been created" text

  Scenario: Create an activation key with a channel for salt-ssh via tunnel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE SSH Tunnel Test Key x86_64" as "description"
    And I enter "SUSE-SSH-TUNNEL-KEY-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "selectedBaseChannel"
    And I select "Push via SSH tunnel" from "contact-method"
    And I click on "Create Activation Key"
