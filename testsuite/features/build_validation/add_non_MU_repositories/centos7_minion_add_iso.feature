# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@centos7_minion
Feature: Add the CentOS 7 distribution custom repositories

  Scenario: Download the iso of CentOS 7 DVD and mount it on the server
    When I mount as "centos-7-iso" the ISO from "http://mirror.chpc.utah.edu/pub/centos/7/isos/x86_64/CentOS-7-x86_64-DVD-2009.iso" in the server, validating its checksum

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add a child channel for CentOS 7 DVD repositories
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Custom Channel for CentOS 7 DVD" as "Channel Name"
    And I enter "centos-7-iso" as "Channel Label"
    And I select the parent channel for the "centos7_minion" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for CentOS 7 DVD created" text

  Scenario: Add the CentOS 7 DVD repositories
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "centos-7-iso" as "label"
    And I enter "file:///srv/www/htdocs/pub/centos-7-iso" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add the repository to the custom channel for CentOS 7 DVD
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for CentOS 7 DVD"
    And I follow "Repositories" in the content area
    And I select the "centos-7-iso" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Synchronize the repository in the custom channel for CentOS 7 DVD
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for CentOS 7 DVD"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait until button "Sync Now" becomes enabled
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text

  Scenario: The custom channel for Centos 7 DVD has been synced
    When I wait until the channel "centos-7-iso" has been synced
