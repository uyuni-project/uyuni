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
    Then I should see package "aaa_base-11-6.71.1.x86_64"
     And I should see package "nfs-client-1.2.3-18.23.1.x86_64"
     And I should see package "kernel-default-3.0.38-0.5.1.x86_64"
     And I should see package "kernel-source-3.0.38-0.5.1.x86_64"

  Scenario: Check Package metadata displayed in WebUI
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "aaa_base-11-6.71.1.x86_64"
    Then I should see a "This package installs several important configuration files." text
     And I should see a "SLES11-SP2-Updates x86_64 Channel" link
     And I should see a "SUSE LINUX Products GmbH, Nuernberg, Germany" text
     And I should see a "SHA256sum:" text
     And I should see a "7f9a8f593daac10167d64fb0e90f216d8eb63b97feeb8280b1e045172762cb3d" text
     And I should see a "packages/1/7f9/aaa_base/11-6.71.1/x86_64/7f9a8f593daac10167d64fb0e90f216d8eb63b97feeb8280b1e045172762cb3d/aaa_base-11-6.71.1.x86_64.rpm" text

  Scenario: Check Package dependencies page
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "aaa_base-11-6.71.1.x86_64"
     And I follow "Dependencies"
    Then I should see a "distribution-release" text
     And I should see a "libc.so.6(GLIBC_2.2.5)(64bit)" text
     And I should see a "aaa_base = 11-6.71.1" text
     And I should see a "aaa_skel" text
     And I should see a "sysvinit < 2.86-198" text

  Scenario: Check Package Changelog page
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "aaa_base-11-6.71.1.x86_64"
     And I follow "Change Log"
    Then I should see a "werner@suse.de" text
     And I should see a "Add a conflict to sysvinit" text

  Scenario: Check Package Filelist page
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "aaa_base-11-6.71.1.x86_64"
     And I follow "File List"
    Then I should see a "This package contains the following files." text
     And I should see a "/etc/init.d/boot" text
     And I should see a "MD5: bbb229b8c448709622bf1a1c492f83b8" text
     And I should see a "/etc/bash.bashrc" text
