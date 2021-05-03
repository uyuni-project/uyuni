# Copyright (c) 2021 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Create a configuration channel and file

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Server side, Create a configuration channel and add a configuration file
    When I follow the left menu "Configuration > Channels"
    And I follow "Create Config Channel"
    And I enter "Mixed Channel" as "cofName"
    And I enter "mixedchannel" as "cofLabel"
    And I enter "This is a configuration channel for different system types" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Mixed Channel" text
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Create Configuration File or Directory"
    And I enter "/etc/s-mgr/config" as "cffPath"
    And I enter "COLOR=white" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/s-mgr/config from channel Mixed Channel" text
    And file "/srv/susemanager/salt/manager_org_1/mixedchannel/init.sls" should exist on server
    And file "/srv/susemanager/salt/manager_org_1/mixedchannel/etc/s-mgr/config" should exist on server
