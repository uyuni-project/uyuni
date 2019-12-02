# Copyright (c) 2019-2020 SUSE LLC
# Licensed under the terms of the MIT license.

@scc_credentials
Feature: Content lifecycle

  Scenario: Create a content lifecycle project
    Given I am authorized with the feature's user
    When I follow the left menu "Content Lifecycle > Projects"
    Then I should see a "Content Lifecycle Projects" text
    And I should see a "There are no entries to show." text
    When I follow "Create Project"
    Then I should see a "Create a new Content Lifecycle Project" text
    And I should see a "Project Properties" text
    When I enter "clp_label" as "label"
    And I enter "clp_name" as "name"
    And I enter "clp_desc" as "description"
    And I click on "Create"
    Then I wait until I see "Content Lifecycle Project - clp_name" text

  Scenario: Verify the content lifecycle project page
    Given I am authorized with the feature's user
    When I follow the left menu "Content Lifecycle > Projects"
    Then I should see a "clp_name" text
    And I should see a "clp_desc" text
    When I follow "clp_name"
    Then I should see a "Project Properties" text
    And I should see a "Versions history" text
    And I should see a "Sources" text
    And I should see a "Filters" text
    And I should see a "Environment Lifecycle" text

  Scenario: Add a source to the project
    Given I am authorized with the feature's user
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    And I follow "Attach/Detach Sources"
    And I select "SLES12-SP5-Pool for x86_64" from "selectedBaseChannel"
    And I click on "Save"
    Then I wait until I see "SLES12-SP5-Pool for x86_64" text
    And I should see a "Version 1: (draft - not built) - Check the changes below" text

@uyuni
  Scenario: Verify added sources for Uyuni
    Given I am authorized with the feature's user
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    And I should see a "SLES12-SP5-Updates for x86_64" text
    And I should see a "Build (2)" text

@susemanager
  Scenario: Verify added sources for SUSE Manager
    Given I am authorized with the feature's user
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    And I should see a "SLE-Manager-Tools12-Updates for x86_64 SP5" text
    And I should see a "SLES12-SP5-Updates for x86_64" text
    And I should see a "SLE-Manager-Tools12-Pool for x86_64 SP5" text
    And I should see a "Build (4)" text

  Scenario: Add environments to the project
    Given I am authorized with the feature's user
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "No environments created" text
    When I follow "Add Environment"
    And I enter "dev_name" as "name"
    And I enter "dev_desc" as "description"
    And I click on "Save"
    Then I wait until I see "dev_name" text
    And I should see a "dev_desc" text
    When I follow "Add Environment"
    And I enter "prod_name" as "name"
    And I enter "prod_desc" as "description"
    And I click on "Save"
    Then I wait until I see "prod_name" text
    And I should see a "prod_desc" text
    When I follow "Add Environment"
    And I enter "qa_name" as "name"
    And I enter "qa_desc" as "description"
    And I select "prod_name" from "predecessorLabel"
    And I click on "Save"
    Then I wait until I see "qa_name" text
    And I should see a "qa_desc" text

@uyuni
  Scenario: Build the sources in the project for Uyuni
    Given I am authorized with the feature's user
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "not built" text in the environment "qa_name"
    When I click on "Build (2)"
    Then I should see a "Version 1 history" text
    When I enter "test version message 1" as "message"
    And I click the environment build button
    And I wait until I see "Version 1 successfully built into dev_name" text
    Then I should see a "Version 1: test version message 1" text
    And I wait at most 600 seconds until I see "Built" text

@susemanager
  Scenario: Build the sources in the project for SUSE Manager
    Given I am authorized with the feature's user
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "not built" text in the environment "qa_name"
    When I click on "Build (4)"
    Then I should see a "Version 1 history" text
    When I enter "test version message 1" as "message"
    And I click the environment build button
    And I wait until I see "Version 1 successfully built into dev_name" text
    Then I should see a "Version 1: test version message 1" text
    And I wait at most 600 seconds until I see "Built" text

  Scenario: Promote promote the sources in the project
    Given I am authorized with the feature's user
    When I follow the left menu "Content Lifecycle > Projects"
    Then I should see a "clp_name" text
    And I should see a "clp_desc" text
    And I should see a "dev_name > qa_name > prod_name" text
    When I follow "clp_name"
    Then I should see a "qa_desc" text in the environment "qa_name"
    And I should see a "not built" text in the environment "qa_name"
    When I click promote from Development to QA
    And I click on "Promote environment" in "Promote version 1 into qa_name" modal
    Then I wait until I see "Version 1: test version message 1" text in the environment "qa_name"
    When I click promote from QA to Production
    And I click on "Promote environment" in "Promote version 1 into prod_name" modal
    Then I wait until I see "Version 1: test version message 1" text in the environment "prod_name"

  Scenario: Add new sources and promote again
    Given I am authorized with the feature's user
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "Build (0)" text
    When I follow "Attach/Detach Sources"
    And I add the "Test Base Channel" channel to sources
    And I click on "Save"
    Then I wait until I see "Test Base Channel" text
    And I should see a "Build (1)" text
    And I should see a "Version 2: (draft - not built) - Check the changes below" text
    When I click on "Build (1)"
    Then I wait until I see "Version 2 history" text
    When I enter "test version message 2" as "message"
    And I click the environment build button
    Then I wait until I see "Version 2: test version message 2" text
    When I click promote from Development to QA
    And I click on "Promote environment" in "Promote version 2 into qa_name" modal
    Then I wait until I see "Version 2: test version message 2" text in the environment "qa_name"
    When I click promote from QA to Production
    And I click on "Promote environment" in "Promote version 2 into prod_name" modal
    Then I wait until I see "Version 2: test version message 2" text in the environment "prod_name"

  Scenario: Clean up the Content Lifecycle Management feature
    Given I am authorized with the feature's user
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    When I click on "Delete"
    And I click on "Delete" in "Delete Project" modal
    And I should see a "There are no entries to show." text
