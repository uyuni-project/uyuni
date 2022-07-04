# Copyright (c) 2017-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
# 1) delete Debian-like minion and register as SSH minion
# 2) run a remote command
# 3) delete Debian-like SSH minion and register as normal minion

@scope_deb
@scope_salt_ssh
@deb_minion
Feature: Bootstrap a SSH-managed Debian-like minion and do some basic operations on it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete the Debian-like minion
    When I am on the Systems overview page of this "deb_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "deb_minion" should not be registered

  Scenario: Bootstrap a SSH-managed Debian-like minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "deb_minion" as "hostname"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I follow the left menu "Systems > Overview"
    And I wait until I see the name of "deb_minion", refreshing the page
    And I wait until onboarding is completed for "deb_minion"

@proxy
  Scenario: Check connection from SSH-managed Debian-like minion to proxy
    Given I am on the Systems overview page of this "deb_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of SSH-managed Debian-like minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "deb_minion" hostname

  Scenario: Subscribe the SSH-managed Debian-like minion to a base channel
    Given I am on the Systems overview page of this "deb_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-Deb-AMD64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  Scenario: Check events history for failures on SSH-managed Debian-like minion
    Given I am on the Systems overview page of this "deb_minion"
    Then I check for failed events on history event page

  Scenario: Run a remote command on the SSH-managed Debian-like minion
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "cat /etc/os-release"
    And I enter target "*ubuntu*"
    And I click on preview
    And I click on run
    Then I should see "deb_minion" hostname
    When I wait until I see "show response" text
    And I expand the results for "deb_minion"
    Then I should see a "ID=ubuntu" text

  Scenario: Check events history for failures on SSH-managed Debian-like minion
    Given I am on the Systems overview page of this "deb_minion"
    Then I check for failed events on history event page

  Scenario: Cleanup: delete the SSH-managed Debian-like minion
    When I am on the Systems overview page of this "deb_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "deb_minion" should not be registered

  Scenario: Cleanup: bootstrap a Debian-like minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "deb_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I follow the left menu "Systems > Overview"
    And I wait until I see the name of "deb_minion", refreshing the page
    And I wait until onboarding is completed for "deb_minion"

  Scenario: Cleanup: re-subscribe the Debian-like minion to a base channel
    Given I am on the Systems overview page of this "deb_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-Deb-AMD64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
