# Copyright (c) 2026 SUSE LLC
# SPDX-License-Identifier: MIT

@debian13_minion
Feature: Debian 13: Main Trixie Repository Integration
  In order to install the Prometheus exporters shipped by the distribution
  As an administrator
  I want to add and sync the Trixie Main repository

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Provision Debian 13 Trixie Child Channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Debian 13 Trixie AMD64" as "Channel Name"
    And I enter "debian-13-trixie-amd64" as "Channel Label"
    And I select the parent channel for the "debian13_minion" from "Parent Channel"
    And I enter "Official Debian Trixie Main packages for amd64" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Debian 13 Trixie AMD64 created" text

  Scenario: Define External Trixie Main Repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "repo-debian-13-main-amd64" as "label"
    And I enter "http://deb.debian.org/debian/dists/trixie/main/binary-amd64/" as "url"
    And I select "deb" from "contenttype"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Associate Trixie Repository with Child Channel
    When I follow the left menu "Software > Manage > Channels"
    And I enter "Debian 13 Trixie AMD64" as the filtered channel name
    And I click on the filter button
    And I follow "Debian 13 Trixie AMD64"
    And I follow "Repositories" in the content area
    And I select the "repo-debian-13-main-amd64" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Perform partial sync of the distro Prometheus exporters and their dependencies into the Trixie channel
    And I use spacewalk-repo-sync to sync channel "debian-13-trixie-amd64" including "prometheus-node-exporter prometheus-node-exporter-collectors moreutils python3-prometheus-client libipc-run-perl libio-pty-perl libtime-duration-perl libtimedate-perl" packages
    When I wait until the channel "debian-13-trixie-amd64" has been synced
    Then the channel "debian-13-trixie-amd64" should not be empty

  Scenario: Verify that all synchronized channels have their dependencies solved
    When I wait until all synchronized channels have solved their dependencies
    Then all channels have been synced without errors
