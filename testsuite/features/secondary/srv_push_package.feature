# Copyright (c) 2015-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Push a package with unset vendor
  In order to distribute software to the clients
  As an authorized user
  I want to push a package with unset vendor

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Push a package with unset vendor
    When I push package "/root/subscription-tools-1.0-0.noarch.rpm" into "test_base_channel" channel
    Then I should see package "subscription-tools-1.0-0.noarch" in channel "Test Base Channel"

  Scenario: Check vendor of package displayed in web UI
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test Base Channel"
    And I follow "Packages"
    And I follow "subscription-tools-1.0-0.noarch"
    Then I should see a "Vendor:" text
    And I should see a "Not defined" text
