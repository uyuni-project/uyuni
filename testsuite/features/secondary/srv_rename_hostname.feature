# Copyright (c) 2022-2023 SUSE LLC
# Licensed under the terms of the MIT License.
#
# This feature can cause failures in the following features:
# All following features
# If the server fails to reboot properly
# or the cleanup fails and renders the server unreachable.

@skip_if_github_validation
@skip_if_cloud
@skip_if_container_server
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
    And I restart the spacewalk service
