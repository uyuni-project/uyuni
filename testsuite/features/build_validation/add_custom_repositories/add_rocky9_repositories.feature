# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

@rocky9_minion
Feature: Add the Rocky 9 distribution custom repositories
  In order to use Rocky 9 channels with Red Hat "modules"
  As a SUSE Manager administrator
  I want to filter them out to remove the modules information

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create CLM filters to remove AppStream metadata from Rocky 9
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    And I enter "ruby-3.0" as "filter_name"
    And I select "Module (Stream)" from "type"
    And I enter "ruby" as "moduleName"
    And I enter "3.0" as "moduleStream"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "ruby-3.0" text
    When I click on "Create Filter"
    Then I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "python-3.9" as "filter_name"
    And I select "Module (Stream)" from "type"
    And I enter "python39" as "moduleName"
    And I enter "3.9" as "moduleStream"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "python-3.9" text

  Scenario: Create a CLM project to remove AppStream metadata from Rocky 9
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "Create Project"
    And I enter "Remove AppStream metadata from Rocky 9" as "name"
    And I enter "no-appstream" as "label"
    And I click on "Create"
    Then I should see a "Content Lifecycle Project - Remove AppStream metadata from Rocky 9" text
    When I click on "Attach/Detach Sources"
    And I select "Rocky Linux 9" from "selectedBaseChannel"
    And I check "Rocky Linux 9 AppStream"
    And I click on "Save"
    Then I should see a "Custom Channel for Rocky Linux 9" text
    When I click on "Attach/Detach Filters"
    And I check "python-3.9: enable module python39:3.9"
    And I check "ruby-3.0: enable module ruby:3.0"
    And I click on "Save"
    Then I should see a "python-3.9: enable module python39:3.9" text
    When I click on "Add Environment"
    And I enter "result" as "name"
    And I enter "result" as "label"
    And I enter "Filtered channels without AppStream channels" as "description"
    And I click on "Save"
    Then I should see a "not built" text
    When I click on "Build"
    And I enter "Initial build" as "message"
    And I click the environment build button
    Then I should see a "Version 1: Initial build" text
