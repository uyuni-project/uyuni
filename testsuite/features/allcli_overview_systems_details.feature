# Copyright (c) 2017 SUSE LLC.
# Licensed under the terms of the MIT license.
#
#  1) test that the salt minion grains are shown on the details page
#  2) test that the salt minion (SSH-managed) grains are shown on the details page
#  3) test that the Centos salt-minion (ssh-managed) grains are visualized on the details page
#  4) test trad-client property are visualized on the details page

Feature: The system details of each minion and client provides an overview of the system

  Scenario: Minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "sle-minion"
    Then I can see all system information for "sle-minion"

  Scenario: SSH-managed minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "ssh-minion"
    Then I can see all system information for "ssh-minion"

@centosminion
  Scenario: CentOS minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "ceos-minion"
    Then I can see all system information for "ceos-minion"

  Scenario: Traditional client grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "sle-client"
    Then I can see all system information for "sle-client"

