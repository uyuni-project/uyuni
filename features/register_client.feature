# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Register a client
  In Order register a client to the spacewalk server
  As the root user
  I want to call rhnreg_ks

  Scenario: Register a client
    Given I am root
    When I register using an activation key
    Then I should see this client in spacewalk

   @pxe_env
   Scenario: trigger the creation of a cobbler system record
     Given I am authorized
     When  I follow this client link
      And I follow "Provisioning"
      And I click on "Create Cobbler System Record"
      And I click on "Continue"
     Then file "/srv/tftpboot/pxelinux.cfg/01-*" contains "ks="

