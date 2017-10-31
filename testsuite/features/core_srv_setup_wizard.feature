# Copyright 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: The Setup Wizard

  Background:
    Given I am on the Admin page

  Scenario: Set up the credentials
    When I follow "Organization Credentials" in the content area
    And I want to add a new credential
    And I enter "asdf" as "edit-user"
    And I enter "asdf" as "edit-password"
    And I click on "Save"
    Then I should see a "asdf" text
    When I make the credentials primary
    And I view the primary subscription list for asdf
    Then I should see a "No subscriptions available" text
    When I click on "Close"
    And I delete the primary credentials
    And I view the primary subscription list
    And I click on "Close"
    Then I should not see a "asdf" text

  Scenario: Play with the products page
    When I refresh SCC
    And I follow "SUSE Products" in the content area
    And I wait until I see "RHEL Expanded Support 5" text
    Then I should see a "Available Products Below" text
    And I should see a "Architecture" text
    And I should see a "Channels" text
    And I should see a "Status" text
    And I should not see a "WebYaST 1.3" text
    When I select "SUSE Linux Enterprise Server 12 SP2" as a product for the "x86_64" architecture
    And I select the addon "Legacy Module 12" for the product "SUSE Linux Enterprise Server 12 SP2" with arch "x86_64"
    And I click the Add Product button
    And I wait for "20" seconds
    Then the products should be added

  Scenario: View the channels list in the products page
    When I follow "SUSE Products" in the content area
    And I wait until I see "RHEL Expanded Support 5" text
    And I click the channel list of product "SUSE Linux Enterprise Server for SAP All-in-One 11 SP2" for the "x86_64" architecture
    Then I should see a "Product Channels" text
    And I should see a "Mandatory Channels" text
    And I should see a "Optional Channels" text
    And I should see a "Close" text
