# Copyright 2017-2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Synchronize products in the products page of the Setup Wizard

  Scenario: Refresh SCC
    When I refresh SCC

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

@sle12sp4_minion
  Scenario: Add SUSE Linux Enterprise Server 12 SP4
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 12 SP4" as the filtered product description
    And I select "SUSE Linux Enterprise Server 12 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 12 SP4 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 12 SP4 x86_64"
    And I select "SUSE Linux Enterprise Server LTSS 12 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server LTSS 12 SP4 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 12 SP4 x86_64" product has been added

@sle12sp5_minion
  Scenario: Add SUSE Linux Enterprise Server 12 SP5
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 12 SP5" as the filtered product description
    And I select "SUSE Linux Enterprise Server 12 SP5 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 12 SP5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 12 SP5 x86_64" product has been added

@sle15sp1_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP1
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP1" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP1 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP1 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP1 x86_64"
    And I select "SUSE Linux Enterprise Server LTSS 15 SP1 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server LTSS 15 SP1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 SP1 x86_64" product has been added

@sle15sp2_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP2
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP2" as the filtered product description
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

@sle15sp3_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP3
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP3" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP3 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP3 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP3 x86_64"
    And I open the sub-list of the product "Basesystem Module 15 SP3 x86_64"
    And I select "Desktop Applications Module 15 SP3 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP3 x86_64" selected
    When I open the sub-list of the product "Desktop Applications Module 15 SP3 x86_64"
    And I select "Development Tools Module 15 SP3 x86_64" as a product
    Then I should see the "Development Tools Module 15 SP3 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Server 15 SP3 x86_64" product has been added

@sle15sp4_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP4
    When I follow the left menu "Admin > Setup Wizard > Products"
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
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Server 15 SP4 x86_64" product has been added

@sle15sp5_minion
Scenario: Add SUSE Linux Enterprise Server 15 SP5
  When I follow the left menu "Admin > Setup Wizard > Products"
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
  When I click the Add Product button
  And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
  And I wait until I see "SUSE Linux Enterprise Server 15 SP5 x86_64" product has been added

@slemicro51_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.1
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.1" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.1 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.1 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Micro 5.1 x86_64"
    And I select "SUSE Manager Client Tools for SLE Micro 5 x86_64" as a product
    Then I should see the "SUSE Manager Client Tools for SLE Micro 5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.1 x86_64" product has been added

@slemicro52_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.2
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.2" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.2 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.2 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Micro 5.2 x86_64"
    And I select "SUSE Manager Client Tools for SLE Micro 5 x86_64" as a product
    Then I should see the "SUSE Manager Client Tools for SLE Micro 5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.2 x86_64" product has been added

@slemicro53_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.3
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.3" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.3 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.3 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Micro 5.3 x86_64"
    And I select "SUSE Manager Client Tools for SLE Micro 5 x86_64" as a product
    Then I should see the "SUSE Manager Client Tools for SLE Micro 5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.3 x86_64" product has been added

@slemicro54_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.4
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.4" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.4 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Micro 5.4 x86_64"
    And I select "SUSE Manager Client Tools for SLE Micro 5 x86_64" as a product
    Then I should see the "SUSE Manager Client Tools for SLE Micro 5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.4 x86_64" product has been added

@opensuse154arm_minion
  Scenario: Add openSUSE 15.4 for ARM
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "openSUSE Leap 15.4 aarch64" as the filtered product description
    And I select "openSUSE Leap 15.4 aarch64" as a product
    Then I should see the "openSUSE Leap 15.4 aarch64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "openSUSE Leap 15.4 aarch64" product has been added

@opensuse155arm_minion
  Scenario: Add openSUSE 15.5 for ARM
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "openSUSE Leap 15.5 aarch64" as the filtered product description
    And I select "openSUSE Leap 15.5 aarch64" as a product
    Then I should see the "openSUSE Leap 15.5 aarch64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "openSUSE Leap 15.5 aarch64" product has been added

@alma9_minion
  Scenario: Add Alma Linux 9
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "AlmaLinux 9" as the filtered product description
    And I select "AlmaLinux 9 x86_64" as a product
    Then I should see the "AlmaLinux 9 x86_64" selected
    When I click the Add Product button
    And I wait until I see "AlmaLinux 9 x86_64" product has been added

@centos7_minion
  Scenario: Add SUSE Linux Enterprise Server with Expanded Support 7
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server with Expanded Support 7" as the filtered product description
    And I select "SUSE Linux Enterprise Server with Expanded Support 7" as a product
    Then I should see the "SUSE Linux Enterprise Server with Expanded Support 7" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server with Expanded Support 7" product has been added

@liberty9_minion
  Scenario: Add Liberty Linux 9
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "RHEL and Liberty 9 Base" as the filtered product description
    And I select "RHEL and Liberty 9 Base" as a product
    Then I should see the "RHEL and Liberty 9 Base" selected
    When I click the Add Product button
    And I wait until I see "RHEL and Liberty 9 Base" product has been added
    When I open the sub-list of the product "RHEL and Liberty 9 Base"
    And I select "SUSE Liberty Linux 9" as a product
    Then I should see the "SUSE Liberty Linux 9" selected
    When I click the Add Product button
    And I wait until I see "SUSE Liberty Linux 9" product has been added

@oracle9_minion
  Scenario: Add Oracle Linux 9
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Oracle Linux 9" as the filtered product description
    And I select "Oracle Linux 9 x86_64" as a product
    Then I should see the "Oracle Linux 9 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Oracle Linux 9 x86_64" product has been added

@rhel9_minion
  Scenario: Add RHEL 9
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "RHEL and Liberty 9 Base" as the filtered product description
    And I select "RHEL and Liberty 9 Base" as a product
    Then I should see the "RHEL and Liberty 9 Base" selected
    When I click the Add Product button
    And I wait until I see "RHEL and Liberty 9 Base" product has been added

@rocky8_minion
  Scenario: Add SUSE Linux Enterprise Server with Expanded Support 8
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "RHEL or SLES ES or CentOS 8 Base" as the filtered product description
    And I select "RHEL or SLES ES or CentOS 8 Base" as a product
    Then I should see the "RHEL or SLES ES or CentOS 8 Base" selected
    When I open the sub-list of the product "RHEL or SLES ES or CentOS 8 Base"
    And I select "SUSE Linux Enterprise Server with Expanded Support 8" as a product
    Then I should see the "SUSE Linux Enterprise Server with Expanded Support 8" selected
    When I click the Add Product button
    And I wait until I see "RHEL or SLES ES or CentOS 8 Base" product has been added

@rocky9_minion
  Scenario: Add Rocky Linux 9
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Rocky Linux 9" as the filtered product description
    And I select "Rocky Linux 9 x86_64" as a product
    Then I should see the "Rocky Linux 9 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Rocky Linux 9 x86_64" product has been added

@ubuntu1804_minion
  Scenario: Add Ubuntu 18.04
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Ubuntu 18.04" as the filtered product description
    And I select "Ubuntu 18.04" as a product
    Then I should see the "Ubuntu 18.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 18.04" product has been added

@ubuntu2004_minion
  Scenario: Add Ubuntu 20.04
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Ubuntu 20.04" as the filtered product description
    And I select "Ubuntu 20.04" as a product
    Then I should see the "Ubuntu 20.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 20.04" product has been added

@ubuntu2204_minion
  Scenario: Add Ubuntu 22.04
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Ubuntu 22.04" as the filtered product description
    And I select "Ubuntu 22.04" as a product
    Then I should see the "Ubuntu 22.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 22.04" product has been added

@debian10_minion
  Scenario: Add Debian 10
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Debian 10" as the filtered product description
    And I select "Debian 10" as a product
    Then I should see the "Debian 10" selected
    When I click the Add Product button
    And I wait until I see "Debian 10" product has been added

@debian11_minion
  Scenario: Add Debian 11
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Debian 11" as the filtered product description
    And I select "Debian 11" as a product
    Then I should see the "Debian 11" selected
    When I click the Add Product button
     And I wait until I see "Debian 11" product has been added

@proxy
  Scenario: Add SUSE Manager Proxy 4.3
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Manager Proxy 4.3" as the filtered product description
    And I select "SUSE Manager Proxy 4.3 x86_64" as a product
    Then I should see the "SUSE Manager Proxy 4.3 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Manager Proxy 4.3 x86_64" product has been added

@proxy
  Scenario: Add SUSE Manager Retail Branch Server 4.3
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Manager Retail Branch Server 4.3" as the filtered product description
    And I select "SUSE Manager Retail Branch Server 4.3 x86_64" as a product
    Then I should see the "SUSE Manager Retail Branch Server 4.3 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Manager Retail Branch Server 4.3 x86_64" product has been added

  Scenario: Detect product loading issues from the UI in Build Validation
    When I follow the left menu "Admin > Setup Wizard > Products"
    Then I should not see a "Operation not successful" text
    And I should not see a warning sign
