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
