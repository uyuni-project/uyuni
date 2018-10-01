# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Push a package with unset vendor
  In order to distribute software to the clients
  As an authorized user
  I want to push a package with unset vendor

  Background:
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu

  Scenario: Download the SSL certificate
    When I download the SSL certificate
    And I make the SSL certificate available to zypper

  Scenario: Push a package with unset vendor
    When I push package "/root/subscription-tools-1.0-0.noarch.rpm" into "test_base_channel" channel
    And I follow "Software" in the left menu
    And I follow "Channel List" in the left menu
    And I follow "Channel List > All" in the left menu
    Then I should see package "subscription-tools-1.0-0.noarch" in channel "Test Base Channel"

  Scenario: Check vendor of package displayed in web UI
    When I follow "Software" in the left menu
    And I follow "Channel List" in the left menu
    And I follow "Channel List > All" in the left menu
    And I follow "Test Base Channel"
    And I follow "Packages"
    And I follow "subscription-tools-1.0-0.noarch"
    Then I should see a "Vendor:" text
    And I should see a "Not defined" text
