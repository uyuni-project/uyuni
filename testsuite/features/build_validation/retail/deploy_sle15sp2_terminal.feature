# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: PXE boot a SLES 15 SP2 retail terminal
  In order to use SUSE Manager for Retail solution
  As the system administrator
  I PXE boot one of the terminals
  I perform a mass import of several virtual terminals and one real minion

  Scenario: PXE boot the SLES 15 SP2 retail terminal
    Given I am authorized as "admin" with password "admin"
    When I run "reboot" on "sle15sp2_terminal"
    And I wait at most 180 seconds until Salt master sees "sle15sp2_terminal" as "unaccepted"
    And I accept "sle15sp2_terminal" key in the Salt master
    And I am on the System Overview page
    And I wait until I see the name of "sle15sp2_terminal", refreshing the page
    And I follow this "sle15sp2_terminal" link
    And I wait until event "Apply states [util.syncstates, saltboot] scheduled by (none)" is completed
    And I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until radio button "SLE-Product-SLES15-SP2-Pool" is checked, refreshing the page
    And I wait until event "Package List Refresh scheduled by (none)" is completed
    Then the PXE boot minion should have been reformatted

  Scenario: Check connection from SLES 15 SP2 retail terminal to branch server
    Given I am on the Systems overview page of this "sle15sp2_terminal"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  Scenario: Install a package on the SLES 15 SP2 retail terminal
    Given I am on the Systems overview page of this "sle15sp2_terminal"
    When I follow "Software" in the content area
    And I follow "Install"
     And I enter "rust" as the filtered package name
    And I check "rust-1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Remove a package on the SLES 15 SP2 retail terminal
    Given I am on the Systems overview page of this "sle15sp2_terminal"
    When I follow "Software" in the content area
    And I follow "List / Remove"
    And I enter "rust" as the filtered package name
    And I click on the filter button
    And I check "rust-1" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    When I wait until event "Package Removal scheduled by admin" is completed
