# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

@rhlike_minion
Feature: Add the Rocky 8 distribution custom repositories
  In order to use Rocky 8 channels with Red Hat "modules"
  As a SUSE Manager administrator
  I want to filter them out to remove the modules information

  Scenario: Download the iso of Rocky 8 DVD and mount it on the server
    When I mount as "rocky-8-iso" the ISO from "http://minima-mirror.mgr.suse.de/pub/rocky/8/isos/x86_64/Rocky-x86_64-dvd.iso" in the server

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add a child channel for Rocky 8 DVD repositories
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Custom Channel for Rocky 8 DVD" as "Channel Name"
    And I enter "rocky-8-iso" as "Channel Label"
    And I select "RHEL8-Pool for x86_64" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for Rocky 8 DVD created" text

  Scenario: Add the Rocky 8 Appstream DVD repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "rocky-8-iso-appstream" as "label"
    And I enter "http://127.0.0.1/rocky-8-iso/AppStream" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add the Rocky 8 BaseOS DVD repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "rocky-8-iso-baseos" as "label"
    And I enter "http://127.0.0.1/rocky-8-iso/BaseOS" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add both repositories to the custom channel for Rocky 8 DVD
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for Rocky 8 DVD"
    And I follow "Repositories" in the content area
    And I select the "rocky-8-iso-appstream" repo
    And I select the "rocky-8-iso-baseos" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Synchronize the repositories in the custom channel for Rocky 8 DVD
    When I call spacewalk-repo-sync to sync the channel "rocky-8-iso"
    When I wait until the channel "rocky-8-iso" has been synced
    And I wait until all spacewalk-repo-sync finished
    Then the "rocky-8-iso.log, rhel8-pool-x86_64.log" reposync logs should not report errors
    And the "res-8-updates-x86_64.log, res-as-8-updates-x86_64.log, res-cb-8-updates-x86_64.log" reposync logs should not report errors
    And the "res8-manager-tools-pool-x86_64.log, res8-manager-tools-updates-x86_64.log, el8-uyuni-client.log" reposync logs should not report errors

  Scenario: The custom channel for Rocky 8 has been synced
    When I wait until the channel "rocky-8-iso" has been synced

  Scenario: Create CLM filters to remove AppStream metadata
    Given I am authorized for the "Admin" section
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "ruby-2.7" as "filter_name"
    And I select "Module (Stream)" from "type"
    And I select "equals" from "matcher"
    And I enter "ruby" as "moduleName"
    And I enter "2.7" as "moduleStream"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "ruby-2.7" text
    When I click on "Create Filter"
    Then I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "python-3.6" as "filter_name"
    And I select "Module (Stream)" from "type"
    And I select "equals" from "matcher"
    And I enter "python36" as "moduleName"
    And I enter "3.6" as "moduleStream"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "python-3.6" text

  Scenario: Create a CLM project to remove AppStream metadata
    When I follow the left menu "Content Lifecycle > Projects"
    Then I should see a "Content Lifecycle Projects" text
    And I should see a "There are no entries to show." text
    When I follow "Create Project"
    And I enter "Remove AppStream metadata" as "name"
    And I enter "no-appstream-8" as "label"
    And I click on "Create"
    Then I should see a "Content Lifecycle Project - Remove AppStream metadata" text
    When I click on "Attach/Detach Sources"
    And I wait until I do not see "Loading" text
    And I select "RHEL8-Pool for x86_64" from "selectedBaseChannel"
    And I check "Custom Channel for Rocky 8 DVD"
    And I click on "Save"
    Then I should see a "Custom Channel for Rocky 8 DVD" text
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

  Scenario: Create the bootstrap repository for the Rocky 8 minion
    When I create the bootstrap repository for "rhlike_minion" on the server
