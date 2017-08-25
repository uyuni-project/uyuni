Feature: register a salt-ssh system via XMLRPC API bootstrap procedure

  Scenario: Setup XMLRPC Bootstrap: Delete ssh-minion system profile before XMLRPC bootstrap test
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I should see a "has been deleted" text
    And I cleanup minion: "ssh-minion"

  Scenario: bootstrap a sles ssh-minion via XMLRPC API (it will be deleted after)
    Given I am logged in via XML-RPC/system as user "admin" and password "admin"
    When I call system.bootstrap() on host "ssh-minion" and saltSSH "enabled", a new system should be bootstraped.
    And I logout from XML-RPC/system.

  Scenario: check new XMLRPC bootstrapped salt-ssh in System Overview page
     Given I am authorized
     And I navigate to "rhn/systems/Overview.do" page
     And I wait until I see the name of "ssh-minion", refreshing the page
     And I wait until onboarding is completed for "ssh-minion"

  Scenario: Check contact method of this salt-ssh system
    Given I am on the Systems overview page of this "ssh-minion"
    Then I should see a "Push via SSH" text

  Scenario: Check spacecmd system ID of XMLRPC-bootstrapped ssh-minion.
    Given I am on the Systems overview page of this "ssh-minion"
    Then I run spacecmd listevents for ssh-minion

  Scenario: Cleanup XMLRPC Bootstrap: Subscribe ssh-minion to base channel
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    And I select "Test-Channel-x86_64" from "new_base_channel_id"
    And I click on "Confirm"
    And I click on "Modify Base Software Channel"
    And I should see a "System's Base Channel has been updated." text
