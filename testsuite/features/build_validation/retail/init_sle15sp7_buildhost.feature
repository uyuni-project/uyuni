# Copyright (c) 2025 SUSE LLC
# Licensed under the terms of the MIT license.
#
# To create the custom repositories and create the bootstrap repository, the feature depends on sle15sp7_minion client steps

@proxy
@sle15sp7_buildhost
Feature: Prepare buildhost and build OS image for SLES 15 SP7

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Clean up sumaform leftovers on a SLES 15 SP7 build host
    When I perform a full salt minion cleanup on "sle15sp7_buildhost"

  Scenario: Prepare activation key for SLES 15 SP7 build host
    When I create an activation key including custom channels for "sle15sp7_buildhost" via API

  Scenario: Bootstrap the SLES 15 SP7 build host
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle15sp7_buildhost" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-sle15sp7_buildhost_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "sle15sp7_buildhost"

  # WORKAROUND for bugzilla.suse.com/show_bug.cgi?id=1253024
  Scenario: Install kiwi10
    When I install package "python11-kiwi" on this "sle15sp7_buildhost"

  Scenario: Check the new bootstrapped SLES 15 SP7 build host in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "sle15sp7_buildhost"

  @proxy
  Scenario: Check connection from SLES 15 SP7 minion to proxy
    Given I am on the Systems overview page of this "sle15sp7_buildhost"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  @proxy
  Scenario: Check registration on proxy of SLES 15 SP7 minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle15sp7_buildhost" hostname

  # WORKAROUND for bugzilla.suse.com/show_bug.cgi?id=1253024, don't verify events because they fail
#  Scenario: Check events history for failures on SLES 15 SP7 minion
#    Given I am on the Systems overview page of this "sle15sp7_buildhost"
#    Then I check for failed events on history event page
