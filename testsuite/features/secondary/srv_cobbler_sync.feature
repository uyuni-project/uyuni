# Copyright (c) 2022-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@scope_cobbler
Feature: Run Cobbler Sync via WebUI

  Scenario: Login as admin
    Given I am authorized for the "Admin" section

  Scenario: Start Cobbler monitoring
    When I start local monitoring of Cobbler
    And I backup Cobbler settings file

  @uyuni
  Scenario: Check that the Cobbler Settings Page exists
    When I follow the left menu "Admin > Manager Configuration > Cobbler"
    Then I should see a "Uyuni Configuration - Cobbler. " text
    And I should see a "Setup your Uyuni Cobbler settings below. " text
    And I should see a "Cobbler sync is used to repair or rebuild the contents /srv/tftpboot or /srv/www/cobbler when manual modification of cobbler has occurred. " text
    And I should see a "For more information refer to the 'cobbler' man page. " text
    And I should see a "Run Cobbler Sync" text in the content area
    And I should see a "Update" button
  
  @susemanager
  Scenario: Check that the Cobbler Settings Page exists
    When I follow the left menu "Admin > Manager Configuration > Cobbler"
    Then I should see a "SUSE Manager Configuration - Cobbler. " text
    And I should see a "Setup your SUSE Manager Cobbler settings below. " text
    And I should see a "Cobbler sync is used to repair or rebuild the contents /srv/tftpboot or /srv/www/cobbler when manual modification of cobbler has occurred. " text
    And I should see a "For more information refer to the 'cobbler' man page. " text
    And I should see a "Run Cobbler Sync" text in the content area
    And I should see a "Update" button

  Scenario: Run Cobbler Sync via button and validate UI output
    When I follow the left menu "Admin > Manager Configuration > Cobbler"
    And I click on "Update"
    Then I should see a "Cobbler Sync action was successfully executed. Look at /var/log/cobbler/*.log for more information" text

  Scenario: Run Cobbler Sync via button and verify task timestamp in Last Execution Times Page
    When I follow the left menu "Admin > Manager Configuration > Cobbler"
    And I click on "Update"
    And I follow the left menu "Admin > Task Engine Status > Last Execution Times"
    Then I should see the correct timestamp for task "Cobbler Sync:"

  Scenario: Check for errors in Cobbler monitoring
    Then the local logs for Cobbler should not contain errors
