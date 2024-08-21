# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@rocky8_minion
Feature: Add the Rocky 8 distribution custom repositories
  In order to use Rocky 8 channels with Red Hat "modules"
  As a SUSE Manager administrator
  I want to filter them out to remove the modules information

  Scenario: Download the iso of Rocky 8 DVD and mount it on the server
    When I mount as "rocky-8-iso" the ISO from "http://mirror.chpc.utah.edu/pub/rocky/8/isos/x86_64/Rocky-x86_64-dvd.iso" in the server, validating its checksum

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

@susemanager
  Scenario: Add a child channel for Rocky 8 DVD repositories
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Custom Channel for Rocky 8 DVD" as "Channel Name"
    And I enter "rocky-8-iso" as "Channel Label"
    And I select "rockylinux-8 for x86_64" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for Rocky 8 DVD created" text

@uyuni
  Scenario: Add a child channel for Rocky 8 DVD repositories
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Custom Channel for Rocky 8 DVD" as "Channel Name"
    And I enter "rocky-8-iso" as "Channel Label"
    And I select "Rocky Linux 8 (x86_64)" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for Rocky 8 DVD created" text

  Scenario: Add the Rocky 8 Appstream DVD repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "rocky-8-iso-appstream" as "label"
    And I enter "file:///srv/www/htdocs/pub/rocky-8-iso/AppStream" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add the Rocky 8 BaseOS DVD repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "rocky-8-iso-baseos" as "label"
    And I enter "file:///srv/www/htdocs/pub/rocky-8-iso/BaseOS" as "url"
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
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for Rocky 8 DVD"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait until I do not see "Repository sync is running" text, refreshing the page
    And I wait until button "Sync Now" becomes enabled
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text

  Scenario: The custom channel for Rocky 8 has been synced
    When I wait until the channel "rocky-8-iso" has been synced

  Scenario: Verify the CLM filters we need for Rocky 8 exist
    When I follow the left menu "Content Lifecycle > Filters"
    Then I should see a "ruby-2.7" text
    And I should see a "python-3.6" text

@susemanager
  Scenario: Create a CLM project to remove AppStream metadata from Rocky 8
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "Create Project"
    And I enter "Remove AppStream metadata from Rocky 8" as "name"
    And I enter "no-appstream-8" as "label"
    And I click on "Create"
    Then I should see a "Content Lifecycle Project - Remove AppStream metadata from Rocky 8" text
    When I click on "Attach/Detach Sources"
    And I wait until I do not see "Loading" text
    And I select "rockylinux-8 for x86_64" from "selectedBaseChannel"
    And I check "Custom Channel for Rocky 8 DVD"
    And I check "Custom Channel for rocky8_minion"
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

@uyuni
  Scenario: Create a CLM project to remove AppStream metadata from Rocky 8
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "Create Project"
    And I enter "Remove AppStream metadata from Rocky 8" as "name"
    And I enter "no-appstream-8" as "label"
    And I click on "Create"
    Then I should see a "Content Lifecycle Project - Remove AppStream metadata from Rocky 8" text
    When I click on "Attach/Detach Sources"
    And I wait until I do not see "Loading" text
    And I select "Rocky Linux 8 (x86_64)" from "selectedBaseChannel"
    And I check "Uyuni Client Tools for Rocky Linux 8 (x86_64)"
    And I check "Rocky Linux 8 - AppStream (x86_64)"
    And I check "Custom Channel for Rocky 8 DVD"
    And I check "Custom Channel for rocky8_minion"
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
