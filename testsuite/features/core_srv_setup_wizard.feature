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
    And I wait until I see "Product Description" text
    And I should see a "Arch" text
    And I should see a "Channels" text
    And I should not see a "WebYaST 1.3" text
    And I enter "RHEL Expanded Support 5" in the css "input[name='product-description-filter']"
    And I should see a "RHEL Expanded Support 5" text
    Then I enter "" in the css "input[name='product-description-filter']"
    And I enter "SUSE Linux Enterprise Server 12 SP2" in the css "input[name='product-description-filter']"
    And I select "x86_64" in the dropdown list of the architecture filter
    When I select "SUSE Linux Enterprise Server 12 SP2" as a product
    And I open the sub-list of the product "SUSE Linux Enterprise Server 12 SP2"
    Then I should see a "Legacy Module 12" text
    When I select the addon "Legacy Module 12"
    And I click the Add Product button
    And I wait for "60" seconds
    Then the products should be added

  Scenario: Select product with recommended enabled
    And I follow "SUSE Products" in the content area
    And I wait until I see "Product Description" text
    And I enter "SUSE Linux Enterprise Server 15" in the css "input[name='product-description-filter']"
    And I select "x86_64" in the dropdown list of the architecture filter
    And I open the sub-list of the product "SUSE Linux Enterprise Server 15"
    Then I should see a "Basesystem Module 15" text
    And I should see that the "Basesystem Module 15" product is "recommended"
    When I select "SUSE Linux Enterprise Server 15" as a product
    Then I should see the "Basesystem Module 15" selected
    And I click the Add Product button
    And I wait for "60" seconds
    Then the products should be added

  Scenario: View the channels list in the products page
    When I follow "SUSE Products" in the content area
    And I wait until I see "Product Description" text
    And I enter "SUSE Linux Enterprise Server for SAP All-in-One 11 SP2" in the css "input[name='product-description-filter']"
    And I select "x86_64" in the dropdown list of the architecture filter
    And I click the channel list of product "SUSE Linux Enterprise Server for SAP All-in-One 11 SP2"
    Then I should see a "Product Channels" text
    And I should see a "Mandatory Channels" text
    And I should see a "Optional Channels" text
