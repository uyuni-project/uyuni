# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.
# After clicking the button we wait until we dont see the text. This normally
# means that the page was refreshed, but in case of a timeout or other error,
# we included a manual refresh and a simple navigation step to make sure the UI
# is indeed up and running after the restart.

@flaky
Feature: Restart the spacewalk services via UI

  Scenario: Restart the SUSE Manager through the WebUI Admin option
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Manager Configuration > Restart"
    And I check "restart"
    And I click on "Restart"
    And I wait until I see "restarting. If this page" text
    And I wait at most "300" seconds until I do not see "restarting. If this page" text
    And I refresh the page
    Then I follow the left menu "Admin > Manager Configuration > Restart"
