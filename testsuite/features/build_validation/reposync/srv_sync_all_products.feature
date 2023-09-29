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

@uyuni
@sle12sp4_minion
  Scenario: Add SUSE Linux Enterprise Server 12 SP4 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles12-sp4-uyuni-client-devel" with arch "x86_64"

@sle12sp5_minion
  Scenario: Add SUSE Linux Enterprise Server 12 SP5
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 12 SP5" as the filtered product description
    And I select "SUSE Linux Enterprise Server 12 SP5 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 12 SP5 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 12 SP5 x86_64" product has been added

@uyuni
@sle12sp5_minion
  Scenario: Add SUSE Linux Enterprise Server 12 SP5 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles12-sp5-uyuni-client-devel" with arch "x86_64"

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

@uyuni
@sle15sp1_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP1 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles15-sp1-devel-uyuni-client" with arch "x86_64"

@sle15sp2_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP2
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP2" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP2 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP2 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP2 x86_64"
    And I select "SUSE Linux Enterprise Server LTSS 15 SP2 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server LTSS 15 SP2 x86_64" selected
    And I open the sub-list of the product "Basesystem Module 15 SP2 x86_64"
    And I select "Desktop Applications Module 15 SP2 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP2 x86_64" selected
    When I open the sub-list of the product "Desktop Applications Module 15 SP2 x86_64"
    And I select "Development Tools Module 15 SP2 x86_64" as a product
    Then I should see the "Development Tools Module 15 SP2 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 SP2 x86_64" product has been added

@cloud
@sle15sp2_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP2 Public Cloud channels
    When I add "sle-module-public-cloud15-sp2-pool-x86_64" channel
    And I add "sle-module-public-cloud15-sp2-updates-x86_64" channel

@uyuni
@sle15sp2_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP2 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles15-sp2-devel-uyuni-client" with arch "x86_64"

@sle15sp3_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP3
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP3" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP3 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP3 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP3 x86_64"
    And I select "SUSE Linux Enterprise Server LTSS 15 SP3 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server LTSS 15 SP3 x86_64" selected
    And I open the sub-list of the product "Basesystem Module 15 SP3 x86_64"
    And I select "Desktop Applications Module 15 SP3 x86_64" as a product
    Then I should see the "Desktop Applications Module 15 SP3 x86_64" selected
    When I open the sub-list of the product "Desktop Applications Module 15 SP3 x86_64"
    And I select "Development Tools Module 15 SP3 x86_64" as a product
    Then I should see the "Development Tools Module 15 SP3 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Server 15 SP3 x86_64" product has been added

@cloud
@sle15sp3_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP3 Public Cloud channels
    When I add "sle-module-public-cloud15-sp3-pool-x86_64" channel
    And I add "sle-module-public-cloud15-sp3-updates-x86_64" channel

@uyuni
@sle15sp3_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP3 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles15-sp3-devel-uyuni-client" with arch "x86_64"

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

@cloud
@sle15sp4_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP4 Public Cloud channels
    When I add "sle-module-public-cloud15-sp4-pool-x86_64" channel
    And I add "sle-module-public-cloud15-sp4-updates-x86_64" channel

@uyuni
@sle15sp4_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP4 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles15-sp4-devel-uyuni-client" with arch "x86_64"

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

@cloud
@sle15sp5_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP5 Public Cloud channels
    When I add "sle-module-public-cloud15-sp5-pool-x86_64" channel
    And I add "sle-module-public-cloud15-sp5-updates-x86_64" channel

@uyuni
@sle15sp5_minion
  Scenario: Add SUSE Linux Enterprise Server 15 SP5 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sles15-sp5-devel-uyuni-client" with arch "x86_64"

@susemanager
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

@uyuni
@slemicro51_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.1
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.1" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.1 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.1 x86_64" product has been added

@uyuni
@slemicro51_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.1 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "suse-microos-5.1-pool-x86_64 suse-microos-5.1-devel-uyuni-client" with arch "x86_64"

@susemanager
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

@uyuyni
@slemicro52_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.2
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.2" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.2 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.2 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.2 x86_64" product has been added

@uyuni
@slemicro52_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.2 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "suse-microos-5.2-pool-x86_64 suse-microos-5.2-devel-uyuni-client" with arch "x86_64"

@susemanager
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

@uyuni
@slemicro53_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.3
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Micro 5.3" as the filtered product description
    And I select "SUSE Linux Enterprise Micro 5.3 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Micro 5.3 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
    And I wait until I see "SUSE Linux Enterprise Micro 5.3 x86_64" product has been added

@uyuni
@slemicro53_minion
  Scenario: Add SUSE Linux Enterprise Micro 5.3 Uyuni Client tools
    When I use spacewalk-common-channel to add channel "sle-micro-5.3-pool-x86_64 sle-micro-5.3-devel-uyuni-client" with arch "x86_64"

@susemanager
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

# disabled until a new Uyuni version will be released
# @uyuni
# @slemicro54_minion
#   Scenario: Add SUSE Linux Enterprise Micro 5.4
#     When I follow the left menu "Admin > Setup Wizard > Products"
#     And I wait until I do not see "Loading" text
#     And I enter "SUSE Linux Enterprise Micro 5.4" as the filtered product description
#     And I select "SUSE Linux Enterprise Micro 5.4 x86_64" as a product
#     Then I should see the "SUSE Linux Enterprise Micro 5.4 x86_64" selected
#     When I click the Add Product button
#     And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
#     And I wait until I see "SUSE Linux Enterprise Micro 5.4 x86_64" product has been added

# @uyuni
# @slemicro54_minion
#   Scenario: Add SUSE Linux Enterprise Micro 5.4 Uyuni Client tools
#     When I use spacewalk-common-channel to add channel "sle-micro-5.4-pool-x86_64 sle-micro-5.4-devel-uyuni-client" with arch "x86_64"

@susemanager
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

@uyuni
@opensuse154arm_minion
  Scenario: Add openSUSE 15.4 for ARM Uyuni Client tools
    When I use spacewalk-common-channel to add channel "opensuse_leap15_4 opensuse_leap15_4-backports-updates opensuse_leap15_4-non-oss opensuse_leap15_4-non-oss-updates opensuse_leap15_4-sle-updates opensuse_leap15_4-updates opensuse_leap15_4-uyuni-client-devel" with arch "aarch64"

@susemanager
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

@uyuni
@opensuse155arm_minion
  Scenario: Add openSUSE 15.5 for ARM Uyuni Client tools
    When I use spacewalk-common-channel to add channel "opensuse_leap15_5 opensuse_leap15_5-backports-updates opensuse_leap15_5-non-oss opensuse_leap15_5-non-oss-updates opensuse_leap15_5-sle-updates opensuse_leap15_5-updates opensuse_leap15_5-uyuni-client-devel" with arch "aarch64"

@susemanager
@alma9_minion
  Scenario: Add Alma Linux 9
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "AlmaLinux 9" as the filtered product description
    And I select "AlmaLinux 9 x86_64" as a product
    Then I should see the "AlmaLinux 9 x86_64" selected
    When I click the Add Product button
    And I wait until I see "AlmaLinux 9 x86_64" product has been added

@uyuni
@alma9_minion
  Scenario: Add Alma Linux 9
    When I use spacewalk-common-channel to add channel "almalinux9 almalinux9-appstream almalinux9-extras almalinux9-uyuni-client-devel" with arch "x86_64"

@susemanager
@centos7_minion
  Scenario: Add SUSE Linux Enterprise Server with Expanded Support 7
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server with Expanded Support 7" as the filtered product description
    And I select "SUSE Linux Enterprise Server with Expanded Support 7" as a product
    Then I should see the "SUSE Linux Enterprise Server with Expanded Support 7" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server with Expanded Support 7" product has been added

@uyuni
@centos7_minion
  Scenario: Add CentOS 7
    When I use spacewalk-common-channel to add channel "centos7 centos7-extras centos7-uyuni-client-devel" with arch "x86_64"

@susemanager
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

@susemanager
@oracle9_minion
  Scenario: Add Oracle Linux 9
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Oracle Linux 9" as the filtered product description
    And I select "Oracle Linux 9 x86_64" as a product
    Then I should see the "Oracle Linux 9 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Oracle Linux 9 x86_64" product has been added

@uyuni
@oracle9_minion
  Scenario: Add Oracle Linux 9
    When I use spacewalk-common-channel to add channel "oraclelinux9 oraclelinux9-appstream oraclelinux9-uyuni-client-devel" with arch "x86_64"

@rhel9_minion
  Scenario: Add RHEL 9
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "RHEL and Liberty 9 Base" as the filtered product description
    And I select "RHEL and Liberty 9 Base" as a product
    Then I should see the "RHEL and Liberty 9 Base" selected
    When I click the Add Product button
    And I wait until I see "RHEL and Liberty 9 Base" product has been added

@susemanager
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

@uyuni
@rocky8_minion
  Scenario: Add Rocky Linux 8
    When I use spacewalk-common-channel to add channel "rockylinux8 rockylinux8-appstream rockylinux8-extras rockylinux8-uyuni-client-devel" with arch "x86_64"

@susemanager
@rocky9_minion
  Scenario: Add Rocky Linux 9
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Rocky Linux 9" as the filtered product description
    And I select "Rocky Linux 9 x86_64" as a product
    Then I should see the "Rocky Linux 9 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Rocky Linux 9 x86_64" product has been added

@ubuntu2004_minion
  Scenario: Add Ubuntu 20.04
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Ubuntu 20.04" as the filtered product description
    And I select "Ubuntu 20.04" as a product
    Then I should see the "Ubuntu 20.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 20.04" product has been added

@uyuni
@ubuntu2004_minion
  Scenario: Add Ubuntu 20.04
    When I use spacewalk-common-channel to add channel "ubuntu-2004-pool-amd64-uyuni ubuntu-2004-amd64-main-uyuni ubuntu-2004-amd64-main-updates-uyuni ubuntu-2004-amd64-main-security-uyuni ubuntu-2004-amd64-universe-uyuni ubuntu-2004-amd64-universe-updates-uyuni ubuntu-2004-amd64-universe-security-uyuni ubuntu-2004-amd64-universe-backports-uyuni ubuntu-2004-amd64-uyuni-client-devel" with arch "amd64-deb"

@susemanager
@ubuntu2204_minion
  Scenario: Add Ubuntu 22.04
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Ubuntu 22.04" as the filtered product description
    And I select "Ubuntu 22.04" as a product
    Then I should see the "Ubuntu 22.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 22.04" product has been added

@uyuni
@ubuntu2204_minion
  Scenario: Add Ubuntu 22.04
    When I use spacewalk-common-channel to add channel "ubuntu-2204-pool-amd64-uyuni ubuntu-2204-amd64-main-uyuni ubuntu-2204-amd64-main-updates-uyuni ubuntu-2204-amd64-main-security-uyuni ubuntu-2204-amd64-universe-uyuni ubuntu-2204-amd64-universe-updates-uyuni ubuntu-2204-amd64-universe-security-uyuni ubuntu-2204-amd64-universe-backports-uyuni ubuntu-2204-amd64-uyuni-client-devel" with arch "amd64-deb"

@susemanager
@debian10_minion
  Scenario: Add Debian 10
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Debian 10" as the filtered product description
    And I select "Debian 10" as a product
    Then I should see the "Debian 10" selected
    When I click the Add Product button
    And I wait until I see "Debian 10" product has been added

@uyuni
@debian10_minion
  Scenario: Add Debian 10
    When I use spacewalk-common-channel to add channel "debian-10-pool-amd64-uyuni debian-10-amd64-main-updates-uyuni debian-10-amd64-main-security-uyuni debian-10-amd64-uyuni-client-devel" with arch "amd64-deb"

@susemanager
@debian11_minion
  Scenario: Add Debian 11
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "Debian 11" as the filtered product description
    And I select "Debian 11" as a product
    Then I should see the "Debian 11" selected
    When I click the Add Product button
     And I wait until I see "Debian 11" product has been added

@uyuni
@debian11_minion
  Scenario: Add Debian 11
    When I use spacewalk-common-channel to add channel "debian-11-pool-amd64-uyuni debian-11-amd64-main-updates-uyuni debian-11-amd64-main-security-uyuni debian-11-amd64-uyuni-client-devel" with arch "amd64-deb"

@susemanager
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

@cloud
@proxy
  Scenario: Add Manager Proxy 4.3 Public Cloud channels
    When I add "sle-module-public-cloud15-sp4-pool-x86_64-proxy-4.3" channel
    And I add "sle-module-public-cloud15-sp4-updates-x86_64-proxy-4.3" channel

@uyuni
@proxy
  Scenario: Add Uyuni Leap 15.4 Proxy, inlcuding Uyuni Client Tools
    When I use spacewalk-common-channel to add channel "opensuse_leap15_4 opensuse_leap15_4-non-oss opensuse_leap15_4-non-oss-updates opensuse_leap15_4-updates opensuse_leap15_4-backports-updates opensuse_leap15_4-sle-updates uyuni-proxy-stable-leap-154 opensuse_leap15_4-uyuni-client-devel" with arch "x86_64"

@susemanager
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
