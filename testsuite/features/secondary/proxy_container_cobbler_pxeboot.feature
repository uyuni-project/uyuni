# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# We also test 'Bootstrapping using the command line' in this feature with the following script:
# https://github.com/uyuni-project/uyuni/blob/master/java/conf/cobbler/snippets/minion_script

@skip_if_github_validation
@containerized_server
@proxy
@buildhost
@private_net
@pxeboot_minion
@scope_cobbler
Feature: PXE boot a terminal with Cobbler and containerized proxy
  In order to automate client system installations with a containerized proxy
  As the system administrator
  I want to PXE boot one host with Cobbler

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Start Cobbler monitoring
    When I start local monitoring of Cobbler

  # We currently test Cobbler with a SLES 15 SP4 terminal
  # The build host has same version as the terminal
  Scenario: Prepare the autoinstallation files on the server
    When I install packages "tftpboot-installation-SLE-15-SP4-x86_64 expect" on this "build_host"
    And I copy the tftpboot installation files from the build host to the server
    And I copy the distribution inside the container on the server

  Scenario: Create auto installation distribution
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    And I follow "Create Distribution"
    And I enter "SLE-15-SP4-TFTP" as "label"
    And I enter "/srv/www/distributions/SLE-15-SP4-TFTP/" as "basepath"
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "channelid"
    And I select "SUSE Linux Enterprise 15" from "installtype"
    And I click on "Create Autoinstallable Distribution"
    Then I should see a "Autoinstallable Distributions" text
    And I should see a "SLE-15-SP4-TFTP" link

  # WORKAROUND bsc#1195842
  # Default Cobbler kernel parameters are wrong in case of proxy
  Scenario: Fix kernel parameters
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    And I follow "SLE-15-SP4-TFTP"
    And I enter "useonlinerepo insecure=1 install=http://proxy.example.org/ks/dist/SLE-15-SP4-TFTP self_update=http://proxy.example.org/ks/dist/child/sle15-sp4-installer-updates-x86_64/SLE-15-SP4-TFTP" as "kernelopts"
    And I click on "Update Autoinstallable Distribution"
    Then I should see a "Autoinstallable Distribution Updated" text

  Scenario: Create auto installation profile
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "Upload Kickstart/AutoYaST File"
    And I enter "15-sp4-nobbler" as "kickstartLabel"
    And I select "SLE-15-SP4-TFTP" from "kstreeId"
    And I attach the file "/sle-15-sp4-autoyast.xml" to "fileUpload"
    And I click on "Create"
    Then I should see a "Autoinstallation: 15-sp4-cobbler" text
    And I should see a "Autoinstallation Details" text

  Scenario: Configure auto installation profile
    When I enter "self_update=0" as "kernel_options"
    And I click on "Update"
    And I follow "Variables"
    And I enter "distrotree=SLE-15-SP4-TFTP\nregistration_key=1-TERMINAL-KEY-x86_64\nredhat_management_server=proxy.example.org" as "variables" text area
    And I click on "Update Variables"
    And I follow "Autoinstallation File"
    Then I should see a "SLE-15-SP4-TFTP" text

  Scenario: Migration of Cobbler settings
    Given cobblerd is running
    And cobbler settings are successfully migrated
    When I restart cobbler on the server
    Then service "cobblerd" is active on "server"

  Scenario: PXE boot the PXE boot minion
    When I set the default PXE menu entry to the target profile on the "server"
    And I reboot the Cobbler terminal "pxeboot_minion"
    And I wait for "60" seconds
    And I set the default PXE menu entry to the local boot on the "server"
    And I wait at most 1200 seconds until Salt master sees "pxeboot_minion" as "unaccepted"
    And I accept "pxeboot_minion" key in the Salt master

  Scenario: Assure the PXE boot minion is onboarded
    Given I am on the Systems page
    When I wait until I see the name of "pxeboot_minion", refreshing the page
    And I wait until onboarding is completed for "pxeboot_minion"
    Then "pxeboot_minion" should have been reformatted

  Scenario: Check connection from PXE boot minion to the proxy
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see a "proxy.example.org" text

  Scenario: Install a package on the PXE boot minion
    When I install the GPG key of the test packages repository on the PXE boot minion
    And I follow "Software" in the content area
    And I follow "Install"
    And I enter "virgo-dummy-2.0-1.1" as the filtered package name
    And I click on the filter button
    And I check "virgo-dummy-2.0-1.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Cleanup: remove the auto installation profile
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "15-sp4-cobbler"
    And I follow "Delete Autoinstallation"
    And I click on "Delete Autoinstallation"
    Then I should not see a "15-sp4-cobbler" text

  Scenario: Cleanup: remove the auto installation distribution
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    And I follow "SLE-15-SP4-TFTP"
    And I follow "Delete Distribution"
    And I click on "Delete Distribution"
    Then I should not see a "SLE-15-SP4-TFTP" text

  Scenario: Cleanup: remove the auto installation files
    When I remove packages "tftpboot-installation-SLE-15-SP4-x86_64 expect" from this "build_host"
    And I remove the autoinstallation files from the server

  Scenario: Cleanup: delete the PXE boot minion
    When I navigate to the Systems overview page of this "pxeboot_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on the PXE boot minion
    Then "pxeboot_minion" should not be registered

@flaky
  Scenario: Check for errors in Cobbler monitoring
    Then the local logs for Cobbler should not contain errors
