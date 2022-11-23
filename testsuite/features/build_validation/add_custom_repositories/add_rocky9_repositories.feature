# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

@rocky9_minion
Feature: Add the Rocky 9 distribution custom repositories
  In order to use Rocky 9 channels with Red Hat "modules"
  As a SUSE Manager administrator
  I want to filter them out to remove the modules information

  Scenario: Download the iso of Rocky 9 DVD and mount it on the server
    When I mount as "rocky-9-iso" the ISO from "http://minima-mirror-bv.mgr.prv.suse.net/pub/rocky/9/isos/x86_64/Rocky-x86_64-dvd.iso" in the server

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add a child channel for Rocky 9 DVD repositories
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Custom Channel for Rocky 9 DVD" as "Channel Name"
    # TODO: the EL9 products are not ready yet

  Scenario: Add the Rocky 9 Appstream DVD repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "rocky-9-iso-appstream" as "label"
    And I enter "http://127.0.0.1/rocky-9-iso/AppStream" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add the Rocky 9 BaseOS DVD repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "rocky-9-iso-baseos" as "label"
    And I enter "http://127.0.0.1/rocky-9-iso/BaseOS" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add both repositories to the custom channel for Rocky 9 DVD
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for Rocky 9 DVD"
    And I follow "Repositories" in the content area
    And I select the "rocky-9-iso-appstream" repo
    And I select the "rocky-9-iso-baseos" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Synchronize the repositories in the custom channel for Rocky 9 DVD
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for Rocky 9 DVD"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text

  Scenario: The custom channel for Rocky 9 has been synced
    When I wait until the channel "rocky-9-iso" has been synced

  Scenario: Create CLM filters to remove AppStream metadata
    Given I am authorized for the "Admin" section
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    And I enter "ruby-2.7" as "filter_name"
    And I select "Module (Stream)" from "type"
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
    And I enter "python36" as "moduleName"
    And I enter "3.6" as "moduleStream"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "python-3.6" text

  Scenario: Create a CLM project to remove AppStream metadata
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "Create Project"
    And I enter "Remove AppStream metadata" as "name"
    And I enter "no-appstream" as "label"
    And I click on "Create"
    Then I should see a "Content Lifecycle Project - Remove AppStream metadata" text
    When I click on "Attach/Detach Sources"
    # TODO: the EL9 products are not ready yet
