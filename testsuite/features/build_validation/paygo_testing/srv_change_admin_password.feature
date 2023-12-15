# Copyright (c) 2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# By default, when deploying a server instance ( paygo or byos ) on aws,
# the admin user is created with the instance id as password.
# https://documentation.suse.com/suma/4.3/en/suse-manager/installation-and-upgrade/pubcloud-setup.html#_complete_setup_in_the_web_ui

# Because the admin password in our testsuite is not parameterizable, change the aws default password for admin

@paygo_server
Feature: Change aws server admin default password by admin

  Scenario: Change aws server admin default password by admin
    Given I am authorized for the "Paygo" section
    When I follow the left menu "Users > User List > Active"
    And I follow "admin" in the content area
    And I enter "admin" as "desiredpassword"
    And I enter "admin" as "desiredpasswordConfirm"
    And I click on "Update"
    Then I should see a "User information updated" text
    And I should see a "admin" text
