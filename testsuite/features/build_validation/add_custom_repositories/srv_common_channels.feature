# Copyright 2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Add common channels and schedule their synchronization
  # needed for external repositories that are not in SCC

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add common channel for Ubuntu 16.04 main
    When I use spacewalk-common-channel to add Debian channel "ubuntu-1604-amd64-main"
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Ubuntu 16.04 LTS AMD64 Main"
    And I follow "Repositories" in the content area
    And I select the "External - Ubuntu 16.04 LTS AMD64 Main" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text
    When I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Ubuntu 16.04 LTS AMD64 Main." text

  Scenario: Add common channel for Ubuntu 16.04 updates
    When I use spacewalk-common-channel to add Debian channel "ubuntu-1604-amd64-updates"
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Ubuntu 16.04 LTS AMD64 Main Updates"
    And I follow "Repositories" in the content area
    And I select the "External - Ubuntu 16.04 LTS AMD64 Main Updates" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text
    When I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Ubuntu 16.04 LTS AMD64 Main Updates." text

  Scenario: Add common channel for Ubuntu 16.04 security
    When I use spacewalk-common-channel to add Debian channel "ubuntu-1604-amd64-security"
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Ubuntu 16.04 LTS AMD64 Security"
    And I follow "Repositories" in the content area
    And I select the "External - Ubuntu 16.04 LTS AMD64 Security" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text
    When I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Ubuntu 16.04 LTS AMD64 Security." text

  Scenario: Add common channels for Ubuntu 18.04 main
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

  # No common channels for Ubuntu 20.04
