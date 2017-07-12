# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Register a system to be managed via SSH push
  Traditional:
  In order to register a system to Spacewalk for being managed via SSH push
  As the testing user
  I want to register a client using spacewalk-ssh-push-init

  Scenario: Delete the trad-client for ssh-reverse bootrap
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"

  Scenario: Create activation key for SSH push
    Given I am on the Systems page
    And I follow "Activation Keys" in the left menu
    And I follow "Create Key"
    When I enter "SSH push key" as "description"
    And I enter "ssh-push" as "key"
    And I select "Push via SSH" from "contact-method"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SSH push key has been created." text

  Scenario: Create activation key for SSH push via tunnel
    Given I am on the Systems page
    And I follow "Activation Keys" in the left menu
    And I follow "Create Key"
    When I enter "SSH push via tunnel key" as "description"
    And I enter "ssh-push-tunnel" as "key"
    And I select "Push via SSH tunnel" from "contact-method"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SSH push via tunnel key has been created." text

  Scenario: Create bootstrap script for traditional SSH push via tunnel
    When I execute mgr-bootstrap "--activation-keys=1-ssh-push-tunnel --script=bootstrap-ssh-push-tunnel.sh --no-up2date --traditional"
    Then I want to get "* bootstrap script (written):"
    And I want to get "    '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'"

  Scenario: Register this client for SSH push via tunnel
    When I register this client for SSH push via tunnel
    Then I should see this client in spacewalk

  Scenario: Check this client's contact method
    Given I am on the Systems overview page of this "sle-client"
    Then I should see a "Push via SSH tunnel" text

  Scenario: Delete the trad-ssh-push-client (CLEANUP)
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"

  Scenario: Delete the activation-key sshpush
    Given I am on the Systems page
    And I follow "Activation Keys" in the left menu
    And I follow "SSH push key" in the content area
    And I follow "Delete Key"
    And I click on "Delete Activation Key"
    And I should see a "Activation key SSH push key has been deleted." text

  Scenario: Delete the activation-key sshpush via tunnel
    Given I am on the Systems page
    And I follow "Activation Keys" in the left menu
    And I follow "SSH push via tunnel key" in the content area
    And I follow "Delete Key"
    And I click on "Delete Activation Key"
    And I should see a "Activation key SSH push via tunnel key has been deleted." text

  Scenario: Cleaunp host file of trad-client ssh-tunnel
    Given I am on the Systems page
    And I run "sed -i '/127.0.1.1/d' /etc/hosts" on "sle-client"
    And I remove server hostname from hosts trad-client
 
  Scenario: Register a trad-client, after ssh-push removal(cleanup),(need always tradclient)
    When I register using an activation key
    Then I should see this client in spacewalk
