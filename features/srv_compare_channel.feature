# COPYRIGHT (c) 2015 SUSE LCC
# Licensed under the terms of the MIT license.

Feature: Compare Channels
  In Order to compare channels
  As a testing user
  I want to see the differences in the packages of two channels

  Scenario: Compare channel Packages (bsc#904690)
    Given I am on the manage software channels page
    When I follow "Clone 2 of Test-Channel-x86_64"
    And I follow "Packages" in the content area
    And I follow "Compare"
    And I select "Clone 3 of Test-Channel-x86_64" from "selected_channel"
    And I click on "View Packages"
    Then I should see a "andromeda-dummy" text
    And I should see a "2.0-1.1" link
    And I should see a "This channel only" text
