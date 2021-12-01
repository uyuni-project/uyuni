# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Restart the spacewak services via UI 

  Scenario:Restart the SUSE Manager through the WebUI Admin option Log in as admin user
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Manager Configuration > Restart"
    And I check "restart"
    And I click on "Restart"
    And I wait for "300" seconds
    And I wait until radio button "restart" is unchecked, refreshing the page
