# Copyright 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: I want to setup the HTTP proxy

  Scenario: I want to test the proxy setup
    When I am authorized as "admin" with password "admin"
    And I follow "Admin" in the left menu
    And I follow "Setup Wizard" in the left menu
    And I should see a "HTTP Proxy Hostname" text
    And I should see a "HTTP Proxy Username" text
    And I should see a "HTTP Proxy Password" text
    When I enter "galaxy-proxy.mgr.suse.de:3128" as "HTTP Proxy Hostname"
    And I enter "suma" as "HTTP Proxy Username"
    And I enter "P4$$word" as "HTTP Proxy Password"
    And I click on "Save and Verify"
    Then I see verification succeeded
