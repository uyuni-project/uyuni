# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Chanel subscription via SSM

  Scenario: Change child channels for two systems subscribed to a base channel
    Given I am authorized as "admin" with password "admin"
    When I am on the System Overview page
    And I check the "sle-minion" client
    And I check the "sle-client" client
    And I should see "2" systems selected for SSM
    And I am on System Set Manager Overview
    And I follow "channel memberships" in the content area
    Then I should see a "Base Channel" text
    And I should see a "Next" text
    And I should see a table line with "Test-Channel-x86_64", "2"
    When I select "Test Base Channel" from drop-down in table line with "Test-Channel-x86_64"
    And I click on "Next"
    Then I should see a "Child Channels" text
    And I should see a "Test Base Channel" text
    And I should see a "2 system(s) to subscribe" text
    When I choose radio button "Subscribe" for child channel "Test Child Channel"
    And I click on "Next"
    Then I should see a "Channel Changes Overview" text
    And I should see a "2 system(s) to subscribe" text
    When I schedule action to 3 minutes from now
    And I click on "Confirm"
    And I remember when I scheduled an action
    Then I wait until I see "Channel Changes Actions" text
    And I should see a "Items 1 - 2 of 2" text
    And a table line should contain system "sle-minion", "Scheduled"
    And a table line should contain system "sle-client", "Scheduled"

  Scenario: Check SLES minion is still subscribed to old channels before channel change completes
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "Test-Channel-x86_64" is checked
    And I wait until I do not see "Loading..." text
    And I should see "Test-Channel-x86_64 Child Channel" as unchecked

  Scenario: Check SLES client is still subscribed to old channels before channel change completes
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "Test-Channel-x86_64" is checked
    And I wait until I do not see "Loading..." text
    And I should see "Test-Channel-x86_64 Child Channel" as unchecked

  Scenario: Check old channels are still enabled on SLES minion before channel change completes
    When I refresh the metadata for "sle-minion"
    Then "1" channels should be enabled on "sle-minion"
    And channel "Test-Channel-x86_64" should be enabled on "sle-minion"

  Scenario: Check old channels are still enabled on SLES client before channel change completes
    When I refresh the metadata for "sle-client"
    Then "1" channels with prefix "spacewalk:" should be enabled on "sle-client"
    And channel "Test-Channel-x86_64" should be enabled on "sle-client"

  Scenario: Check channel change has completed for the SLES minion
    Given I am on the Systems overview page of this "sle-minion"
    When I wait until event "Subscribe channels scheduled by admin" is completed
    Then I should see "The client completed this action on" at least 3 minutes after I scheduled an action

  Scenario: Check channel change has completed for the SLES client
    Given I am on the Systems overview page of this "sle-client"
    When I wait until event "Subscribe channels scheduled by admin" is completed
    Then I should see "The client completed this action on" at least 3 minutes after I scheduled an action

  Scenario: Check the SLES minion is subscribed to the new channels
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "Test Base Channel" is checked
    And I wait until I do not see "Loading..." text
    And I should see "Test Child Channel" as checked

  Scenario: Check the SLES client is subscribed to the new channels
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "Test Base Channel" is checked
    And I wait until I do not see "Loading..." text
    And I should see "Test Child Channel" as checked

  Scenario: Check the new channels are enabled on the SLES minion
    When I refresh the metadata for "sle-minion"
    Then "2" channels should be enabled on "sle-minion"
    And channel "Test Base Channel" should be enabled on "sle-minion"
    And channel "Test Child Channel" should be enabled on "sle-minion"

  Scenario: Check the new channels are enabled on the SLES client
    When I refresh the metadata for "sle-client"
    Then "2" channels with prefix "spacewalk:" should be enabled on "sle-client"
    And channel "Test Base Channel" should be enabled on "sle-client"
    And channel "Test Child Channel" should be enabled on "sle-client"

@centos_minion
  Scenario: System default channel can't be determined
    Given I am authorized as "admin" with password "admin"
    When I am on the System Overview page
    And I check the "ceos-minion" client
    And I uncheck the "sle-minion" client
    And I uncheck the "sle-client" client
    Then I should see "1" systems selected for SSM
    When I am on System Set Manager Overview
    And I follow "channel memberships" in the content area
    And I select "System Default Base Channel" from drop-down in table line with "Test Base Channel"
    And I click on "Next"
    Then I should see a "Child Channels" text
    And I should see a "Couldn't determine new base channel" text
    And I should see a "1 system(s) incompatible" text
    When I click on "Next"
    Then I should see a "Channel Changes Overview" text
    And I should see a "Couldn't determine new base channel" text
    And I should see a "1 system(s) incompatible" text
    When I click on "Confirm"
    Then I should see a "Channel Changes Actions" text
    And I should see a "Items 1 - 1 of 1" text
    And a table line should contain system "ceos-minion", "Could not determine system default channel"

@centos_minion
  Scenario: Cleanup: make sure the CentOS minion is still unchanged
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    Then radio button "Test Base Channel" is checked

@centos_minion
  Scenario: Cleanup: remove CentOS systems from SSM
    Given I am authorized as "admin" with password "admin"
    And I am on the System Overview page
    When I uncheck the "ceos-minion" client
    And I check the "sle-minion" client
    And I check the "sle-client" client
    And I should see "2" systems selected for SSM

@ubuntu_minion
  Scenario: System default channel can't be determined
    Given I am authorized as "admin" with password "admin"
    When I am on the System Overview page
    And I check the "ubuntu-minion" client
    And I uncheck the "sle-minion" client
    And I uncheck the "sle-client" client
    Then I should see "1" systems selected for SSM
    When I am on System Set Manager Overview
    And I follow "channel memberships" in the content area
    And I select "System Default Base Channel" from drop-down in table line with "Test-Channel-Deb-AMD64"
    And I click on "Next"
    Then I should see a "Child Channels" text
    And I should see a "Couldn't determine new base channel" text
    And I should see a "1 system(s) incompatible" text
    When I click on "Next"
    Then I should see a "Channel Changes Overview" text
    And I should see a "Couldn't determine new base channel" text
    And I should see a "1 system(s) incompatible" text
    When I click on "Confirm"
    Then I should see a "Channel Changes Actions" text
    And I should see a "Items 1 - 1 of 1" text
    And a table line should contain system "ubuntu-minion", "Could not determine system default channel"

@ubuntu_minion
  Scenario: Cleanup: make sure the Ubuntu minion is still unchanged
    Given I am on the Systems overview page of this "ubuntu-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    Then radio button "Test-Channel-Deb-AMD64" is checked

@ubuntu_minion
  Scenario: Cleanup: remove Ubuntu system from SSM
    Given I am authorized as "admin" with password "admin"
    And I am on the System Overview page
    When I uncheck the "ubuntu-minion" client
    And I check the "sle-minion" client
    And I check the "sle-client" client
    And I should see "2" systems selected for SSM

  Scenario: Cleanup: subscribe the SLES minion back to previous channels
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I see "Test-Channel-x86_64 Child Channel" text
    And I uncheck "Test-Channel-x86_64 Child Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Cleanup: subscribe the SLES client back to previous channels
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I see "Test-Channel-x86_64 Child Channel" text
    And I uncheck "Test-Channel-x86_64 Child Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Cleanup: remove remaining systems from SSM
    Given I am authorized as "admin" with password "admin"
    And I am on the System Overview page
    And I uncheck the "sle-minion" client
    And I uncheck the "sle-client" client
    And I should see "0" systems selected for SSM
