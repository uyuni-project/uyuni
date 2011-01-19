# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Install a package to the client

  Scenario: Install a package to the client
    Given I am on the Systems overview page of this client
     And I follow "Software" in class "content-nav"
     And I follow "Install"
    When I check "aspell-0.60.6-26.22" in the list
     And I click on "Install Selected Packages"
     And I click on "Confirm"
     And I run rhn_check on this client
    Then I should see a "1 package install has been scheduled for" text
     And "aspell-0.60.6-26.22" is installed
