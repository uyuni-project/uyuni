# Copyright (c) 2015-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_configuration_channels
Feature: The channels page
  In Order to use the channels page and its subpages
  As an authorized user
  I want to see all the texts and links

  Scenario: Completeness of the channels page
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Channel List > All"
    Then I should see a "Full Software Channel List" text
    And I should see a "Channel List" link in the left menu
    And I should see a "All" link in the left menu
    And I should see a "Popular" link in the left menu
    And I should see a "My Channels" link in the left menu
    And I should see a "Shared" link in the left menu
    And I should see a "Retired" link in the left menu
    And I should see a "Package Search" link in the left menu
    And I should see a "Manage" link in the left menu
    And I should see a "All" link in the content area
    And I should see a "Popular" link in the content area
    And I should see a "My Channels" link in the content area
    And I should see a "Shared" link in the content area
    And I should see a "Retired" link in the content area

  Scenario: Popular channels
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Channel List > Popular"
    Then I should see a "Popular" text

  Scenario: Check packages in test channel
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    Then I should see package "andromeda-dummy-2.0-1.1.noarch"
    And I should see package "hoag-dummy-1.1-1.1.i586"
    And I should see package "hoag-dummy-1.1-1.1.x86_64"
    And I should see package "milkyway-dummy-2.0-1.1.i586"
    And I should see package "milkyway-dummy-2.0-1.1.x86_64"
    And I should see package "virgo-dummy-2.0-1.1.noarch"

  Scenario: Check package metadata
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "andromeda-dummy-2.0-1.1.noarch"
    Then I should see a "This is the andromeda dummy package used for testing SUSE Manager" text
    And I should see a "Test-Channel-x86_64" link
    And I should see a "build.opensuse.org" text
    And I should see a "SHA256sum:" text
    And I should see a "packages/1/ba3/andromeda-dummy/2.0-1.1/noarch/ba3f6d939fce43b60f4d20a09887e211f11024b61defb246dd62705bf4f4ced0/andromeda-dummy-2.0-1.1.noarch.rpm" text

  Scenario: Check package dependencies page
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "andromeda-dummy-2.0-1.1.noarch"
    And I follow "Dependencies"
    Then I should see a "pam" text
    And I should see a "rpmlib(PayloadIsXz) <= 5.2-1" text
    And I should see a "andromeda-dummy = 2.0-1.1" text

  Scenario: Check package change log page
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "andromeda-dummy-2.0-1.1.noarch"
    And I follow "Change Log"
    Then I should see a "mc@suse.de" text
    And I should see a "version 2.0" text

  Scenario: Check package file list page
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "andromeda-dummy-2.0-1.1.noarch"
    And I follow "File List"
    Then I should see a "This package contains the following files." text
    And I should see a "/usr/share/doc/packages/andromeda-dummy/COPYING" text
    And I should see a "sha256: 32b1062f7da84967e7019d01ab805935caa7ab7321a7ced0e30ebe75e5df1670" text
