# Copyright (c) 2017-2025 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Synchronize products in the products page of the Setup Wizard

@scc_credentials
  Scenario: Refresh SCC
    When I refresh SCC

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
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "RHEL and Liberty" as the filtered product description
    Then I should see a "RHEL and Liberty 8 Base" text
    When I select "x86_64" from "product-arch-filter"
    Then I should see a "RHEL and Liberty 8 Base" text

@scc_credentials
@susemanager
  Scenario: View the channels list in the products page
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server for SAP Applications 15 x86_64" as the filtered product description
    And I click the channel list of product "SUSE Linux Enterprise Server for SAP Applications 15 x86_64"
    Then I should see a "Product Channels" text
    And I should see a "Mandatory Channels" text
    And I should see a "Optional Channels" text
    When I close the modal dialog

@scc_credentials
@susemanager
  Scenario: Synchronize SLES 15 SP7 product with recommended sub-products, including MLM Client Tools
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP7" as the filtered product description
    And I wait until I see "SUSE Linux Enterprise Server 15 SP7 x86_64" text
    And I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP7 x86_64"
    And I open the sub-list of the product "Basesystem Module 15 SP7 x86_64"
    And I open the sub-list of the product "Desktop Applications Module 15 SP7 x86_64"
    And I open the sub-list of the product "SUSE Multi-Linux Manager Client Tools for SLE 15 x86_64" if present
    Then I should see that the "Basesystem Module 15 SP7 x86_64" product is "recommended"
    And I should see that the "Server Applications Module 15 SP7 x86_64" product is "recommended"
    And I should see that the "SUSE Multi-Linux Manager Client Tools for SLE 15 x86_64" product is "recommended"
    When I select "SUSE Linux Enterprise Server 15 SP7 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP7 x86_64" selected
    And I should see the "Basesystem Module 15 SP7 x86_64" selected
    And I should see the "Server Applications Module 15 SP7 x86_64" selected
    And I should see the "SUSE Multi-Linux Manager Client Tools for SLE 15 x86_64" selected
    When I select "Desktop Applications Module 15 SP7 x86_64" as a product
    And I select "Development Tools Module 15 SP7 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP7 x86_64" selected
    And I should see the "Development Tools Module 15 SP7 x86_64" selected
    When I select "Python 3 Module 15 SP7 x86_64" as a product
    Then I should see the "Python 3 Module 15 SP7 x86_64" selected
    When I select "Containers Module 15 SP7 x86_64" as a product
    Then I should see the "Containers Module 15 SP7 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 SP7 x86_64" product has been added
    Then the SLE15 SP7 product should be added
    When I wait until all synchronized channels for "sles15-sp7" have finished

@uyuni
  Scenario: Partially add openSUSE Tumbleweed product, only including the required packages to generate the bootstrap repository
    When I use spacewalk-common-channel to add channel "opensuse_tumbleweed" with arch "x86_64"
    And I kill running spacewalk-repo-sync for "opensuse_tumbleweed-x86_64" channel
    And I use spacewalk-repo-sync to sync channel "opensuse_tumbleweed-x86_64" including only client tools dependencies
    And I use spacewalk-common-channel to add all "tumbleweed-client-tools-x86_64" channels with arch "x86_64"
    When I wait until all synchronized channels for "tumbleweed" have finished

@containerized_server
@proxy
@uyuni
  Scenario: Add Uyuni Proxy product for Tumbleweed, including Uyuni Client Tools
    When I use spacewalk-common-channel to add all "uyuni-proxy" channels with arch "x86_64"
    And I wait until all synchronized channels for "uyuni-proxy" have finished

@scc_credentials
@uyuni
@build_host
  Scenario: Synchronize SLES 15 SP7 product with recommended sub-products for Retail feature
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP7 x86_64" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP7 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP7 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP7 x86_64"
    And I open the sub-list of the product "Basesystem Module 15 SP7 x86_64"
    And I select "Desktop Applications Module 15 SP7 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP7 x86_64" selected
    When I open the sub-list of the product "Desktop Applications Module 15 SP7 x86_64"
    And I select "Development Tools Module 15 SP7 x86_64" as a product
    Then I should see the "Development Tools Module 15 SP7 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    Then the SLE15 SP7 product should be added
    When I use spacewalk-common-channel to add channel "sles15-sp7-devel-uyuni-client" with arch "x86_64"
    And I wait until I see "SUSE Linux Enterprise Server 15 SP7 x86_64" product has been added
    And I wait until all synchronized channels for "sles15-sp7" have finished
      # TODO: Refactor the scenarios in order to not require a full synchronization of SLES 15 SP7 product in Uyuni
    # When I kill running spacewalk-repo-sync for "sles15-sp7"

@proxy
@susemanager
@transactional_server
  Scenario: Add SL Micro 6.1 as base OS for proxy
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Micro 6.1" as the filtered product description
    And I select "SUSE Linux Micro 6.1 x86_64" as a product
    Then I should see the "SUSE Linux Micro 6.1 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Micro 6.1 x86_64"
    And I open the sub-list of the product "SUSE Multi-Linux Manager Client Tools for SL Micro 6 x86_64" if present
    And I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Micro 6.1 x86_64" product has been added
    And I wait until all synchronized channels for "sl-micro-6.1" have finished

@proxy
@susemanager
@skip_if_transactional_server
  Scenario: Add SLES 15 SP7 as base OS for proxy
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP7" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP7 (BETA)" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP7 (BETA)" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Server 15 SP7 (BETA)" product has been added
    And I wait until all synchronized channels for "sles15-sp7" have finished

@proxy
@susemanager
@transactional_server
  Scenario: Add SUSE MLM Proxy Extension 5.1 with SL Micro 6.1 as base OS
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Micro 6.1" as the filtered product description
    When I open the sub-list of the product "SUSE Linux Micro 6.1 x86_64"
    And I select "SUSE Linux Micro 6.1 x86_64" as a product
    And I select "SUSE Multi-Linux Manager Proxy Extension 5.1 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Proxy Extension 5.1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Multi-Linux Manager Proxy Extension 5.1 x86_64" product has been added
    And I wait until all synchronized channels for "suse-multi-linux-manager-proxy-51" have finished

@proxy
@susemanager
@skip_if_transactional_server
  Scenario: Add SUSE MLM Proxy Extension 5.1 with SLES 15 SP7 as base OS
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP7" as the filtered product description
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP7 x86_64"
    And I select "SUSE Multi-Linux Manager Proxy Extension 5.1 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Proxy Extension 5.1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Multi-Linux Manager Proxy Extension 5.1 x86_64" product has been added
    And I wait until all synchronized channels for "suse-multi-linux-manager-proxy-51-sp7" have finished

@proxy
@susemanager
@transactional_server
  Scenario: Add SUSE MLM Retail Branch Server Extension 5.1 with SL Micro 6.1 as base OS
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Micro 6.1" as the filtered product description
    When I open the sub-list of the product "SUSE Linux Micro 6.1 x86_64"
    And I select "SUSE Linux Micro 6.1 x86_64" as a product
    And I select "SUSE Multi-Linux Manager Retail Branch Server Extension 5.1 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Retail Branch Server Extension 5.1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Multi-Linux Manager Retail Branch Server Extension 5.1 x86_64" product has been added
    And I wait until all synchronized channels for "suse-multi-linux-manager-retail-branch-server-51" have finished

@proxy
@susemanager
@skip_if_transactional_server
  Scenario: Add SUSE MLM Retail Branch Server Extension 5.1 with SLES 15 SP7 as base OS
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP7" as the filtered product description
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP7 x86_64"
    And I select "SUSE Multi-Linux Manager Retail Branch Server Extension 5.1 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Retail Branch Server Extension 5.1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Multi-Linux Manager Retail Branch Server Extension 5.1 x86_64" product has been added
    And I wait until all synchronized channels for "suma-retail-branch-server-extension-51-sp7" have finished

@scc_credentials
@susemanager
  Scenario: Installer update channels got enabled when products were added
    When I execute mgr-sync "list channels" with user "admin" and password "admin"
    And I should get "    [I] SLE15-SP7-Installer-Updates for x86_64 SUSE Linux Enterprise Server 15 SP7 x86_64 [sle15-sp7-installer-updates-x86_64]"

@scc_credentials
  Scenario: Verify all channels are solved
    When I wait until all synchronized channels have solved their dependencies
    Then all channels have been synced without errors

@scc_credentials
@skip_if_github_validation
  Scenario: Detect product loading issues from the UI
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "Setup Wizard" text
    And I wait until I do not see "Loading" text
    Then I should not see a "Operation not successful" text
    And I select "250" from "pageSize"
    And I should only see success signs in the product list

@scc_credentials
@skip_if_github_validation
  Scenario: Report the synchronization duration for SLES 15 SP7
    When I report the synchronization duration for "sles15-sp7"
