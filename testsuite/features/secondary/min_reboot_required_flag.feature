# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Reboot Required Indication

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  @sle_minion
  Scenario: Trigger reboot required indication for SUSE distributions
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Remote Command"
    And I enter "#!/bin/sh\ntouch /run/reboot-needed" as "script_body" text area
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I follow "Overview"
    And I wait until I see "The system requires a reboot" text, refreshing the page

  @sle_minion
  Scenario: Remove reboot required indication for SUSE distributions
    Given I am on the Systems overview page of this "sle_minion"
    Then I should see a "The system requires a reboot" text
    When I follow "Remote Command"
    And I enter "#!/bin/sh\nrm -rf /run/reboot-needed" as "script_body" text area
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I follow "Software" in the content area
    And I click on "Update Package List"
    Then I should see a "You have successfully scheduled a package profile refresh" text
    When I follow "Details"
    And I wait until I do not see "The system requires a reboot" text, refreshing the page

  @deblike_minion
  Scenario: Trigger reboot required indication for Debian-like distributions
    Given I am on the Systems overview page of this "deblike_minion"
    When I follow "Remote Command"
    And I enter "#!/bin/sh\ntouch /var/run/reboot-required" as "script_body" text area
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I follow "Overview"
    And I wait until I see "The system requires a reboot" text, refreshing the page

  @deblike_minion
  Scenario: Remove reboot required indication for Debian-like distributions
    Given I am on the Systems overview page of this "deblike_minion"
    Then I should see a "The system requires a reboot" text
    When I follow "Remote Command"
    And I enter "#!/bin/sh\nrm -rf /var/run/reboot-required" as "script_body" text area
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I follow "Software" in the content area
    And I click on "Update Package List"
    Then I should see a "You have successfully scheduled a package profile refresh" text
    When I follow "Details"
    And I wait until I do not see "The system requires a reboot" text, refreshing the page
