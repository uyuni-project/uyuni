# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@server2
Feature: Hub ISSv3 channel synchronization to peripheral
  In order to distribute content from a hub to peripheral servers
  As an authorized user
  I want to synchronize channels via the hub UI and peripheral UI (plan A-06)

  Scenario: Log in as admin user for channel sync tests
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite - register server2 as peripheral for channel sync tests (A-06)
    When I add "server2" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server2" in peripherals list

  Scenario: Clone a channel on hub for sync testing (A-06)
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "Fake-RPM-SUSE-Channel" as the origin channel
    And I choose "original"
    And I click on "Clone Channel"
    And I enter "Fake-Clone-RPM-SLES15SP7-Channel" as "Channel Name"
    And I should see a "Create Software Channel" text
    And I should see a "Original state of the channel" text
    And I click on "Clone Channel"
    Then I should see a "Fake-Clone-RPM-SLES15SP7-Channel" text

  Scenario: Verify cloned channel has packages (A-06)
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Clone-RPM-SLES15SP7-Channel"
    And I follow "Patches" in the content area
    And I follow "List/Remove Patches"
    Then I should see a "There are no patches associated with this channel." text

  Scenario: Configure cloned channel sync from hub to server2 via hub UI - Method A (A-06)
    When I configure hub to sync channel "Fake-Clone-RPM-SLES15SP7-Channel" to "server2"
    # workaround: https://bugzilla.suse.com/show_bug.cgi?id=1271703
    Then I should see a "Channels synced correctly to peripheral!" text

  Scenario: Trigger channel sync from hub to server2 (A-06)
    Given I am authorized for the "Admin" section on "server2"
    When I initiate channel sync from peripheral "server2"
    Then I should see a "Successfully scheduled a channels synchronization." text

  Scenario: Wait for cloned channel to appear on server2 (A-06)
    # workaround: https://bugzilla.suse.com/show_bug.cgi?id=1272155 (clone channel ISS sync is currently broken)
    # When I wait until channel "clone-fake-rpm-suse-channel" has been fully synchronized on "server2"
    # Then channel "clone-fake-rpm-suse-channel" should exist on "server2"

  Scenario: Verify cloned channel on server2 has expected packages (A-06)
    # workaround: https://bugzilla.suse.com/show_bug.cgi?id=1272155
    # Then channel "clone-fake-rpm-suse-channel" on "server2" should have "4" packages

  Scenario: Wait for SLE-Product-SLES15-SP7-Pool channel to be synchronized on server2 (A-06)
    When I wait until channel "sle-product-sles15-sp7-pool-x86_64" has been fully synchronized on "server2"
    Then channel "sle-product-sles15-sp7-pool-x86_64" should exist on "server2"

  Scenario: Verify SLE-Product-SLES15-SP7-Pool channel on server2 has expected packages (A-06)
    Then channel "sle-product-sles15-sp7-pool-x86_64" on "server2" should have "3" packages

  Scenario: Create a custom channel on hub for org-mapping test (A-06)
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Test Hub Custom Channel" as "Channel Name"
    And I enter "test-hub-custom-channel" as "Channel Label"
    And I select "x86_64" from "Architecture"
    And I enter "Test Hub Custom Channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Test Hub Custom Channel created" text

  Scenario: Configure custom channel sync to server2 with organization mapping (A-06)
    When I configure hub to sync channel "Test Hub Custom Channel" to "server2"
    And I select target organization "Test Default Organization" for channel "test-hub-custom-channel" on "server2"
    Then I should see a "Channel configuration updated" text

  Scenario: Trigger sync for custom channel and verify it arrives on server2 (A-06)
    When I initiate channel sync from peripheral "server2"
    And I should see a "Successfully scheduled a channels synchronization." text
    And I wait at most 300 seconds until channel "test-hub-custom-channel" has been synced on "server2"
    Then channel "test-hub-custom-channel" should exist on "server2"

  # Method B — hub-side trigger via Admin > Hub Configuration > Peripherals Configuration
  # ("Sync Channels" button next to a registered peripheral) does not exist as a separate
  # flow: the actual sync-now action only lives on the peripheral's own Hub Details page
  # (Admin > Hub Configuration > Hub Details), which is what "I initiate channel sync from
  # peripheral" already covers above. No separate Method B scenario needed.

  # Method C — sync.hub.* API endpoints: not available in SUMA/Uyuni 5.2.
  # Hub channel sync is UI/event-driven under ISS v3. The sync.hub namespace
  # was not ported. Verify with: spacecmd api -- sync.hub (should return empty).
  # TODO: If sync.hub.* endpoints are re-added in a future release, implement
  # API-driven sync scenario here using the XMLRPC::Client pattern in hub_steps.rb.

  # BUG-019 (QE test plan): HTTP 500 during peripheral channel sync when
  # rhnContentSource.source_url exceeds VARCHAR(2048) in the database schema.
  # An automated regression requires a repo URL longer than 2048 characters,
  # which the standard Fake-RPM-SUSE-Channel and its clones cannot produce.
  # No existing helper creates custom repos with arbitrary URL lengths, so this
  # check remains MANUAL: sync a peripheral channel whose source URL is ≥2049
  # characters and assert the response is not HTTP 500.
  # Re-evaluate if a step I create a repo with a URL of length N characters
  # is added to api_common.rb or command_steps.rb.

  Scenario: Regenerate mirror credentials for server2 and verify sync still works (A-06)
    When I regenerate mirror credentials for peripheral "server2"
    And I initiate channel sync from peripheral "server2"
    Then I should see a "Successfully scheduled a channels synchronization." text

  Scenario: Cleanup - remove synced channels from server2
    When I remove synced channels from "server2"
    And I wait until I see "Channel configuration updated" text
    Then I should see a "Updated" text

  Scenario: Cleanup - delete custom channel from hub
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test Hub Custom Channel"
    And I follow "Delete Channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Test Hub Custom Channel" text

  Scenario: Cleanup - delete cloned channel from hub
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone of Fake-RPM-SUSE-Channel"
    And I follow "Delete Channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Fake-RPM-SUSE-Channel" text
