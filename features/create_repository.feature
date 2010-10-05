#@wip
Feature: Adding repository to a channel
  In Order distribute software to the clients
  As an authorized user
  I want to add a repository
  And I want to add this repository to the base channel


  Scenario: Adding a repository
   Given I am authorized as "testing" with password "testing"
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "Manage Repositories" in the left menu
     And I follow "create new repository"
    When I enter "cmpi-zypp" as "label"
     And I enter "http://download.opensuse.org/repositories/home:/mcalmer:/cmpi-zypp/openSUSE_Factory/" as "url"
     And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add repository to the channel
   Given I am authorized as "testing" with password "testing"
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "Test Base Channel"
     And I follow "Repositories"
    When I check "cmpi-zypp" in the list
     And I click on "Update Repositories"
    Then I should see a "Test Base Channel repository information was successfully updated" text

  Scenario: Sync the repository in the channel
  Scenario: Add repository to the channel
   Given I am authorized as "testing" with password "testing"
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "Test Base Channel"
     And I follow "Repositories"
     And I follow "Sync"
    When I click on "Sync"
    Then I should see a "Repository sync scheduled for Test Base Channel." text


