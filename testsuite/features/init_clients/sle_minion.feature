# Copyright (c) 2016-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to bootstrap a Salt minion via the GUI

  Scenario: Create the bootstrap repository for a Salt client
     Given I am authorized
     And I create the "x86_64" bootstrap repository for "sle_minion" on the server

  Scenario: Bootstrap a SLES minion
     Given I am authorized
     When I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle_minion" as "hostname"
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I select the hostname of "proxy" from "proxies"
     And I click on "Bootstrap"
     And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Check the new bootstrapped minion in System Overview page
    Given I am authorized
    When I go to the minion onboarding page
    Then I should see a "accepted" text
    When I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "sle_minion", refreshing the page
    And I wait until onboarding is completed for "sle_minion"
    Then the Salt master can reach "sle_minion"

@proxy
  Scenario: Check connection from minion to proxy
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_minion" hostname

  Scenario: Subscribe the SLES minion to a base channel
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  # bsc#1085436 - Apache returns 403 Forbidden after a zypper refresh on minion
  Scenario: Check the new channel is working
    When I refresh the metadata for "sle_minion"

  Scenario: Detect latest Salt changes on the SLES minion
    When I query latest Salt changes on "sle_minion"


  Scenario: Turn the SLES minion into a container build host
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "container_build_host"
    And I click on "Update Properties"
    Then I should see a "Container Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

  Scenario: Turn the SLES minion into a OS image build host
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "osimage_build_host"
    And I click on "Update Properties"
    Then I should see a "OS Image Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

  Scenario: Apply the highstate to build host
    Given I am on the Systems overview page of this "sle_minion"
    When I wait until no Salt job is running on "sle_minion"
    And I enable repositories before installing Docker
    And I apply highstate on "sle_minion"
    And I wait until "docker" service is active on "sle_minion"
    And I wait until file "/var/lib/Kiwi/repo/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm" exists on "sle_minion"
    And I disable repositories after installing Docker

  Scenario: Check that the minion is now a build host
    Given I am on the Systems overview page of this "sle_minion"
    Then I should see a "[Container Build Host]" text
    Then I should see a "[OS Image Build Host]" text

  Scenario: Check events history for failures on SLES minion
    Given I am on the Systems overview page of this "sle_minion"
    Then I check for failed events on history event page
