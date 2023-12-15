# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

@paygo_server
Feature: Disable SCC forward

  Scenario: Disable SCC forward
    Given I disable SCC forward on server
    And I restart the spacewalk service
    When I go to the home page resetting the session
    Then I should see a "SUSE Manager PAYG instances must forward registration data to SCC when credentials are provided. Data will be sent independently of the configuration setting." text

  Scenario: Re-enable SCC forward
    Given I enable SCC forward on server
    And I restart the spacewalk service
    When I go to the home page resetting the session
    Then I should not see a "SUSE Manager PAYG instances must forward registration data to SCC when credentials are provided. Data will be sent independently of the configuration setting." text
