# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: CENTOS7 feature.
   1) Bootstrap a new salt host via salt-ssh
   verify functionality: script, remote cmds, packages install, channel subscription.
  2) delete the ssh-minion
  3) bootstap a ssh-minion tunneled by ssh-reverse. 
     verify functionality: script, remote cmds, packages install, channel subscription.
     verify tunnel proprety: repo 1233 port, and installation of pkg only via server.

  Scenario: No Salt Package and service are running on Minion centos
    Given no Salt packages are installed on remote minion host
    And centos minion is not registered in Spacewalk
    
  Scenario: Bootstrap a system via salt-ssh
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
    Given I am authorized as "testing" with password "testing"
    And I follow "Systems"
    And I follow centos ssh-minion hostname
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    And I select "Test Base Channel" from "new_base_channel_id"
    And I click on "Confirm"
    And I click on "Modify Base Software Channel"
    And I should see a "System's Base Channel has been updated." text

  Scenario: Run a remote command on ssh-minion
    Given I am authorized as "testing" with password "testing"
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    Then I enter command "rpm -q salt-minion"
    And I click on preview
    And I click on run
    Then I wait for "3" seconds
    And I expand the results for "ceos-minion"
    Then I should see a "package salt-minion is not installed" text

   Scenario: Run a remote command from the systems overview page
    Given I am authorized as "testing" with password "testing"
    And I follow "Systems"
    And I follow centos ssh-minion hostname
    When I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      sleep 1
      """
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled successfully" text
    And I wait for "10" seconds
    And I check status "Completed" with spacecmd on "ceos-minion"
    Then I run "yum repoinfo | grep :443/rhn" on "ceos-minion"

   Scenario: Install a package to centos ssh-normal minion
    Given I am authorized as "testing" with password "testing"
    And I follow "Systems"
    And I follow centos ssh-minion hostname
    And I follow "Software" in the content area
    And I follow "Install"
    When I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait for "60" seconds
    And "hoag-dummy-1.1-2.1" is installed on "ceos-minion"

  Scenario: Delete minion system profile
    Given I am authorized as "testing" with password "testing"
    And I follow "Systems"
    And I follow centos ssh-minion hostname
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I should see a "has been deleted" text

  Scenario: Bootstrap a centos-salt-ssh with reverse ssh-tunnel
    Given I am authorized
    When I follow "Salt"
    Then I should see a "Bootstrapping" text
    And I follow "Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    And I check "manageWithSSH"
    And I enter the hostname of "ceos-minion" as hostname
    And I enter "linux" as "password"
    And I select "1-ssh-push-tunnel" from "activationKeys"
    And I click on "Bootstrap"
    And I wait for "15" seconds
    Then I should see a "Successfully bootstrapped host! Your system should appear in System Overview shortly." text
    And I wait for "10" seconds
    And I follow "System Overview"
    Then I should see centos ssh-minion hostname as link
    And I follow centos ssh-minion hostname
    Then I should see a "Push via SSH tunnel" text

   Scenario: Subscribe ssh-tunnel-minion to a base-channel for testing
    Given I am authorized as "testing" with password "testing"
    And I follow "Systems"
    And I follow centos ssh-minion hostname
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    And I select "Test Base Channel" from "new_base_channel_id"
    And I click on "Confirm"
    And I click on "Modify Base Software Channel"
    And I should see a "System's Base Channel has been updated." text

  Scenario: Run a remote command on centos ssh-minion-tunnel
    Given I am authorized as "testing" with password "testing"
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    Then I enter command "rpm -q salt-minion"
    And I click on preview
    And I click on run
    Then I wait for "3" seconds
    And I expand the results for "ceos-minion"
    Then I should see a "package salt-minion is not installed" text

   Scenario: Run a remote command from the systems overview page: ssh-tunnel
    Given I am authorized as "testing" with password "testing"
    And I follow "Systems"
    And I follow centos ssh-minion hostname
    When I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      sleep 1
      """
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled successfully" text
    And I wait for "10" seconds
    And I check status "Completed" with spacecmd on "ceos-minion"

   Scenario: test the ssh-reversing tunnel on ceos-minion
   # Verify that the repository of ssh-tunnel-minion has port on 1233
   # normally this point somewhere else.
   Given I am authorized as "testing" with password "testing"
   Then I run "yum repoinfo | grep :1233/rhn" on "ceos-minion"

   Scenario: Install a package to ssh-tunnel-minion
    Given I am authorized as "testing" with password "testing"
    And I follow "Systems"
    And I follow centos ssh-minion hostname
    And I follow "Software" in the content area
    And I follow "Install"
    When I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait for "60" seconds
    And "hoag-dummy-1.1-2.1" is installed on "ceos-minion"
