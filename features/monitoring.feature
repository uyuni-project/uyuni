# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

# feature/monitoring.feature

Feature: Configure monitoring
  In order to monitor a client
  As the root user
  I want to activate the monitoring scout
  I want to create a probe suite
  I want to create a probe
  I want to add a system to the probe suite

  Scenario: Activate monitoring scout
    Given I am on the Admin page
     When I follow "SUSE Manager Configuration"
       And I follow "Monitoring" in class "content-nav"
       And I check "Enable Monitoring Scout"
       And I click on "Update Config"
    Then I should see a "Configuration updated, Monitoring services restarted." text

  Scenario: Check monitoring scout public key
    Given I am on the Monitoring page
     And I follow "Scout Config Push"
     And I follow "SUSE Manager Monitoring Scout"
    Then I should see a "ssh-dss" text

  Scenario: Create a probe suite
    Given I am on the Monitoring page
     And I follow "Probe Suites"
     And I follow "create new probe suite"
     And I enter "Test" as "suite_name"
     And I enter "Just testing" as "description"
     And I click on "Create Probe Suite"
    Then I should see a "Probe Suite Test created" text
    
  Scenario: Create a probe for probe suite "Test"
    Given I am on the Monitoring page
     And I follow "Probe Suites"
     And I follow "Test"
     And I follow "Probes"
     And I follow "create new probe"
     And I click on "Create probe"
    Then I should see a "Probe Linux: Load created" text

  Scenario: Push Scout config
    Given I am on the Monitoring page
     And I follow "Scout Config Push"
     And I check "checkall"
     And I click on "Push Scout Configs"
    Then I should see a "Config Push Initiated" text
