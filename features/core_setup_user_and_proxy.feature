# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create initial users
  In order to run the tests
  As the admin user
  I need to create the admin and a testing users

  Scenario: Create admin user and first organization
    Given I access the host the first time
    And I run "rm -Rf /srv/salt/*" on "server"
    When I go to the home page
    And I enter "SUSE Test" as "orgName"
    And I enter "admin" as "login"
    And I enter "admin" as "desiredpassword"
    And I enter "admin" as "desiredpasswordConfirm"
    And I select "Mr." from "prefix"
    And I enter "Admin" as "firstNames"
    And I enter "Admin" as "lastName"
    And I enter "galaxy-noise@suse.de" as "email"
    And I click on "Create Organization"
    Then I am logged-in

  Scenario: Create testing username
    Given I am authorized as "admin" with password "admin"
    When I follow "Users" in the left menu
    And I follow "User List" in the left menu
    And I follow "Active" in the left menu
    And I follow "Create User"
    And I enter "testing" as "login"
    And I enter "testing" as "desiredpassword"
    And I enter "testing" as "desiredpasswordConfirm"
    And I select "Mr." from "prefix"
    And I enter "Test" as "firstNames"
    And I enter "User" as "lastName"
    And I enter "galaxy-noise@suse.de" as "email"
    And I click on "Create Login"
    Then I should see a "Account testing created, login information sent to galaxy-noise@suse.de" text
    And I should see a "testing" link

  Scenario: Grant testing user administrative priviledges
    Given I am authorized as "admin" with password "admin"
    When I follow "Users" in the left menu
    And I follow "User List" in the left menu
    And I follow "Active" in the left menu
    And I follow "testing"
    And I check "role_org_admin"
    And I check "role_system_group_admin"
    And I check "role_channel_admin"
    And I check "role_activation_key_admin"
    And I check "role_config_admin"
    And I click on "Update"
    Then I should see a "User information updated" text
    And I should see a "testing" text

  Scenario: Wait for refresh of list of products to finish
    When I wait for mgr-sync refresh is finished

  Scenario: Check services which should run
    Then Service "atftpd" is enabled on the Server
    And Service "atftpd" is running on the Server
    And Service "auditlog-keeper" is enabled on the Server
    And Service "auditlog-keeper" is running on the Server
    And Service "cobblerd" is enabled on the Server
    And Service "cobblerd" is running on the Server
    And Service "jabberd" is enabled on the Server
    And Service "jabberd" is running on the Server
    And Service "osa-dispatcher" is enabled on the Server
    And Service "osa-dispatcher" is running on the Server
    And Service "rhn-search" is enabled on the Server
    And Service "rhn-search" is running on the Server
    And Service "salt-api" is enabled on the Server
    And Service "salt-api" is running on the Server
    And Service "salt-master" is enabled on the Server
    And Service "salt-master" is running on the Server
    And Service "taskomatic" is enabled on the Server
    And Service "taskomatic" is running on the Server
    And Service "apache2" is enabled on the Server
    And Service "apache2" is running on the Server
    And Service "tomcat" is enabled on the Server
    And Service "tomcat" is running on the Server

  Scenario: Setup HTTP proxy
    When I am authorized as "admin" with password "admin"
    And I follow "Admin" in the left menu
    And I follow "Setup Wizard" in the left menu
    Then I should see a "HTTP Proxy Hostname" text
    And I should see a "HTTP Proxy Username" text
    And I should see a "HTTP Proxy Password" text
    When I enter "galaxy-proxy.mgr.suse.de:3128" as "HTTP Proxy Hostname"
    And I enter "suma" as "HTTP Proxy Username"
    And I enter "P4$$word" as "HTTP Proxy Password"
    And I click on "Save and Verify"
    Then I see verification succeeded
