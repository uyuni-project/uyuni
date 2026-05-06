# Copyright (c) 2015-2026 SUSE LLC
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
    Then I install "mgr-push" on "sle_minion" using the API

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

  Scenario: Cleanup: remove mgr-push from the SLES minion
    Then I remove "mgr-push" on "sle_minion" using the API
