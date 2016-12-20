# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Bootstrap a new salt host via salt-ssh
  In order to operate SUSE Manager based on salt-ssh
  I want to verify general salt functionality and system registration

  Scenario: No Salt Package and service are running on Minion

    Given no Salt packages are installed on remote minion host
    And remote minion host is not registered in Spacewalk
    
  Scenario: Bootstrap a system via salt-ssh

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

  # testing GUI
  Scenario: Run a remote command
    And I am authorized as "testing" with password "testing"
    Given I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    Then I enter command "hostname -f"
    And I click on preview
    Then I should see my hostname
    And I click on run
    Then I wait for "3" seconds
    And I expand the results
    Then I should see "SuSE-release" in the command output

