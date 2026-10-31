# Copyright (c) 2017-2025 SUSE LLC
# SPDX-License-Identifier: MIT

# This feature is a dependency for:
# - features/secondary/min_docker_api.feature

Feature: Prepare server for using Docker

  Scenario: Create a Docker user with image administrators rights
    Given I am authorized for the "Admin" section
    When I create a user with name "docker" and password "docker" with roles "image_admin"

@no_auth_registry
  Scenario: Create an image store without credentials
    When I follow the left menu "Images > Stores"
    And I follow "Create"
    And I enter "galaxy-registry" as "label"
    And I enter the URI of the registry as "uri"
    And I click on "create-btn"
    Then I wait until table row contains a "galaxy-registry" text
    And I should see a "Items 1 - 1 of 1" text
