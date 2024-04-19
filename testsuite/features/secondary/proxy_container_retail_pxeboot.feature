# Copyright (c) 2024 SUSE LLC
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
#
# This bug prevents using mgr-bootstrap command:
#  https://bugzilla.suse.com/show_bug.cgi?id=1220864

@containerized_server
@skip_if_github_validation
@buildhost
@proxy
@private_net
@pxeboot_minion
@scope_retail
Feature: PXE boot a Retail terminal behind a containerized proxy
  In order to manage my terminals in a Retail context
  As the system administrator
  I PXE boot one of the terminals

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

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
    # TODO: Re-add this when we onboard the proxy host again (no more "Foreign" system type):
    # When I follow "Target Systems"
    # And I check the "proxy" client
    # And I click on "Add Systems"
    # Then I should see a "1 systems were added to example server group." text

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
    # TODO: Re-add this when we onboard the proxy host again (no more "Foreign" system type):
    # When I follow "Target Systems"
    # And I check the "proxy" client
    # And I click on "Add Systems"
    # Then I should see a "1 systems were added to SERVERS server group." text

  Scenario: Enable Saltboot Group formula for branch terminals group
    When I follow the left menu "Systems > System Groups"
    And I follow "example" in the content area
    And I follow "Formulas" in the content area
    And I check the "saltboot-group" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "saltboot-group" formula should be checked

  Scenario: Parametrize the Saltboot Group formula
    When I follow the left menu "Systems > System Groups"
    And I follow "example" in the content area
    And I follow "Formulas" in the content area
    And I follow first "Saltboot Group" in the content area
    And I enter "proxy.example.org" as "Image download server"
    And I check containerized proxy box
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

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
