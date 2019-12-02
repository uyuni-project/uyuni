# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test the notification/notification-messages feature

  Scenario: Check the unread notification counter is correct
    Given I am authorized with the feature's user
    When I follow the left menu "Home > Notification Messages"
    And I wait until I see "The server has collected the following notification messages." text
    Then I follow "Unread Messages"
    And the notification badge and the table should count the same amount of messages

  Scenario: Delete notification-messages
    Given I am authorized with the feature's user
    When I follow the left menu "Home > Notification Messages"
    And I wait until I see "The server has collected the following notification messages." text
    Then I follow "All Messages"
    Then I check the first notification message
    And I delete it via the "Delete selected messages" button

  Scenario: Flag a notification-message as read
    Given I am authorized with the feature's user
    When I follow the left menu "Home > Notification Messages"
    And I wait until I see "The server has collected the following notification messages." text
    Then I follow "All Messages"
    Then I check the first notification message
    And I mark as read it via the "Mark selected as read" button
