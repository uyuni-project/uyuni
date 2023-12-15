# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

@paygo_server
Feature: Channel subscription via SSM

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  @sle15sp5_paygo_minion
  @sleforsap15sp5_paygo_minion
  Scenario: Clone HA15 child channel to sle product on paygo server
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "SLE-Product-HA15-SP5-Pool" from "original_id" dropdown
    And I click on "Clone Channel"
    And I select "SLE-Product-SLES15-SP5-Pool" from "parent" dropdown
    And I click on "Clone Channel"
    And I wait until I see "Cloning of channels under different product channels on PAYG instances is forbidden" text

  @sle15sp5_paygo_minion
  @sleforsap15sp5_paygo_minion
  Scenario: Clone SAP child channel to sle product on paygo server
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "SLE-Module-SAP-Applications15-SP5-Pool" from "original_id" dropdown
    And I click on "Clone Channel"
    And I select "SLE-Product-SLES15-SP5-Pool" from "parent" dropdown
    And I click on "Clone Channel"
    And I wait until I see "Cloning of channels under different product channels on PAYG instances is forbidden" text

  @sle15sp5_paygo_minion
  @proxy
  Scenario: Clone Proxy child channel to sle product on paygo server
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "SLE-Module-SUSE-Manager-Proxy-4.3-Pool" from "original_id" dropdown
    And I click on "Clone Channel"
    And I select "SLE-Product-SLES15-SP5-Pool" from "parent" dropdown
    And I click on "Clone Channel"
    And I wait until I see "Cloning of channels under different product channels on PAYG instances is forbidden" text


  @sle15sp5_paygo_minion
  @sle15sp4_minion
  Scenario: Clone Public cloud child channel from sle15sp4 to sle15sp5 product on payg server
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "SLE-Module-Public-Cloud15-SP4-Pool" from "original_id" dropdown
    And I click on "Clone Channel"
    And I select "SLE-Product-SLES15-SP5-Pool" from "parent" dropdown
    And I click on "Clone Channel"
    And I wait until I see "Channel Clone of SLE-Module-Public-Cloud15-SP4-Pool for x86_64 cloned from channel SLE-Module-Public-Cloud15-SP4-Pool for x86_64." text
