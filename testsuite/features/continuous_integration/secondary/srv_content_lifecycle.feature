# Copyright (c) 2019-2023 SUSE LLC
# Licensed under the terms of the MIT license.

@scc_credentials
@scope_content_lifecycle_management
Feature: Content lifecycle

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

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

  Scenario: Add a source to the project
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    And I click on "Attach/Detach Sources"
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "selectedBaseChannel"
    And I exclude the recommended child channels
    And I click on "Save"
    And I wait until I see "SLE-Product-SLES15-SP4-Pool for x86_64" text
    Then I should see a "Version 1: (draft - not built) - Check the changes below" text

  Scenario: Verify added sources
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "SLE-Product-SLES15-SP4-Updates for x86_64" text
    And I should see a "Build (2)" text

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

  Scenario: Add new sources and promote again
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    Then I should see a "Build (0)" text
    When I click on "Attach/Detach Sources"
    And I uncheck "Vendors"
    And I add the "Fake Base Channel" channel to sources
    And I click on "Save"
    Then I wait until I see "Fake Base Channel" text
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

  Scenario: Cleanup: remove the Content Lifecycle Management project
    When I follow the left menu "Content Lifecycle > Projects"
    And I follow "clp_name"
    And I click on "Delete"
    And I click on "Delete" in "Delete Project" modal
    Then I should not see a "clp_name" text

  Scenario: Cleanup: remove the created channels
    When I delete these channels with spacewalk-remove-channel:
      |clp_label-prod_label-fake_base_channel|
      |clp_label-prod_label-sle-product-sles15-sp4-updates-x86_64|
      |clp_label-qa_label-fake_base_channel|
      |clp_label-qa_label-sle-product-sles15-sp4-updates-x86_64|
      |clp_label-dev_label-fake_base_channel|
      |clp_label-dev_label-sle-product-sles15-sp4-updates-x86_64|
    And I delete these channels with spacewalk-remove-channel:
      |clp_label-prod_label-sle-product-sles15-sp4-pool-x86_64|
      |clp_label-qa_label-sle-product-sles15-sp4-pool-x86_64|
      |clp_label-dev_label-sle-product-sles15-sp4-pool-x86_64|
    And I list channels with spacewalk-remove-channel
    Then I shouldn't get "clp_label"
