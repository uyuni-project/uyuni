# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test a reboot on a salt minion (ssh-managed)
         And test the webui for this feature.

  Scenario: Reboot a salt minion (ssh-managed) (centos)
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    And I click on "Reboot system"
    Then I wait and check that "ceos-minion" has rebooted
    And I wait until "salt-minion" service is up and running on "ceos-minion"
