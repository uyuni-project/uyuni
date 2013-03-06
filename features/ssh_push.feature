# Copyright (c) 2013 Novell, Inc.
# Licensed under the terms of the MIT license.

# features/ssh_push.feature
Feature: Register a system to be managed via SSH push
  In order to register a system to Spacewalk for being managed via SSH push
  As the testing user
  I want to register a client using spacewalk-push-register

  Scenario: Create activation key for SSH push
    Given I am on the Systems page
      And I follow "Activation Keys" in the left menu
      And I follow "create new key"
     When I enter "SSH push key" as "description"
      And I enter "ssh-push" as "key"
      And I check "provisioning_entitled"
      And I select "Push via SSH" from "contact-method"
      And I click on "Create Activation Key"
     Then I should see a "Activation key SSH push key has been created." text

  Scenario: Create activation key for SSH push via tunnel
    Given I am on the Systems page
      And I follow "Activation Keys" in the left menu
      And I follow "create new key"
     When I enter "SSH push via tunnel key" as "description"
      And I enter "ssh-push-tunnel" as "key"
      And I check "provisioning_entitled"
      And I select "Push via SSH tunnel" from "contact-method"
      And I click on "Create Activation Key"
     Then I should see a "Activation key SSH push via tunnel key has been created." text

  Scenario: Create bootstrap script for SSH push via tunnel
    When I execute mgr-bootstrap "--activation-keys=1-ssh-push-tunnel --script=bootstrap-ssh-push-tunnel.sh --no-up2date"
    Then I want to get "* bootstrap script (written):"
     And I want to get "    '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'"

  Scenario: Register this client for SSH push via tunnel
    When I register this client for SSH push via tunnel
    Then I should see this client in spacewalk

  Scenario: Check this client's contact method
    Given I am on the Systems overview page of this client
     Then I should see a "Push via SSH tunnel" text

