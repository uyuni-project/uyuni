# Copyright (c) 2010-2011 Novell, Inc.
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
     And I should see a "Software Channels" link in element "sidenav"
     And I should see a "All Channels" link in element "sidenav"
     And I should see a "Popular Channels" link in element "sidenav"
     And I should see a "My Channels" link in element "sidenav"
     And I should see a "Shared Channels" link in element "sidenav"
     And I should see a "Retired Channels" link in element "sidenav"
     And I should see a "Package Search" link in element "sidenav"
     And I should see a "Manage Software Channels" link in element "sidenav"
     And I should see a "All Channels" link in element "content-nav"
     And I should see a "Popular Channels" link in element "content-nav"
     And I should see a "My Channels" link in element "content-nav"
     And I should see a "Shared Channels" link in element "content-nav"
     And I should see a "Retired Channels" link in element "content-nav"

  Scenario: Completeness of Channels page
    When I follow "Channels"
    When I follow "Popular Channels" in element "sidenav"
    Then I should see a "Popular Channels" text

  Scenario: Check Packages in SLES11-SP2-Updates x86_64 Channel
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
    Then I should see package "bitstream-vera-1.10-303.1.noarch"
     And I should see package "liblzma5-5.0.3-0.12.1.x86_64"
     And I should see package "sles-release-11.3-99.1.x86_64"
     And I should see package "xz-5.0.3-0.12.1.x86_64"

  Scenario: Check Package metadata displayed in WebUI
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "sles-release-11.3-99.1.x86_64"
    Then I should see a "SUSE Linux Enterprise offers a comprehensive" text
     And I should see a "SLES11-SP2-Updates x86_64 Channel" link
     And I should see a "openSUSE Build Service" text
     And I should see a "SHA1sum:" text
     And I should see a "3236c19066c784159cc425ba620a26c5914e5f99" text
     And I should see a "packages/1/323/sles-release/11.3-99.1/x86_64/3236c19066c784159cc425ba620a26c5914e5f99/sles-release-11.3-99.1.x86_64.rpm" text

  Scenario: Check Package dependencies page
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "sles-release-11.3-99.1.x86_64"
     And I follow "Dependencies"
    Then I should see a "distribution-release" text
     And I should see a "glibc >= 2.11.3" text
     And I should see a "sles-release = 11.3-99.1" text
     And I should see a "product()" text
     And I should see a "aalib < 1.4.0-306" text

  Scenario: Check Package Changelog page
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "sles-release-11.3-99.1.x86_64"
     And I follow "Change Log"
    Then I should see a "kukuk@suse.de" text
     And I should see a "Fix summary in prod file [bnc#477710]" text

  Scenario: Check Package Filelist page
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "sles-release-11.3-99.1.x86_64"
     And I follow "File List"
    Then I should see a "This package contains the following files." text
     And I should see a "/etc/products.d/SUSE_SLES.prod" text
     And I should see a "MD5: 4885d8df578b6ee5762e48035861f1e5" text
     And I should see a "/etc/SuSE-release" text
     And I should see a "MD5: bb6a7b9e455f2d1af5e36f473a182668" text

