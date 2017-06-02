# Copyright (c) 2017 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Sync real channels
  Sync is scheduled as a taskomatic task in background. 
  
 Scenario: Sync sles-12sp2 product channels
    Given I add "sles12-sp2-pool-x86_64" channel
    And I add "sle-manager-tools12-pool-x86_64-sp2" channel
    And I add "sle-manager-tools12-updates-x86_64-sp2" channel
    And I add "sles12-sp2-updates-x86_64" channel

 Scenario: Sync containers module channels
    Given I add "sle-module-containers12-pool-x86_64-sp2" channel
    And I add "sle-module-containers12-updates-x86_64-sp2" channel

 Scenario: Sync SLES11 SP4 product channels
    Given I add "sles11-sp4-pool-x86_64" channel
      And I add "sles11-sp4-suse-manager-tools-x86_64" channel
      And I add "sles11-sp4-updates-x86_64" channel
      
 Scenario: Sync SLES12 SP1 product channels
    Given I add "sles12-sp1-pool-x86_64" channel
      And I add "sles12-sp1-suse-manager-tools-x86_64" channel
      And I add "sles12-sp1-updates-x86_64" channel 
     
 Scenario: Sync Rhel7 product channels
    Given I add "rhel-x86_64-server-7"
    And I add "res7-x86_64" channel
    And I add "res7-suse-manager-tools-x86_64"

 Scenario: Sync Rhel6 product channels
    Given I add "rhel-x86_64-server-6"
    And I add "res6-x86_64" channel
    And I add "res6-suse-manager-tools-x86_64"
    
