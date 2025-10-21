# Copyright (c) 2019-2025 SUSE LLC
# Licensed under the terms of the MIT license.

@scc_credentials
@scope_content_lifecycle_management
Feature: Content lifecycle

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Create CLM filter to remove all fonts packages
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    And I enter "remove fonts packages" as "filter_name"
    And I select "Package (Name)" from "type"
    And I select "contains" from "matcher"
    And I enter "fonts" as "name"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "remove fonts packages" text

  Scenario: Create a content lifecycle project
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "Create Project"
    Then I should see a "Create a new Content Lifecycle Project" text
    And I should see a "Project Properties" text
    When I enter "clp_label" as "label"
    And I enter "clp_name" as "name"
    And I enter "clp_desc" as "description"
    And I click on "Create"
    And I wait until I see "Content Lifecycle Project - clp_name" text

  Scenario: Verify the content lifecycle project page
    When I follow the left menu "Content Lifecycle > Projects"
    Then I should see a "clp_name" text
    And I should see a "clp_desc" text
    When I follow "clp_name"
    Then I should see a "Project Properties" text
    And I should see a "Versions history" text
    And I should see a "Sources" text
    And I should see a "Filters" text
    And I should see a "Environment Lifecycle" text

@susemanager
  Scenario: Add a source to the project
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    And I click on "Attach/Detach Sources"
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "selectedBaseChannel"
    And I exclude the recommended child channels
    And I click on "Save"
    And I wait until I see "SLE-Product-SLES15-SP4-Pool for x86_64" text
    Then I should see a "Version 1: (draft - not built) - Check the changes below" text

@uyuni
  Scenario: Add a source to the project
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    And I click on "Attach/Detach Sources"
    And I select "openSUSE Tumbleweed (x86_64)" from "selectedBaseChannel"
    And I wait until I see "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" text
    And I click on "Save"
    And I wait until I see "openSUSE Tumbleweed (x86_64)" text
    Then I should see a "Version 1: (draft - not built) - Check the changes below" text

@susemanager
  Scenario: Verify added sources
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "SLE-Product-SLES15-SP4-Updates for x86_64" text
    And I should see a "Build (2)" text

@uyuni
  Scenario: Verify added sources
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "openSUSE Tumbleweed (x86_64)" text
    And I should see a "Build (1)" text

  Scenario: Add fonts packages filter to the project
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "Content Lifecycle Project - clp_name" text
    When I click on "Attach/Detach Filters"
    And I check the "remove fonts packages" CLM filter
    And I click on "Save"
    And I wait until I see "Deny" text
    Then I should see a "remove fonts packages" text
    When I follow the left menu "Content Lifecycle > Filters"
    Then I should see a "clp_name" text

  Scenario: Add environments to the project
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "No environments created" text
    When I click on "Add Environment"
    And I enter "dev_name" as "name"
    And I enter "dev_label" as "label"
    And I enter "dev_desc" as "description"
    And I click on "Save"
    Then I wait until I see "dev_name" text
    And I should see a "dev_desc" text
    When I click on "Add Environment"
    And I enter "prod_name" as "name"
    And I enter "prod_label" as "label"
    And I enter "prod_desc" as "description"
    And I click on "Save"
    Then I wait until I see "prod_name" text
    And I should see a "prod_desc" text
    When I click on "Add Environment"
    And I enter "qa_name" as "name"
    And I enter "qa_label" as "label"
    And I enter "qa_desc" as "description"
    And I select "prod_name" from "predecessorLabel"
    And I click on "Save"
    Then I wait until I see "qa_name" text
    And I should see a "qa_desc" text

@susemanager
  Scenario: Build the sources in the project
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "not built" text in the environment "qa_name"
    When I click on "Build (3)"
    Then I should see a "Version 1 history" text
    When I enter "test version message 1" as "message"
    And I click the environment build button
    And I wait until I see "Version 1: test version message 1" text in the environment "dev_name"
    And I wait at most 600 seconds until I see "Built" text in the environment "dev_name"

@uyuni
  Scenario: Build the sources in the project
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "not built" text in the environment "qa_name"
    When I click on "Build (2)"
    Then I should see a "Version 1 history" text
    When I enter "test version message 1" as "message"
    And I click the environment build button
    And I wait until I see "Version 1: test version message 1" text in the environment "dev_name"
    And I wait at most 600 seconds until I see "Built" text in the environment "dev_name"

  Scenario: Promote the sources in the project
    When I follow the left menu "Content Lifecycle > Projects"
    Then I should see a "clp_name" text
    And I should see a "clp_desc" text
    And I should see a "dev_name > qa_name > prod_name" text
    When I follow "clp_name"
    Then I should see a "qa_desc" text in the environment "qa_name"
    And I should see a "not built" text in the environment "qa_name"
    When I click promote from Development to QA
    Then I should see a "Version 1: test version message 1" text
    And I click on "Promote environment" in "Promote version 1 into qa_name" modal
    Then I wait at most 600 seconds until I see "Built" text in the environment "qa_name"
    When I click promote from QA to Production
    Then I should see a "Version 1: test version message 1" text
    And I click on "Promote environment" in "Promote version 1 into prod_name" modal
    Then I wait at most 600 seconds until I see "Built" text in the environment "prod_name"

# flaky test
@skip_if_github_validation
  Scenario: Add new sources and promote again
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "Build (0)" text
    When I click on "Attach/Detach Sources"
    And I uncheck "Vendors"
    And I enter "Fake-Base-Channel-SUSE-like" in the placeholder "Search a channel"
    And I add the "Fake-Base-Channel-SUSE-like" channel to sources
    And I click on "Save"
    Then I wait until I see "Fake-Base-Channel-SUSE-like" text
    And I wait until I see "Build (1)" text
    And I should see a "Version 2: (draft - not built) - Check the changes below" text
    When I click on "Build (1)"
    Then I wait until I see "Version 2 history" text
    When I enter "test version message 2" as "message"
    And I click the environment build button
    Then I wait until I see "Version 2: test version message 2" text in the environment "dev_name"
    And I wait at most 600 seconds until I see "Built" text in the environment "dev_name"
    When I click promote from Development to QA
    Then I should see a "Version 2: test version message 2" text
    And I click on "Promote environment" in "Promote version 2 into qa_name" modal
    And I wait for "1" second
    Then I wait at most 600 seconds until I see "Built" text in the environment "qa_name"
    When I click promote from QA to Production
    Then I should see a "Version 2: test version message 2" text
    And I click on "Promote environment" in "Promote version 2 into prod_name" modal
    And I wait for "1" second
    Then I wait at most 600 seconds until I see "Built" text in the environment "prod_name"


  Scenario: Create a CLM filter of type Package(NEVRA) that allows packages whose version and release number are lower to a defined one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "mercury" as "filter_name"
    And I select "Package (NEVRA)" from "type"
    And I select "lower" from "matcher"
    And I enter "mercury" as "Package Name"
    And I enter "mercury" as "Epoch"
    And I enter "0.0.0" as "version"
    And I enter "0.0.0" as "Release"
    And I enter "x86_64" as "Architecture"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

   Scenario: Create a CLM filter of type Package(NEVRA) that denys packages whose version and release number are lower to a defined one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "venus" as "filter_name"
    And I select "Package (NEVRA)" from "type"
    And I select "lower" from "matcher"
    And I enter "venus" as "Package Name"
    And I enter "venus" as "Epoch"
    And I enter "0.0.0" as "version"
    And I enter "0.0.0" as "Release"
    And I enter "x86_64" as "Architecture"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

Scenario: Create CLM filter that allows packages of type Package (Provides Name)
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "cereal" as "filter_name"
    And I select "Package (Provides Name)" from "type"
    And I select "provides name" from "matcher"
    And I enter "cereal" as "Provides Name"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter that denys packages of type Package (Provides Name)
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "potato" as "filter_name"
    And I select "Package (Provides Name)" from "type"
    And I select "provides name" from "matcher"
    And I enter "potato" as "Provides Name"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

Scenario: Create CLM filter of type Package (Build date) that allows packages whose date is lower than a defined one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "cherry" as "filter_name"
    And I select "Package (Build date)" from "type"
    And I select "lower" from "matcher"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Package (Build date) that denys packages whose date is lower than a defined one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "drummer" as "filter_name"
    And I select "Package (Build date)" from "type"
    And I select "lower" from "matcher"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Advisory Name) that allows patches that are equal to a defined one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "africa-patch" as "filter_name"
    And I select "Patch (Advisory Name)" from "type"
    And I select "equals" from "matcher"
    And I enter "africa" as "Advisory name"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Advisory Name) that denys patches that are equal to a defined one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "asia-patch" as "filter_name"
    And I select "Patch (Advisory Name)" from "type"
    And I select "equals" from "matcher"
    And I enter "asia" as "Advisory name"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Advisory Type) that allows Security Advisory patches that are equal to a defined one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "key" as "filter_name"
    And I select "Patch (Advisory Type)" from "type"
    And I select "equals" from "matcher"
    And I check radio button "Security Advisory"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Advisory Type) that denys Security Advisory patches that are equal to a defined one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "geminis-patch" as "filter_name"
    And I select "Patch (Advisory Type)" from "type"
    And I select "equals" from "matcher"
    And I check radio button "Security Advisory"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Synopsis) that allows patches that that are equal to a defined one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "aries-patch" as "filter_name"
    And I select "Patch (Synopsis)" from "type"
    And I select "equals" from "matcher"
    And I enter "aries" as "Synopsis"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Synopsis) that denys patches that are equal to a defined one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "andromeda-patch" as "filter_name"
    And I select "Patch (Synopsis)" from "type"
    And I select "equals" from "matcher"
    And I enter "andromeda" as "Synopsis"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Keyword) that allows patches that contains Package Manager Restart Required keyword in its name
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "mars-patch" as "filter_name"
    And I select "Patch (Keyword)" from "type"
    And I select "contains" from "matcher"
    And I check radio button "Package Manager Restart Required"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Keyword) that denys patches that contains Package Manager Restart Required Keyword in its name
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "eurasia-patch" as "filter_name"
    And I select "Patch (Keyword)" from "type"
    And I select "contains" from "matcher"
    And I check radio button "Package Manager Restart Required"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Issue date) that allows patches whose date is greater or equal than a defined one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "milkyway-patch" as "filter_name"
    And I select "Patch (Issue date)" from "type"
    And I select "greater or equal" from "matcher"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Issue date) that denys patches whose date is greater or equal than a defined one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "venus-patch" as "filter_name"
    And I select "Patch (Issue date)" from "type"
    And I select "greater or equal" from "matcher"
    When I enter "solar" as "filter_name"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Contains Package Name) that allows patches that are equal to a specific one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "Triangulum-patch" as "filter_name"
    And I select "Patch (Contains Package Name)" from "type"
    And I select "equals" from "matcher"
    When I enter "Triangulum-patch" as "Package Name"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Contains Package Name) that denys patches that are equal to a specific one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "Pinwheel-patch" as "filter_name"
    And I select "Patch (Contains Package Name)" from "type"
    And I select "equals" from "matcher"
    When I enter "Pinwheel-patch" as "Package Name"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter that allows patches of type Patch(Contains Package Name) that matches to a specific one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "Sunflower-patch" as "filter_name"
    And I select "Patch (Contains Package Name)" from "type"
    And I select "matches" from "matcher"
    When I enter "Sunflower-patch" as "Package Name"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Contains Package Name) that denys patches that matches to a specific one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "Whirlpool-patch" as "filter_name"
    And I select "Patch (Contains Package Name)" from "type"
    And I select "matches" from "matcher"
    When I enter "Whirlpool-patch" as "Package Name"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Contains Package Provides Name) that allows patches with a specific name
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "Antennae-patch" as "filter_name"
    And I select "Patch (Contains Package Provides Name)" from "type"
    And I select "provides name" from "matcher"
    When I enter "Antennae-patch" as "Package Provides Name"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Contains Package Provides Name) that denys patches with a specific name
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "hat-patch" as "filter_name"
    And I select "Patch (Contains Package Provides Name)" from "type"
    And I select "provides name" from "matcher"
    When I enter "hat-patch" as "Package Provides Name"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Contains Package) that allows patches whose version is lower than a specific one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "Hubble-patch" as "filter_name"
    And I select "Patch (Contains Package)" from "type"
    And I select "version lower than" from "matcher"
    When I enter "Hubble-patch" as "Package Name"
    And I enter "Hubble-patch" as "Epoch"
    And I enter "0.0.0" as "Version"
    And I enter "0.0.0" as "Release"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Contains Package) that denys patches whose version is lower than a specific one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "galaxy-patch" as "filter_name"
    And I select "Patch (Contains Package)" from "type"
    And I select "version lower than" from "matcher"
    When I enter "galaxy-patch" as "Package Name"
    And I enter "galaxy-patch" as "Epoch"
    And I enter "0.0.0" as "Version"
    And I enter "0.0.0" as "Release"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Contains Package) that allows patches whose version is lower or equal than a specific one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "earth-patch" as "filter_name"
    And I select "Patch (Contains Package)" from "type"
    And I select "version lower or equal" from "matcher"
    When I enter "earth-patch" as "Package Name"
    And I enter "earth-patch" as "Epoch"
    And I enter "0.0.0" as "Version"
    And I enter "0.0.0" as "Release"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Patch(Contains Package) that denys patches whose version is lower or equal than a specific one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "moon-patch" as "filter_name"
    And I select "Patch (Contains Package)" from "type"
    And I select "version lower or equal" from "matcher"
    When I enter "moon-patch" as "Package Name"
    And I enter "moon-patch" as "Epoch"
    And I enter "0.0.0" as "Version"
    And I enter "0.0.0" as "Release"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter to enable Ruby 2.7 module
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    And I enter "ruby 2.7 module" as "filter_name"
    And I select "Module (Stream)" from "type"
    And I select "equals" from "matcher"
    And I enter "ruby" as "moduleName"
    And I enter "2.7" as "moduleStream"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter that allows Product Temporary Fix (All)
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "mars" as "filter_name"
    And I select "Product Temporary Fix (All)" from "type"
    And I select "all" from "matcher"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter that denys Product Temporary Fix (All)
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "mercury-patch" as "filter_name"
    And I select "Product Temporary Fix (All)" from "type"
    And I select "all" from "matcher"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Product Temporary Fix (Number) that allows packages of a version lower than a specific one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "jupiter-patch" as "filter_name"
    And I select "Product Temporary Fix (Number)" from "type"
    And I select "lower" from "matcher"
    And I enter "1" as "Number"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Create CLM filter of type Product Temporary Fix (Number) that denys packages of a version lower than a specific one
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "pluto-patch" as "filter_name"
    And I select "Product Temporary Fix (Number)" from "type"
    And I select "lower" from "matcher"
    And I enter "2" as "Number"
    And I check radio button "Deny"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

# This test fails, but this error is tracked in the bug 1238922
@skip_if_github_validation
  Scenario: Create CLM filter that allows packages versions that are equal to a specific Product Temporary Fix (Fixes Package Name)
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Create Filter"
    And I wait at most 10 seconds until I see modal containing "Create a new filter" text
    Then I should see a "Create a new filter" text
    When I enter "comet-patch" as "filter_name"
    And I select "Product Temporary Fix (Fixes Package Name)" from "type"
    And I select "equals" from "matcher"
    And I enter "comet-patch" as "Package Name"
    And I check radio button "Allow"
    And I click on "Save" in "Create a new filter" modal
    Then I should see a "Filter created successfully" text

  Scenario: Cleanup: remove the Content Lifecycle Management project
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    And I click on "Delete"
    And I click on "Delete" in "Delete Project" modal
    Then I should not see a "clp_name" text

  Scenario: Cleanup: remove the CLM filters
    When I follow the left menu "Content Lifecycle > Filters"
    And I click on "Select unused"
    And I click on "Delete"
    Then I should not see a "remove fonts packages" text
    And I should not see a "africa-patch" text
    And I should not see a "andromeda-patch" text
    And I should not see a "Antennae-patch" text
    And I should not see a "aries-patch" text
    And I should not see a "asia-patch" text
    And I should not see a "cereal" text
    And I should not see a "comet-patch" text
    And I should not see a "cherry" text
    And I should not see a "drummer" text
    And I should not see a "earth-patch" text
    And I should not see a "galaxy-patch" text
    And I should not see a "hat-patch" text
    And I should not see a "Hubble-patch" text
    And I should not see a "mars" text
    And I should not see a "mars-patch" text
    And I should not see a "mercury" text
    And I should not see a "milkyway-patch" text
    And I should not see a "moon-patch" text
    And I should not see a "Pinwheel-patch" text
    And I should not see a "pluto-patch" text
    And I should not see a "solar" text
    And I should not see a "Sunflower-patch" text
    And I should not see a "Triangulum-patch" text
    And I should not see a "venus" text
    And I should not see a "Whirlpool-patch" text
    And I should not see a "remove fonts packages" text
    And I should not see a "ruby 2.7 module" text
    And I should not see a "key" text

@susemanager
  Scenario: Cleanup: remove the created channels
    When I delete these channels with spacewalk-remove-channel:
      | clp_label-prod_label-fake-base-channel-suse-like           |
      | clp_label-prod_label-sle-product-sles15-sp4-updates-x86_64 |
      | clp_label-qa_label-fake-base-channel-suse-like             |
      | clp_label-qa_label-sle-product-sles15-sp4-updates-x86_64   |
      | clp_label-dev_label-fake-base-channel-suse-like            |
      | clp_label-dev_label-sle-product-sles15-sp4-updates-x86_64|
    And I delete these channels with spacewalk-remove-channel:
      |clp_label-prod_label-sle-product-sles15-sp4-pool-x86_64|
      |clp_label-qa_label-sle-product-sles15-sp4-pool-x86_64|
      |clp_label-dev_label-sle-product-sles15-sp4-pool-x86_64|
    And I list channels with spacewalk-remove-channel
    Then I shouldn't get "clp_label"

# flaky test
@skip_if_github_validation
@uyuni
  Scenario: Cleanup: remove the created channels
    When I delete these channels with spacewalk-remove-channel:
      | clp_label-prod_label-fake-base-channel-suse-like |
      | clp_label-prod_label-opensuse_tumbleweed-x86_64    |
      | clp_label-qa_label-fake-base-channel-suse-like   |
      | clp_label-qa_label-opensuse_tumbleweed-x86_64      |
      | clp_label-dev_label-fake-base-channel-suse-like  |
      | clp_label-dev_label-opensuse_tumbleweed-x86_64     |
    And I list channels with spacewalk-remove-channel
    Then I shouldn't get "clp_label"
