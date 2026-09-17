# Copyright (c) 2025 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
Feature: Test Program Temporary Fixes (PTF) deployment
  Applying and rolling back Program Temporary Fixes should work reliably
  Without leaving the system in an inactive state

  @susemanager
  Scenario: Successfully apply a Program Temporary Fix
    When I apply a Program Temporary Fix to the containerized server
    And I wait for "30" seconds
    Then I expect "uyuni-server" container to be healthy within 300 seconds
    And I expect "uyuni-server" container to run the PTF image

  @susemanager
  Scenario: Re-deploy the original server container
    When I redeploy the original server container
    And I wait for "30" seconds
    Then I expect "uyuni-server" container to be healthy within 300 seconds
    And I expect "uyuni-server" container to not run the PTF image
