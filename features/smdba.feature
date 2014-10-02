# Copyright (c) 2013 Novell, Inc.
# Licensed under the terms of the MIT license.

# features/smdba.feature
Feature: Verify SMDBA infrastructure
  In order to operate embedded database with SMDBA tool
  As the testing user
  I want to check if infrastructure is consistent

  Scenario: Check PostgreSQL database running
    When I execute smdba db-check
    Then I want to get "* online"
