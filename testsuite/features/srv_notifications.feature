# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test the notification/notification-messages feature
  
  Scenario: Check the unread notification counter is correct
    Given I am authorized as "admin" with password "admin"
    When I follow "Notification Messages"
    And I wait until I see "The server has collected the following notification messages." text