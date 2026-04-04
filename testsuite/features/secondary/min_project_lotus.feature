# Copyright (c) 2023-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@sle_minion
@susemanager
@scope_project_lotus
Feature: Project Lotus
  In order to manage Program Temporary Fixes (PTFs)
  As an authorized user
  I want to be able to install and remove them through the WebUI

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Pre-requisite: Create custom channel for PTFs
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Custom Channel for SLES15SP7 PTFs" as "Channel Name"
    And I enter "sles15sp7-ptfs" as "Channel Label"
    And I select the parent channel for the "sle_minion" from "Parent Channel"
    And I enter "Custom channel for PTFs" as "Channel Summary"
    And I uncheck "gpg_check"
    And I click on "Create Channel"
    Then I should see a "Custom Channel for SLES15SP7 PTFs" text

  Scenario: Pre-requisite: Create custom repository for PTFs
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "sles15sp7_ptf_repo" as "label"
    And I enter "http://updates.suse.de/PTF/Release/A127499/SLES/15.7/x86_64/ptf/" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Pre-requisite: Add PTF repository to custom channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for SLES15SP7 PTFs"
    And I follow "Repositories" in the content area
    And I select the "sles15sp7_ptf_repo" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Pre-requisite: Sync PTF repository
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for SLES15SP7 PTFs"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    # no need to click on "Sync Now" as it's automatically enabled by default on Uyuni
    Then I should see a "Repository sync is running" text
    When I wait until the channel "sles15sp7-ptfs" has been synced

  Scenario: Pre-requisite: Add custom channel to minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP7-Pool for x86_64"
    And I wait until I do not see "Loading..." text
    Then radio button "SLE-Product-SLES15-SP7-Pool for x86_64" should be checked
    And I wait until I do not see "Loading..." text
    And I check "Custom Channel for SLES15SP7 PTFs"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    And I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
    Then channel "Custom Channel for SLES15SP7 PTFs" should be enabled on "sle_minion"

  Scenario: Install PTF through PTFs tab
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "PTFs" in the content area
    And I follow "Install" in the content area
    And I check "ptf-30961-3-0" in the list
    And I click on "Install PTFs"
    And I click on "Confirm"
    Then I should see a "The action has been scheduled" text
    And I wait until event "Package Install/Upgrade scheduled" is completed

  Scenario: Remove PTF through PTFs tab
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "PTFs" in the content area
    And I follow "List / Remove" in the content area
    And I check "ptf-30961-3-0" in the list
    And I click on "Remove PTFs"
    And I click on "Confirm"
    Then I should see a "The action has been scheduled" text
    And I wait until event "Package Removal scheduled" is completed

  Scenario: Install PTF through Packages tab
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Packages" in the content area
    And I follow "Install" in the content area
    And I enter "ptf-30961-3-0" as the filtered package name
    And I click on the filter button
    And I check "ptf-30961-3-0" in the list
    And I click on "Install Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until event "Package Install/Upgrade scheduled" is completed

  Scenario: Remove PTF through Packages tab
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Packages" in the content area
    And I follow "List / Remove" in the content area
    And I enter "ptf-30961-3-0" as the filtered package name
    And I click on the filter button
    And I check "ptf-30961-3-0" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled for" text
    And I wait until event "Package Removal scheduled" is completed

  Scenario: Cleanup: Delete custom channel for PTFs
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for SLES15SP7 PTFs"
    And I follow "Delete Channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Channel Custom Channel for SLES15SP7 PTFs has been deleted" text

  Scenario: Cleanup: Remove custom repository for PTFs
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "sles15sp7_ptf_repo"
    And I follow "Delete Repository"
    And I click on "Delete Repository"
    Then I should see a "Repository deleted successfully" text
