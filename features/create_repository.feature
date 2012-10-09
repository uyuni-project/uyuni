# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#@wip
Feature: Adding repository to a channel
  In Order distribute software to the clients
  As an authorized user
  I want to add a repository
  And I want to add this repository to the base channel

  Scenario: Adding SLES11-SP2-Updates-x86_64 repository
   Given I am authorized as "testing" with password "testing"
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "Manage Repositories" in the left menu
     And I follow "create new repository"
    When I enter "SLES11-SP2-Updates-x86_64" as "label"
     And I enter "http://localhost/pub/SLES11-SP2-Updates-x86_64/" as "url"
     And I click on "Create Repository"
    Then I should see a "Repository created successfully" text
     And I should see "metadataSigned" as checked

  Scenario: Disable Metadata check for SLES11-SP2-Updates-x86_64 repository
   Given I am authorized as "testing" with password "testing"
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "Manage Repositories" in the left menu
     And I follow "SLES11-SP2-Updates-x86_64"
    When I uncheck "metadataSigned"
     And I click on "Update Repository"
    Then I should see a "Repository updated successfully" text
     And I should see "metadataSigned" as unchecked

  Scenario: Add repository to the x86_64 channel
   Given I am authorized as "testing" with password "testing"
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Repositories"
    When I check "SLES11-SP2-Updates-x86_64" in the list
     And I click on "Update Repositories"
    Then I should see a "SLES11-SP2-Updates x86_64 Channel repository information was successfully updated" text

  Scenario: Sync the repository in the x86_64 channel
   Given I am authorized as "testing" with password "testing"
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Repositories"
     And I follow "Sync"
    When I click on "Sync Now"
    Then I should see a "Repository sync scheduled for SLES11-SP2-Updates x86_64 Channel." text

  Scenario: Adding SLES11-SP2-Updates-i586 repository
   Given I am authorized as "testing" with password "testing"
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "Manage Repositories" in the left menu
     And I follow "create new repository"
    When I enter "SLES11-SP2-Updates-i586" as "label"
     And I enter "http://localhost/pub/SLES11-SP2-Updates-i586/" as "url"
     And I uncheck "metadataSigned"
     And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add repository to the i586 channel
   Given I am authorized as "testing" with password "testing"
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "SLES11-SP2-Updates i586 Channel"
     And I follow "Repositories"
    When I check "SLES11-SP2-Updates-i586" in the list
     And I click on "Update Repositories"
    Then I should see a "SLES11-SP2-Updates i586 Channel repository information was successfully updated" text

  Scenario: Sync the repository in the i586 channel
   Given I am authorized as "testing" with password "testing"
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "SLES11-SP2-Updates i586 Channel"
     And I follow "Repositories"
     And I follow "Sync"
    When I click on "Sync Now"
    Then I should see a "Repository sync scheduled for SLES11-SP2-Updates i586 Channel." text

