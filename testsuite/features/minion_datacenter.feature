# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: SUSE Manager can handle a lot of minions

  Scenario: Create dockerized minions
  Given I am authorized as "admin" with password "admin"
  Then I create dockerized minions
