# COPYRIGHT (c) 2017 SUSE LLC
# License under the terms of the MIT License.

Feature: Clone a Channel
  In Order to validate the channel cloning feature
  As a testing user
  I want to clone a channel with errata, without errata and with selected erratas

  Scenario: Clone a Channel without errata
    Given I am on the manage software channels page
    When I follow "Clone Channel"
    And I select "Test-Channel-x86_64" as the origin channel
    And I choose "original"
    And I click on "Clone Channel"
    And I should see a "Create Software Channel" text
    And I should see a "Original state of the channel" text
    And I click on "Clone Channel"
    Then I should see a "Clone of Test-Channel-x86_64" text

  Scenario: Check, that this channel has no erratas
    Given I am on the manage software channels page
    And I follow "Clone of Test-Channel-x86_64"
    When I follow "Errata" in the content area
    And I follow "List/Remove Errata"
    Then I should see a "There are no errata associated with this channel." text

  Scenario: Clone a Channel with errata
    Given I am on the manage software channels page
    When I follow "Clone Channel"
    And I select "Test-Channel-x86_64" as the origin channel
    And I choose "current"
    And I click on "Clone Channel"
    And I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    And I click on "Clone Channel"
    Then I should see a "Clone 2 of Test-Channel-x86_64" text

  Scenario: Check, that this channel has erratas
    Given I am on the manage software channels page
    And I follow "Clone 2 of Test-Channel-x86_64"
    When I follow "Errata" in the content area
    And I follow "List/Remove Errata"
    Then I should see a "CL-hoag-dummy-7890" link
    And I should see a "CL-virgo-dummy-3456" link
    And I should see a "CL-milkyway-dummy-2345" link
    And I should see a "CL-andromeda-dummy-6789" link

  Scenario: Clone a Channel with selected errata
    Given I am on the manage software channels page
    When I follow "Clone Channel"
    And I select "Test-Channel-x86_64" as the origin channel
    And I choose "select"
    And I click on "Clone Channel"
    And I should see a "Create Software Channel" text
    And I should see a "Select errata" text
    And I click on "Clone Channel"
    And I should see a "Clone 3 of Test-Channel-x86_64" text
    And I should see a "Channel Clone 3 of Test-Channel-x86_64 cloned from channel Test-Channel-x86_64." text
    And I should see a "You may now wish to clone the errata associated with Test-Channel-x86_64." text
    And I check the row with the "hoag-dummy-7890" link
    And I check the row with the "virgo-dummy-3456" link
    And I click on "Clone Errata"
    And I click on "Confirm"
    Then I should see a "CL-hoag-dummy-7890" link
    And I should see a "CL-virgo-dummy-3456" link

  Scenario: check new errata exists
    Given I am on the errata page
    When I follow "All" in the left menu
    And I select "500" from "1154021400_PAGE_SIZE_LABEL"
    Then I should see a "CL-hoag-dummy-7890" link
    And I should see a "CL-virgo-dummy-3456" link
    And I should see a "CL-milkyway-dummy-2345" link
    And I should see a "CL-andromeda-dummy-6789" link

  Scenario: check CL-hoag-dummy-7890 errata
    Given I am on the errata page
    When I follow "All" in the left menu
    And I select "500" from "1154021400_PAGE_SIZE_LABEL"
    And I follow "CL-hoag-dummy-7890"
    Then I should see a "CL-hoag-dummy-7890 - Security Advisory" text
    And I should see a "mcalmer" text
    And I should see a "https://bugzilla.suse.com/show_bug.cgi?id=704608" link

  Scenario: check CM-virgo-dummy-3456 errata
    Given I am on the errata page
    When I follow "All" in the left menu
    And I select "500" from "1154021400_PAGE_SIZE_LABEL"
    And I follow "CL-virgo-dummy-3456"
    Then I should see a "CL-virgo-dummy-3456 - Bug Fix Advisory" text
    And I should see a "mcalmer" text
    And I should see a "CVE-1999-9998" link
