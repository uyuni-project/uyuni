# Copyright (c) 2018-2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Idempotency note:
# This feature depends on a JeOS image present on the proxy
# Please make sure that the feature
#     features/secondary/buildhost_osimage_build_image.feature
# has been tested previously
#
# The scenarios in this feature are skipped:
# * if there is no proxy ($proxy is nil)
# * if there is no private network ($private_net is nil)
# * if there is no PXE boot minion ($pxeboot_mac is nil)

@skip_if_containerized_server
@skip_if_github_validation
@buildhost
@proxy
@private_net
@pxeboot_minion
@scope_retail
Feature: PXE boot a Retail terminal behind a traditional proxy
  In order to manage my terminals in a Retail context
  As the system administrator
  I PXE boot one of the terminals

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Enable the PXE formulas on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas" text
    And I should see a "Suse Manager For Retail" text
    And I should see a "General System Configuration" text
    When I check the "tftpd" formula
    And I check the "vsftpd" formula
    And I check the "pxe" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "tftpd" formula should be checked
    And the "vsftpd" formula should be checked
    And the "pxe" formula should be checked

  Scenario: Configure general info for PXE part of DNS on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Bind" in the content area
    And I click on "Expand All Sections"
    And I press "Add Item" in CNAME section of example.org zone
    And I enter "ftp" in first CNAME alias field of example.org zone
    And I enter "proxy" in first CNAME name field of example.org zone
    And I press "Add Item" in CNAME section of example.org zone
    And I enter "tftp" in second CNAME alias field of example.org zone
    And I enter "proxy" in second CNAME name field of example.org zone
    And I press "Add Item" in CNAME section of example.org zone
    And I enter "salt" in third CNAME alias field of example.org zone
    And I enter "proxy" in third CNAME name field of example.org zone
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Configure PXE part of DHCP on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Dhcpd" in the content area
    And I click on "Expand All Sections"
    And I enter the local IP address of "proxy" in next server field
    And I enter "boot/pxelinux.0" in filename field
    And I enter "boot/pxelinux.0" in pxeboot filename field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Parametrize TFTP on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Tftpd" in the content area
    And I click on "Expand All Sections"
    And I enter the local IP address of "proxy" in internal network address field
    And I enter "/srv/saltboot" in TFTP base directory field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Parametrize vsFTPd on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Vsftpd" in the content area
    And I click on "Expand All Sections"
    And I enter the local IP address of "proxy" in vsftpd internal network address field
    And I enter "/srv/saltboot" in FTP server directory field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Configure PXE itself on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Pxe" in the content area
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Apply the PXE formulas via the highstate
    Given I am on the Systems overview page of this "proxy"
    When I follow "States" in the content area
    And I enable repositories before installing branch server
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
    And I disable repositories after installing branch server
    Then socket "tftp" is enabled on "proxy"
    And socket "tftp" is active on "proxy"

  Scenario: Create hardware type group
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "HWTYPE:Intel-Genuine" as "name"
    And I enter "Terminal hardware type: genuine Intel" as "description"
    And I click on "Create Group"
    Then I should see a "System group HWTYPE:Intel-Genuine created." text

  Scenario: Create branch terminals group
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "example" as "name"
    And I enter "Terminal branch: example.org" as "description"
    And I click on "Create Group"
    Then I should see a "System group example created." text
    When I follow "Target Systems"
    And I check the "proxy" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to example server group." text

  Scenario: Create all terminals group
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "TERMINALS" as "name"
    And I enter "All terminals" as "description"
    And I click on "Create Group"
    Then I should see a "System group TERMINALS created." text

  Scenario: Create all branch servers group
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "SERVERS" as "name"
    And I enter "All branch servers" as "description"
    And I click on "Create Group"
    Then I should see a "System group SERVERS created." text
    When I follow "Target Systems"
    And I check the "proxy" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to SERVERS server group." text

  Scenario: Move the image to the branch server
    When I enable repositories before installing branch server
    And I apply state "image-sync" to "proxy"
    And I disable repositories after installing branch server
    Then the image for "pxeboot_minion" should exist on the branch server

  Scenario: Enable Saltboot formula for hardware type group
    When I follow the left menu "Systems > System Groups"
    And I follow "HWTYPE:Intel-Genuine" in the content area
    And I follow "Formulas" in the content area
    And I check the "saltboot" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "saltboot" formula should be checked

  Scenario: Parametrize the Saltboot formula
    When I follow the left menu "Systems > System Groups"
    And I follow "HWTYPE:Intel-Genuine" in the content area
    And I follow "Formulas" in the content area
    And I follow first "Saltboot" in the content area
    And I click on "Expand All Sections"
    And I enter "disk1" in disk id field
    And I enter "/dev/vda" in disk device field
    And I select "msdos" in disk label field
    And I enter "p1" in first partition id field
    And I enter "256" in first partition size field
    And I select "swap" in first filesystem format field
    And I select "swap" in first partition flags field
    And I press "Add Item" in partitions section
    And I enter "p2" in second partition id field
    And I enter "/data" in second mount point field
    And I select "xfs" in second filesystem format field
    And I enter "secret-password" in second partition password field
    And I press "Add Item" in partitions section
    And I enter "p3" in third partition id field
    And I enter "/" in third mount point field
    And I enter the image name for "pxeboot_minion" in third OS image field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: PXE boot the PXE boot minion
    When I reboot the Retail terminal "pxeboot_minion"
    And I wait at most 180 seconds until Salt master sees "pxeboot_minion" as "unaccepted"
    And I accept "pxeboot_minion" key in the Salt master

  Scenario: Assure the PXE boot minion is onboarded
    Given I am on the Systems page
    When I wait until I see the name of "pxeboot_minion", refreshing the page
    And I follow this "pxeboot_minion" link
    And I follow "Events"
    And I follow "History"
    And I wait until I see the event "added system entitlement" completed during last minute, refreshing the page
    And I wait until event "Apply states [saltboot]" is completed
    And I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until radio button "SLE-Product-SLES15-SP4-Pool for x86_64" is checked, refreshing the page
    And I wait until event "Package List Refresh" is completed
    Then "pxeboot_minion" should have been reformatted

  Scenario: Check connection from terminal to branch server
    When I navigate to the Systems overview page of this "pxeboot_minion"
    And I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see a "proxy.example.org" text

  Scenario: Install a package on the new Retail terminal
    When I navigate to the Systems overview page of this "pxeboot_minion"
    And I install the GPG key of the test packages repository on the PXE boot minion
    And I follow "Software" in the content area
    And I follow "Install"
    And I enter "virgo" as the filtered package name
    And I click on the filter button
    And I check "virgo-dummy-2.0-1.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Remove the package from the new Retail terminal
    When I navigate to the Systems overview page of this "pxeboot_minion"
    And I follow "Software" in the content area
    And I follow "List / Remove"
    And I enter "virgo" as the filtered package name
    And I click on the filter button
    And I check "virgo-dummy-2.0-1.1" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    When I wait until event "Package Removal scheduled by admin" is completed

  Scenario: Cleanup: let the terminal be reinstalled again
    When I navigate to the Systems overview page of this "pxeboot_minion"
    And I follow "Remote Command"
    And I enter "#!/bin/sh\nrm /etc/ImageVersion*" as "script_body" text area
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I wait until event "Remote Command" is completed

  Scenario: Cleanup: delete the new Retail terminal
    When I navigate to the Systems overview page of this "pxeboot_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on the PXE boot minion
    Then "pxeboot_minion" should not be registered

  Scenario: Cleanup: undo TFTP and PXE formulas
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I uncheck the "tftpd" formula
    And I uncheck the "pxe" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "tftpd" formula should be unchecked
    And the "pxe" formula should be unchecked

  Scenario: Cleanup: undo CNAME aliases
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Bind" in the content area
    And I click on "Expand All Sections"
    And I press "Remove Item" in salt CNAME of example.org zone section
    And I press "Remove Item" in tftp CNAME of example.org zone section
    And I press "Remove Item" in ftp CNAME of example.org zone section
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Cleanup: delete the terminal groups
    When I follow the left menu "Systems > System Groups"
    And I follow "HWTYPE:Intel-Genuine" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text
    When I follow "example" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text
    When I follow "TERMINALS" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text
    When I follow "SERVERS" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text

  Scenario: Cleanup: apply the highstate to clear PXE formulas
    Given I am on the Systems overview page of this "proxy"
    When I follow "States" in the content area
    And I enable repositories before installing branch server
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
