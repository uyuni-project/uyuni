# Copyright 2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Add common channels and schedule their synchronization
  # needed for external repositories that are not in SCC

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

  Scenario: Add common channels for openSUSE 15.3 ARM
    When I use spacewalk-common-channel to add channel "repo-15.3-oss-aarch64" with arch "aarch64"
    And I follow the left menu "Software > Manage > Channels"
    And I follow "openSUSE Leap 15.3 (aarch64)"
    And I follow "Repositories" in the content area
    And I select the "External - openSUSE Leap 15.3 (aarch64)" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text
    When I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for openSUSE Leap 15.3 (aarch64)" text
