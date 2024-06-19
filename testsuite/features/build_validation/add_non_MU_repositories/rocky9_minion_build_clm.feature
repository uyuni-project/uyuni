# Copyright (c) 2022-2023 SUSE LLC
# Licensed under the terms of the MIT license.

@rocky9_minion
Feature: Add the Rocky 9 distribution custom repositories
  In order to use Rocky 9 channels with Red Hat "modules"
  As a SUSE Manager administrator
  I want to filter them out to remove the modules information

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Verify the CLM filters we need for Rocky 9 exist
    When I follow the left menu "Content Lifecycle > Filters"
    Then I should see a "ruby-3.1" text
    And I should see a "php-8.2" text

@susemanager
  Scenario: Create a CLM project to remove AppStream metadata from Rocky 9
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "Create Project"
    And I enter "Remove AppStream metadata from Rocky 9" as "name"
    And I enter "no-appstream-9" as "label"
    And I click on "Create"
    Then I should see a "Content Lifecycle Project - Remove AppStream metadata from Rocky 9" text
    When I click on "Attach/Detach Sources"
    And I wait until I do not see "Loading" text
    And I select "rockylinux-9 for x86_64" from "selectedBaseChannel"
    # "rockylinux-9-appstream for x86_64" is already checked
    And I check "Custom Channel for rocky9_minion"
    And I click on "Save"
    Then I should see a "EL9-Manager-Tools-Pool for x86_64 Rocky" text
    When I click on "Attach/Detach Filters"
    And I check "php-8.2: enable module php:8.2"
    And I check "ruby-3.1: enable module ruby:3.1"
    And I click on "Save"
    Then I should see a "php-8.2: enable module php:8.2" text
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
  Scenario: Create a CLM project to remove AppStream metadata from Rocky 9
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "Create Project"
    And I enter "Remove AppStream metadata from Rocky 9" as "name"
    And I enter "no-appstream-9" as "label"
    And I click on "Create"
    Then I should see a "Content Lifecycle Project - Remove AppStream metadata from Rocky 9" text
    When I click on "Attach/Detach Sources"
    And I wait until I do not see "Loading" text
    And I select "Rocky Linux 9 (x86_64)" from "selectedBaseChannel"
    And I check "Uyuni Client Tools for Rocky Linux 9 (x86_64)"
    And I check "Rocky Linux 9 AppStream (x86_64)"
    And I check "Custom Channel for rocky9_minion"
    And I click on "Save"
    Then I should see a "Rocky Linux 9 AppStream (x86_64)" text
    When I click on "Attach/Detach Filters"
    And I check "php-8.2: enable module php:8.2"
    And I check "ruby-3.1: enable module ruby:3.1"
    And I click on "Save"
    Then I should see a "php-8.2: enable module php:8.2" text
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
