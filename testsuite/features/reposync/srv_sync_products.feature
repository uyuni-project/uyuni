# Copyright 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Synchronize products in the products page of the Setup Wizard

@scc_credentials
  Scenario: Let the products page appear
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "Product Description" text
    Then I should see a "Arch" text
    And I should see a "Channels" text
    And I should not see a "WebYaST 1.3" text

@scc_credentials
  Scenario: Use the products and architecture filters
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I enter "RHEL7" as the filtered product description
    Then I should see a "RHEL7 Base" text
    When I select "x86_64" in the dropdown list of the architecture filter
    Then I should see a "RHEL7 Base x86_64" text

@scc_credentials
  Scenario: View the channels list in the products page
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server for SAP Applications 15 x86_64" as the filtered product description
    And I click the channel list of product "SUSE Linux Enterprise Server for SAP Applications 15 x86_64"
    Then I should see a "Product Channels" text
    And I should see a "Mandatory Channels" text
    And I should see a "Optional Channels" text
    When I close the modal dialog

@scc_credentials
  Scenario: Add a product and one of its modules
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server 12 SP5" as the filtered product description
    Then I wait until I see "SUSE Linux Enterprise Server 12 SP5 x86_64" text
    And I select "SUSE Linux Enterprise Server 12 SP5 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 12 SP5 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 12 SP5 x86_64"
    Then I should see the "SUSE Linux Enterprise Server 12 SP5 x86_64" selected
    And I should see a "Legacy Module 12 x86_64" text
    When I select the addon "Legacy Module 12 x86_64"
    Then I should see the "Legacy Module 12 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 12 SP5 x86_64" product has been added
    Then the SLE12 SP5 product should be added

@scc_credentials
  Scenario: Add the initial product for the service pack migration
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server 15 SP1" as the filtered product description
    Then I wait until I see "SUSE Linux Enterprise Server 15 SP1 x86_64" text
    And I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP1 x86_64"
    Then I should see a "Basesystem Module 15 SP1 x86_64" text
    When I select "SUSE Linux Enterprise Server 15 SP1 x86_64" as a product
    And I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 SP1 x86_64" product has been added
    Then the SLE15 SP1 products should be added

@scc_credentials
  Scenario: Add a product with recommended enabled
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server 15 SP2" as the filtered product description
    Then I wait until I see "SUSE Linux Enterprise Server 15 SP2 x86_64" text
    And I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP2 x86_64"
    Then I should see a "Basesystem Module 15 SP2 x86_64" text
    And I should see that the "Basesystem Module 15 SP2 x86_64" product is "recommended"
    When I select "SUSE Linux Enterprise Server 15 SP2 x86_64" as a product
    # Drop following 2 lines if you wish to re-enable testing with beta client tools for SLE15
    And I open the sub-list of the product "Basesystem Module 15 SP2 x86_64"
    And I deselect "SUSE Manager Client Tools Beta for SLE 15 x86_64 (BETA)" as a SUSE Manager product
    Then I should see the "SUSE Linux Enterprise Server 15 SP2 x86_64" selected
    Then I should see the "Basesystem Module 15 SP2 x86_64" selected
    And I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 SP2 x86_64" product has been added
    Then the SLE15 SP2 product should be added

@scc_credentials
  Scenario: Installer update channels got enabled when products were added
    When I execute mgr-sync "list channels" with user "admin" and password "admin"
    Then I should get "    [I] SLES12-SP5-Installer-Updates for x86_64 SUSE Linux Enterprise Server 12 SP5 x86_64 [sles12-sp5-installer-updates-x86_64]"
    And I should get "    [I] SLE15-SP1-Installer-Updates for x86_64 SUSE Linux Enterprise Server 15 SP1 x86_64 [sle15-sp1-installer-updates-x86_64]"
    And I should get "    [I] SLE15-SP2-Installer-Updates for x86_64 SUSE Linux Enterprise Server 15 SP2 x86_64 [sle15-sp2-installer-updates-x86_64]"
