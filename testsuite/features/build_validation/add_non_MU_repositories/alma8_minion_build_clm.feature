# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.

@alma8_minion
Feature: Add the Alma 8 distribution custom repositories
  In order to use Alma 8 channels with Appstream modules
  As a SUSE Manager administrator
  I want to filter them out to remove the modules information

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Verify the CLM filters we need for Alma 8 exist
    When I follow the left menu "Content Lifecycle > Filters"
    Then I should see a "ruby-3.1" text
    And I should see a "php-8.1" text

@susemanager
  Scenario: Create a CLM project to remove AppStream metadata from Alma 8
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "Create Project"
    And I enter "Remove AppStream metadata from Alma 8" as "name"
    And I enter "no-appstream-alma-8" as "label"
    And I click on "Create"
    Then I should see a "Content Lifecycle Project - Remove AppStream metadata from Alma 8" text
    When I click on "Attach/Detach Sources"
    And I wait until I do not see "Loading" text
    And I select "almalinux8 for x86_64" from "selectedBaseChannel"
    # "almalinux8-appstream for x86_64" is already checked
    And I check "Custom Channel for alma8_minion"
    And I click on "Save"
    Then I should see a "RES8-Manager-Tools-Pool for x86_64 Alma" text
    When I click on "Attach/Detach Filters"
    And I check "python-3.6: enable module python36:3.6"
    And I check "ruby-2.7: enable module ruby:2.7"
    And I click on "Save"
    Then I should see a "python-3.6: enable module python36:3.6" text
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

@uyuni
  Scenario: Create a CLM project to remove AppStream metadata from Alma 8
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "Create Project"
    And I enter "Remove AppStream metadata from Alma 8" as "name"
    And I enter "no-appstream-alma-8" as "label"
    And I click on "Create"
    Then I should see a "Content Lifecycle Project - Remove AppStream metadata from Alma 8" text
    When I click on "Attach/Detach Sources"
    And I wait until I do not see "Loading" text
    And I select "AlmaLinux 8 (x86_64)" from "selectedBaseChannel"
    And I check "Uyuni Client Tools for AlmaLinux 8 (x86_64)"
    And I check "Custom Channel for alma8_minion"
    And I check "AlmaLinux 8 AppStream (x86_64)"
    And I click on "Save"
    Then I should see a "AlmaLinux 8 AppStream (x86_64)" text
    When I click on "Attach/Detach Filters"
    And I check "python-3.6: enable module python36:3.6"
    And I check "ruby-2.7: enable module ruby:2.7"
    And I click on "Save"
    Then I should see a "python-3.6: enable module python36:3.6" text
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
