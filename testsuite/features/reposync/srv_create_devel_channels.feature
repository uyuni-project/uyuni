# Copyright (c) 2025 SUSE LLC
# SPDX-License-Identifier: MIT

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
    And I enter "Devel-SUSE-Channel" as "Channel Name"
    And I enter "devel-suse-channel" as "Channel Label"
    And I select the parent channel for the "sle_minion" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Devel-SUSE-Channel for development repositories" as "Channel Summary"
    And I enter "Channel containing development repositories" as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Devel-SUSE-Channel created." text

@sle_minion
  Scenario: Create custom repositories inside the SUSE custom channel
    When I prepare the development repositories of "sle_minion" as part of "devel-suse-channel" channel

@uyuni
@build_host
  Scenario: Create a custom channel for Build Host
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Devel-Build-Host-Channel" as "Channel Name"
    And I enter "devel-build-host-channel" as "Channel Label"
    And I select the parent channel for the "build_host" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Devel-Build-Host-Channel for development repositories" as "Channel Summary"
    And I enter "Channel containing development repositories" as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Devel-Build-Host-Channel created." text

@uyuni
@build_host
  Scenario: Create custom repositories inside the Build Host custom channel
    When I prepare the development repositories of "build_host" as part of "devel-build-host-channel" channel

@deblike_minion
  Scenario: Create a custom channel for Debian-like minions
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Devel-Debian-like-Channel" as "Channel Name"
    And I enter "devel-debian-like-channel" as "Channel Label"
    And I select "Fake-Base-Channel-Debian-like" from "Parent Channel"
    And I select "AMD64 Debian" from "Architecture:"
    And I enter "Devel-Debian-like-Channel for development repositories" as "Channel Summary"
    And I enter "Channel containing development repositories" as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Devel-Debian-like-Channel created." text

@deblike_minion
  Scenario: Create custom repositories inside the Debian-like custom channel
    When I prepare the development repositories of "deblike_minion" as part of "devel-debian-like-channel" channel

@rhlike_minion
  Scenario: Create a custom channel for RH-like minions
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Devel-RH-like-Channel" as "Channel Name"
    And I enter "devel-rh-like-channel" as "Channel Label"
    And I select "Fake-Base-Channel-RH-like" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Devel-RH-like-Channel for development repositories" as "Channel Summary"
    And I enter "Channel containing development repositories" as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Devel-RH-like-Channel created." text

@rhlike_minion
  Scenario: Create custom repositories inside the RH-like custom channel
    When I prepare the development repositories of "rhlike_minion" as part of "devel-rh-like-channel" channel
