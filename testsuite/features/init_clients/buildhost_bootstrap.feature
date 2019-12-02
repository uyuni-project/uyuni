# Copyright (c) 2016-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to bootstrap a Salt build host via the GUI

@buildhost
  Scenario: Create the bootstrap repository for a Salt client build host
     Given I am authorized with the feature's user
     When I create the "x86_64" bootstrap repository for "build_host" on the server

@buildhost
  Scenario: Bootstrap a SLES build host
     Given I am authorized with the feature's user
     When I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "build_host" as "hostname"
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I select the hostname of "proxy" from "proxies"
     And I click on "Bootstrap"
     And I wait until I see "Successfully bootstrapped host!" text

@buildhost
  Scenario: Check the new bootstrapped build host in System Overview page
    Given I am authorized with the feature's user
    When I go to the minion onboarding page
    Then I should see a "accepted" text
    When I am on the System Overview page
    And I wait until I see the name of "build_host", refreshing the page
    And I wait until onboarding is completed for "build_host"
    Then the Salt master can reach "build_host"

@proxy
@buildhost
  Scenario: Check connection from build host to proxy
    Given I am on the Systems overview page of this "build_host"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
@buildhost
  Scenario: Check registration on build host of minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "build_host" hostname

@buildhost
  Scenario: Detect latest Salt changes on the SLES build host
    When I query latest Salt changes on "build_host"

@buildhost
  Scenario: Turn the SLES build host into a container build host
    Given I am on the Systems overview page of this "build_host"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "container_build_host"
    And I click on "Update Properties"
    Then I should see a "Container Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

@buildhost
  Scenario: Turn the SLES build host into a OS image build host
    Given I am on the Systems overview page of this "build_host"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "osimage_build_host"
    And I click on "Update Properties"
    Then I should see a "OS Image Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

@buildhost
  Scenario: Apply the highstate to the build host
    Given I am on the Systems overview page of this "build_host"
    When I wait until no Salt job is running on "build_host"
    And I enable repositories before installing Docker
    And I apply highstate on "build_host"
    And I wait until "docker" service is active on "build_host"
    And I wait until file "/var/lib/Kiwi/repo/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm" exists on "build_host"
    And I disable repositories after installing Docker

@buildhost
  Scenario: Check that the build host is now a build host
    Given I am on the Systems overview page of this "build_host"
    Then I should see a "[Container Build Host]" text
    Then I should see a "[OS Image Build Host]" text

@buildhost
  Scenario: Check events history for failures on SLES build host
    Given I am on the Systems overview page of this "build_host"
    Then I check for failed events on history event page
