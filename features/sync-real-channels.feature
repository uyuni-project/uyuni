# Copyright (c) 2017 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Sync real channels
  This feature will sync real channels on suse-manager.
  It will take a while.
  
 Scenario: Sync sles-12sp1 channel
    # TODO: implement the step
    # echo -e "admin\nadmin\n" | mgr-sync add channel sles12-sp2-pool-x86_64
    Given I add "sle-12sp1" channel #FIXME implement this
    Then I sync "sle-12sp1" channel
# FIXME, impelemt
 Scenario: Sync sles-12sp2 channel
    # TODO: implement the step
    # echo -e "admin\nadmin\n" | mgr-sync add channel sles12-sp2-pool-x86_64
    Given I add "sle-12sp2" channel
    Then I sync "sle-12sp2" channel

#  todo sync this ones:
# [ ] RHEL x86_64 Server 5 RES 5 [rhel-x86_64-server-5]
# [ ] RHEL x86_64 Server 6 Red Hat Expanded Support for RHEL 6 [rhel-x86_64-server-6]
# [ ] RHEL x86_64 Server 7 Red Hat Expanded Support for RHEL 7 [rhel-x86_64-server-7]

