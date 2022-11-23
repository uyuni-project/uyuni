# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Run Cobbler Sync via WebUI

  Scenario: Login as admin
    Given I am authorized for the "Admin" section

  Scenario: Sanity check for page existance
    When I follow the left menu "Admin > Manager Configuration > Cobbler"
    Then I should see a "Uyuni Configuration - Cobbler. " text
    And I should see a "Setup your Uyuni Cobbler settings below. " text
    And I should see a "Cobbler sync is used to repair or rebuild the contents /srv/tftpboot or /srv/www/cobbler when manual modification of cobbler has occurred. " text
    And I should see a "For more information refer to the 'cobbler' man page. " text
    And I should see a "Run Cobbler Sync" text in the content area
    And I should see a "Update" button

  Scenario: Run Cobbler Sync via button and validate the task ran
    When I follow the left menu "Admin > Manager Configuration > Cobbler"
    And I click on "Update"
    Then I should see a "Cobbler Sync action was successfully executed. Look at /var/log/cobbler/*.log for more information" text
