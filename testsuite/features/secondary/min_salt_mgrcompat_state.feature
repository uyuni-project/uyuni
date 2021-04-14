# Copyright (c) 2015-2020 SUSE LLC.
# Licensed under the terms of the MIT license.

@sle_minion
@scope_salt
Feature: Verify that Salt mgrcompat state works when the new module.run syntax is enabled

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Remove mgrcompat module from minion synced modules and schedule Hardware Refresh
    Given I remove "/var/cache/salt/minion/extmods/states/mgrcompat.py" from "sle_minion"
    And I remove "/var/cache/salt/minion/extmods/states/__pycache__/mgrcompat*" from "sle_minion"
    And I am on the Systems overview page of this "sle_minion"
    When I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled by admin" is completed

  Scenario: Remove saltutil grain and mgrcompat module from minion and schedule Hardware Refresh
    Given I remove "/var/cache/salt/minion/extmods/states/mgrcompat.py" from "sle_minion"
    And I remove "/var/cache/salt/minion/extmods/states/__pycache__/mgrcompat*" from "sle_minion"
    And I store "grains: {__suse_reserved_saltutil_states_support: False}" into file "/etc/salt/minion.d/custom_grains.conf" on "sle_minion"
    And I run "salt-call saltutil.refresh_grains" on "sle_minion"
    And I am on the Systems overview page of this "sle_minion"
    When I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled by admin" is completed

  Scenario: Delete SLES minion system profile before mgrcompat test
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_minion" should not be registered

  Scenario: Enable new module.run syntax on the minion and perform registration
    Given I store "use_superseded: [module.run]" into file "/etc/salt/minion.d/custom_modulerun.conf" on "sle_minion"
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "sle_minion"

  Scenario: Check if onboarding for the minion with the new module.run syntax was successful
    When I am on the System Overview page
    And I wait until I see the name of "sle_minion", refreshing the page
    And I wait until onboarding is completed for "sle_minion"

  Scenario: Check that installed packages are visible with the new module.run syntax
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove"
    Then I should see a "aaa_base" text

  Scenario: Check that Hardware Refresh button works on a SLE minion with new module.run syntax
    Given I am on the Systems overview page of this "sle_minion"
    And I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled by admin" is completed

  Scenario: Cleanup: Delete profile of the minion and disable new module.run syntax
    Given I am on the Systems overview page of this "sle_minion"
    And I remove "/etc/salt/minion.d/custom_modulerun.conf" from "sle_minion"
    And I remove "/etc/salt/minion.d/custom_grains.conf" from "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_minion" should not be registered

  Scenario: Cleanup: bootstrap again the minion after mgrcompat tests
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "sle_minion"

  Scenario: Cleanup: restore channels on the minion after mgrcompat tests
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
