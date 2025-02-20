# Copyright (c) 2025 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
Feature: Create custom channels with development repositories
  In Order to use product packages in development to the clients
  As an authorized user
  I want to create custom channels for each distribution

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

@sle_minion
  Scenario: Create a custom channel for SUSE minions
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Dev-SUSE-Channel" as "Channel Name"
    And I enter "dev-suse-channel" as "Channel Label"
    And I select the parent channel for the "sle_minion" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Dev-SUSE-Channel for development repositories" as "Channel Summary"
    And I enter "Channel containing development repositories" as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Dev-SUSE-Channel created." text

@sle_minion
  Scenario: Create custom repositories inside the SUSE custom channel
    When I prepare the development repositories of "sle_minion" as part of "dev-suse-channel" channel

@uyuni
@build_host
  Scenario: Create a custom channel for Build Host
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Dev-Build-Host-Channel" as "Channel Name"
    And I enter "dev-build-host-channel" as "Channel Label"
    And I select the parent channel for the "build_host" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Dev-Build-Host-Channel for development repositories" as "Channel Summary"
    And I enter "Channel containing development repositories" as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Dev-Build-Host-Channel created." text

@uyuni
@build_host
  Scenario: Create custom repositories inside the Build Host custom channel
    When I prepare the development repositories of "build_host" as part of "dev-build-host-channel" channel

@deblike_minion
  Scenario: Create a custom channel for Debian-like minions
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Dev-Debian-like-Channel" as "Channel Name"
    And I enter "dev-debian-like-channel" as "Channel Label"
    And I select "Fake-Base-Channel-Debian-like" from "Parent Channel"
    And I select "AMD64 Debian" from "Architecture:"
    And I enter "Dev-Debian-like-Channel for development repositories" as "Channel Summary"
    And I enter "Channel containing development repositories" as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Dev-Debian-like-Channel created." text

@deblike_minion
  Scenario: Create custom repositories inside the Debian-like custom channel
    When I prepare the development repositories of "deblike_minion" as part of "dev-debian-like-channel" channel

@rhlike_minion
  Scenario: Create a custom channel for RH-like minions
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Dev-RH-like-Channel" as "Channel Name"
    And I enter "dev-rh-like-channel" as "Channel Label"
    And I select "Fake-Base-Channel-RH-like" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Dev-RH-like-Channel for development repositories" as "Channel Summary"
    And I enter "Channel containing development repositories" as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Dev-RH-like-Channel created." text

@rhlike_minion
  Scenario: Create custom repositories inside the RH-like custom channel
    When I prepare the development repositories of "rhlike_minion" as part of "dev-rh-like-channel" channel
