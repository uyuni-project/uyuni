# Copyright 2021-2023 SUSE LLC
# Licensed under the terms of the MIT license.

@ubuntu1804_minion
Feature: Add Ubuntu common channels and schedule their synchronization
  In order to use external repositories for Ubuntu that are not in SCC
  As an authorized user
  I want to declare channels with these repositories and synchronize them

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add common channels for Ubuntu 18.04 main
    When I use spacewalk-common-channel to add channel "ubuntu-1804-amd64-main" with arch "amd64-deb"
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
    When I use spacewalk-common-channel to add channel "ubuntu-1804-amd64-main-updates" with arch "amd64-deb"
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
    When I use spacewalk-common-channel to add channel "ubuntu-1804-amd64-main-security" with arch "amd64-deb"
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Ubuntu 18.04 LTS AMD64 Main Security"
    And I follow "Repositories" in the content area
    And I select the "External - Ubuntu 18.04 LTS AMD64 Main Security" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text
    When I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Ubuntu 18.04 LTS AMD64 Main Security." text

  # No common channels for Ubuntu 20.04
