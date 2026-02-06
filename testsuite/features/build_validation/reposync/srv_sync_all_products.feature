# Copyright 2017-2026 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Synchronize products in the products page of the Setup Wizard

  Scenario: Refresh SCC
    When I refresh SCC

@sle12sp5_minion
  Scenario: Add SUSE Linux Enterprise Server 12 SP5
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 12 SP5" as the filtered product description
    And I select "SUSE Linux Enterprise Server 12 SP5 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 12 SP5 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 12 SP5 x86_64"
    And I select "SUSE Linux Enterprise Server LTSS 12 SP5 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server LTSS 12 SP5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 12 SP5 x86_64" product has been added
    And I wait until all synchronized channels for "sles12-sp5" have finished

@uyuni
@sle12sp5_minion
  Scenario: Add SUSE Linux Enterprise Server 12 SP5 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles12-sp5-uyuni-client-devel" with arch "x86_64"
    And I wait until the channel "sles12-sp5-uyuni-client-devel-x86_64" has been synced

@sle15sp3_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP3
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP3" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP3 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP3 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP3 x86_64"
    And I select "SUSE Linux Enterprise Server LTSS 15 SP3 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server LTSS 15 SP3 x86_64" selected
    When I open the sub-list of the product "Basesystem Module 15 SP3 x86_64"
    And I select "Desktop Applications Module 15 SP3 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP3 x86_64" selected
    When I open the sub-list of the product "Desktop Applications Module 15 SP3 x86_64"
    And I select "Development Tools Module 15 SP3 x86_64" as a product
    Then I should see the "Development Tools Module 15 SP3 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Server 15 SP3 x86_64" product has been added
    And I wait until all synchronized channels for "sles15-sp3" have finished

@uyuni
@sle15sp3_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP3 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles15-sp3-devel-uyuni-client" with arch "x86_64"
    And I wait until the channel "sles15-sp3-devel-uyuni-client-x86_64" has been synced

@sle15sp4_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP4
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP4" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP4 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP4 x86_64"
    And I open the sub-list of the product "Basesystem Module 15 SP4 x86_64"
    And I select "Desktop Applications Module 15 SP4 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP4 x86_64" selected
    When I open the sub-list of the product "Desktop Applications Module 15 SP4 x86_64"
    And I select "Development Tools Module 15 SP4 x86_64" as a product
    Then I should see the "Development Tools Module 15 SP4 x86_64" selected
    When I select "Containers Module 15 SP4 x86_64" as a product
    Then I should see the "Containers Module 15 SP4 x86_64" selected
    When I select or deselect "SUSE Multi-Linux Manager Beta Client Tools for SLE 15 x86_64 (BETA)" beta client tools
    And I select "SUSE Linux Enterprise Server LTSS 15 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server LTSS 15 SP4 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Server 15 SP4 x86_64" product has been added
    And I wait until all synchronized channels for "sles15-sp4" have finished

@cloud
@sle15sp4_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP4 Public Cloud channels
    When I add "sle-module-public-cloud15-sp4-pool-x86_64" channel
    And I wait until the channel "sle-module-public-cloud15-sp4-pool-x86_64" has been synced
    And I add "sle-module-public-cloud15-sp4-updates-x86_64" channel
    And I wait until the channel "sle-module-public-cloud15-sp4-updates-x86_64" has been synced

@uyuni
@sle15sp4_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP4 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles15-sp4-devel-uyuni-client" with arch "x86_64"
    And I wait until the channel "sles15-sp4-devel-uyuni-client-x86_64" has been synced

@sle15sp5_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP5
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP5" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP5 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP5 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP5 x86_64"
    And I open the sub-list of the product "Basesystem Module 15 SP5 x86_64"
    And I select "Desktop Applications Module 15 SP5 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP5 x86_64" selected
    When I open the sub-list of the product "Desktop Applications Module 15 SP5 x86_64"
    And I select "Development Tools Module 15 SP5 x86_64" as a product
    Then I should see the "Development Tools Module 15 SP5 x86_64" selected
    When I select "SUSE Linux Enterprise Server LTSS 15 SP5 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server LTSS 15 SP5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Server 15 SP5 x86_64" product has been added
    And I wait until all synchronized channels for "sles15-sp5" have finished

@cloud
@sle15sp5_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP5 Public Cloud channels
    When I add "sle-module-public-cloud15-sp5-pool-x86_64" channel
    And I wait until the channel "sle-module-public-cloud15-sp5-pool-x86_64" has been synced
    And I add "sle-module-public-cloud15-sp5-updates-x86_64" channel
    And I wait until the channel "sle-module-public-cloud15-sp5-updates-x86_64" has been synced

@uyuni
@sle15sp5_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP5 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles15-sp5-devel-uyuni-client" with arch "x86_64"
    And I wait until the channel "sles15-sp5-devel-uyuni-client-x86_64" has been synced

@sle15sp6_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP6
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP6" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP6 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP6 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP6 x86_64"
    And I open the sub-list of the product "Basesystem Module 15 SP6 x86_64"
    And I select "Desktop Applications Module 15 SP6 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP6 x86_64" selected
    When I open the sub-list of the product "Desktop Applications Module 15 SP6 x86_64"
    And I select "Development Tools Module 15 SP6 x86_64" as a product
    Then I should see the "Development Tools Module 15 SP6 x86_64" selected
    When I select "SUSE Linux Enterprise Server LTSS 15 SP6 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server LTSS 15 SP6 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Server 15 SP6 x86_64" product has been added
    And I wait until all synchronized channels for "sles15-sp6" have finished

@cloud
@sle15sp6_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP6 Public Cloud channels
    When I add "sle-module-public-cloud15-sp6-pool-x86_64" channel
    And I wait until the channel "sle-module-public-cloud15-sp6-pool-x86_64" has been synced
    And I add "sle-module-public-cloud15-sp6-updates-x86_64" channel
    And I wait until the channel "sle-module-public-cloud15-sp6-updates-x86_64" has been synced

@uyuni
@sle15sp6_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP6 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles15-sp6-devel-uyuni-client" with arch "x86_64"
    And I wait until the channel "sles15-sp6-devel-uyuni-client-x86_64" has been synced

@cloud
@sle15sp7_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP7 Public Cloud channels
    When I add "sle-module-public-cloud15-sp7-pool-x86_64" channel
    And I wait until the channel "sle-module-public-cloud15-sp7-pool-x86_64" has been synced
    And I add "sle-module-public-cloud15-sp7-updates-x86_64" channel
    And I wait until the channel "sle-module-public-cloud15-sp7-updates-x86_64" has been synced

@uyuni
@sle15sp7_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP7 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles15-sp7-devel-uyuni-client" with arch "x86_64"
    And I wait until the channel "sles15-sp7-devel-uyuni-client-x86_64" has been synced

@susemanager
@slemicro51_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.1
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.1" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.1 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.1 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Micro 5.1 x86_64"
    And I select "SUSE Multi-Linux Manager Client Tools for SLE Micro 5 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Client Tools for SLE Micro 5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.1 x86_64" product has been added
    And I wait until all synchronized channels for "suse-microos-5.1" have finished

@uyuni
@slemicro51_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.1
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.1" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.1 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.1 x86_64" product has been added
    And I wait until all synchronized channels for "suse-microos-5.1" have finished

@uyuni
@slemicro51_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.1 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "suse-microos-5.1-devel-uyuni-client" with arch "x86_64"
    And I wait until the channel "suse-microos-5.1-devel-uyuni-client-x86_64" has been synced

@susemanager
@slemicro52_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.2
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.2" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.2 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.2 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Micro 5.2 x86_64"
    And I select "SUSE Multi-Linux Manager Client Tools for SLE Micro 5 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Client Tools for SLE Micro 5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.2 x86_64" product has been added
    And I wait until all synchronized channels for "suse-microos-5.2" have finished

@uyuni
@slemicro52_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.2
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.2" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.2 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.2 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.2 x86_64" product has been added
    And I wait until all synchronized channels for "suse-microos-5.2" have finished

@uyuni
@slemicro52_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.2 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "suse-microos-5.2-devel-uyuni-client" with arch "x86_64"
    And I wait until the channel "suse-microos-5.2-devel-uyuni-client-x86_64" has been synced

@susemanager
@slemicro53_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.3
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.3" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.3 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.3 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Micro 5.3 x86_64"
    And I select "SUSE Multi-Linux Manager Client Tools for SLE Micro 5 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Client Tools for SLE Micro 5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.3 x86_64" product has been added
    And I wait until all synchronized channels for "sle-micro-5.3" have finished

@uyuni
@slemicro53_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.3
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.3" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.3 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.3 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.3 x86_64" product has been added
    And I wait until all synchronized channels for "sle-micro-5.3" have finished

@uyuni
@slemicro53_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.3 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sle-micro-5.3-devel-uyuni-client" with arch "x86_64"
    And I wait until the channel "sle-micro-5.3-devel-uyuni-client-x86_64" has been synced

@susemanager
@slemicro54_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.4
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.4" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.4 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Micro 5.4 x86_64"
    And I select "SUSE Multi-Linux Manager Client Tools for SLE Micro 5 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Client Tools for SLE Micro 5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.4 x86_64" product has been added
    And I wait until all synchronized channels for "sle-micro-5.4" have finished

@uyuni
@slemicro54_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.4
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.4" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.4 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.4 x86_64" product has been added
    And I wait until all synchronized channels for "sle-micro-5.4" have finished

@uyuni
@slemicro54_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.4 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sle-micro-5.4-devel-uyuni-client" with arch "x86_64"
    And I wait until the channel "sle-micro-5.4-devel-uyuni-client-x86_64" has been synced

@susemanager
@slemicro55_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.5
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.5" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.5 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.5 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Micro 5.5 x86_64"
    And I select "SUSE Multi-Linux Manager Client Tools for SLE Micro 5 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Client Tools for SLE Micro 5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.5 x86_64" product has been added
    And I wait until all synchronized channels for "sle-micro-5.5" have finished

@uyuni
@slemicro55_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.5
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.5" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.5 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.5 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Micro 5.5 x86_64"
    And I open the sub-list of the product "SUSE Multi-Linux Manager Client Tools for SLE Micro 5 x86_64" if present
    And I select or deselect "SUSE Multi-Linux Manager Beta Client Tools Beta for SLE Micro 5 x86_64 (BETA)" beta client tools
    And I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.5 x86_64" product has been added
    And I wait until all synchronized channels for "sle-micro-5.5" have finished

@uyuni
@slemicro55_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.5 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sle-micro-5.5-devel-uyuni-client" with arch "x86_64"
    And I wait until the channel "sle-micro-5.5-devel-uyuni-client-x86_64" has been synced

@susemanager
@slmicro60_minion
  Scenario: Add SUSE Linux Micro 6.0
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Micro 6.0" as the filtered product description
    And I select "SUSE Linux Micro 6.0 x86_64" as a product
    Then I should see the "SUSE Linux Micro 6.0 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Micro 6.0 x86_64" product has been added
    And I wait until all synchronized channels for "sl-micro-6.0" have finished

@uyuni
@slmicro60_minion
  Scenario: Add SUSE Linux Micro 6.0
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Micro 6.0" as the filtered product description
    And I select "SUSE Linux Micro 6.0 x86_64" as a product
    Then I should see the "SUSE Linux Micro 6.0 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Micro 6.0 x86_64" product has been added
    And I wait until all synchronized channels for "sl-micro-6.0" have finished

@uyuni
@slmicro60_minion
  Scenario: Add SUSE Linux Micro 6.0 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sl-micro-6.0-devel-uyuni-client" with arch "x86_64"
    And I wait until the channel "sl-micro-6.0-devel-uyuni-client-x86_64" has been synced

@uyuni
@slmicro61_minion
  Scenario: Add SUSE Linux Micro 6.1
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Micro 6.1" as the filtered product description
    And I select "SUSE Linux Micro 6.1 x86_64" as a product
    Then I should see the "SUSE Linux Micro 6.1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Micro 6.1 x86_64" product has been added
    And I wait until all synchronized channels for "sl-micro-6.1" have finished

@uyuni
@slmicro61_minion
  Scenario: Add SUSE Linux Micro 6.1 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sl-micro-6.1-devel-uyuni-client" with arch "x86_64"
    And I wait until the channel "sl-micro-6.1-devel-uyuni-client-x86_64" has been synced

@susemanager
@opensuse156arm_minion
  Scenario: Add openSUSE 15.6 for ARM
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "openSUSE Leap 15.6 aarch64" as the filtered product description
    And I select "openSUSE Leap 15.6 aarch64" as a product
    Then I should see the "openSUSE Leap 15.6 aarch64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "openSUSE Leap 15.6 aarch64" product has been added
    And I wait until all synchronized channels for "leap15.6-aarch64" have finished

@uyuni
@opensuse156arm_minion
  Scenario: Add openSUSE 15.6 for ARM Uyuni Client tools
    When I use spacewalk-common-channel to add all "leap15.6" channels with arch "aarch64"
    And I wait until all synchronized channels for "leap15.6-aarch64" have finished

@sle15sp5s390_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP5 for s390x
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP5" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP5 s390x" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP5 s390x" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Server 15 SP5 s390x" product has been added
    And I wait until all synchronized channels for "sles15-sp5-s390x" have finished

@uyuni
@sle15sp5s390_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP5 for s390x Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles15-sp5-devel-uyuni-client" with arch "s390x"
    And I wait until the channel "sles15-sp5-devel-uyuni-client-s390x" has been synced

@susemanager
@alma8_minion
  Scenario: Add Alma Linux 8
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "AlmaLinux 8" as the filtered product description
    And I select "AlmaLinux 8 x86_64" as a product
    Then I should see the "AlmaLinux 8 x86_64" selected
    When I click the Add Product button
    And I wait until I see "AlmaLinux 8 x86_64" product has been added
    And I wait until all synchronized channels for "almalinux8" have finished

@uyuni
@alma8_minion
  Scenario: Add Alma Linux 8
    When I use spacewalk-common-channel to add all "almalinux8" channels with arch "x86_64"
    And I wait until all synchronized channels for "almalinux8" have finished

@susemanager
@alma9_minion
  Scenario: Add Alma Linux 9
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "AlmaLinux 9" as the filtered product description
    And I select "AlmaLinux 9 x86_64" as a product
    Then I should see the "AlmaLinux 9 x86_64" selected
    When I click the Add Product button
    And I wait until I see "AlmaLinux 9 x86_64" product has been added
    And I wait until all synchronized channels for "almalinux9" have finished

@uyuni
@alma9_minion
  Scenario: Add Alma Linux 9
    When I use spacewalk-common-channel to add all "almalinux9" channels with arch "x86_64"
    And I wait until all synchronized channels for "almalinux9" have finished

@susemanager
@amazon2023_minion
  Scenario: Add Amazon Linux 2023
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "Amazon Linux 2023" as the filtered product description
    And I select "Amazon Linux 2023 x86_64" as a product
    Then I should see the "Amazon Linux 2023 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Amazon Linux 2023 x86_64" product has been added
    And I wait until all synchronized channels for "amazonlinux2023" have finished

@uyuni
@amazon2023_minion
  Scenario: Add Amazon Linux 2023
    When I use spacewalk-common-channel to add all "amazonlinux2023" channels with arch "x86_64"
    And I wait until all synchronized channels for "amazonlinux2023" have finished

@susemanager
@centos7_minion
  Scenario: Add SUSE Liberty Linux 7 LTSS
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Liberty Linux LTSS 7" as the filtered product description
    And I select "SUSE Liberty Linux LTSS 7 x86_64" as a product
    Then I should see the "SUSE Liberty Linux LTSS 7 x86_64" selected
    When I open the sub-list of the product "SUSE Liberty Linux LTSS 7 x86_64"
    And I should see the "SUSE Multi-Linux Manager Beta Client Tools for SUSE Liberty Linux 7, RHEL and clones 7 x86_64 (BETA)" selected
    When I click the Add Product button
    And I wait until I see "SUSE Liberty Linux LTSS 7 x86_64" product has been added
    And I wait until all synchronized channels for "sll-7-ltss" have finished

@uyuni
@centos7_minion
  Scenario: Add CentOS 7
    When I use spacewalk-common-channel to add all "centos7" channels with arch "x86_64"
    And I wait until all synchronized channels for "sll-7-ltss" have finished

@susemanager
@liberty9_minion
  Scenario: Add Liberty Linux 9 Base product
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "RHEL and Liberty 9 Base" as the filtered product description
    And I select "RHEL and Liberty 9 Base" as a product
    Then I should see the "RHEL and Liberty 9 Base" selected
    When I click the Add Product button
    And I wait until I see "RHEL and Liberty 9 Base" product has been added
    And I wait until all synchronized channels for "el9" have finished

@susemanager
@liberty9_minion
  Scenario: Add Liberty Linux 9
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "RHEL and Liberty 9 Base" as the filtered product description
    And I select "RHEL and Liberty 9 Base" as a product
    Then I should see the "RHEL and Liberty 9 Base" selected
    When I open the sub-list of the product "RHEL and Liberty 9 Base"
    And I select "SUSE Liberty Linux 9 x86_64" as a product
    Then I should see the "SUSE Liberty Linux 9 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Liberty Linux 9 x86_64" product has been added
    And I wait until all synchronized channels for "sll-9" have finished

@susemanager
@oracle9_minion
  Scenario: Add Oracle Linux 9
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "Oracle Linux 9" as the filtered product description
    And I select "Oracle Linux 9 x86_64" as a product
    Then I should see the "Oracle Linux 9 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Oracle Linux 9 x86_64" product has been added
    And I wait until all synchronized channels for "oraclelinux9" have finished

@uyuni
@oracle9_minion
  Scenario: Add Oracle Linux 9
    When I use spacewalk-common-channel to add all "oraclelinux9" channels with arch "x86_64"
    And I wait until all synchronized channels for "oraclelinux9" have finished

@rhel9_minion
  Scenario: Add RHEL 9
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "RHEL and Liberty 9 Base" as the filtered product description
    And I select "RHEL and Liberty 9 Base" as a product
    Then I should see the "RHEL and Liberty 9 Base" selected
    When I click the Add Product button
    And I wait until I see "RHEL and Liberty 9 Base" product has been added
    And I wait until all synchronized channels for "el9" have finished

@susemanager
@rocky8_minion
  Scenario: Add Rocky Linux 8
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "Rocky Linux 8" as the filtered product description
    And I select "Rocky Linux 8 x86_64" as a product
    Then I should see the "Rocky Linux 8 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Rocky Linux 8 x86_64" product has been added
    And I wait until all synchronized channels for "rockylinux8" have finished

@uyuni
@rocky8_minion
  Scenario: Add Rocky Linux 8
    When I use spacewalk-common-channel to add all "rockylinux8" channels with arch "x86_64"
    And I wait until all synchronized channels for "rockylinux8" have finished

@susemanager
@rocky9_minion
  Scenario: Add Rocky Linux 9
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "Rocky Linux 9" as the filtered product description
    And I select "Rocky Linux 9 x86_64" as a product
    Then I should see the "Rocky Linux 9 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Rocky Linux 9 x86_64" product has been added
    And I wait until all synchronized channels for "rockylinux9" have finished

@uyuni
@rocky9_minion
  Scenario: Add Rocky Linux 9
    When I use spacewalk-common-channel to add all "rockylinux9" channels with arch "x86_64"
    And I wait until all synchronized channels for "rockylinux9" have finished

@susemanager
@ubuntu2204_minion
  Scenario: Add Ubuntu 22.04
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "Ubuntu 22.04" as the filtered product description
    And I select "Ubuntu 22.04" as a product
    Then I should see the "Ubuntu 22.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 22.04" product has been added
    And I wait until all synchronized channels for "ubuntu-2204" have finished

@uyuni
@ubuntu2204_minion
  Scenario: Add Ubuntu 22.04
    When I use spacewalk-common-channel to add all "ubuntu-2204" channels with arch "amd64-deb"
    And I wait until all synchronized channels for "ubuntu-2204" have finished

@susemanager
@ubuntu2404_minion
  Scenario: Add Ubuntu 24.04
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "Ubuntu 24.04" as the filtered product description
    And I select "Ubuntu 24.04" as a product
    Then I should see the "Ubuntu 24.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 24.04" product has been added
    And I wait until all synchronized channels for "ubuntu-2404" have finished

@uyuni
@ubuntu2404_minion
  Scenario: Add Ubuntu 24.04
    When I use spacewalk-common-channel to add all "ubuntu-2404" channels with arch "amd64-deb"
    And I wait until all synchronized channels for "ubuntu-2404" have finished

@susemanager
@debian12_minion
  Scenario: Add Debian 12
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "Debian 12" as the filtered product description
    And I select "Debian 12" as a product
    Then I should see the "Debian 12" selected
    When I click the Add Product button
    And I wait until I see "Debian 12" product has been added
    And I wait until all synchronized channels for "debian-12" have finished

@uyuni
@debian12_minion
  Scenario: Add Debian 12
    When I use spacewalk-common-channel to add all "debian-12" channels with arch "amd64-deb"
    And I wait until all synchronized channels for "debian-12" have finished

@uyuni
@proxy
  Scenario: Add Uyuni Proxy on Tumbleweed, including Uyuni Client Tools
    When I use spacewalk-common-channel to add all "tumbleweed" channels with arch "x86_64"
    And I wait until all synchronized channels for "uyuni-proxy" have finished

@susemanager
@run_if_proxy_transactional_or_slmicro61_minion
  Scenario: Add SUSE Linux Micro 6.1
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Micro 6.1" as the filtered product description
    And I select "SUSE Linux Micro 6.1 x86_64" as a product
    Then I should see the "SUSE Linux Micro 6.1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Micro 6.1 x86_64" product has been added
    And I wait until all synchronized channels for "sl-micro-6.1" have finished

@susemanager
@run_if_proxy_not_transactional_or_sles15sp7_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP7
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP7" as the filtered product description
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
    And I wait until I see "SUSE Linux Enterprise Server 15 SP7 x86_64" product has been added
    And I wait until all synchronized channels for "sles15-sp7" have finished

@susemanager
@proxy
@transactional_server
  Scenario: Add SUSE Manager Proxy Extension 5.1 on top of SUSE Linux Enterprise Micro 6.1
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Multi-Linux Manager Proxy Extension 5.1" as the filtered product description
    Then I should see the "SUSE Linux Micro 6.1 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Micro 6.1 x86_64"
    And I select "SUSE Multi-Linux Manager Proxy Extension 5.1 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Proxy Extension 5.1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Multi-Linux Manager Proxy Extension 5.1 x86_64" product has been added
    And I wait until all synchronized channels for "suse-multi-linux-manager-proxy-51" have finished

@uyuni
@proxy
  Scenario: Add Uyuni Proxy for Tumbleweed, including Uyuni Client Tools
    When I use spacewalk-common-channel to add all "uyuni-proxy" channels with arch "x86_64"
    And I wait until all synchronized channels for "uyuni-proxy" have finished

@susemanager
@proxy
@skip_if_transactional_server
  Scenario: Add SUSE Multi-Linux Manager Proxy Extension 5.1 on top of SUSE Linux Enterprise Server 15 SP7
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Multi-Linux Manager Proxy Extension for SLE 5.1 x86_64" as the filtered product description
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP7 x86_64"
    And I open the sub-list of the product "Basesystem Module 15 SP7 x86_64"
    And I select "Containers Module 15 SP7 x86_64" as a product
    Then I should see the "Containers Module 15 SP7 x86_64" selected
    When I open the sub-list of the product "Containers Module 15 SP7 x86_64"
    And I select "SUSE Multi-Linux Manager Proxy Extension for SLE 5.1 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Proxy Extension for SLE 5.1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until all synchronized channels for "suse-multi-linux-manager-proxy-51-sp7" have finished

@susemanager
@proxy
@transactional_server
  Scenario: Add SUSE Manager Retail Branch Server Extension 5.1 on top of SUSE Linux Enterprise Micro 6.1
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Multi-Linux Manager Retail Branch Server Extension 5.1" as the filtered product description
    Then I should see the "SUSE Linux Micro 6.1 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Micro 6.1 x86_64"
    And I select "SUSE Multi-Linux Manager Retail Branch Server Extension 5.1 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Retail Branch Server Extension 5.1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Multi-Linux Manager Proxy Extension 5.1 x86_64" product has been added
    And I wait until all synchronized channels for "suse-multi-linux-manager-retail-branch-server-51" have finished

@susemanager
@proxy
@skip_if_transactional_server
  Scenario: Add SUSE Multi-Linux Manager Retail Branch Server Extension 5.1 on top of SUSE Linux Enterprise Server 15 SP7
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "SUSE Multi-Linux Manager Proxy Extension for SLE 5.1 x86_64" as the filtered product description
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP7 x86_64"
    And I open the sub-list of the product "Basesystem Module 15 SP7 x86_64"
    And I open the sub-list of the product "Containers Module 15 SP7 x86_64"
    And I select "SUSE Multi-Linux Manager Retail Branch Server Extension for SLE 5.1 x86_64" as a product
    Then I should see the "SUSE Multi-Linux Manager Retail Branch Server Extension for SLE 5.1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until all synchronized channels for "suse-multi-linux-manager-retail-branch-server-51-sp7" have finished

# There are no channels for Retail under Uyuni

  Scenario: Verify all channels are solved
    When I wait until all synchronized channels have solved their dependencies
    Then all channels have been synced without errors

  Scenario: Detect product loading issues from the UI in Build Validation
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "Setup Wizard" text
    And I wait until I do not see "Loading" text
    Then I should not see a "Operation not successful" text
    And I should only see success signs in the product list
