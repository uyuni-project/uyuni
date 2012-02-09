# Copyright (c) 2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Delete a system profile
  In Order to delete a system profile from spacewalk
  As the testing user
  I want to delete this client's system profile

  Scenario: Delete a system profile
    Given I am on the Systems overview page of this client
    When I follow "delete system"
     And I should see a "Confirm System Profile Deletion" text
     And I click on "Delete Profile"
     Then I should see a "System profile 1000010001 has been deleted." text

