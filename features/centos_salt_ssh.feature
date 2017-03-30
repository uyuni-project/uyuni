# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: CENTOS7 feature.
   1) Bootstrap a new salt host via salt-ssh
   verify functionality: script, remote cmds, packages install, channel subscription.
  2) delete the ssh-minion
  3) bootstap a ssh-minion tunneled by ssh-reverse. 
     verify functionality: script, remote cmds, packages install, channel subscription.
     verify tunnel proprety: repo 1233 port, and installation of pkg only via server.

  Scenario: Deletes centos minion
    Given no Salt packages are installed on remote "centos"
    When I am on the Systems overview page of this "ceos-minion"
    And I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I should see a "has been deleted" text
    # Hack this just wait to make the server deleting, and don't get problem beeing to fast for boostrap
    And I wait for "60" seconds

  Scenario: Bootstrap a system (centos salt-ssh managed)
    Given I am authorized
    When I follow "Salt"
    Then I should see a "Bootstrapping" text
    And I follow "Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    And I check "manageWithSSH"
    And I enter the hostname of "ceos-minion" as hostname
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    And I wait for "15" seconds
    Then I should see a "Successfully bootstrapped host! Your system should appear in System Overview shortly." text
    And I wait for "10" seconds
    And I follow "System Overview"
    Then I should see centos ssh-minion hostname as link
    And I follow centos ssh-minion hostname
    Then I should see a "Push via SSH" text

   Scenario: Subscribe centos ssh-minion to a base-channel for testing
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    And I select "Test Base Channel" from "new_base_channel_id"
    And I click on "Confirm"
    And I click on "Modify Base Software Channel"
    And I should see a "System's Base Channel has been updated." text

