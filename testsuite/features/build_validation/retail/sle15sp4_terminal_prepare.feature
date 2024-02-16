# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@proxy
@sle15sp4_terminal
Feature: Prepare prerequisites for SLES 15 SP4 terminal deployment

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Prepare activation key for SLES 15 SP4 terminal
    When I create an activation key including custom channels for "sle15sp4_terminal" via API

  Scenario: Create hardware type group for SLES 15 SP4 terminal
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "HWTYPE:HP-ProLiantDL360Gen9" as "name"
    And I enter "Terminal hardware type: HP host" as "description"
    And I click on "Create Group"
    Then I should see a "System group HWTYPE:HP-ProLiantDL360Gen9 created." text

  Scenario: Enable Saltboot formula for hardware type group SLES 15 SP4 terminal
    When I follow the left menu "Systems > System Groups"
    And I follow "HWTYPE:HP-ProLiantDL360Gen9" in the content area
    And I follow "Formulas" in the content area
    And I check the "saltboot" formula
    And I click on "Save"
    Then the "saltboot" formula should be checked

  Scenario: Parametrize the Saltboot formula SLES 15 SP4 terminal
    When I follow the left menu "Systems > System Groups"
    And I follow "HWTYPE:HP-ProLiantDL360Gen9" in the content area
    And I follow "Formulas" in the content area
    And I follow first "Saltboot" in the content area
    And I click on "Expand All Sections"
    And I enter "disk1" in disk id field
    And I enter "/dev/vda" in disk device field
    And I select "msdos" in disk label field
    And I enter "p1" in first partition id field
    And I enter "512" in first partition size field
    And I enter "/data" in first mount point field
    And I select "ext4" in first filesystem format field
    And I press "Add Item" in partitions section
    And I enter "p2" in second partition id field
    And I enter "1024" in second partition size field
    And I select "swap" in second filesystem format field
    And I select "swap" in second partition flags field
    And I press "Add Item" in partitions section
    And I enter "p3" in third partition id field
    And I enter "/" in third mount point field
    And I select "boot" in third partition flags field
    And I enter the image name for "sle15sp4_terminal" in third OS image field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text
