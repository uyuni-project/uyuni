# Copyright (c) 2017 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: The system details of each minion and client provides an overview of the system

  Scenario: Traditional client grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "sle-client"
    Then I can see all system information for "sle-client"

  Scenario: Minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "sle-minion"
    Then I can see all system information for "sle-minion"

@centos_minion
  Scenario: CentOS minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "ceos-minion"
    Then I can see all system information for "ceos-minion"

@ubuntu_minion
  Scenario: Ubuntu minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "ubuntu-minion"
    Then I can see all system information for "ubuntu-minion"

@ssh_minion
  Scenario: SSH-managed minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "ssh-minion"
    Then I can see all system information for "ssh-minion"

