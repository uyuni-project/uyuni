# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test the notification/notification-messages feature
  
  Scenario: Check the unread notification counter is correct
    Given I am authorized as "admin" with password "admin"
    When I follow "Notification Messages" in the left menu
    And I wait until I see "The server has collected the following notification messages." text
    Then the notification badge and the table should count the same amount of messages
