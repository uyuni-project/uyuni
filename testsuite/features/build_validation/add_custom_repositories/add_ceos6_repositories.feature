# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@ceos6_minion
Feature: Adding the CentOS 6 distribution custom repositories

  Scenario: Download the iso of CentOS 6 DVD and mount it on the server
    When I mount as "centos-6-iso" the ISO from "http://minima-mirror-bv.mgr.prv.suse.net/pub/centos/6.10/isos/x86_64/CentOS-6.10-x86_64-bin-DVD1.iso" in the server

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add a child channel for CentOS 6 DVD repositories
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    When I enter "Custom Channel for CentOS 6 DVD" as "Channel Name"
    And I enter "centos-6-iso" as "Channel Label"
    And I select the parent channel for the "ceos6_minion" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for CentOS 6 DVD created" text

  Scenario: Add the CentOS 6 DVD repositories
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "centos-6-iso" as "label"
    And I enter "http://127.0.0.1/centos-6-iso" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add the repository to the custom channel for CentOS 6 DVD
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for CentOS 6 DVD"
    And I follow "Repositories" in the content area
    And I select the "centos-6-iso" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Synchronize the repository in the custom channel for CentOS 6 DVD
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for CentOS 6 DVD"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text
