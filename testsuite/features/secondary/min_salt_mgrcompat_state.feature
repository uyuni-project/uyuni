# Copyright (c) 2015-2023 SUSE LLC.
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in the following features:
# - features/secondary/min_salt_lock_packages.feature
# - features/secondary/min_action_chain.feature
# - features/secondary/allcli_action_chain.feature
# - features/secondary/min_recurring_action.feature
# - features/secondary/min_change_software_channel.feature
# - features/secondary/min_retracted_patches.feature
# - features/secondary/min_timezone.feature
# - features/secondary/min_move_from_and_to_proxy.feature
# If the minion fails to bootstrap again.

@skip_if_github_validation
@sle_minion
@scope_salt
Feature: Verify that Salt mgrcompat state works when the new module.run syntax is enabled

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Remove mgrcompat module from minion synced modules and schedule Hardware Refresh
    Given I remove "minion/extmods/states/mgrcompat.py" from salt cache on "sle_minion"
    And I remove "minion/extmods/states/__pycache__/mgrcompat*" from salt cache on "sle_minion"
    And I am on the Systems overview page of this "sle_minion"
    When I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    When I wait until event "Hardware List Refresh scheduled" is completed
    And I wait until there is no Salt job calling the module "hardware.profileupdate" on "sle_minion"

  Scenario: Remove saltutil grain and mgrcompat module from minion and schedule Hardware Refresh
    Given I remove "minion/extmods/states/mgrcompat.py" from salt cache on "sle_minion"
    And I remove "minion/extmods/states/__pycache__/mgrcompat*" from salt cache on "sle_minion"
    And I store "grains: {__suse_reserved_saltutil_states_support: False}" into file "custom_grains.conf" in salt minion config directory on "sle_minion"
    And I refresh salt-minion grains on "sle_minion"
    And I am on the Systems overview page of this "sle_minion"
    When I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    When I wait until event "Hardware List Refresh scheduled" is completed
    And I wait until there is no Salt job calling the module "hardware.profileupdate" on "sle_minion"

  Scenario: Delete SLES minion system profile before mgrcompat test
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on "sle_minion"
    Then "sle_minion" should not be registered

  Scenario: Enable new module.run syntax on the minion and perform registration
    Given I store "use_superseded: [module.run]" into file "custom_modulerun.conf" in salt minion config directory on "sle_minion"
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "sle_minion"

  Scenario: Check if onboarding for the minion with the new module.run syntax was successful
    When I follow the left menu "Systems > System List > All"
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
    When I wait until event "Hardware List Refresh scheduled" is completed
    And I wait until there is no Salt job calling the module "hardware.profileupdate" on "sle_minion"

  Scenario: Cleanup: Delete profile of the minion and disable new module.run syntax
    Given I am on the Systems overview page of this "sle_minion"
    And I remove "custom_modulerun.conf" from salt minion config directory on "sle_minion"
    And I remove "custom_grains.conf" from salt minion config directory on "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on "sle_minion"
    Then "sle_minion" should not be registered

  Scenario: Cleanup: bootstrap again the minion after mgrcompat tests
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "sle_minion"
