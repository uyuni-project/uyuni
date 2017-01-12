# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Register a client
  In Order register a client to the spacewalk server
  As the root user
  I want to call rhnreg_ks

  Scenario: Register a client
    When I register using an activation key
    Then I should see this client in spacewalk

  Scenario: trigger the creation of a cobbler system record
    Given I am authorized
    And I follow "Home" in the left menu
    And I follow "Systems"
    And I follow "Overview" in the left menu
    And I follow this client link
    And I follow "Provisioning"
    And I click on "Create PXE installation configuration"
    And I click on "Continue"
    Then file "/srv/tftpboot/pxelinux.cfg/01-*" contains "ks="
