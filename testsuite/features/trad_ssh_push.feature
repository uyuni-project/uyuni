# Copyright (c) 2016-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Register a traditional system to be managed via SSH push

  Scenario: Delete the traditional client for ssh-reverse bootrap
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle-client" should not be registered

  Scenario: Create an activation key for SSH push
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SSH push key" as "description"
    And I enter "ssh-push" as "key"
    And I select "Push via SSH" from "contact-method"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SSH push key has been created." text

  Scenario: Create an activation key for SSH push via tunnel
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SSH push via tunnel key" as "description"
    And I enter "ssh-push-tunnel" as "key"
    And I select "Push via SSH tunnel" from "contact-method"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SSH push via tunnel key has been created." text

  Scenario: Create bootstrap script for traditional SSH push via tunnel
    When I execute mgr-bootstrap "--activation-keys=1-ssh-push-tunnel --script=bootstrap-ssh-push-tunnel.sh --no-up2date --traditional"
    Then I should get "* bootstrap script (written):"
    And I should get "    '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'"

  Scenario: Register this client for SSH push via tunnel
    When I register this client for SSH push via tunnel
    Then I should see "sle-client" in spacewalk

  Scenario: Check this client's contact method
    Given I am on the Systems overview page of this "sle-client"
    Then I should see a "Push via SSH tunnel" text

  Scenario: Cleanup: delete the traditional SSH push client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle-client" should not be registered

  Scenario: Cleanup: delete the activation key for SSH push
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SSH push key" in the content area
    And I follow "Delete Key"
    And I click on "Delete Activation Key"
    And I should see a "Activation key SSH push key has been deleted." text

  Scenario: Cleanup: delete the activation key for SSH push via tunnel
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SSH push via tunnel key" in the content area
    And I follow "Delete Key"
    And I click on "Delete Activation Key"
    And I should see a "Activation key SSH push via tunnel key has been deleted." text

  Scenario: Cleanup: hosts file of traditional client via SSH tunnel
    Given I am on the Systems page
    And I run "sed -i '/127.0.1.1/d' /etc/hosts" on "sle-client"
    And I run "rm /srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh" on "server"
    And I remove server hostname from hosts file on "sle-client"
 
  Scenario: Cleanup: register a traditional client after SSH push tests
    When I register using "1-SUSE-DEV-x86_64" key
    Then I should see "sle-client" in spacewalk
