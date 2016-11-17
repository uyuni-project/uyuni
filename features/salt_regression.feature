# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: register a salt-minion via bootstrap

  #FIXME: This scenario should have 2 minion.
  Scenario: BUG 993209: Manager Hangs if one of  registered salt-minion is down
    Given I am on the Systems overview page of this minion
