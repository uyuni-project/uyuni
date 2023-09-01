# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Distribution Channel Mapping

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Check if Distribution Channel Mapping page exists
    When I follow the left menu "Software > Distribution Channel Mapping"
    Then I should see a "Distribution Channel Mapping" text
    And I should see a "Channel List" link in the left menu
    And I should see a "Package Search" link in the left menu
    And I should see a "Manage" link in the left menu
    And I should see a "Distribution Channel Mapping" link in the left menu
    And I should see a "Create Distribution Channel Mapping" link
    And I should see a "No distribution channel mappings currently exist." text in the content area

@scc_credentials
  Scenario: Create new map for x86_64 SUSE clients
    When I follow the left menu "Software > Distribution Channel Mapping"
    And I follow "Create Distribution Channel Mapping"
    Then I should see a "Create Distribution Channel Map" text
    When I enter "SUSE Linux Enterprise Server 15 SP 4" as "os"
    And I enter "15.4" as "release"
    And I select "x86_64" from "architecture" dropdown
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "channel_label" dropdown
    And I click on "Create Mapping"
    Then I should see a "SUSE Linux Enterprise Server 15 SP 4" link in the content area

  Scenario: Create new map for x86_64 Ubuntu clients with test base channel
    When I follow the left menu "Software > Distribution Channel Mapping"
    And I follow "Create Distribution Channel Mapping"
    Then I should see a "Create Distribution Channel Map" text
    When I enter "Ubuntu 22.04.01 LTS" as "os"
    And I enter "22.04" as "release"
    And I select "x86_64" from "architecture" dropdown
    And I select "Fake Base Channel" from "channel_label" dropdown
    And I click on "Create Mapping"
    Then I should see a "Ubuntu 22.04.01 LTS" link in the content area

@scc_credentials
  Scenario: Create new map for iSeries SUSE clients using test channel
    When I follow the left menu "Software > Distribution Channel Mapping"
    And I follow "Create Distribution Channel Mapping"
    Then I should see a "Create Distribution Channel Map" text
    When I enter "SUSE Linux Enterprise Server 15 SP 4 iSeries" as "os"
    And I enter "15.4" as "release"
    And I select "iSeries" from "architecture" dropdown
    And I select "Fake-i586-Channel" from "channel_label" dropdown
    And I click on "Create Mapping"
    Then I should see a "SUSE Linux Enterprise Server 15 SP 4 iSeries" link in the content area

@scc_credentials
  Scenario: Update map for x86_64 SUSE clients using test-x86_64 channel
    When I follow the left menu "Software > Distribution Channel Mapping"
    Then I should see the text "SUSE Linux Enterprise Server 15 SP 4" in the Operating System field
    And I should see the text "x86_64" in the Architecture field
    And I should see the text "sle-product-sles15-sp4-pool-x86_64" in the Channel Label field
    When I follow "SUSE Linux Enterprise Server 15 SP 4"
    Then I should see a "Update Distribution Channel Map" text
    When I enter "SUSE Linux Enterprise Server 15 SP 4 modified" as "os"
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "channel_label" dropdown
    And I click on "Update Mapping"
    Then I should see the text "SUSE Linux Enterprise Server 15 SP 4 modified" in the Operating System field
    And I should see the text "sle-product-sles15-sp4-pool-x86_64" in the Channel Label field

  Scenario: Update map for x86_64 Ubuntu clients using test base channel
    When I follow the left menu "Software > Distribution Channel Mapping"
    Then I should see the text "Ubuntu 22.04.01 LTS" in the Operating System field
    And I should see the text "x86_64" in the Architecture field
    And I should see the text "sle-product-sles15-sp4-pool-x86_64" in the Channel Label field
    When I follow "Ubuntu 22.04.01 LTS"
    And I enter "Ubuntu 22.04.01 LTS modified" as "os"
    And I select "Fake Base Channel" from "channel_label" dropdown
    And I click on "Update Mapping"
    Then I should see the text "Ubuntu 22.04.01 LTS modified" in the Operating System field
    And I should see the text "fake_base_channel" in the Channel Label field

@scc_credentials
  Scenario: Update map for IA-32 SUSE clients using amd deb test channel
    When I follow the left menu "Software > Distribution Channel Mapping"
    Then I should see the text "SUSE Linux Enterprise Server 15 SP 4 iSeries" in the Operating System field
    And I should see the text "iSeries" in the Architecture field
    And I should see the text "fake-i586-channel" in the Channel Label field
    When I follow "SUSE Linux Enterprise Server 15 SP 4 iSeries"
    And I enter "SUSE Linux Enterprise Server 15 SP 4 iSeries modified" as "os"
    And I select "Fake-Deb-AMD64-Channel" from "channel_label" dropdown
    And I click on "Update Mapping"
    Then I should see the text "SUSE Linux Enterprise Server 15 SP 4 iSeries modified" in the Operating System field
    And I should see the text "fake-deb-amd64-channel" in the Channel Label field

@scc_credentials
  Scenario: Cleanup: delete the map created for x68_64 SUSE clients
    When I follow the left menu "Software > Distribution Channel Mapping"
    Then I should see the text "SUSE Linux Enterprise Server 15 SP 4 modified" in the Operating System field
    And I should see the text "x86_64" in the Architecture field
    When I follow "SUSE Linux Enterprise Server 15 SP 4 modified"
    Then I should see a "Update Distribution Channel Map" text
    And I should see a "Delete Distribution Channel" link
    When I follow "Delete Distribution Channel Mapping"
    Then I should see a "Delete Distribution Channel Map" text
    When I click on "Delete Mapping"
    Then I should not see a "SUSE Linux Enterprise Server 15 SP 4 modified" link
    
  Scenario: Cleanup: delete the map created for x68_64 Ubuntu clients
    When I follow the left menu "Software > Distribution Channel Mapping"
    Then I should see the text "Ubuntu 22.04.01 LTS modified" in the Operating System field
    And I should see the text "x86_64" in the Architecture field
    When I follow "Ubuntu 22.04.01 LTS modified"
    Then I should see a "Update Distribution Channel Map" text
    And I should see a "Delete Distribution Channel" link
    When I follow "Delete Distribution Channel Mapping"
    Then I should see a "Delete Distribution Channel Map" text
    When I click on "Delete Mapping"
    Then I should not see a "Ubuntu 22.04.01 LTS modified" link
    
@scc_credentials
  Scenario: Cleanup: delete the map created for i586 clients
    When I follow the left menu "Software > Distribution Channel Mapping"
    Then I should see the text "SUSE Linux Enterprise Server 15 SP 4 iSeries modified" in the Operating System field
    And I should see the text "x86_64" in the Architecture field
    When I follow "SUSE Linux Enterprise Server 15 SP 4 iSeries modified"
    Then I should see a "Update Distribution Channel Map" text
    And I should see a "Delete Distribution Channel" link
    When I follow "Delete Distribution Channel Mapping"
    Then I should see a "Delete Distribution Channel Map" text
    When I click on "Delete Mapping"
    Then I should not see a "SUSE Linux Enterprise Server 15 SP 4 iSeries modified" link

  Scenario: Sanity check whether the page is in its default state
    When I follow the left menu "Software > Distribution Channel Mapping"
    Then I should see a "Distribution Channel Mapping" text
    And I should see a "No distribution channel mappings currently exist." text in the content area
