# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Negative tests for bootstrap normal minions
  In order to register only valid minions 
  As an authorized user
  I want to avoid registration with invalid input parameters

  Scenario: Bootstrap should fail when minion already exists
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle_minion" as "hostname"
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait until I see "A salt key for this host" text
     Then I should not see a "GenericSaltError" text
     And I should see a "seems to already exist, please check!" text

  Scenario: Bootstrap a SLES minion with wrong hostname
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter "not-existing-name" as "hostname"
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait until I see " Could not resolve hostname not-existing-name: Name or service not known" text
     Then I should not see a "GenericSaltError" text

  Scenario: Bootstrap a SLES minion with wrong SSH credentials
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle_minion" as "hostname"
     And I enter "22" as "port"
     And I enter "FRANZ" as "user"
     And I enter "KAFKA" as "password"
     And I click on "Bootstrap"
     And I wait until I see "Permission denied (publickey,keyboard-interactive)." text or "Password authentication failed" text
     Then I should not see a "GenericSaltError" text

  Scenario: Bootstrap a SLES minion with wrong SSH port number
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle_minion" as "hostname"
     And I enter "11" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait until I see "ssh: connect to host" text
     Then I should not see a "GenericSaltError" text
     And I should see a "port 11: Connection refused" text or "port 11: Invalid argument" text

  Scenario: Cleanup: bootstrap a SLES minion after negative tests
     Given I am authorized
     When I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle_minion" as "hostname"
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I select the hostname of "proxy" from "proxies"
     And I click on "Bootstrap"
     And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Cleanup: turn the SLES minion into a container build
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "container_build_host"
    And I click on "Update Properties"

  Scenario: Cleanup: turn the SLES minion into a OS image build host
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "osimage_build_host"
    And I click on "Update Properties"
    Then I should see a "OS Image Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

  Scenario: Cleanup: apply the highstate to build host
    Given I am on the Systems overview page of this "sle_minion"
    When I wait until no Salt job is running on "sle_minion"
    And I enable repositories before installing Docker
    And I apply highstate on "sle_minion"
    And I wait until "docker" service is active on "sle_minion"
    And I wait until file "/var/lib/Kiwi/repo/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm" exists on "sle_minion"
    And I disable repositories after installing Docker

  Scenario: Cleanup: check that the minion is now a build host
    Given I am on the Systems overview page of this "sle_minion"
    Then I should see a "[Container Build Host]" text
    Then I should see a "[OS Image Build Host]" text
