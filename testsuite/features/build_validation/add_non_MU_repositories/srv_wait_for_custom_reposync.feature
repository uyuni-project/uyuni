# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Wait for reposync activity to finish after adding custom channels

  Scenario: Wait for running reposyncs to finish after adding custom channels
    When I wait until all spacewalk-repo-sync finished

  Scenario: Verify the reposync went fine after adding custom channels
    Then the reposync logs should not report errors
