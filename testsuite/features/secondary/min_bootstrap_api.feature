# Copyright (c) 2017-2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in the following features when running in sequential:
# - features/secondary/min_bootstrap_negative.feature
# If the minion fails to bootstrap again.

@skip_if_github_validation
@scope_onboarding
Feature: Register a Salt minion via API

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Delete SLES minion system profile before API bootstrap test
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on "sle_minion"
    Then "sle_minion" should not be registered

  Scenario: Bootstrap a SLES minion via API
    When I call system.bootstrap() on host "sle_minion" and salt-ssh "disabled"

  Scenario: Check new minion bootstrapped via API in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    When I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "sle_minion", refreshing the page
    And I wait until onboarding is completed for "sle_minion"
    Then the Salt master can reach "sle_minion"

  Scenario: Check contact method of this minion
    Given I am on the Systems overview page of this "sle_minion"
    Then I should see a "Default" text

@proxy
  Scenario: Check registration on proxy of minion bootstrapped via API
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_minion" hostname

  Scenario: Check spacecmd system ID of minion bootstrapped via API
    Given I am on the Systems overview page of this "sle_minion"
    Then I run spacecmd listeventhistory for "sle_minion"

@susemanager
  Scenario: API bootstrap: subscribe to base channel
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP4-Pool for x86_64"
    And I wait until I do not see "Loading..." text
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed

@uyuni
  Scenario: API bootstrap: subscribe to base channel
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "openSUSE Leap 15.5 (x86_64)"
    And I wait until I do not see "Loading..." text
    And I check "openSUSE 15.5 non oss (x86_64)"
    And I check "openSUSE Leap 15.5 non oss Updates (x86_64)"
    And I check "openSUSE Leap 15.5 Updates (x86_64)"
    And I check "Update repository of openSUSE Leap 15.5 Backports (x86_64)"
    And I check "Update repository with updates from SUSE Linux Enterprise 15 for openSUSE Leap 15.5 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.5 (x86_64) (Development)"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed

  Scenario: Check events history for failures on SLES minion after API bootstrap
    Given I am on the Systems overview page of this "sle_minion"
    Then I check for failed events on history event page

  Scenario: Bootstrap via API a non-existing system
    When I call system.bootstrap() on unknown host, I should get an API fault

  Scenario: Bootstrap a salt-ssh system with activation key and default contact method
    When I call system.bootstrap() on a Salt minion with saltSSH = true, but with activation key with default contact method, I should get an API fault
