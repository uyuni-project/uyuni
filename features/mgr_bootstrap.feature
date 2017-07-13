# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: mgr-bootstrap generation and registration
  In Order to validate correct working of mgr-bootstrap command
  As user root
  I want to be able to create a bootstrap script
  And register this client with it.

  Scenario: Create the bootstrap script - traditional
    When I execute mgr-bootstrap "--script=bootstrap-test.sh --no-up2date --allow-config-actions --allow-remote-commands --traditional"
    Then I want to get "* bootstrap script (written):"
     And I want to get "    '/srv/www/htdocs/pub/bootstrap/bootstrap-test.sh'"

  Scenario: register this client using the bootstrap script
    When I fetch "pub/bootstrap/bootstrap-test.sh" from server
    And I execute "bootstrap-test.sh"
    Then I should see this client in spacewalk
    And "man" is installed on "client"
    And config-actions are enabled
    And remote-commands are enabled

  Scenario: Cleanup script
   Then I run "rm /srv/www/htdocs/pub/bootstrap/bootstrap-test.sh" on "server"
