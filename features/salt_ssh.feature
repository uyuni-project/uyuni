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
    When I follow "Salt"
    Then I should see a "Bootstrapping" text
    And I follow "Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    And I check "manageWithSSH"
    And I enter remote ssh-minion hostname as "hostname"
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    And I wait for "15" seconds
    Then I should see a "Successfully bootstrapped host! Your system should appear in System Overview shortly." text
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

  Scenario: Run a remote command on ssh-minion sles
    Given I am authorized as "testing" with password "testing"
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    Then I enter command "rpm -q salt-minion"
    And I click on preview
    And I click on run
    Then I wait for "15" seconds
    And I expand the results for "ssh-minion"
    Then I should see a "package salt-minion is not installed" text

