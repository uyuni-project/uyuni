# Copyright (c) 2018-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_changing_software_channels
@scc_credentials
Feature: Channel subscription via SSM

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

@sle_minion
  Scenario: Change child channels for SLES minion subscribed to a base channel
    When I follow the left menu "Systems > System List > All"
    And I click on the clear SSM button
    And I check the "sle_minion" client
    And I should see "1" systems selected for SSM
    And I follow the left menu "Systems > System Set Manager > Overview"
    And I follow "channel memberships" in the content area
    Then I should see a "Base Channel" text
    And I should see a "Next" text
    When I select "Fake Base Channel" from drop-down in table line with "SLE-Product-SLES15-SP4-Pool for x86_64"
    And I click on "Next"
    Then I should see a "Child Channels" text
    And I should see a "Fake Base Channel" text
    And I should see a "1 system(s) to subscribe" text
    When I choose radio button "Subscribe" for child channel "Fake Child Channel"
    And I click on "Next"
    Then I should see a "Channel Changes Overview" text
    And I should see a "1 system(s) to subscribe" text
    When I schedule action to 3 minutes from now
    And I click on "Confirm"
    And I remember when I scheduled an action
    Then I wait until I see "Channel Changes Actions" text
    And a table line should contain system "sle_minion", "Scheduled"

@sle_minion
  Scenario: Check SLES minion is still subscribed to old channels before channel change completes
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "SLE-Product-SLES15-SP4-Pool for x86_64" is checked
    And I wait until I do not see "Loading..." text
    And I should see "SLE15-SP4-Installer-Updates for x86_64" as unchecked

@sle_minion
@susemanager
  Scenario: Check old channels are still enabled on SLES minion before channel change completes
    When I refresh the metadata for "sle_minion"
    Then "17" channels should be enabled on "sle_minion"
    And channel "SLE-Product-SLES15-SP4-Pool for x86_64" should be enabled on "sle_minion"

@sle_minion
@uyuni
  Scenario: Check old channels are still enabled on SLES minion before channel change completes
    When I refresh the metadata for "sle_minion"
    Then "13" channels should be enabled on "sle_minion"
    And channel "SLE-Product-SLES15-SP4-Pool for x86_64" should be enabled on "sle_minion"

  Scenario: Wait 3 minutes for the scheduled action to be executed
    When I wait for "180" seconds

@sle_minion
  Scenario: Check channel change has completed for the SLES minion
    Given I am on the Systems overview page of this "sle_minion"
    When I wait until event "Subscribe channels scheduled by admin" is completed
    Then I should see "The client completed this action on" at least 3 minutes after I scheduled an action

@sle_minion
  Scenario: Check the SLES minion is subscribed to the new channels
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "Fake Base Channel" is checked
    And I wait until I do not see "Loading..." text
    And I should see "Fake Child Channel" as checked

@sle_minion
  Scenario: Check the new channels are enabled on the SLES minion
    When I refresh the metadata for "sle_minion"
    Then "2" channels should be enabled on "sle_minion"
    And channel "Fake Base Channel" should be enabled on "sle_minion"
    And channel "Fake Child Channel" should be enabled on "sle_minion"

@rhlike_minion
  Scenario: System default channel can't be determined on the Red Hat-like minion
    When I follow the left menu "Systems > System List > All"
    And I click on the clear SSM button
    And I check the "rhlike_minion" client
    Then I should see "1" systems selected for SSM
    When I follow the left menu "Systems > System Set Manager > Overview"
    And I follow "channel memberships" in the content area
    And I select "System Default Base Channel" from drop-down in table line with "Fake Base Channel"
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
    And a table line should contain system "rhlike_minion", "Could not determine system default channel"
    And I click on the clear SSM button

@rhlike_minion
  Scenario: Cleanup: make sure the Red Hat-like minion is still unchanged
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    Then radio button "Fake Base Channel" is checked

@deblike_minion
  Scenario: System default channel can't be determined on the Debian-like minion
    When I follow the left menu "Systems > System List > All"
    And I click on the clear SSM button
    And I check the "deblike_minion" client
    Then I should see "1" systems selected for SSM
    When I follow the left menu "Systems > System Set Manager > Overview"
    And I follow "channel memberships" in the content area
    And I select "System Default Base Channel" from drop-down in table line with "Fake-Deb-AMD64-Channel"
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
    And a table line should contain system "deblike_minion", "Could not determine system default channel"
    And I click on the clear SSM button

@deblike_minion
  Scenario: Cleanup: make sure the Debian-like minion is still unchanged
    Given I am on the Systems overview page of this "deblike_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    Then radio button "Fake-Deb-AMD64-Channel" is checked

@sle_minion
  Scenario: Cleanup: subscribe the SLES minion back to previous channels
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP4-Pool for x86_64"
    And I wait until I do not see "Loading..." text
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I check "SLE-Module-Containers15-SP4-Pool for x86_64"
    And I check "Fake-RPM-SUSE-Channel"
    And I wait until I do not see "Loading..." text
    And I wait until I see "SLE15-SP4-Installer-Updates for x86_64" text
    And I wait until I do not see "Loading..." text
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
    Then channel "SLE15-SP4-Installer-Updates for x86_64" should not be enabled on "sle_minion"

  Scenario: Cleanup: remove remaining systems from SSM after channel subscription tests
    When I click on the clear SSM button
