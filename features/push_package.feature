# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Push a package with unset vendor
  In Order distribute software to the clients
  As an authorized user
  I want to push a package with unset vendor

  Background:
  Given I am authorized as "admin" with password "admin"
  And I follow "Home" in the left menu

  Scenario: download the SSL certificate
    Then I download the SSL certificate

  Scenario: push a package with unset vendor
    When I push package "/root/subscription-tools-1.0-0.noarch.rpm" into "test_base_channel" channel
    And I follow "Software" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Channels > All" in the left menu
    Then I should see package "subscription-tools-1.0-0.noarch" in channel "Test Base Channel"

  Scenario: Check Vendor of Package displayed in WebUI
    When I follow "Software" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Channels > All" in the left menu
    And I follow "Test Base Channel"
    And I follow "Packages"
    And I follow "subscription-tools-1.0-0.noarch"
    Then I should see a "Vendor:" text
    And I should see a "Not defined" text

  Scenario: Push anaconda package in fedora channel
    When I push package "/root/anaconda-18.37.11-1.fc18.x86_64.rpm" into "fedora-x86_64-channel" channel
    And I follow "Software" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Channels > All" in the left menu
    Then I should see package "anaconda-18.37.11-1.fc18.x86_64" in channel "Fedora x86_64 Channel"
