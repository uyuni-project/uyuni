# Copyright (c) 2023-2024 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create the CLM filters for RH-like minions
  In order to use RH-like channels with Red Hat "modules"
  As a SUSE Manager administrator
  I want to have filters ready

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create CLM filters to remove AppStream metadata from RH-like 8
    Given I am authorized for the "Admin" section
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    And I enter "ruby-2.7" as "filter_name"
    And I select "Module (Stream)" from "type"
    And I select "equals" from "matcher"
    And I enter "ruby" as "moduleName"
    And I enter "2.7" as "moduleStream"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "ruby-2.7" text
    When I click on "Create Filter"
    Then I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    # 3.6 is the version needed by our spacecmd
    When I enter "python-3.6" as "filter_name"
    And I select "Module (Stream)" from "type"
    And I select "equals" from "matcher"
    And I enter "python36" as "moduleName"
    And I enter "3.6" as "moduleStream"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "python-3.6" text

  Scenario: Create CLM filters to remove AppStream metadata from RH-like 9
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    And I enter "ruby-3.1" as "filter_name"
    And I select "Module (Stream)" from "type"
    And I select "equals" from "matcher"
    And I enter "ruby" as "moduleName"
    And I enter "3.1" as "moduleStream"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "ruby-3.1" text
    When I click on "Create Filter"
    Then I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "php-8.2" as "filter_name"
    And I select "Module (Stream)" from "type"
    And I select "equals" from "matcher"
    And I enter "php" as "moduleName"
    And I enter "8.2" as "moduleStream"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "php-8.2" text
