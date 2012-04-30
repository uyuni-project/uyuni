Feature: Explore the main landing page
  In Order to avoid systems with different running/installed kernel
  As a authorized user
  I want to see systems that need a reboot

Scenario: Feature should be accessible
  Given I am on the Systems page
    When I follow "Systems" in the left menu
    Then I should see a "All" link in the left menu
     And I should see a "Requiring Reboot" link in the left menu

Scenario: No reboot notice if no need to reboot
    Given I am on the Systems overview page of this client
    Then I should not see a "The system requires a reboot" text

Scenario: Display Reboot Required after installing an Errata
    Given I am on the Systems overview page of this client
     And I follow "Software" in class "content-nav"
     And I follow "Errata" in class "contentnav-row2"
    When I check "slessp1-kernel-3280" in the list
     And I wait for "2" seconds
     And I click on "Apply Errata"
     And I wait for "2" seconds
     And I click on "Confirm"
     And I wait for "5" seconds
     And I run rhn_check on this client
     And I wait for "5" seconds
     And I follow "Systems" in element "sidenav"
     And I follow this client link
     Then I should see a "The system requires a reboot" text



