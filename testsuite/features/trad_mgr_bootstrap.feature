# Copyright (c) 2015-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Generate a bootstrap script and use it to register a client

  Scenario: Create the bootstrap script for a traditional client
    When I execute mgr-bootstrap "--script=bootstrap-test.sh --no-up2date --allow-config-actions --allow-remote-commands --traditional"
    Then I should get "* bootstrap script (written):"
     And I should get "    '/srv/www/htdocs/pub/bootstrap/bootstrap-test.sh'"

  Scenario: Register this client using the bootstrap script
    When I fetch "pub/bootstrap/bootstrap-test.sh" to "sle-client"
    And I run "sh ./bootstrap-test.sh" on "sle-client"
    Then I should see "sle-client" in spacewalk
    And "sed" should be installed on "sle-client"
    And config-actions are enabled
    And remote-commands are enabled

  Scenario: Cleanup: remove client bootstrap scripts
   Then I run "rm /srv/www/htdocs/pub/bootstrap/bootstrap-test.sh" on "server"
   And I run "rm /root/bootstrap-test.sh" on "sle-client"
