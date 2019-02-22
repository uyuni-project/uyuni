# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature depends on a JeOS image present on the proxy
# Please make sure that the feature
#     features/min_osimage_build_image.feature
# has been tested previously
#
# The scenarios in this feature are skipped:
# * if there is no proxy ($proxy is nil)
# * if there is no private network ($private_net is nil)
# * if there is no PXE boot minion ($pxeboot_mac is nil)

Feature: PXE boot a Retail terminal
  In order to use SUSE Manager for Retail solution
  As the system administrator
  I want to PXE boot one of the terminals

@proxy
@private_net
@pxeboot_minion
  Scenario: Install or update PXE formulas on the server
    When I manually install the "tftpd" formula on the server
    And I manually install the "vsftpd" formula on the server
    And I manually install the "saltboot" formula on the server
    And I manually install the "pxe" formula on the server
    And I wait for "16" seconds

@proxy
@private_net
@pxeboot_minion
  Scenario: Enable the PXE formulas on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I check the "tftpd" formula
    And I check the "vsftpd" formula
    And I check the "pxe" formula
    And I click on "Save"
    Then the "tftpd" formula should be checked

@proxy
@private_net
@pxeboot_minion
  Scenario: Configure PXE part of DNS on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Bind" in the content area
    # general information:
    #   (Avahi does not cross networks, so we need to cheat by serving tf.local)
    And I press "Add Item" in configured zones section
    And I enter "tf.local" in third configured zone name field
    # direct zone example.org:
    And I press "Add Item" in first A section
    And I enter "pxeboot" in fourth A name field
    And I enter the local IP address of "pxeboot" in fourth A address field
    And I press "Add Item" in first CNAME section
    And I enter "ftp" in first CNAME alias field
    And I enter "proxy" in first CNAME name field
    And I press "Add Item" in first CNAME section
    And I enter "tftp" in second CNAME alias field
    And I enter "proxy" in second CNAME name field
    And I press "Add Item" in first CNAME section
    And I enter "salt" in third CNAME alias field
    And I enter the hostname of "proxy" in third CNAME name field
    # direct zone tf.local:
    #   (Avahi does not cross networks, so we need to cheat by serving tf.local)
    And I press "Add Item" in available zones section
    And I enter "tf.local" in third available zone name field
    And I enter "master/db.tf.local" in third file name field
    And I enter the hostname of "proxy" in third name server field
    And I enter "admin@tf.local." in third contact field
    And I press "Add Item" in third A section
    And I enter the hostname of "proxy" in fifth A name field
    And I enter the IP address of "proxy" in fifth A address field
    And I press "Add Item" in third NS section
    And I enter the hostname of "proxy" in third NS field
    # end
    And I click on "Save Formula"
    Then I should see a "Formula saved!" text

@proxy
@private_net
@pxeboot_minion
  Scenario: Configure PXE part of DHCP on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Dhcpd" in the content area
    And I enter the local IP address of "proxy" in next server field
    And I enter "boot/pxelinux.0" in filename field
    And I press "Add Item" in host reservations section
    And I enter "pxeboot" in third reserved hostname field
    And I enter the local IP address of "pxeboot" in third reserved IP field
    And I enter the MAC address of "pxeboot-minion" in third reserved MAC field
    And I click on "Save Formula"
    Then I should see a "Formula saved!" text

@proxy
@private_net
@pxeboot_minion
  Scenario: Parametrize vsFTPd on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Vsftpd" in the content area
    And I enter the local IP address of "proxy" in internal network address field for vsftpd
    And I enter "/srv/saltboot" in FTP server directory field
    And I click on "Save Formula"
    Then I should see a "Formula saved!" text

@proxy
@private_net
@pxeboot_minion
  Scenario: Parametrize TFTP on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Tftpd" in the content area
    And I enter the local IP address of "proxy" in internal network address field
    And I enter "/srv/saltboot" in TFTP base directory field
    And I click on "Save Formula"
    Then I should see a "Formula saved!" text

@proxy
@private_net
@pxeboot_minion
  Scenario: Configure PXE itself on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Pxe" in the content area
    And I enter "example" in branch id field
    And I click on "Save Formula"
    Then I should see a "Formula saved!" text

@proxy
@private_net
@pxeboot_minion
  Scenario: Apply the PXE formulas via the highstate
    Given I am on the Systems overview page of this "proxy"
    When I follow "States" in the content area
    And I enable repositories before installing branch server
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
    And I disable repositories after installing branch server
    Then socket "tftp" is enabled on "proxy"
    And socket "tftp" is active on "proxy"

@proxy
@private_net
@pxeboot_minion
  Scenario: Create hardware type group
    Given I am on the groups page
    When I follow "Create Group"
    And I enter "HWTYPE:Intel-Genuine" as "name"
    And I enter "Terminal hardware type: genuine Intel" as "description"
    And I click on "Create Group"
    Then I should see a "System group HWTYPE:Intel-Genuine created." text

@proxy
@private_net
@pxeboot_minion
  Scenario: Create terminal branch group
    Given I am on the groups page
    When I follow "Create Group"
    And I enter "example" as "name"
    And I enter "Terminal branch: example.org" as "description"
    And I click on "Create Group"
    Then I should see a "System group example created." text

@proxy
@private_net
@pxeboot_minion
  Scenario: Create all terminals group
    Given I am on the groups page
    When I follow "Create Group"
    And I enter "TERMINALS" as "name"
    And I enter "All terminals" as "description"
    And I click on "Create Group"
    Then I should see a "System group TERMINALS created." text

@proxy
@private_net
@pxeboot_minion
  Scenario: Create all branch servers group
    Given I am on the groups page
    When I follow "Create Group"
    And I enter "SERVERS" as "name"
    And I enter "All branch servers" as "description"
    And I click on "Create Group"
    Then I should see a "System group SERVERS created." text

@proxy
@private_net
@pxeboot_minion
  Scenario: Enable Saltboot formula for hardware type group
    Given I am on the groups page
    When I follow "HWTYPE:Intel-Genuine" in the content area
    And I follow "Formulas" in the content area
    And I check the "saltboot" formula
    And I click on "Save"
    Then the "saltboot" formula should be checked

@proxy
@private_net
@pxeboot_minion
  Scenario: Parametrize the Saltboot formula
    Given I am on the groups page
    When I follow "HWTYPE:Intel-Genuine" in the content area
    When I follow "Formulas" in the content area
    And I follow first "Saltboot" in the content area
    And I enter "disk1" in disk id field
    And I enter "/dev/vda" in disk device field
    And I select "msdos" in disk label field
    And I enter "p1" in first partition id field
    And I enter "256" in first partition size field
    And I select "swap" in first filesystem format field
    And I select "swap" in first partition flags field
    And I press "Add Item" in partitions section
    And I enter "p2" in second partition id field
    And I enter "/" in second mount point field
    And I enter "POS_Image_JeOS6" in second OS image field
    And I click on "Save Formula"
    Then I should see a "Formula saved!" text

@proxy
@private_net
@pxeboot_minion
  Scenario: PXE boot the PXE boot minion
    Given I am authorized as "admin" with password "admin"
    When I reboot the PXE boot minion
    And I wait at most 90 seconds until Salt master sees "pxeboot-minion" as "unaccepted"
    And I accept "pxeboot-minion" key in the Salt master
    And I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "pxeboot-minion", refreshing the page
    And I follow this "pxeboot-minion" link
    And I wait until event "Apply states [util.syncstates, saltboot] scheduled by (none)" is completed
    And I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until radio button "Test-Channel-x86_64" is checked, refreshing the page
    And I wait until event "Package List Refresh scheduled by (none)" is completed
    Then the PXE boot minion should have been reformatted

@proxy
@private_net
@pxeboot_minion
  Scenario: Check connection from terminal to branch server
    Given I am on the Systems overview page of this "pxeboot-minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" hostname

@proxy
@private_net
@pxeboot_minion
  Scenario: Install a package on the new Retail terminal
    Given I am on the Systems overview page of this "pxeboot-minion"
    When I install the GPG key of the server on the PXE boot minion
    And I follow "Software" in the content area
    And I follow "Install"
    And I check "virgo-dummy-2.0-1.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed

@proxy
@private_net
@pxeboot_minion
  Scenario: Cleanup: delete the new Retail terminal
    Given I am on the Systems overview page of this "pxeboot-minion"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I should see a "has been deleted" text
    # TODO: for full idempotency, also stop salt-minion service

@proxy
@private_net
@pxeboot_minion
  Scenario: Cleanup: undo TFTP and PXE formulas
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I uncheck the "tftpd" formula
    And I uncheck the "pxe" formula
    And I click on "Save"
    Then the "tftpd" formula should be unchecked
    And the "pxe" formula should be unchecked

@proxy
@private_net
@pxeboot_minion
  Scenario: Cleanup: undo CNAME aliases
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Bind" in the content area
    And I press "Remove Item" in third CNAME section
    And I press "Remove Item" in second CNAME section
    And I press "Remove Item" in first CNAME section
    And I click on "Save Formula"
    Then I should see a "Formula saved!" text

@proxy
@private_net
@pxeboot_minion
  Scenario: Cleanup: delete the terminal groups
    Given I am on the groups page
    When I follow "HWTYPE:Intel-Genuine" in the content area
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

@proxy
@private_net
@pxeboot_minion
  Scenario: Apply the highstate to clear PXE formulas
    Given I am on the Systems overview page of this "proxy"
    When I follow "States" in the content area
    And I enable repositories before installing branch server
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
