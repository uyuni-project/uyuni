# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@ubuntu2404_minion
Feature: Ubuntu 24.04: Universe and Main Noble Repositories Integration
  In order to resolve dependencies like python3-rpm
  As an administrator
  I want to add and sync the Noble Universe and Main repositories

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Provision Ubuntu 24.04 Noble Child Channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Ubuntu 24.04 Noble AMD64" as "Channel Name"
    And I enter "ubuntu-2404-noble-amd64" as "Channel Label"
    And I select the parent channel for the "ubuntu2404_minion" from "Parent Channel"
    And I enter "Official Ubuntu Noble Universe and Main packages for amd64" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Ubuntu 24.04 Noble AMD64 created" text

  Scenario: Define External Noble Universe Repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "repo-ubuntu-2404-universe-amd64" as "label"
    And I enter "http://archive.ubuntu.com/ubuntu/dists/noble/universe/binary-amd64/" as "url"
    And I select "deb" from "contenttype"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Define External Noble Main Repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "repo-ubuntu-2404-main-amd64" as "label"
    And I enter "http://archive.ubuntu.com/ubuntu/dists/noble/main/binary-amd64/" as "url"
    And I select "deb" from "contenttype"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Associate Noble Repositories with Child Channel
    When I follow the left menu "Software > Manage > Channels"
    And I enter "Ubuntu 24.04 Noble AMD64" as the filtered channel name
    And I click on the filter button
    And I follow "Ubuntu 24.04 Noble AMD64"
    And I follow "Repositories" in the content area
    And I select the "repo-ubuntu-2404-universe-amd64" repo
    And I select the "repo-ubuntu-2404-main-amd64" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Perform partial sync of python3-rpm and its dependencies into the Noble channel
    And I use spacewalk-repo-sync to sync channel "ubuntu-2404-noble-amd64" including "python3-rpm librpm9t64 librpmbuild9t64 librpmio9t64 librpmsign9t64 rpm-common liblua5.3-0 libfsverity0" packages
    When I wait until the channel "ubuntu-2404-noble-amd64" has been synced

  Scenario: Verify that all synchronized channels have their dependencies solved
    When I wait until all synchronized channels have solved their dependencies
    Then all channels have been synced without errors
