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
     Then I should see a "CL-slessp2-kernel-6648" link
      And I should see a "CL-slessp2-nfs-client-6222" link
      And I should see a "CL-slessp2-aaa_base-6544" link

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
     And I choose "Merge w/CL-slessp2-aaa_base-6544" for "slessp2-aaa_base-6544"
     And I choose "Clone as CM-slessp2-nfs-client-6222" for "slessp2-nfs-client-6222"
     And I choose "Do Nothing" for "slessp2-kernel-6648"
     And I click on "Clone Errata"
     And I click on "Update Errata"
     And I follow "List/Remove Errata"
    Then I should see a "CM-slessp2-nfs-client-6222" link
     And I should see a "CL-slessp2-aaa_base-6544" link

  Scenario: check new errata exists
    Given I am on the errata page
     When I follow "All" in the left menu
      And I select "500" from "1154021400_PAGE_SIZE_LABEL"
     Then I should see a "CL-slessp2-kernel-6648" link
      And I should see a "CM-slessp2-nfs-client-6222" link
      And I should see a "CM-slessp2-nfs-client-6222" link
      And I should see a "CL-slessp2-aaa_base-6544" link

  Scenario: check CL-slessp2-nfs-client-6222 errata
    Given I am on the errata page
     When I follow "All" in the left menu
      And I select "500" from "1154021400_PAGE_SIZE_LABEL"
      And I follow "CL-slessp2-nfs-client-6222"
     Then I should see a "CL-slessp2-nfs-client-6222 - Bug Fix Advisory" text
      And I should see a "maint-coord@suse.de" text
      And I should see a "bug number 758492" link

  Scenario: check CM-slessp2-nfs-client-6222 errata
    Given I am on the errata page
     When I follow "All" in the left menu
      And I select "500" from "1154021400_PAGE_SIZE_LABEL"
      And I follow "CM-slessp2-nfs-client-6222"
     Then I should see a "CM-slessp2-nfs-client-6222 - Bug Fix Advisory" text
      And I should see a "maint-coord@suse.de" text
      And I should see a "bug number 758492" link
