# Copyright 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: I want to verify the Setup Wizard

  Scenario: I want to test the credentials setup
    Given I am on the Admin page
    And I follow "Organization Credentials" in the content area
    And I want to add a new credential
    And I enter "asdf" as "edit-user"
    And I enter "asdf" as "edit-password"
    And I click on "Save"
    And I should see a "asdf" text
    And I make the credentials primary
    And I view the primary subscription list for asdf
    And I should see a "No subscriptions available" text
    And I click on "Close"
    And I delete the primary credentials
    And I view the primary subscription list
    And I click on "Close"
    And I should not see a "asdf" text

  Scenario: I want to test the products page
    Given I am on the Admin page
    And I follow "Admin" in the tab bar
    And I refresh scc
    And I follow "SUSE Products" in the content area
    And I wait until I see "RHEL Expanded Support 5" text
    And I should see a "Available Products Below" text
    And I should see a "Architecture" text
    And I should see a "Channels" text
    And I should see a "Status" text
    And I should not see a "WebYaST 1.3" text
    When I select "SUSE Linux Enterprise Server 12 SP1" as a product for the "x86_64" architecture
    And I select the addon "Legacy Module 12" for the product "SUSE Linux Enterprise Server 12 SP1" with arch "x86_64"
    And I click the Add Product button
    And I wait for "20" seconds
    And I verify the products were added

   @lolol
  Scenario: I want to test the channels list in the products page
    Given I am on the Admin page
    And I follow "Admin" in the tab bar
    And I follow "SUSE Products" in the content area
    # HACK: this should not be needed at all, but Capybara 2.1.0/WebDriver loses browser
    # connection if next requests arrive concurrently with the AJAX request. Might be removed
    # in future versions
#    And I wait for "30" seconds
    When I click the channel list of product "SUSE Linux Enterprise Server for SAP All-in-One 11 SP2" for the "x86_64" architecture
    Then I should see a "Product Channels" text
    And I should see a "Mandatory Channels" text
    And I should see a "Optional Channels" text
    And I should see a "Close" text
