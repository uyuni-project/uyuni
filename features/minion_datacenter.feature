# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature:  Tests Suse-manager with a lot of minions

  Scenario: Create minions dockerized
  Given I am authorized as "admin" with password "admin"
  Then I create dockerized minions
