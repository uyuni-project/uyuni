# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Configure groups and saltboot formula for terminals
  In order to use SUSE Manager for Retail solution
  As the system administrator
  I configure groups and saltboot formulas for all types of terminals

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Apply configured formulas via the highstate
    Given I am on the Systems overview page of this "proxy"
    When I follow "States" in the content area
    And I enable repositories before installing branch server
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
    And I disable repositories after installing branch server

  Scenario: Create hardware type group for SLES 15 SP2 terminals
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "HWTYPE:Intel-Genuine15" as "name"
    And I enter "Terminal hardware type: genuine Intel15" as "description"
    And I click on "Create Group"
    Then I should see a "System group HWTYPE:Intel-Genuine15 created." text

  Scenario: Create hardware type group for SLES 12 SP4 terminals
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "HWTYPE:Intel-Genuine12" as "name"
    And I enter "Terminal hardware type: genuine Intel12" as "description"
    And I click on "Create Group"
    Then I should see a "System group HWTYPE:Intel-Genuine12 created." text

  Scenario: Create hardware type group for SLES 11 SP3 terminals
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "HWTYPE:Intel-Genuine11" as "name"
    And I enter "Terminal hardware type: genuine Intel11" as "description"
    And I click on "Create Group"
    Then I should see a "System group HWTYPE:Intel-Genuine11 created." text

  Scenario: Create terminal branch group for terminals
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "example" as "name"
    And I enter "Terminal branch: example.org" as "description"
    And I click on "Create Group"
    Then I should see a "System group example created." text

  Scenario: Create all terminals group for terminals
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "TERMINALS" as "name"
    And I enter "All terminals" as "description"
    And I click on "Create Group"
    Then I should see a "System group TERMINALS created." text

  Scenario: Create all branch servers group for terminals
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "SERVERS" as "name"
    And I enter "All branch servers" as "description"
    And I click on "Create Group"
    Then I should see a "System group SERVERS created." text

  Scenario: Enable Saltboot formula for hardware type group of SLES 15 SP2 terminals
    When I follow the left menu "Systems > System Groups"
    When I follow "HWTYPE:Intel-Genuine15" in the content area
    And I follow "Formulas" in the content area
    And I check the "saltboot" formula
    And I click on "Save"
    Then the "saltboot" formula should be checked

  Scenario: Parametrize the Saltboot formula for SLES 15 SP2 terminals
    When I follow the left menu "Systems > System Groups"
    When I follow "HWTYPE:Intel-Genuine15" in the content area
    When I follow "Formulas" in the content area
    And I follow first "Saltboot" in the content area
    And I enter "disk1" in disk id field
    And I enter "/dev/vda" in disk device field
    And I select "msdos" in disk label field
    And I enter "p1" in first partition id field
    And I enter "1024" in first partition size field
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
    And I enter "POS_Image_JeOS7" third OS image field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Parametrize the Saltboot formula for SLES 12 SP4 terminals
    When I follow the left menu "Systems > System Groups"
    When I follow "HWTYPE:Intel-Genuine12" in the content area
    When I follow "Formulas" in the content area
    And I follow first "Saltboot" in the content area
    And I enter "disk1" in disk id field
    And I enter "/dev/vda" in disk device field
    And I select "msdos" in disk label field
    And I enter "p1" in first partition id field
    And I enter "512" in first partition size field
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
    And I enter "POS_Image_JeOS6" third OS image field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Parametrize the Saltboot formula for SLES 11 SP3 terminals
    When I follow the left menu "Systems > System Groups"
    When I follow "HWTYPE:Intel-Genuine11" in the content area
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
    And I enter "/data" in second mount point field
    And I select "xfs" in second filesystem format field
    And I enter "secret-password" in second partition password field
    And I press "Add Item" in partitions section
    And I enter "p3" in third partition id field
    And I enter "/" in third mount point field
    And I enter "POS_Image_JeOS6_SLE11" third OS image field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text
