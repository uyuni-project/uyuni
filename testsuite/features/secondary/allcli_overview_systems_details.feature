# Copyright (c) 2017-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: The system details of each minion and client provides an overview of the system

  Scenario: Traditional client grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "sle_client"
    Then I can see all system information for "sle_client"

  Scenario: Minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "sle_minion"
    Then I can see all system information for "sle_minion"

@centos_minion
  Scenario: CentOS minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "ceos_minion"
    Then I can see all system information for "ceos_minion"

@ubuntu_minion
  Scenario: Ubuntu minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "ubuntu_minion"
    Then I can see all system information for "ubuntu_minion"

@ssh_minion
  Scenario: SSH-managed minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "ssh_minion"
    Then I can see all system information for "ssh_minion"

