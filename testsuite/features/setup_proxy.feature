# Copyright 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: I want to setup the HTTP proxy

  Scenario: I want to test the proxy setup
    Given I am on the Admin page
    And I should see a "HTTP Proxy Hostname" text
    And I should see a "HTTP Proxy Username" text
    And I should see a "HTTP Proxy Password" text
    When I enter "galaxy-proxy.mgr.suse.de:3128" as "HTTP Proxy Hostname"
    And I enter "suma" as "HTTP Proxy Username"
    And I enter "P4$$word" as "HTTP Proxy Password"
    And I click on "Save and Verify"
#    And I wait for "5" seconds
    Then I see verification succeeded
