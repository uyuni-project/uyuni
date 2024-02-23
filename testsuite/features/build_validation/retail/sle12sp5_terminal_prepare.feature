# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@proxy
@sle12sp5_terminal
Feature: Prepare prerequisites for SLES 12 SP5 terminal deployment

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Prepare activation key for SLES 12 SP5 terminal
    When I create an activation key including custom channels for "sle12sp5_terminal" via API

  Scenario: Create hardware type group for SLES 12 SP5 terminal
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "HWTYPE:Supermicro-X9DR3-F" as "name"
    And I enter "Terminal hardware type: Supermicro host" as "description"
    And I click on "Create Group"
    Then I should see a "System group HWTYPE:Supermicro-X9DR3-F created." text

  Scenario: Enable Saltboot formula for hardware type group for SLES 12 SP5 terminal
    When I follow the left menu "Systems > System Groups"
    And I follow "HWTYPE:Supermicro-X9DR3-F" in the content area
    And I follow "Formulas" in the content area
    And I check the "saltboot" formula
    And I click on "Save"
    Then the "saltboot" formula should be checked

  Scenario: Parametrize the Saltboot formula for SLES 12 SP5 terminal
    When I follow the left menu "Systems > System Groups"
    And I follow "HWTYPE:Supermicro-X9DR3-F" in the content area
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
    And I enter the image name for "sle12sp5_terminal" in third OS image field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text
