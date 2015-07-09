# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

#@wip
Feature: Explore the Channels page
  In Order to validate completeness of the Channels page and it's subpages
  As an authorized user
  I want to see all the texts and links

  Background:
    Given I am testing channels

  Scenario: Completeness of Channels page
    When I follow "Channels"
    Then I should see a "Full Software Channel List" text
    And I should see a "Software Channels" link in the left menu
    And I should see a "All Channels" link in the left menu
    And I should see a "Popular Channels" link in the left menu
    And I should see a "My Channels" link in the left menu
    And I should see a "Shared Channels" link in the left menu
    And I should see a "Retired Channels" link in the left menu
    And I should see a "Package Search" link in the left menu
    And I should see a "Manage Software Channels" link in the left menu
    And I should see a "All Channels" link in the content area
    And I should see a "Popular Channels" link in the content area
    And I should see a "My Channels" link in the content area
    And I should see a "Shared Channels" link in the content area
    And I should see a "Retired Channels" link in the content area

  Scenario: Completeness of Channels page
    When I follow "Channels"
    When I follow "Popular Channels" in the left menu
    Then I should see a "Popular Channels" text

  Scenario: Check Packages in SLES11-SP3-Updates x86_64 Channel
    When I follow "Channels"
    And I follow "SLES11-SP3-Updates x86_64 Channel"
    And I follow "Packages"
    Then I should see package "andromeda-dummy-2.0-1.1.noarch"
    And I should see package "hoag-dummy-1.1-2.1.i586"
    And I should see package "hoag-dummy-1.1-2.1.x86_64"
    And I should see package "milkyway-dummy-2.0-1.1.i586"
    And I should see package "milkyway-dummy-2.0-1.1.x86_64"
    And I should see package "virgo-dummy-2.0-1.1.noarch"

  Scenario: Check Package metadata displayed in WebUI
    When I follow "Channels"
    And I follow "SLES11-SP3-Updates x86_64 Channel"
    And I follow "Packages"
    And I follow "andromeda-dummy-2.0-1.1.noarch"
    Then I should see a "This is the andromeda dummy package used for testing SUSE Manager" text
    And I should see a "SLES11-SP3-Updates x86_64 Channel" link
    And I should see a "openSUSE Build Service" text
    And I should see a "SHA256sum:" text
    And I should see a "631f37496202385e0cb7ab83c59accf70e21c24f14f5201ac26a42c0ec501bb9" text
    And I should see a "packages/1/3bb/andromeda-dummy/2.0-1.1/noarch/3bb3a56e6654f14300ab815c3f6e2af848874c829541b4e1b342262bb2f72d30/andromeda-dummy-2.0-1.1.noarch.rpm" text

  Scenario: Check Package dependencies page
    When I follow "Channels"
    And I follow "SLES11-SP3-Updates x86_64 Channel"
    And I follow "Packages"
    And I follow "andromeda-dummy-2.0-1.1.noarch"
    And I follow "Dependencies"
    Then I should see a "pam" text
    And I should see a "rpmlib(PayloadIsLzma) <= 4.4.6-1" text
    And I should see a "andromeda-dummy = 2.0-1.2" text

  Scenario: Check Package Changelog page
    When I follow "Channels"
    And I follow "SLES11-SP3-Updates x86_64 Channel"
    And I follow "Packages"
    And I follow "andromeda-dummy-2.0-1.1.noarch"
    And I follow "Change Log"
    Then I should see a "mc@suse.de" text
    And I should see a "version 2.0" text

  Scenario: Check Package Filelist page
    When I follow "Channels"
    And I follow "SLES11-SP3-Updates x86_64 Channel"
    And I follow "Packages"
    And I follow "andromeda-dummy-2.0-1.1.noarch"
    And I follow "File List"
    Then I should see a "This package contains the following files." text
    And I should see a "/usr/share/doc/packages/andromeda-dummy/COPYING" text
    And I should see a "md5: 94d55d512a9ba36caa9b7df079bae19f" text
