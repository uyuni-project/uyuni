# Copyright (c) 2017 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Test the systems overview page for minions and client
  1) test salt-minion grains are visualized on the details page
  2) test salt-minion (ssh-managed) grains are visualized on the details page
  3) test centos-salt-minion (ssh-managed) grains are visualized on the details page
  4) test trad-client property are visualized on the details page

  Scenario: salt-minion grains are displayed correctly on the details page
  Given I am on the Systems overview page of this "sle-minion"
  Then I can see all system information for "sle-minion"

  Scenario: salt-minion (ssh-managed) grains are displayed correctly on the details page
  Given I am on the Systems overview page of this "ssh-minion"
  Then I can see all system information for "ssh-minion"

  Scenario: centos minion (ssh-managed) grains are displayed correctly on the details page
  Given I am on the Systems overview page of this "ceos-minion"
  Then I can see all system information for "ceos-minion"

  Scenario: trad-client grains are displayed correctly on the details page
  Given I am on the Systems overview page of this "sle-client"
  Then I can see all system information for "sle-client"
