# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: The spacewalk-repo-sync command
  In order to synchronize channels without using the web interface
  As an system administrator logged into the server
  I want to be able to run the spacewalk-repo-sync command

  Scenario: spacewalk-repo-sync with custom urls
    When I call spacewalk-repo-sync for channel "test_base_channel" with a custom url "http://localhost/pub/TestRepo/"
    Then I should see "Channel: test_base_channel" in the output
    And I should see "Sync completed." in the output
    And I should see "Total time:" in the output
    And I should see "Repo URL:" in the output
