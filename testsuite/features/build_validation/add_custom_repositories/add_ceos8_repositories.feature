# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@ceos8_minion
Feature: Adding the CentOS 8 distribution custom repositories

  Scenario: Download the iso of CentOS 8 DVD and mount it on the server
    When I mount as "centos-8-iso" the ISO from "http://minima-mirror-bv.mgr.prv.suse.net/pub/centos/8/isos/x86_64/CentOS-8.2.2004-x86_64-dvd1.iso" in the server

  Scenario: Add a child channel for CentOS 8 DVD repositories
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    When I enter "Custom Channel for CentOS 8 DVD" as "Channel Name"
    And I enter "centos-8-iso" as "Channel Label"
    And I select the parent channel for the "ceos8_minion" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for CentOS 8 DVD created" text

  Scenario: Add the CentOS 8 DVD repositories
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "centos-8-iso" as "label"
    And I enter "http://127.0.0.1/centos-8-iso" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add the repository to the custom channel for CentOS 8 DVD
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for CentOS 8 DVD"
    And I follow "Repositories" in the content area
    And I select the "centos-8-iso" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Synchronize the repository in the custom channel for CentOS 8 DVD
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for CentOS 8 DVD"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text

  Scenario: The custom channel for CentOS 8 has been synced
    When I wait until the channel "centos-8-iso" has been synced
