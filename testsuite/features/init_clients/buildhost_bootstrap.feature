# Copyright (c) 2016-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@buildhost
Feature: Bootstrap a Salt build host via the GUI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Update the SLES activation key
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key x86_64" in the content area
    And I wait until I see "Container Build Host" text
    And I check "container_build_host"
    And I check "osimage_build_host"
    And I click on "Update Activation Key"

  Scenario: Bootstrap a SLES build host
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "build_host" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Check the new bootstrapped build host in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    When I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "build_host", refreshing the page
    And I wait until onboarding is completed for "build_host"
    Then the Salt master can reach "build_host"

@proxy
  Scenario: Check connection from build host to proxy
    Given I am on the Systems overview page of this "build_host"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on build host of minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "build_host" hostname

  Scenario: Detect latest Salt changes on the SLES build host
    When I query latest Salt changes on "build_host"

  Scenario: Check that the build host is a build host
    Given I am on the Systems overview page of this "build_host"
    Then I should see a "[Container Build Host]" text
    Then I should see a "[OS Image Build Host]" text

  Scenario: Check events history for failures on SLES build host
    Given I am on the Systems overview page of this "build_host"
    Then I check for failed events on history event page

  Scenario: Cleanup: Restore the SLES activation key to its original state
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key x86_64" in the content area
    And I wait until I see "Container Build Host" text
    And I uncheck "container_build_host"
    And I uncheck "osimage_build_host"
    And I click on "Update Activation Key"
