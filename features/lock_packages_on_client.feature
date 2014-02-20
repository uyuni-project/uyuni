# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Lock packages on client

  Scenario: Lock a package on the client
    Given I am on the Systems overview page of this client
      And I follow "Software" in the content area
      And I follow "Lock"
     When I check "hoag-dummy-1.1-2.1" in the list
      And I click on "Request Lock"
      And I run rhn_check on this client
     Then I should see a "Packages has been requested for being locked." text
      And "hoag-dummy-1.1-2.1" is locked on this client
     Then I follow "Lock"
     Then Package "hoag-dummy-1.1-2.1" is reported as locked

  Scenario: Attempt to install a locked package on the client
    Given I am on the Systems overview page of this client
      And I follow "Software" in the content area
      And I follow "Lock"
      And Package "hoag-dummy-1.1-2.1" is reported as locked
     Then I follow "Install"
     When I check "hoag-dummy-1.1-2.1" in the list
      And I click on "Install Selected Packages"
      And I click on "Confirm"
      And I run rhn_check on this client
     Then I should see a "1 package install has been scheduled for" text
     Then I follow "Events"
      And I follow "History"
      And I follow first "Package Install scheduled by testing"
     Then The package scheduled is "hoag-dummy-1.1-2.1"
      And The action status is "Failed"

  Scenario: Unlock a package on the client
    Given I am on the Systems overview page of this client
      And I follow "Software" in the content area
      And I follow "Lock"
      And Package "hoag-dummy-1.1-2.1" is reported as locked
     When I check "hoag-dummy-1.1-2.1" in the list
      And I click on "Request Unlock"
      And I run rhn_check on this client
     Then I should see a "Packages has been requested for being unlocked." text
      And "hoag-dummy-1.1-2.1" is unlocked on this client
     Then I follow "Lock"
     Then Package "hoag-dummy-1.1-2.1" is reported as unlocked

