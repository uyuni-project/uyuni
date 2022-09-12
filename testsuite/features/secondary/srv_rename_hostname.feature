# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT License.

#@scope_configuration_channels
Feature: Reconfiguring 
  Using the tool spacewalk-hostname-rename, reconfigure the Spacewalk server when its hostname or IP address has changed.
  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Change hostname and reboot server
    #Command prerequisites
    When I change server short hostname from hosts and hostname files as "uyu-serv"
    Then I reboot server through SSH
    And I run spacewalk-hostname-rename command on the server
    And I change back the server hostname
    And I reboot server through SSH
    And I run spacewalk-hostname-rename command on the server
