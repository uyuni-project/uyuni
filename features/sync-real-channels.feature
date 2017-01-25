# Copyright (c) 2017 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Sync real channels
  Sync is scheduled as a taskomatic task in background. 
  
 Scenario: Sync sles-12sp1 channel
    Given I add "sles12-sp1-pool-x86_64" channel

 Scenario: Sync sles-12sp2 channel
    Given I add "sles12-sp2-pool-x86_64" channel

 Scenario: Sync sles-12sp2 channel
    Given I add "sles12-sp2-pool-x86_64" channel

 Scenario: Sync rhel-7-server channel
    Given I add "res7-x86_64" channel
