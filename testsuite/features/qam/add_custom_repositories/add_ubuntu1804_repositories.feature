# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

@ubuntu1804_minion
Feature: Adding the Ubuntu 18.04 distribution custom repositories

  Scenario: Add a child channel for Ubuntu Bionic main repositories
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    When I enter "Custom Channel for ubuntu-bionic-main" as "Channel Name"
    And I enter "ubuntu-bionic-main" as "Channel Label"
    And I select the parent channel for the "ubuntu1804_minion" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for ubuntu-bionic-main created" text

  Scenario: Add a child channel for Ubuntu Bionic main updates repositories
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    When I enter "Custom Channel for ubuntu-bionic-main-updates" as "Channel Name"
    And I enter "ubuntu-bionic-main-updates" as "Channel Label"
    And I select the parent channel for the "ubuntu1804_minion" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for ubuntu-bionic-main-updates created" text

  Scenario: Add the Ubuntu Bionic main repositories
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "ubuntu-bionic-main" as "label"
    And I enter "http://archive.ubuntu.com/ubuntu/dists/bionic/main/binary-amd64/" as "url"
    And I select "deb" from "contenttype"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text
    And I should see "metadataSigned" as checked

  Scenario: Add the Ubuntu Bionic main updates repositories
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "ubuntu-bionic-main-updates" as "label"
    And I enter "http://archive.ubuntu.com/ubuntu/dists/bionic-updates/main/binary-amd64/" as "url"
    And I select "deb" from "contenttype"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text
    And I should see "metadataSigned" as checked

  Scenario: Add the repository to the custom channel for ubuntu-bionic-main
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for ubuntu-bionic-main"
    And I follow "Repositories" in the content area
    And I select the "ubuntu-bionic-main" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Add the repository to the custom channel for ubuntu-bionic-main-updates
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for ubuntu-bionic-main-updates"
    And I follow "Repositories" in the content area
    And I select the "ubuntu-bionic-main-updates" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Synchronize the repository in the custom channel for ubuntu-bionic-main
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for ubuntu-bionic-main"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text

  Scenario: Synchronize the repository in the custom channel for ubuntu-bionic-main-updates
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for ubuntu-bionic-main-updates"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text

  Scenario: The custom channel for ubuntu-bionic-main has been synced
    When I wait until the channel "ubuntu-bionic-main" has been synced

  Scenario: The custom channel for ubuntu-bionic-main-updates has been synced
    When I wait until the channel "ubuntu-bionic-main-updates" has been synced
