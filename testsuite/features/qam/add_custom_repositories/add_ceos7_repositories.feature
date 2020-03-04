# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

@ceos7_minion
Feature: Adding the CentOS 7 distribution custom repositories

  Scenario: Download the iso of CentOS 7 DVD and mount it on the server
    When I mount as "centos-7-iso" the ISO from "http://schnell.suse.de/CentOS/CentOS-7.0-1406-x86_64-Minimal.iso" in the server

  Scenario: Add a child channel for CentOS 7 DVD repositories
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    When I enter "Custom Channel for CentOS 7 DVD" as "Channel Name"
    And I enter "centos-7-iso" as "Channel Label"
    And I select the parent channel for the "ceos7_minion" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for CentOS 7 DVD created" text

  Scenario: Add the CentOS 7 DVD repositories
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "centos-7-iso" as "label"
    And I enter "https://127.0.0.1/pub/centos-7-iso" as "url"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text
    And I should see "metadataSigned" as checked

  Scenario: Add the repository to the Custom Channel for <label>
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for CentOS 7 DVD"
    And I follow "Repositories" in the content area
    And I select the "centos-7-iso" repo
    And I click on "Update Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Synchronize the repository in the Custom Channel for <label>
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for CentOS 7 DVD"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text
