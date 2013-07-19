Feature: Clone a Channel
  In Order to validate the channel cloning feature
  As a testing user
  I want to clone a channel with errata, without errata and with selected erratas

  Scenario: Clone a Channel without errata
    Given I am on the manage software channels page
    When I follow "clone channel"
     And I select "SLES11-SP2-Updates x86_64 Channel" from "clone_from"
     And I choose "original"
     And I click on "Create Channel"
     And I should see a "Software Channel: New Channel" text
     And I should see a "Original channel with no updates" text
     And I click on "Create Channel"
    Then I should see a "Clone of SLES11-SP2-Updates x86_64 Channel" text

  Scenario: Check, that this channel has no erratas
    Given I am on the manage software channels page
      And I follow "Clone of SLES11-SP2-Updates x86_64 Channel"
     When I follow "Errata" in class "content-nav"
      And I follow "List/Remove Errata"
     Then I should see a "There are no errata associated with this channel." text

  Scenario: Clone a Channel with errata
    Given I am on the manage software channels page
    When I follow "clone channel"
     And I select "SLES11-SP2-Updates x86_64 Channel" from "clone_from"
     And I choose "current"
     And I click on "Create Channel"
     And I should see a "Software Channel: New Channel" text
     And I should see a "Current state of the channel" text
     And I click on "Create Channel"
    Then I should see a "Clone 2 of SLES11-SP2-Updates x86_64 Channel" text

  Scenario: Check, that this channel has erratas
    Given I am on the manage software channels page
      And I follow "Clone 2 of SLES11-SP2-Updates x86_64 Channel"
     When I follow "Errata" in class "content-nav"
      And I follow "List/Remove Errata"
     Then I should see a "CL-xz-3456" link
      And I should see a "CL-hplip-2345" link
      And I should see a "CL-sles-release-6789" link

  Scenario: Clone a Channel with selected errata
    Given I am on the manage software channels page
    When I follow "clone channel"
     And I select "SLES11-SP2-Updates x86_64 Channel" from "clone_from"
     And I choose "select_errata"
     And I click on "Create Channel"
     And I should see a "Software Channel: New Channel" text
     And I should see a "Select errata" text
     And I click on "Create Channel"
     And I should see a "Software Channel: Clone 3 of SLES11-SP2-Updates x86_64 Channel" text
     And I choose "Merge w/CL-xz-3456" for "xz-3456"
     And I choose "Clone as CM-hplip-2345" for "hplip-2345"
     And I choose "Do Nothing" for "sles-release-6789"
     And I click on "Clone Errata"
     And I click on "Update Errata"
     And I follow "List/Remove Errata"
    Then I should see a "CM-hplip-2345" link
     And I should see a "CL-xz-3456" link

  Scenario: check new errata exists
    Given I am on the errata page
     When I follow "All" in the left menu
      And I select "500" from "1154021400_PAGE_SIZE_LABEL"
     Then I should see a "CL-xz-3456" link
      And I should see a "CM-hplip-2345" link
      And I should see a "CL-sles-release-6789" link

  Scenario: check CL-hplip-2345 errata
    Given I am on the errata page
     When I follow "All" in the left menu
      And I select "500" from "1154021400_PAGE_SIZE_LABEL"
      And I follow "CL-hplip-2345"
     Then I should see a "CL-hplip-2345 - Security Advisory" text
      And I should see a "mcalmer" text
      And I should see a "https://bugzilla.novell.com/show_bug.cgi?id=704608" link

  Scenario: check CM-hplip-2345 errata
    Given I am on the errata page
     When I follow "All" in the left menu
      And I select "500" from "1154021400_PAGE_SIZE_LABEL"
      And I follow "CM-hplip-2345"
     Then I should see a "CM-hplip-2345 - Security Advisory" text
      And I should see a "mcalmer" text
      And I should see a "https://bugzilla.novell.com/show_bug.cgi?id=704608" link


