# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: 1) Bootstrap a new salt host via salt-ssh
   verify functionality: script, remote cmds, packages install, channel subscription.
  2) delete the ssh-minion
  3) bootstap a ssh-minion tunneled by ssh-reverse. 
     verify functionality: script, remote cmds, packages install, channel subscription.
     verify tunnel proprety: repo 1233 port, and installation of pkg only via server.

  Scenario: No Salt Package and service are running on Minion
    Given no Salt packages are installed on remote "ssh-minion"
    And remote minion host is not registered in Spacewalk
    
  Scenario: Bootstrap a system (sles-salt-ssh managed)
    Given I am authorized
    And I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    And I check "manageWithSSH"
    And I enter remote ssh-minion hostname as "hostname"
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    And I wait for "15" seconds
    Then I wait until i see "Successfully bootstrapped host! " text
    And I wait for "10" seconds
    And I follow "System Overview"
    Then I should see remote ssh-minion hostname as link
    And I follow remote ssh-minion hostname
    Then I should see a "Push via SSH" text

   Scenario: Subscribe ssh-minion to a base-channel for testing
    Given I am authorized as "testing" with password "testing"
    And I follow "Home" in the left menu
    And I follow "Systems" in the left menu
    And I follow "Overview" in the left menu
    And I follow remote ssh-minion hostname
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    And I select "Test Base Channel" from "new_base_channel_id"
    And I click on "Confirm"
    And I click on "Modify Base Software Channel"
    And I should see a "System's Base Channel has been updated." text

   Scenario: Reboot a salt minion (ssh-managed) (sle)
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    And I click on "Reboot system"
    Then I wait and check that "ssh-minion" has rebooted

   Scenario: Install a package for ssh minion
   Given I am authorized as "testing" with password "testing"
    And I follow "Home" in the left menu
    And I follow "Systems" in the left menu
    And I follow "Overview" in the left menu
    And I follow remote ssh-minion hostname
    When I follow "Software" in the content area
    And I follow "Install"
    When I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"  
    Then I should see a "1 package install has been scheduled" text
    And I wait for "hoag-dummy-1.1-2.1" to be installed on this "ssh-minion"
