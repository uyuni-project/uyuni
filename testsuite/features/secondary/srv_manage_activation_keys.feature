# Copyright (c) 2010-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to manipulate activation keys
  In order to register systems to the spacewalk server
  As the testing user
  I want to use activation keys

  Scenario: Change limit of the activation key
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key i586"
    And I enter "20" as "usageLimit"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key i586 has been modified." text
    And I should see "20" in field "usageLimit"

  Scenario: Change the base channel of the activation key
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key i586"
    And I select "Test-Channel-i586" from "selectedBaseChannel"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key i586 has been modified." text