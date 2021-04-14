# Copyright (c) 2017-2020 SUSE LLC
# Licensed under the terms of the MIT license.
#
# 1) delete CentOS minion and register as Centos SSH minion
# 2) run a remote command
# 3) delete CentOS SSH minion client and register as Centos minion

@scope_res
Feature: Bootstrap a SSH-managed CentOS minion and do some basic operations on it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  @centos_minion
  Scenario: Delete the CentOS minion before SSH minion tests
    When I am on the Systems overview page of this "ceos_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "ceos_minion" should not be registered

@centos_minion
  Scenario: Bootstrap a SSH-managed CentOS minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "ceos_ssh_minion" as "hostname"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I am on the System Overview page
    And I wait until I see the name of "ceos_ssh_minion", refreshing the page
    And I wait until onboarding is completed for "ceos_ssh_minion"

@proxy
@centos_minion
  Scenario: Check connection from SSH-managed CentOS minion to proxy
    Given I am on the Systems overview page of this "ceos_ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
@centos_minion
  Scenario: Check registration on proxy of SSH-managed CentOS minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos_ssh_minion" hostname

@centos_minion
  Scenario: Subscribe the SSH-managed CentOS minion to a base channel
    Given I am on the Systems overview page of this "ceos_ssh_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test Base Channel"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

@centos_minion
  Scenario: Check events history for failures on SSH-managed CentOS minion
    Given I am on the Systems overview page of this "ceos_ssh_minion"
    Then I check for failed events on history event page

@centos_minion
  Scenario: Run a remote command on the SSH-managed CentOS minion
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "cat /etc/os-release"
    And I enter target "*centos*"
    And I click on preview
    And I click on run
    Then I should see "ceos_ssh_minion" hostname
    When I wait for "15" seconds
    And I expand the results for "ceos_ssh_minion"
    Then I should see a "rhel fedora" text
    And I should see a "REDHAT_SUPPORT_PRODUCT" text

@centos_minion
  Scenario: Check events history for failures on SSH-managed CentOS minion
    Given I am on the Systems overview page of this "ceos_ssh_minion"
    Then I check for failed events on history event page

@centos_minion
  Scenario: Cleanup: delete the SSH-managed CentOS minion
    When I am on the Systems overview page of this "ceos_ssh_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "ceos_ssh_minion" should not be registered

@centos_minion
  Scenario: Cleanup: bootstrap a CentOS minion after SSH minion tests
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "ceos_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I am on the System Overview page
    And I wait until I see the name of "ceos_minion", refreshing the page
    And I wait until onboarding is completed for "ceos_minion"

@centos_minion
  Scenario: Cleanup: re-subscribe the CentOS minion to a base channel
    Given I am on the Systems overview page of this "ceos_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test Base Channel"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
