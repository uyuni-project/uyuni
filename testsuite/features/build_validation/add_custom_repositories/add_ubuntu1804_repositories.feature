# Copyright (c) 2021 SUSE LLC
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

  Scenario: Add common channels for Ubuntu 18.04 main
    Given I am authorized as "admin" with password "admin"
    When I use spacewalk-common-channel to add Debian channel "ubuntu-1804-amd64-main"
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Ubuntu 18.04 LTS AMD64 Main"
    And I follow "Repositories" in the content area
    And I select the "External - Ubuntu 18.04 LTS AMD64 Main" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text
    When I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Ubuntu 18.04 LTS AMD64 Main." text

  Scenario: Add common channels for Ubuntu 18.04 updates
    Given I am authorized as "admin" with password "admin"
    When I use spacewalk-common-channel to add Debian channel "ubuntu-1804-amd64-main-updates"
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Ubuntu 18.04 LTS AMD64 Main Updates"
    And I follow "Repositories" in the content area
    And I select the "External - Ubuntu 18.04 LTS AMD64 Main Updates" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text
    When I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Ubuntu 18.04 LTS AMD64 Main Updates." text

  Scenario: Add common channels for Ubuntu 18.04 security
    Given I am authorized as "admin" with password "admin"
    When I use spacewalk-common-channel to add Debian channel "ubuntu-1804-amd64-main-security"
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Ubuntu 18.04 LTS AMD64 Main Security"
    And I follow "Repositories" in the content area
    And I select the "External - Ubuntu 18.04 LTS AMD64 Main Security" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text
    When I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Ubuntu 18.04 LTS AMD64 Main Security." text