# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check if Distribution Channel Mapping works correctly

    # User needs to be authorized to create maps. Logging in as admin achieves that.
    Scenario: Log in as admin user
        Given I am authorized for the "Admin" section

    # sanity check if the page exists and is displayed correctly
    Scenario: Check if Distribution Channel Mapping page exists
        When I follow the left menu "Software > Distribution Channel Mapping"
        Then I should see a "Distribution Channel Mapping" text
        And I should see a "Channel List" link in the left menu
        And I should see a "Package Search" link in the left menu
        And I should see a "Manage" link in the left menu
        And I should see a "Distribution Channel Mapping" link in the left menu
        And I should see a "Create Distribution Channel Mapping" link
        And I should see a "No distribution channel mappings currently exist." text in the content area

    # check if creating new mappings for various client-channel combinations is possible
    # These might throw errors if there are more than one channel containing the name of the selected channel
    Scenario: Create new map for x86_64 suse clients
        When I am on the "Distribution Channel Mapping" page
        And I follow "Create Distribution Channel Mapping"
        Then I should see a "Create Distribution Channel Map" text
        When I enter "SUSE Linux Enterprise Server 15 SP 4" as "os"
        And I enter "15.4" as "release"
        And I select "x86_64" from architecture dropdown
        And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from channel list dropdown
        And I click on "Create Mapping"
        Then I should see a "SUSE Linux Enterprise Server 15 SP 4" link in the content area

    Scenario: Create new map for x86_64 ubuntu clients with test base channel
        When I am on the "Distribution Channel Mapping" page
        And I follow "Create Distribution Channel Mapping"
        Then I should see a "Create Distribution Channel Map" text
        When I enter "Ubuntu 22.04.01 LTS" as "os"
        And I enter "22.04" as "release"
        And I select "x86_64" from architecture dropdown
        And I select "Test Base Channel" from channel list dropdown
        And I click on "Create Mapping"
        Then I should see a "Ubuntu 22.04.01 LTS" link in the content area

    Scenario: Create new map for iSeries suse clients using test channel
        When I am on the "Distribution Channel Mapping" page
        And I follow "Create Distribution Channel Mapping"
        Then I should see a "Create Distribution Channel Map" text
        When I enter "SUSE Linux Enterprise Server 15 SP 4 iSeries" as "os"
        And I enter "15.4" as "release"
        And I select "iSeries" from architecture dropdown
        And I select "Test-Channel-i586" from channel list dropdown
        And I click on "Create Mapping"
        Then I should see a "SUSE Linux Enterprise Server 15 SP 4 iSeries" link in the content area

    # test if updating maps with different channels works
    Scenario: Update map for x86_64 suse clients using test-x86_64 channel
        When I am on the "Distribution Channel Mapping" page
        Given I see a "SUSE Linux Enterprise Server 15 SP 4" link in the table
        And I see a "x86_64" architecture description in the table
        And I see a "sle-product-sles15-sp4-pool-x86_64" link in the table
        When I follow "SUSE Linux Enterprise Server 15 SP 4"
        Then I should see a "Update Distribution Channel Map" text
        When I enter "SUSE Linux Enterprise Server 15 SP 4 modified" as "os"
        And I select "Test-Channel-x86_64" from channel list dropdown
        And I click on "Update Mapping"
        Then I should see a "SUSE Linux Enterprise Server 15 SP 4 modified" link in the table
        And I should see a "test-channel-x86_64" link in the table

    Scenario: Update map for x86_64 ubuntu clients using test base channel
        When I am on the "Distribution Channel Mapping" page
        Given I see a "Ubuntu 22.04.01 LTS" link in the table
        And I see a "x86_64" architecture description in the table
        And I see a "sle-product-sles15-sp4-pool-x86_64" link in the table
        When I follow "Ubuntu 22.04.01 LTS"
        And I enter "Ubuntu 22.04.01 LTS modified" as "os"
        And I select "Test Base Channel" from channel list dropdown
        And I click on "Update Mapping"
        Then I should see a "Ubuntu 22.04.01 LTS modified" link in the table
        And I should see a "test_base_channel" link in the table

    Scenario: Update map for IA-32 suse clients using amd deb test channel
        When I am on the "Distribution Channel Mapping" page
        Given I see a "SUSE Linux Enterprise Server 15 SP 4 iSeries" link in the table
        And I see a "iSeries" architecture description in the table
        And I see a "test-channel-i586" link in the table
        When I follow "SUSE Linux Enterprise Server 15 SP 4 iSeries"
        And I enter "SUSE Linux Enterprise Server 15 SP 4 iSeries modified" as "os"
        And I select "Test-Channel-Deb-AMD64" from channel list dropdown
        And I click on "Update Mapping"
        Then I should see a "SUSE Linux Enterprise Server 15 SP 4 iSeries modified" link in the table
        And I should see a "test-channel-deb-amd64" link in the table

    # delete all maps to reset the machine to the default state
    Scenario: Cleanup: delete the map created for x68_64 suse clients
        When I am on the "Distribution Channel Mapping" page
        Given I see a "SUSE Linux Enterprise Server 15 SP 4 modified" link in the table
        And I see a "x86_64" architecture description in the table
        When I follow "SUSE Linux Enterprise Server 15 SP 4 modified"
        Then I should see a "Update Distribution Channel Map" text
        And I should see a "Delete Distribution Channel" link
        When I follow "Delete Distribution Channel Mapping"
        Then I should see a "Delete Distribution Channel Map" text
        When I click on "Delete Mapping"
        Then I should not see a "SUSE Linux Enterprise Server 15 SP 4 modified" link
    
    Scenario: Cleanup: delete the map created for x68_64 ubuntu clients
        When I am on the "Distribution Channel Mapping" page
        Given I see a "Ubuntu 22.04.01 LTS modified" link in the table
        And I see a "x86_64" architecture description in the table
        When I follow "Ubuntu 22.04.01 LTS modified"
        Then I should see a "Update Distribution Channel Map" text
        And I should see a "Delete Distribution Channel" link
        When I follow "Delete Distribution Channel Mapping"
        Then I should see a "Delete Distribution Channel Map" text
        When I click on "Delete Mapping"
        Then I should not see a "Ubuntu 22.04.01 LTS modified" link
    
    Scenario: Cleanup: delete the map created for i586 clients
        When I am on the "Distribution Channel Mapping" page
        Given I see a "SUSE Linux Enterprise Server 15 SP 4 iSeries modified" link in the table
        And I see a "x86_64" architecture description in the table
        When I follow "SUSE Linux Enterprise Server 15 SP 4 iSeries modified"
        Then I should see a "Update Distribution Channel Map" text
        And I should see a "Delete Distribution Channel" link
        When I follow "Delete Distribution Channel Mapping"
        Then I should see a "Delete Distribution Channel Map" text
        When I click on "Delete Mapping"
        Then I should not see a "SUSE Linux Enterprise Server 15 SP 4 iSeries modified" link

    # A quick sanity check to asess whether the DCM page is back to its default state
    Scenario: Cleanup: Sanity check that the page is its in default state
        When I am on the "Distribution Channel Mapping" page
        Then I should see a "Distribution Channel Mapping" text
        And I should see a "Channel List" link in the left menu
        And I should see a "Package Search" link in the left menu
        And I should see a "Manage" link in the left menu
        And I should see a "Distribution Channel Mapping" link in the left menu
        And I should see a "Create Distribution Channel Mapping" link
        And I should see a "No distribution channel mappings currently exist." text in the content area
