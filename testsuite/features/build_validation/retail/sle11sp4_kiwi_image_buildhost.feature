# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Bootstrap a SLES 11 SP4 Salt build host via the GUI

  Scenario: Create the bootstrap repository for a SLES 11 SP4 build host
     Given I am authorized as "admin" with password "admin"
     When I create the bootstrap repository for "sle11sp4_buildhost" on the server

  Scenario: Bootstrap a SLES 11 SP4 build host
     Given I am authorized as "admin" with password "admin"
     When I follow the left menu "Systems > Bootstrapping"
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle11sp4_buildhost" as "hostname"
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I select the hostname of "proxy" from "proxies"
     And I click on "Bootstrap"
     And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Check the new bootstrapped SLES 11 SP4 build host in System Overview page
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    When I am on the System Overview page
    And I wait until I see the name of "sle11sp4_buildhost", refreshing the page
    And I wait until onboarding is completed for "sle11sp4_buildhost"
    Then the Salt master can reach "sle11sp4_buildhost"

  Scenario: Check connection from SLES 11 SP4 build host to proxy
    Given I am on the Systems overview page of this "sle11sp4_buildhost"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  Scenario: Check registration on SLES 11 SP4 build host of minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle11sp4_buildhost" hostname

  Scenario: Detect latest Salt changes on the SLES 11 SP4 build host
    When I query latest Salt changes on "sle11sp4_buildhost"

  Scenario: Turn the SLES 11 SP4 build host into a OS image build host
    Given I am on the Systems overview page of this "sle11sp4_buildhost"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "osimage_build_host"
    And I click on "Update Properties"
    Then I should see a "OS Image Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

  Scenario: Apply the highstate to SLES 11 SP4 build host
    Given I am on the Systems overview page of this "sle11sp4_buildhost"
    When I wait until no Salt job is running on "sle11sp4_buildhost"
    And I apply highstate on "sle11sp4_buildhost"
    And I wait until event "Apply highstate scheduled by admin" is completed

  Scenario: Check that SLES 11 SP4 build host is now a build host
    Given I am on the Systems overview page of this "sle11sp4_buildhost"
    Then I should see a "[OS Image Build Host]" text

  Scenario: Check events history for failures on SLES 11 SP4 build host
    Given I am on the Systems overview page of this "sle11sp4_buildhost"
    Then I check for failed events on history event page
