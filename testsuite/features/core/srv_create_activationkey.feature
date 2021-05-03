# Copyright (c) 2010-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create activation keys
  In order to register systems to the spacewalk server
  As the testing user
  I want to use activation keys

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create an activation key with a channel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE Test Key x86_64" as "description"
    And I enter "SUSE-KEY-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-x86_64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SUSE Test Key x86_64 has been created" text
    And I should see a "Details" link
    And I should see a "Packages" link
    And I should see a "Configuration" link in the content area
    And I should see a "Groups" link
    And I should see a "Activated Systems" link

@ubuntu_minion
  Scenario: Create an activation key for Ubuntu
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "Ubuntu Test Key" as "description"
    And I enter "UBUNTU-KEY" as "key"
    And I select "Test-Channel-Deb-AMD64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key Ubuntu Test Key has been created" text
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
    And I select "Test-Channel-x86_64" from "selectedBaseChannel"
    And I select "Push via SSH" from "contact-method"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SUSE SSH Test Key x86_64 has been created" text

  Scenario: Create an activation key with a channel for salt-ssh via tunnel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE SSH Tunnel Test Key x86_64" as "description"
    And I enter "SUSE-SSH-TUNNEL-KEY-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-x86_64" from "selectedBaseChannel"
    And I select "Push via SSH tunnel" from "contact-method"
    And I click on "Create Activation Key"
