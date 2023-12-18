# Copyright (c) 2017-2023 SUSE LLC
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
    And I wait until I do not see "Loading" text
    And I enter "RHEL or SLES ES" as the filtered product description
    Then I should see a "RHEL or SLES ES or CentOS 8 Base" text
    When I select "x86_64" in the dropdown list of the architecture filter
    Then I should see a "RHEL or SLES ES or CentOS 8 Base" text

@scc_credentials
@susemanager
  Scenario: View the channels list in the products page
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server for SAP Applications 15 x86_64" as the filtered product description
    And I click the channel list of product "SUSE Linux Enterprise Server for SAP Applications 15 x86_64"
    Then I should see a "Product Channels" text
    And I should see a "Mandatory Channels" text
    And I should see a "Optional Channels" text
    When I close the modal dialog

@scc_credentials
@susemanager
  Scenario: Synchronize SLES 15 SP4 product with recommended sub-products, including SUMA Client Tools
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP4" as the filtered product description
    And I wait until I see "SUSE Linux Enterprise Server 15 SP4 x86_64" text
    And I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP4 x86_64"
    And I open the sub-list of the product "Basesystem Module 15 SP4 x86_64"
    And I open the sub-list of the product "Desktop Applications Module 15 SP4 x86_64"
    And I open the sub-list of the product "SUSE Manager Client Tools for SLE 15 x86_64" if present
    Then I should see that the "Basesystem Module 15 SP4 x86_64" product is "recommended"
    And I should see that the "Server Applications Module 15 SP4 x86_64" product is "recommended"
    And I should see that the "SUSE Manager Client Tools for SLE 15 x86_64" product is "recommended"
    When I select "SUSE Linux Enterprise Server 15 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP4 x86_64" selected
    And I should see the "Basesystem Module 15 SP4 x86_64" selected
    And I should see the "Server Applications Module 15 SP4 x86_64" selected
    And I should see the "SUSE Manager Client Tools for SLE 15 x86_64" selected
    When I select "Desktop Applications Module 15 SP4 x86_64" as a product
    And I select "Development Tools Module 15 SP4 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP4 x86_64" selected
    And I should see the "Development Tools Module 15 SP4 x86_64" selected
    When I select "Containers Module 15 SP4 x86_64" as a product
    Then I should see the "Containers Module 15 SP4 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 SP4 x86_64" product has been added
    Then the SLE15 SP4 product should be added
    When I wait until all synchronized channels for "sles15-sp4" have finished

@scc_credentials
@uyuni
  Scenario: Synchronize SLES 15 SP4 product with recommended sub-products for Retail feature
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP4" as the filtered product description
    And I wait until I see "SUSE Linux Enterprise Server 15 SP4 x86_64" text
    And I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP4 x86_64"
    And I open the sub-list of the product "Basesystem Module 15 SP4 x86_64"
    And I open the sub-list of the product "Desktop Applications Module 15 SP4 x86_64"
    Then I should see that the "Basesystem Module 15 SP4 x86_64" product is "recommended"
    And I should see that the "Server Applications Module 15 SP4 x86_64" product is "recommended"
    When I select "SUSE Linux Enterprise Server 15 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP4 x86_64" selected
    And I should see the "Basesystem Module 15 SP4 x86_64" selected
    And I should see the "Server Applications Module 15 SP4 x86_64" selected
    When I select "Desktop Applications Module 15 SP4 x86_64" as a product
    And I select "Development Tools Module 15 SP4 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP4 x86_64" selected
    And I should see the "Development Tools Module 15 SP4 x86_64" selected
    When I select "Containers Module 15 SP4 x86_64" as a product
    Then I should see the "Containers Module 15 SP4 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 SP4 x86_64" product has been added
    Then the SLE15 SP4 product should be added
    When I use spacewalk-common-channel to add channel "sles15-sp4-uyuni-client" with arch "x86_64"
    And I wait until all synchronized channels for "sles15-sp4" have finished
    # TODO: Refactor the scenarios in order to don't require a full synchronization of SLES 15 SP4 product in Uyuni
    # When I kill running spacewalk-repo-sync for "sles15-sp4"

@uyuni
  Scenario: Add openSUSE Leap 15.5 product, including Uyuni Client Tools
    When I use spacewalk-common-channel to add channel "opensuse_leap15_5 opensuse_leap15_5-non-oss opensuse_leap15_5-non-oss-updates opensuse_leap15_5-updates opensuse_leap15_5-backports-updates opensuse_leap15_5-sle-updates uyuni-proxy-devel-leap opensuse_leap15_5-uyuni-client" with arch "x86_64"
    And I kill running spacewalk-repo-sync for "leap15.5-x86_64"

@proxy
@susemanager
  Scenario: Add SUSE Manager Proxy 4.3
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Manager Proxy 4.3" as the filtered product description
    And I select "SUSE Manager Proxy 4.3 x86_64" as a product
    Then I should see the "SUSE Manager Proxy 4.3 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Manager Proxy 4.3 x86_64" product has been added
    And I wait until all synchronized channels for "suma-proxy-43" have finished

@proxy
@susemanager
  Scenario: Add SUSE Manager Retail Branch Server
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Manager Retail Branch Server 4.3" as the filtered product description
    And I select "SUSE Manager Retail Branch Server 4.3 x86_64" as a product
    Then I should see the "SUSE Manager Retail Branch Server 4.3 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Manager Retail Branch Server 4.3 x86_64" product has been added
    And  I wait until all synchronized channels for "suma-retail-branch-server-43" have finished

@scc_credentials
@susemanager
  Scenario: Installer update channels got enabled when products were added
    When I execute mgr-sync "list channels" with user "admin" and password "admin"
    And I should get "    [I] SLE15-SP4-Installer-Updates for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle15-sp4-installer-updates-x86_64]"

@scc_credentials
  Scenario: Detect product loading issues from the UI
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    Then I should not see a "Operation not successful" text
    And I should not see a warning sign

@scc_credentials
  Scenario: Trigger a refresh of the products synched from SCC
    When I execute mgr-sync refresh
