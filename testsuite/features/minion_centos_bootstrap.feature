# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: CENTOS: register a salt-minion via bootstrap

  Scenario: bootstrap a centos minion
     Given I am authorized
     When I follow "Salt"
     Then I should see a "Bootstrapping" text
     And I follow "Bootstrapping"
     Then I should see a "Bootstrap Minions" text
     # sle-minion = sles, ceos-minion = redhat
     And  I enter the hostname of "ceos-minion" as hostname
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait for "150" seconds
     Then I should see a "Successfully bootstrapped host! Your system should appear in System Overview shortly." text
