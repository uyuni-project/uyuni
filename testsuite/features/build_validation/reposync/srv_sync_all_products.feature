# Copyright 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Synchronize products in the products page of the Setup Wizard

  Scenario: Refresh SCC
    When I refresh SCC

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add Ubuntu 16.04
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "Ubuntu 16.04" as the filtered product description
    And I select "Ubuntu 16.04" as a product
    Then I should see the "Ubuntu 16.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 16.04" product has been added

  Scenario: Add Ubuntu 18.04
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "Ubuntu 18.04" as the filtered product description
    And I select "Ubuntu 18.04" as a product
    Then I should see the "Ubuntu 18.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 18.04" product has been added

  Scenario: Add Ubuntu 20.04
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "Ubuntu 20.04" as the filtered product description
    And I select "Ubuntu 20.04" as a product
    Then I should see the "Ubuntu 20.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 20.04" product has been added

  Scenario: SUSE Linux Enterprise Server 11 SP3
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server 11 SP3 i586" as the filtered product description
    And I select "SUSE Linux Enterprise Server 11 SP3 i586" as a product
    Then I should see the "SUSE Linux Enterprise Server 11 SP3 i586" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 11 SP3 i586" product has been added
    And I add "sles11-sp3-ltss-updates-i586" channel

  Scenario: SUSE Linux Enterprise Server 11 SP4
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server 11 SP4" as the filtered product description
    And I select "SUSE Linux Enterprise Server 11 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 11 SP4 x86_64" selected
    And I open the sub-list of the product "SUSE Linux Enterprise Server 11 SP4 x86_64"
    And I select "SUSE Linux Enterprise Software Development Kit 11 SP4" as a product
    Then I should see the "SUSE Linux Enterprise Software Development Kit 11 SP4" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 11 SP4 x86_64" product has been added
    And I add "sles11-sp4-ltss-updates-x86_64" channel

  Scenario: SUSE Linux Enterprise Server 12 SP4
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server 12 SP4" as the filtered product description
    And I select "SUSE Linux Enterprise Server 12 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 12 SP4 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 12 SP4 x86_64"
    And I select "SUSE Linux Enterprise Server LTSS 12 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server LTSS 12 SP4 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 12 SP4 x86_64" product has been added

  Scenario: SUSE Linux Enterprise Server 15
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server 15" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 x86_64"
    Then I should see the "SUSE Linux Enterprise Server 15 x86_64" selected
    And I select "SUSE Linux Enterprise Server LTSS 15 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server LTSS 15 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 x86_64" product has been added

  Scenario: SUSE Linux Enterprise Server 15 SP1
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server 15 SP1" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP1 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 SP1 x86_64" product has been added

  Scenario: SUSE Linux Enterprise Server 15 SP2
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server 15 SP2" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP2 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP2 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP2 x86_64"
    And I open the sub-list of the product "Basesystem Module 15 SP2 x86_64"
    And I select "Desktop Applications Module 15 SP2 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP2 x86_64" selected
    When I open the sub-list of the product "Desktop Applications Module 15 SP2 x86_64"
    And I select "Development Tools Module 15 SP2 x86_64" as a product
    Then I should see the "Development Tools Module 15 SP2 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 SP2 x86_64" product has been added

  Scenario: SUSE Linux Enterprise Server 15 SP3
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server 15 SP3 x86_64 (BETA)" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP3 x86_64 (BETA)" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP3 x86_64 (BETA)" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP3 x86_64 (BETA)"
    And I open the sub-list of the product "Basesystem Module 15 SP3 x86_64"
    And I select "Desktop Applications Module 15 SP3 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP3 x86_64" selected
    When I open the sub-list of the product "Desktop Applications Module 15 SP3 x86_64"
    And I select "Development Tools Module 15 SP3 x86_64" as a product
    Then I should see the "Development Tools Module 15 SP3 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Server 15 SP3 x86_64 (BETA)" product has been added

  Scenario: SUSE Linux Enterprise Server with Expanded Support 6
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server with Expanded Support 6" as the filtered product description
    And I select "SUSE Linux Enterprise Server with Expanded Support 6" as a product
    Then I should see the "SUSE Linux Enterprise Server with Expanded Support 6" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server with Expanded Support 6" product has been added

  Scenario: SUSE Linux Enterprise Server with Expanded Support 7
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Linux Enterprise Server with Expanded Support 7" as the filtered product description
    And I select "SUSE Linux Enterprise Server with Expanded Support 7" as a product
    Then I should see the "SUSE Linux Enterprise Server with Expanded Support 7" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server with Expanded Support 7" product has been added

  Scenario: SUSE Linux Enterprise Server with Expanded Support 8
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "RHEL or SLES ES or CentOS 8 Base" as the filtered product description
    And I select "RHEL or SLES ES or CentOS 8 Base" as a product
    Then I should see the "RHEL or SLES ES or CentOS 8 Base" selected
    When I open the sub-list of the product "RHEL or SLES ES or CentOS 8 Base"
    And I select "SUSE Linux Enterprise Server with Expanded Support 8" as a product
    Then I should see the "SUSE Linux Enterprise Server with Expanded Support 8" selected
    When I click the Add Product button
    And I wait until I see "RHEL or SLES ES or CentOS 8 Base" product has been added

  Scenario: SUSE Manager Proxy 4.2 x86_64
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Manager Proxy 4.2 x86_64" as the filtered product description
    And I select "SUSE Manager Proxy 4.2 x86_64" as a product
    Then I should see the "SUSE Manager Proxy 4.2 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Manager Proxy 4.2 x86_64" product has been added

  Scenario: SUSE Manager Retail Branch Server 4.2 x86_64
    When I follow the left menu "Admin > Setup Wizard > Products"
    When I enter "SUSE Manager Retail Branch Server 4.2 x86_64" as the filtered product description
    And I select "SUSE Manager Retail Branch Server 4.2 x86_64" as a product
    Then I should see the "SUSE Manager Retail Branch Server 4.2 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Manager Retail Branch Server 4.2 x86_64" product has been added
