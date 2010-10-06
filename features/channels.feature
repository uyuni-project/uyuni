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
  
  Scenario: Check Packages in Test Base Channel
    When I follow "Channels"
     And I follow "Test Base Channel"
     And I follow "Packages"
    Then I should see package "aaa_base-11-6.30.1.x86_64"
     And I should see package "sles-manuals_en-11.1-16.20.1.noarch"
     And I should see package "sles-manuals_en-pdf-11.1-16.20.1.noarch"
     And I should see package "timezone-java-2010l-0.10.1.noarch"

  Scenario: Check Package metadata displayed in WebUI
    When I follow "Channels"
     And I follow "Test Base Channel"
     And I follow "Packages"
     And I follow "aaa_base-11-6.30.1.x86_64"
    Then I should see a "This package installs several important configuration files." text
     And I should see a "Test Base Channel" link
     And I should see a "SUSE LINUX Products GmbH, Nuernberg, Germany" text
     And I should see a "f4b60101281a777ae1bdfa2749f1a9e6" text
     And I should see a "redhat/1/f4b/aaa_base/11-6.30.1/x86_64/f4b60101281a777ae1bdfa2749f1a9e6/aaa_base-11-6.30.1.x86_64.rpm" text

  Scenario: Check Package dependencies page
    When I follow "Channels"
     And I follow "Test Base Channel"
     And I follow "Packages"
     And I follow "aaa_base-11-6.30.1.x86_64"
     And I follow "Dependencies"
    Then I should see a "distribution-release" text
     And I should see a "libc.so.6(GLIBC_2.2.5)(64bit)" text
     And I should see a "aaa_base = 11-6.30.1" text
     And I should see a "aaa_skel" text
     And I should see a "sysvinit < 2.86-198" text

  Scenario: Check Package Changelog page
    When I follow "Channels"
     And I follow "Test Base Channel"
     And I follow "Packages"
     And I follow "aaa_base-11-6.30.1.x86_64"
     And I follow "Change Log"
    Then I should see a "werner@suse.de" text
     And I should see a "Add a conflict to sysvinit" text

  Scenario: Check Package Filelist page
    When I follow "Channels"
     And I follow "Test Base Channel"
     And I follow "Packages"
     And I follow "aaa_base-11-6.30.1.x86_64"
     And I follow "File List"
    Then I should see a "This package contains the following files." text
     And I should see a "/etc/init.d/boot" text
     And I should see a "MD5: bafa339fa0d1c1665685100026c6ae85" text
     And I should see a "/etc/bash.bashrc" text







