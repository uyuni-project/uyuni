# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

@sle_minion
Feature: Live Patching on a SLE Minion
  In order to check if systems are patched against certain vulnerabilities
  As an authorized user
  I want to see the Salt Minions that need to be patched

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: downgrade milkyway-dummy to lower version
    