# Copyright (c) 2019-2021 SUSE LLC
# Licensed under the terms of the MIT license.
#
#
# 1) delete SLES minion and register again with bootstrap script
# 2) subscribe minion to a base channels
# 3) install and remove a package

@sle_minion
@scope_onboarding
Feature: Register a Salt minion via Bootstrap-script

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete SLES minion system profile before script bootstrap test
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_minion" should not be registered

  Scenario: Bootstrap the minion using the script
    When I bootstrap minion client "sle_minion" using bootstrap script with activation key "1-SUSE-KEY-x86_64" from the proxy
    And I wait at most 10 seconds until Salt master sees "sle_minion" as "unaccepted"
    And I accept "sle_minion" key in the Salt master
    Then I should see "sle_minion" via spacecmd

  Scenario: Check if onboarding for the script-bootstrapped minion was successful
    When I am on the System Overview page
    And I wait until I see the name of "sle_minion", refreshing the page
    And I wait until onboarding is completed for "sle_minion"

  Scenario: Detect latest Salt changes on the script-bootstrapped SLES minion
    When I query latest Salt changes on "sle_minion"

  Scenario: Check the activation key
    Given I am on the Systems overview page of this "sle_minion"
    Then I should see a "1-SUSE-KEY-x86_64" text

  Scenario: Subscribe the script-bootstrapped SLES minion to a base channel
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

  Scenario: Install a package to the script-bootstrapped SLES minion
   Given I am on the Systems overview page of this "sle_minion"
   When I follow "Software" in the content area
   And I follow "Install"
   And I check row with "orion-dummy" and arch of "sle_minion"
   And I click on "Install Selected Packages"
   And I click on "Confirm"
   Then I should see a "1 package install has been scheduled for" text
   When I wait until event "Package Install/Upgrade scheduled by admin" is completed
   Then "orion-dummy-1.1-1.1" should be installed on "sle_minion"

  Scenario: Run a remote command on normal SLES minion
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "file /tmp"
    And I click on preview
    Then I should see "sle_minion" hostname
    And I wait until I do not see "pending" text
    When I click on run
    And I wait until I do not see "pending" text
    And I expand the results for "sle_minion"
    Then I should see "/tmp: sticky, directory" in the command output for "sle_minion"

  Scenario: Check spacecmd system ID of bootstrapped minion
    Given I am on the Systems overview page of this "sle_minion"
    Then I run spacecmd listevents for "sle_minion"

  Scenario: Cleanup: remove package from script-bootstrapped SLES minion
   When I remove package "orion-dummy-1.1-1.1" from this "sle_minion"
   Then "orion-dummy-1.1-1.1" should not be installed on "sle_minion"
