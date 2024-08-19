# Copyright (c) 2022-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
Feature: Export and import configuration channels with new ISS implementation
  Distribute configuration between servers
  Run export and import with ISS v2

  Scenario: Install inter server sync package
    When I install packages "inter-server-sync" on this "server"
    Then "inter-server-sync" should be installed on "server"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Create Config Channel"
    And I enter "Test Config Channel" as "cofName"
    And I enter "testconfigchannel" as "cofLabel"
    And I enter "This is a test configuration channel" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Test Config Channel" text

  Scenario: Add a configuration file to the test configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Test Config Channel"
    And I follow "Create Configuration File or Directory"
    And I enter "/etc/s-mgr/config" as "cffPath"
    And I enter "COLOR=white" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/s-mgr/config from channel Test Config Channel" text
    And file "/srv/susemanager/salt/manager_org_1/testconfigchannel/init.sls" should exist on server
    And file "/srv/susemanager/salt/manager_org_1/testconfigchannel/etc/s-mgr/config" should exist on server

  Scenario: Export data with ISS v2
    When I ensure folder "/tmp/export_iss_v2" doesn't exist on "server"
    When I export config channels "testconfigchannel" with ISS v2 to "/tmp/export_iss_v2"
    Then "/tmp/export_iss_v2" folder on server is ISS v2 export directory

  Scenario: Cleanup: remove the test configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Test Config Channel"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"
    Then file "/srv/susemanager/salt/manager_org_1/testconfigchannel/init.sls" should not exist on server
    And I should not see a "Test Config Channel" link

  Scenario: Import data with ISS v2
    When I import data with ISS v2 from "/tmp/export_iss_v2"

  Scenario: Check that the config channel was imported
    When I follow the left menu "Configuration > Channels"
    Then I should see a "Test Config Channel" link
    And file "/srv/susemanager/salt/manager_org_1/testconfigchannel/init.sls" should exist on server
    And file "/srv/susemanager/salt/manager_org_1/testconfigchannel/etc/s-mgr/config" should exist on server

  Scenario: Cleanup: remove the test configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Test Config Channel"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"
    Then file "/srv/susemanager/salt/manager_org_1/testconfigchannel/init.sls" should not exist on server
    And I should not see a "Test Config Channel" link

  Scenario: Cleanup: remove ISS v2 export folder
    When I ensure folder "/tmp/export_iss_v2" doesn't exist on "server"
