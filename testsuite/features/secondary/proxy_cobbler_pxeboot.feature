# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#

@proxy
@private_net
@pxeboot_minion
@scope_cobbler
Feature: PXE boot a terminal with Cobbler
  In order to automate client system installations in Uyuni
  As the system administrator
  I want to PXE boot one host with Cobbler

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Configure PXE part of DHCP on the proxy
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Dhcpd" in the content area
    And I click on "Expand All Sections"
    And I enter the local IP address of "proxy" in pxeboot next server field
    And I enter "pxelinux.0" in pxeboot filename field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Apply the highstate after the formula setup
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed

  Scenario: Install TFTP boot package on the server
    When I install package tftpboot-installation on the server
    And I wait for "tftpboot-installation-SLE-15-SP2-x86_64" to be installed on "server"

  Scenario: Create auto installation distribution
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    And I follow "Create Distribution"
    And I enter "SLE-15-SP2-TFTP" as "label"
    And I enter "/usr/share/tftpboot-installation/SLE-15-SP2-x86_64/" as "basepath"
    And I select "SLE-Product-SLES15-SP2-Pool for x86_64" from "channelid"
    And I select "SUSE Linux Enterprise 15" from "installtype"
    And I click on "Create Autoinstallable Distribution"
    Then I should see a "Autoinstallable Distributions" text
    And I should see a "SLE-15-SP2-TFTP" link

  # WORKAROUND bsc#1195842
  # Default cobbler kernel parameters are wrong in case of proxy
  Scenario: Fix kernel parameters
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    And I follow "SLE-15-SP2-TFTP"
    And I enter "useonlinerepo insecure=1 install=http://proxy.example.org/ks/dist/SLE-15-SP2-TFTP self_update=http://proxy.example.org/ks/dist/child/sle15-sp2-installer-updates-x86_64/SLE-15-SP2-TFTP" as "kernelopts"
    And I click on "Update Autoinstallable Distribution"
    Then I should see a "Autoinstallable Distribution Updated" text

  Scenario: Create auto installation profile
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "Upload Kickstart/Autoyast File"
    And I enter "15-sp2-cobbler" as "kickstartLabel"
    And I select "SLE-15-SP2-TFTP" from "kstreeId"
    And I attach the file "/sle-15-sp2-autoyast.xml" to "fileUpload"
    And I click on "Create"
    Then I should see a "Autoinstallation: 15-sp2-cobbler" text
    And I should see a "Autoinstallation Details" text

  Scenario: Configure auto installation profile
    When I enter "self_update=0" as "kernel_options"
    And I click on "Update"
    And I follow "Variables"
    And I enter "distrotree=SLE-15-SP2-TFTP\nregistration_key=1-SUSE-KEY-x86_64\nredhat_management_server=proxy.example.org" as "variables" text area
    And I click on "Update Variables"
    And I follow "Autoinstallation File"
    Then I should see a "SLE-15-SP2-TFTP" text

  Scenario: Set up tftp installation
    When I configure tftp on the "server"
    And I start tftp on the proxy
    And I configure tftp on the "proxy"
    And I synchronize the tftp configuration on the proxy with the server

  Scenario: Restart squid so proxy.example.org is recognized
    When I restart squid service on the proxy

  Scenario: PXE boot the PXE boot minion
    Given I set the default PXE menu entry to the "target profile" on the "proxy"
    When I reboot the PXE boot minion
    And I wait for "60" seconds
    And I set the default PXE menu entry to the "local boot" on the "proxy"
    And I wait at most 1200 seconds until Salt master sees "pxeboot_minion" as "unaccepted"
    And I accept "pxeboot_minion" key in the Salt master
    And I am on the Systems page
    And I wait until I see the name of "pxeboot_minion", refreshing the page
    And I wait until onboarding is completed for "pxeboot_minion"

  Scenario: Check connection from PXE boot minion to the proxy
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see a "proxy.example.org" text

  Scenario: Install a package on the PXE boot minion
    When I install the GPG key of the test packages repository on the PXE boot minion
    And I follow "Software" in the content area
    And I follow "Install"
    And I check "virgo-dummy-2.0-1.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Cleanup: remove the auto installation profile
    Given I follow the left menu "Systems > Autoinstallation > Profiles"
    When I follow "15-sp2-cobbler"
    And I follow "Delete Autoinstallation"
    And I click on "Delete Autoinstallation"
    Then I should not see a "15-sp2-cobbler" text

  Scenario: Cleanup: remove the auto installation distribution
    Given I follow the left menu "Systems > Autoinstallation > Distributions"
    When I follow "SLE-15-SP2-TFTP"
    And I follow "Delete Distribution"
    And I click on "Delete Distribution"
    And I remove package "tftpboot-installation-SLE-15-SP2-x86_64" from this "server"
    And I wait for "tftpboot-installation-SLE-15-SP2-x86_64" to be uninstalled on "server"
    Then I should not see a "SLE-15-SP2-TFTP" text

  Scenario: Cleanup: delete the PXE boot minion
    Given I am on the Systems overview page of this "pxeboot_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "pxeboot_minion" should not be registered
    And I stop salt-minion on the PXE boot minion

  Scenario: Cleanup: the PXE boot minion prefers booting via saltboot
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Dhcpd" in the content area
    And I click on "Expand All Sections"
    And I enter "boot/pxelinux.0" in pxeboot filename field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Cleanup: apply the highstate after the formula cleanup changes
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
