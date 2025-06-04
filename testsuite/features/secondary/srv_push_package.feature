# Copyright (c) 2015-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@sle_minion
@scc_credentials
@skip_if_github_validation
Feature: Push a package with unset vendor
  In order to distribute software to the clients
  As an authorized user
  I want to push a package with unset vendor

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Pre-requisite: mgr-push package must be installed on the SLES minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "Install"
    And I wait until I see "Installable Packages" text
    And I enter "mgr-push" as the filtered package name
    And I click on the filter button
    And I check "mgr-push" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until event "Package Install/Upgrade scheduled" is completed

  Scenario: Push a package with unset vendor through the SLES minion
    When I copy unset package file on "sle_minion"
    And I push package "/root/subscription-tools-1.0-0.noarch.rpm" into "fake-base-channel-suse-like" channel through "sle_minion"
    Then I should see package "subscription-tools-1.0-0.noarch" in channel "Fake-Base-Channel-SUSE-like"

  Scenario: Check vendor of package displayed in web UI
    When I follow the left menu "Software > Channel List > All"
    And I follow "Fake-Base-Channel-SUSE-like"
    And I follow "Packages"
    And I follow "subscription-tools-1.0-0.noarch"
    Then I should see a "Vendor:" text
    And I should see a "Not defined" text
