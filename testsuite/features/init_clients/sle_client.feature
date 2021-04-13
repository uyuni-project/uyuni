# Copyright (c) 2015-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Register a traditional client
  In order to register a traditional client to the SUSE Manager server
  I want to create, parametrize and run boostrap script from proxy

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Register a traditional client
    When I bootstrap traditional client "sle_client" using bootstrap script with activation key "1-SUSE-KEY-x86_64" from the proxy
    And I wait until onboarding is completed for "sle_client"
    Then I should see "sle_client" via spacecmd

  Scenario: Check registration values
    Given I update the profile of this client
    When I am on the Systems overview page of this "sle_client"
    And I click on the clear SSM button
    And I wait until I see "Software Updates Available" text, refreshing the page
    Then I should see a "System Status" text
    And I should see a "Software Updates Available" text
    And I should see a "Critical:" link
    And I should see a "Non-Critical:" link
    And I should see a "Packages:" link
    And I should see a "Edit These Properties" link
    And I should see a "[Management]" text
    And I should see a "Add to SSM" link
    And I should see a "Delete System" link
    And I should see a "Initial Registration Parameters:" text
    And I should see a text describing the OS release

@proxy
  Scenario: Check connection from traditional to proxy
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of traditional
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_client" hostname
