
Feature: The spacewalk-repo-sync command
  In Order to sync channels without using the web interface
  As an sysadmin logged into the server
  I want to be able to run a command to do it

  Scenario: spacewalk-repo-sync with custom urls
    When I call spacewalk-repo-sync for channel "test_base_channel" with a custom url "http://localhost/pub/TestRepo/"
    Then I should see "Channel: test_base_channel" in the output
    And I should see "Sync completed." in the output
    And I should see "Total time:" in the output
    And I should see "Repo URL:" in the output
