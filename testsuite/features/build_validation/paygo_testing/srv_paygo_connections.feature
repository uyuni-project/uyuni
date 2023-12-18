# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

@susemanager
@cloud
Feature: Make sure using paygo instances give access to the related products
  In order to use paygo
  As admin user
  I want to list available products depending on the paygo connection

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  @paygo_server
  Scenario: Check server pay-as-you-go connection
    When I follow the left menu "Admin > Setup Wizard > PAYG Connections"
    Then I wait until I see "localhost" text, refreshing the page
    And I should see a "Credentials successfully updated" text
    And I should see a "localhost" link
    And I should see a "SUSE Manager PAYG" text

  @paygo_server
  Scenario: Check default products are available
    When I execute mgr-sync refresh with authentification
    Then I should see the "server" paygo products

  @sle15sp5_paygo_minion
  Scenario: Add sle15sp5 paygo connection
    When I follow the left menu "Admin > Setup Wizard > PAYG Connections"
    And I click on "Add PAYG Connection"
    And I enter "sle15 paygo instance" as "description"
    And I enter the hostname of "sle15sp5_paygo_minion" as "host"
    And I enter "root" as "username"
    And I enter "linux" as "password"
    And I click on "Create"
    Then I should see a "created successfully" text
    And I should see "sle15sp5_paygo_minion" hostname in element "Instance-panel-wrapper"
    And I should see a "root" text in element "Instance-panel-wrapper"
    And I should see a "Delete" button
    When I wait until I see "Credentials successfully updated" text, refreshing the page

  @sle15sp5_paygo_minion
  Scenario: Check sle15sp5 products are available
    When I execute mgr-sync refresh with authentification
    Then I should see the "sle15sp5_paygo_minion" paygo products

  @sle12sp5_paygo_minion
  Scenario: Add sle12sp5 paygo connection
    When I follow the left menu "Admin > Setup Wizard > PAYG Connections"
    And I click on "Add PAYG Connection"
    And I enter "sle12 paygo instance" as "description"
    And I enter the hostname of "sle12sp5_paygo_minion" as "host"
    And I enter "root" as "username"
    And I enter "linux" as "password"
    And I click on "Create"
    Then I should see a "created successfully" text
    And I should see "sle12sp5_paygo_minion" hostname in element "Instance-panel-wrapper"
    And I should see a "root" text in element "Instance-panel-wrapper"
    And I should see a "Delete" button
    When I wait until I see "Credentials successfully updated" text, refreshing the page

  @sle12sp5_paygo_minion
  Scenario: Check sle12sp5 products are available
    When I execute mgr-sync refresh with authentification
    Then I should see the "sle12sp5_paygo_minion" paygo products

  @sleforsap15sp5_paygo_minion
  Scenario: Add sleforsap15sp5 paygo connection
    When I follow the left menu "Admin > Setup Wizard > PAYG Connections"
    And I click on "Add PAYG Connection"
    And I enter "sleforsap15 paygo instance" as "description"
    And I enter the hostname of "sleforsap15sp5_paygo_minion" as "host"
    And I enter "root" as "username"
    And I enter "linux" as "password"
    And I click on "Create"
    Then I should see a "created successfully" text
    And I should see "sleforsap15sp5_paygo_minion" hostname in element "Instance-panel-wrapper"
    And I should see a "root" text in element "Instance-panel-wrapper"
    And I should see a "Delete" button
    When I wait until I see "Credentials successfully updated" text, refreshing the page

  @sleforsap15sp5_paygo_minion
  Scenario: Check sleforsap15sp5 products are available
    When I execute mgr-sync refresh with authentification
    Then I should see the "sleforsap15sp5_paygo_minion" paygo products
