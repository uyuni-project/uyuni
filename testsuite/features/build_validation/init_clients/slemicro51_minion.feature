# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.

# Beware: After altering the system e.g. package installation/removal, the system
#         has to be rebooted to take effect due to the nature of SLE Micro.

@slemicro51_minion
Feature: Bootstrap a SLE Micro 5.1 Salt minion

  Scenario: Clean up sumaform leftovers on a SLE Micro 5.1 minion
    When I perform a full salt minion cleanup on "slemicro51_minion"
    And I reboot the "slemicro51_minion" host through SSH, waiting until it comes back

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a SLE Micro 5.1 minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "slemicro51_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-slemicro51_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text

  # Following the bootstrapping process, automatic booting is disabled.
  # This change was implemented due to intermittent errors with automatic reboots, which could occur before Salt could relay the results of applying the bootstrap salt state.
  Scenario: Reboot the SLE Micro 5.1 minion through SSH
    When I reboot the "slemicro51_minion" host through SSH, waiting until it comes back

  Scenario: Check the new bootstrapped SLE Micro 5.1 minion in System Overview page
    When I wait until onboarding is completed for "slemicro51_minion"
    And I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "slemicro51_minion"

  # 2 reboots are necessary after bootstrapping
  Scenario: Reboot the SLE Micro 5.1 minion and wait until reboot is completed
    Given I am on the Systems overview page of this "slemicro51_minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text

@proxy
  Scenario: Check connection from SLE Micro 5.1 minion to proxy
    Given I am on the Systems overview page of this "slemicro51_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of SLE Micro 5.1 minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "slemicro51_minion" hostname

  Scenario: Check events history for failures on SLE Micro 5.1 minion
    Given I am on the Systems overview page of this "slemicro51_minion"
    Then I check for failed events on history event page
