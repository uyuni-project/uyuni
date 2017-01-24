# Copyright (c) 2017 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Sync real channels
  This feature will sync real channels on suse-manager.
  It will take a while.
  
 Scenario: Sync sles-12sp1 channel
    Given I am authorized
    Then I sync "sle-12sp1" channel
