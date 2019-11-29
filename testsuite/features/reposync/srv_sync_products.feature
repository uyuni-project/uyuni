# Copyright 2017-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Synchronize products in the products page of the Setup Wizard

  Scenario: Let the products page appear
    Given I am authorized for the "Admin" section
    # Order matters here, refresh first
    When I refresh SCC
    And I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "Product Description" text
    Then I should see a "Arch" text
    And I should see a "Channels" text
    And I should not see a "WebYaST 1.3" text

  Scenario: Use the products filter
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "Product Description" text
    And I enter "RHEL Expanded Support 7" as the filtered product description
    Then I should see a "RHEL Expanded Support 7" text

  Scenario: View the channels list in the products page
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "Product Description" text
    And I enter "SUSE Linux Enterprise Server for SAP All-in-One 11 SP2" as the filtered product description
    And I click the channel list of product "SUSE Linux Enterprise Server for SAP All-in-One 11 SP2"
    Then I should see a "Product Channels" text
    And I should see a "Mandatory Channels" text
    And I should see a "Optional Channels" text

  Scenario: Add a product and one of its modules
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "Product Description" text
    And I enter "SUSE Linux Enterprise Server 12 SP2" as the filtered product description
    And I select "x86_64" in the dropdown list of the architecture filter
    And I select "SUSE Linux Enterprise Server 12 SP2 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 12 SP2 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 12 SP2 x86_64"
    Then I should see the "SUSE Linux Enterprise Server 12 SP2 x86_64" selected
    And I should see a "Legacy Module 12 x86_64" text
    When I select the addon "Legacy Module 12 x86_64"
    Then I should see the "Legacy Module 12 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 12 SP2 x86_64" product has been added
    Then the SLE12 products should be added

  Scenario: Add a product with recommended enabled
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "Product Description" text
    And I enter "SUSE Linux Enterprise Server 15" as the filtered product description
    And I select "x86_64" in the dropdown list of the architecture filter
    And I open the sub-list of the product "SUSE Linux Enterprise Server 15 x86_64"
    Then I should see a "Basesystem Module 15 x86_64" text
    And I should see that the "Basesystem Module 15 x86_64" product is "recommended"
    When I select "SUSE Linux Enterprise Server 15 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 x86_64" selected
    Then I should see the "Basesystem Module 15 x86_64" selected
    And I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 x86_64" product has been added
    Then the SLE15 products should be added
