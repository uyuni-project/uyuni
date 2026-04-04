# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Very first settings
  In order to use the product
  As the admin user
  I want to create the organisation, the first users and set the HTTP proxy

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create testing username
    When I create a user with name "testing" and password "testing"
