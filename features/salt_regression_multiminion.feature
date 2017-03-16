# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test that Manager doesn't hang if a salt minion is down.
   We have 2 minions (with salt-minion systemd service), we stop one of them, and then we execute a command.
   Suse Manager server webui shouldn't freeze, this was a bug.
   We should test command asynchronously now.

   Scenario: Stop centos7 minion
   Given I am authorized as "testing" with password "testing"
   And I run "systemctl stop salt-minion" on "ceos-minion"

   Scenario:  Test that Manager doesn't hang if a registered salt minion is down
    Given I am authorized as "testing" with password "testing"
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    And I should see a "Target systems" text
    And I enter command "cat /etc/os-release"
    And I click on preview
    Then I wait for "10" seconds
    And I click on "stop"
    Then I should see "sle-minion" hostname
    And I wait for "10" seconds
    And I click on "run"
    And I click on "stop"
    And I click on "run"
    Then I wait for "10" seconds
    And I click on "stop"
    And I expand the results for "sle-minion"
    Then I should see a "SUSE Linux Enterprise Server" text

   Scenario:  Restart centos7 salt-minion service after stop
    Given I am authorized as "testing" with password "testing"
    And  I run "systemctl restart salt-minion" on "ceos-minion"
    And I wait until "salt-minion" service is up and running on "ceos-minion"
    And I wait for "10" seconds
   Scenario: Test centos7 minion reachability and that it works again after restart
    Given I am authorized as "testing" with password "testing"
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    And I should see a "Target systems" text
    And I run "systemctl status salt-minion" on "ceos-minion"
    And I enter command "cat /etc/os-release"
    And I click on preview
    Then I should see "ceos-minion" hostname
    And I click on "run"
    Then I wait for "3" seconds
    And I expand the results for "ceos-minion"
    And I should see a "rhel fedora" text
    Then I should see a "REDHAT_SUPPORT_PRODUCT" text
