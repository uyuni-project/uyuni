# Copyright (c) 2015-2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Listing, adding and removing ssh connection data for the payg feature
  In order to use payg
  As admin user
  I want to list available ssh connections and add or remove them

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Enter minimal information for payg ssh connection data
    When I follow the left menu "Admin > Setup Wizard > Pay-as-you-go"
    And I click on "Add Pay-as-you-go"
    And I enter "My Instance" as "description"
    And I enter "my-host.local" as "host"
    And I enter "root" as "username"
    And I click on "Create"
    Then I should see a "Pay-as-you-go my-host.local created successfully" text
    And I should see a "My Instance" text in element "Info-panel-wrapper"
    And I should see a "my-host.local" text in element "Instance-panel-wrapper"
    And I should see a "root" text in element "Instance-panel-wrapper"
    And I should see a "Delete" button

  Scenario: Enter full information for payg ssh connection data
    When I follow the left menu "Admin > Setup Wizard > Pay-as-you-go"
    And I click on "Add Pay-as-you-go"
    And I enter "My Full Instance" as "description"
    And I enter "my-host-full.local" as "host"
    And I enter "21" as "port"
    And I enter "rootFull" as "username"
    And I enter "passwordFull" as "password"
    And I enter "keyFull" as "key"
    And I enter "keyPasswordFull" as "key_password"
    And I enter "my-bastion.local" as "bastion_host"
    And I enter "22" as "bastion_port"
    And I enter "b_rootFull" as "bastion_username"
    And I enter "b_passwordFull" as "bastion_password"
    And I enter "b_keyFull" as "bastion_key"
    And I enter "b_keyPasswordFull" as "bastion_key_password"
    And I click on "Create"
    Then I should see a "Pay-as-you-go my-host-full.local created successfully" text
    And I should see a "My Full Instance" text in element "Info-panel-wrapper"
    And I should see a "my-host-full.local" text in element "Instance-panel-wrapper"
    And I should see a "21" text in element "Instance-panel-wrapper"
    And I should see a "rootFull" text in element "Instance-panel-wrapper"
    And I should not see a "passwordFull" text in element "Instance-panel-wrapper"
    And I should not see a "keyFull" text in element "Instance-panel-wrapper"
    And I should not see a "keyPasswordFull" text in element "Instance-panel-wrapper"
    And I should see a "my-bastion.local" text in element "Bastion-panel-wrapper"
    And I should see a "22" text in element "Bastion-panel-wrapper"
    And I should see a "b_rootFull" text in element "Bastion-panel-wrapper"
    And I should not see a "b_passwordFull" text in element "Bastion-panel-wrapper"
    And I should not see a "b_keyFull" text in element "Bastion-panel-wrapper"
    And I should not see a "b_keyPasswordFull" text in element "Bastion-panel-wrapper"
    And I should see a "Delete" button

  Scenario: Check pay-as-you-go list
    When I follow the left menu "Admin > Setup Wizard > Pay-as-you-go"
    Then I should see a "my-host.local" link
    And I should see a "My Instance" text
    And I should see a "my-host-full.local" link
    And I should see a "My Full Instance" text

  Scenario: Edit connection description
    When I follow the left menu "Admin > Setup Wizard > Pay-as-you-go"
    And I follow "my-host-full.local"
    And I click on "Edit Information"
    And I enter "My new Full Instance" as "description"
    And I click on "Save" in "Information" modal
    Then I should see a "Pay-as-you-go properties updated successfully" text
    And I should see a "My new Full Instance" text in element "Info-panel-wrapper"

  Scenario: Edit instance ssh connection data
    When I follow the left menu "Admin > Setup Wizard > Pay-as-you-go"
    And I follow "my-host-full.local"
    And I click on "Edit Instance"
    And I enter "221" as "port"
    And I enter "NewRootFull" as "username"
    And I enter "NewPasswordFull" as "password"
    And I enter "newKeyFull" as "key"
    And I enter "newKeyPasswordFull" as "key_password"
    And I click on "Save" in "Instance SSH connection" modal
    Then I should see a "Pay-as-you-go properties updated successfully" text
    And I should see a "221" text in element "Instance-panel-wrapper"
    And I should see a "NewRootFull" text in element "Instance-panel-wrapper"
    And I should not see a "NewPasswordFull" text in element "Instance-panel-wrapper"
    And I should not see a "newKeyFull" text in element "Instance-panel-wrapper"
    And I should not see a "newKeyPasswordFull" text in element "Instance-panel-wrapper"

  Scenario: Edit bastion ssh connection data
    When I follow the left menu "Admin > Setup Wizard > Pay-as-you-go"
    And I follow "my-host-full.local"
    And I click on "Edit Bastion"
    And I enter "my-new-bastion.local" as "bastion_host"
    And I enter "222" as "bastion_port"
    And I enter "b_new_rootFull" as "bastion_username"
    And I enter "b_new_passwordFull" as "bastion_password"
    And I enter "b_new_keyFull" as "bastion_key"
    And I enter "b_new_keyPasswordFull" as "bastion_key_password"
    And I click on "Save" in "Bastion SSH connection" modal
    Then I should see a "Pay-as-you-go properties updated successfully" text
    And I should see a "my-new-bastion.local" text in element "Bastion-panel-wrapper"
    And I should see a "222" text in element "Bastion-panel-wrapper"
    And I should see a "b_new_rootFull" text in element "Bastion-panel-wrapper"
    And I should not see a "b_new_passwordFull" text in element "Bastion-panel-wrapper"
    And I should not see a "b_new_keyFull" text in element "Bastion-panel-wrapper"
    And I should not see a "b_new_keyPasswordFull" text in element "Bastion-panel-wrapper"

  Scenario: Delete minimal information for payg ssh connection data
    When I follow the left menu "Admin > Setup Wizard > Pay-as-you-go"
    And I follow "my-host.local"
    And I click on "Delete"
    And I click on "Delete" in "Delete Pay-as-you-go" modal
    Then I should not see a "my-host.local" link
    And I should not see a "my-host.local" text

  Scenario: Delete Full information for payg ssh connection data
    When I follow the left menu "Admin > Setup Wizard > Pay-as-you-go"
    And I follow "my-host-full.local"
    And I click on "Delete"
    And I click on "Delete" in "Delete Pay-as-you-go" modal
    Then I should not see a "my-bastion.local" link
    And I should not see a "my-host.local" text
