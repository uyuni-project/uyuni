# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rhel9_minion
Feature: Add the RHEL 9 base OS custom repositories

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add a child channel for the RHEL 9 base OS packages
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Custom Channel for RHEL 9 base OS packages" as "Channel Name"
    And I enter "rhel9-ubi-9" as "Channel Label"
    And I select the parent channel for the "rhel9_minion" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for RHEL 9 base OS packages created" text

  Scenario: Add the UBI 9 BaseOS repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "rhel9-ubi-9-baseos" as "label"
    And I enter "https://cdn-ubi.redhat.com/content/public/ubi/dist/ubi9/9/x86_64/baseos/os" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add the UBI 9 AppStream repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "rhel9-ubi-9-appstream" as "label"
    And I enter "https://cdn-ubi.redhat.com/content/public/ubi/dist/ubi9/9/x86_64/appstream/os" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add the repositories to the custom channel for the RHEL 9 base OS packages
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for RHEL 9 base OS packages"
    And I follow "Repositories" in the content area
    And I select the "rhel9-ubi-9-baseos" repo
    And I select the "rhel9-ubi-9-appstream" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Synchronize the repositories in the custom channel for the RHEL 9 base OS packages
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for RHEL 9 base OS packages"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait until I do not see "Repository sync is running" text, refreshing the page
    And I wait until button "Sync Now" becomes enabled
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text

  Scenario: The custom channel for the RHEL 9 base OS packages has been synced
    When I wait until the channel "rhel9-ubi-9" has been synced
