# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@ceos7_minion
Feature: Adding the CentOS 7 distribution custom repositories

  Scenario: Download the iso of CentOS 7 DVD and mount it on the server
    When I mount as "centos-7-iso" the ISO from "http://minima-mirror-bv.mgr.prv.suse.net/pub/centos/7/isos/x86_64/CentOS-7-x86_64-DVD-2003.iso" in the server

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add a child channel for CentOS 7 DVD repositories
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    When I enter "Custom Channel for CentOS 7 DVD" as "Channel Name"
    And I enter "centos-7-iso" as "Channel Label"
    And I select the parent channel for the "ceos7_minion" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for CentOS 7 DVD created" text

  Scenario: Add the CentOS 7 DVD repositories
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "centos-7-iso" as "label"
    And I enter "http://127.0.0.1/centos-7-iso" as "url"
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
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text
