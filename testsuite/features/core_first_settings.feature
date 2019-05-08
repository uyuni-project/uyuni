# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Very first settings
  In order to use the product
  As the admin user
  I want to create the organisation, the first users and set the HTTP proxy

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
    Then I am logged in

  Scenario: Create testing username
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Users > User List > Active"
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
    When I follow the left menu "Users > User List > Active"
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
    When I wait until mgr-sync refresh is finished

  Scenario: Check services which should run
    Then service "atftpd" is enabled on "server"
    And service "atftpd" is active on "server"
    And service "auditlog-keeper" is enabled on "server"
    And service "auditlog-keeper" is active on "server"
    And service "cobblerd" is enabled on "server"
    And service "cobblerd" is active on "server"
    And service "jabberd" is enabled on "server"
    And service "jabberd" is active on "server"
    And service "osa-dispatcher" is enabled on "server"
    And service "osa-dispatcher" is active on "server"
    And service "rhn-search" is enabled on "server"
    And service "rhn-search" is active on "server"
    And service "salt-api" is enabled on "server"
    And service "salt-api" is active on "server"
    And service "salt-master" is enabled on "server"
    And service "salt-master" is active on "server"
    And service "taskomatic" is enabled on "server"
    And service "taskomatic" is active on "server"
    And service "apache2" is enabled on "server"
    And service "apache2" is active on "server"
    And service "tomcat" is enabled on "server"
    And service "tomcat" is active on "server"

@server_http_proxy
  Scenario: Setup HTTP proxy
    When I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Setup Wizard"
    Then I should see a "HTTP Proxy Hostname" text
    And I should see a "HTTP Proxy Username" text
    And I should see a "HTTP Proxy Password" text
    When I enter the address of the HTTP proxy as "HTTP Proxy Hostname"
    And I enter "suma" as "HTTP Proxy Username"
    And I enter "P4$$word" as "HTTP Proxy Password"
    And I click on "Save and Verify"
    Then I see verification succeeded

  Scenario: Detect latest Salt changes on the server
    When I query latest Salt changes on "server"
