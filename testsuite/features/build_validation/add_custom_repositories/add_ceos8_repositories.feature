# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@ceos8_minion
Feature: Add the CentOS 8 distribution custom repositories
  In order to use CentOS 8 channels with Red Hat "modules"
  As a SUSE Manager administrator
  I want to filter them out to remove the modules information

  Scenario: Download the iso of CentOS 8 DVD and mount it on the server
    When I mount as "centos-8-iso" the ISO from "http://minima-mirror-bv.mgr.prv.suse.net/pub/centos/8/isos/x86_64/CentOS-8.2.2004-x86_64-dvd1.iso" in the server

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add a child channel for CentOS 8 DVD repositories
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Custom Channel for CentOS 8 DVD" as "Channel Name"
    And I enter "centos-8-iso" as "Channel Label"
    And I select "RHEL8-Pool for x86_64" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for CentOS 8 DVD created" text

  Scenario: Add the CentOS 8 Appstream DVD repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "centos-8-iso-appstream" as "label"
    And I enter "http://127.0.0.1/centos-8-iso/AppStream" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add the CentOS 8 BaseOS DVD repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "centos-8-iso-baseos" as "label"
    And I enter "http://127.0.0.1/centos-8-iso/BaseOS" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add both repositories to the custom channel for CentOS 8 DVD
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for CentOS 8 DVD"
    And I follow "Repositories" in the content area
    And I select the "centos-8-iso-appstream" repo
    And I select the "centos-8-iso-baseos" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Synchronize the repositories in the custom channel for CentOS 8 DVD
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for CentOS 8 DVD"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text

  Scenario: The custom channel for CentOS 8 has been synced
    When I wait until the channel "centos-8-iso" has been synced

  Scenario: Create CLM filters to remove AppStream metadata
    Given I am authorized for the "Admin" section
    When I follow the left menu "Content Lifecycle > Filters"
    And I follow "Create Filter"
    Then I should see a "Create a new filter" text
    When I enter "ruby-2.7" as "filter_name"
    And I select "Module (Stream)" from "type"
    And I enter "ruby" as "moduleName"
    And I enter "2.7" as "moduleStream"
    And I click on "Save"
    Then I should see a "ruby-2.7" text
    When I follow "Create Filter"
    Then I should see a "Create a new filter" text
    When I enter "python-3.6" as "filter_name"
    And I select "Module (Stream)" from "type"
    And I enter "python36" as "moduleName"
    And I enter "3.6" as "moduleStream"
    And I click on "Save"
    Then I should see a "python-3.6" text

  Scenario: Create a CLM project to remove AppStream metadata
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "Create Project"
    And I enter "Remove AppStream metadata" as "name"
    And I enter "no-appstream" as "label"
    And I click on "Create"
    Then I should see a "Content Lifecycle Project - Remove AppStream metadata" text
    When I follow "Attach/Detach Sources"
    And I select "RHEL8-Pool for x86_64" from "selectedBaseChannel"
    And I check "Custom Channel for CentOS 8 DVD"
    And I check "RES-AS-8-Updates for x86_64"
    And I click on "Save"
    Then I should see a "Custom Channel for CentOS 8 DVD" text
    When I follow "Attach/Detach Filters"
    And I check "python-3.6: enable module python36:3.6"
    And I check "ruby-2.7: enable module ruby:2.7"
    And I click on "Save"
    Then I should see a "python-3.6: enable module python36:3.6" text
    When I follow "Add Environment"
    And I enter "result" as "name"
    And I enter "result" as "label"
    And I enter "Filtered channels without AppStream channels" as "description"
    And I click on "Save"
    Then I should see a "not built" text
    When I click on "Build"
    And I enter "Initial build" as "message"
    And I click the environment build button
    Then I should see a "Version 1: Initial build" text
