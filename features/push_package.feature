# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.


Feature: Push a package with unset vendor
  In Order distribute software to the clients
  As an authorized user
  I want to push a package with unset vendor

  Scenario: push a package with unset vendor
   Given I am root
    When I push package "/root/subscription-tools-1.0-0.noarch.rpm" into "test_base_channel" channel
    Then I should see package "subscription-tools-1.0-0.noarch" in channel "Test Base Channel"


  Scenario: Check Vendor of Package displayed in WebUI
   Given I am authorized as "admin" with password "admin"
    When I follow "Channels"
     And I follow "Test Base Channel"
     And I follow "Packages"
     And I follow "subscription-tools-1.0-0.noarch"
    Then I should see a "Not defined" text in the "Vendor:" column

