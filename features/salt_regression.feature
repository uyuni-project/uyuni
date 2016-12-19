# Copyright (c) 2015-16 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test Salt for regression

   Scenario: There are no top.sls file in certain folders
   When  I run "ls /srv/susemanager/salt/top.sls" on "server"
   Then the command should fail
   
   When  I run "ls /srv/susemanager/salt/top.sls" on "server"
   Then the command should fail

   When  I run "ls /srv/susemanager/pillar/top.sls" on "server"
   Then the command should fail

   When  I run "ls /usr/share/susemanager/salt/top.sls" on "server"
   Then the command should fail

   When  I run "ls /usr/share/susemanager/pillar/top.sls" on "server"
   Then the command should fail

   Scenario:  Manager Hangs if a registered salt-minion is down
    Given I am authorized as "testing" with password "testing"
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    And I should see a "Target systems" text
    And I run "systemctl stop salt-minion" on "ceos-minion"
    And I enter command "cat /etc/os-release"
    And I click on preview
    Then I should see "sle-minion" hostname

   Scenario:  Restart ceos-minion and test that we run cmd
    Given I am authorized as "testing" with password "testing"
    And  I run "systemctl restart salt-minion" on "ceos-minion"
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    And I should see a "Target systems" text
    And I run "systemctl status salt-minion" on "ceos-minion"
    And I enter command "cat /etc/os-release"
    And I click on preview
    Then I should see "ceos-minion" hostname
    And I click on run
    Then I wait for "3" seconds
    And I expand the results for "ceos-minion"
    And I should see a "rhel fedora" text
    Then I should see a "REDHAT_SUPPORT_PRODUCT" text
