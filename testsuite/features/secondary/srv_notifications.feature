# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: Test the notification/notification-messages feature

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Check the unread notification counter is correct
    When I follow the left menu "Home > Notification Messages"
    And I wait until I see "The server has collected the following notification messages." text
    Then I follow "Unread Messages"
    And the notification badge and the table should count the same amount of messages

  Scenario: Delete notification-messages
    When I follow the left menu "Home > Notification Messages"
    And I wait until I see "The server has collected the following notification messages." text
    Then I follow "All Messages"
    Then I check the first notification message
    And I delete it via the "Delete selected messages" button

  Scenario: Flag a notification-message as read
    When I follow the left menu "Home > Notification Messages"
    And I wait until I see "The server has collected the following notification messages." text
    Then I follow "All Messages"
    Then I check the first notification message
    And I mark as read it via the "Mark selected as read" button
