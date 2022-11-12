# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT License.

@skip_if_cloud
Feature: Reconfigure the server's hostname
  As admin 
  In order to change the server's hostname
  I want to use the tool spacewalk-hostname-rename.

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Change hostname and reboot server
    #Command prerequisites
    When I change the server's short hostname from hosts and hostname files
    And I reboot the server through SSH
    And I run spacewalk-hostname-rename command on the server
  
  Scenario: Change hostname back
    When I change back the server's hostname
    And I reboot the server through SSH
    And I run spacewalk-hostname-rename command on the server

  Scenario: Cleanup after hostname rename test
    When I clean up the server's hosts file
