# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Basic images do not contain zypper nor the name of the server,
# so the inspect functionality is not tested here.

Feature: Build OS images

  Scenario: Create a Kiwi user with image administrators rights
  Given I am on the active Users page
  When I follow "Create User"
  And I enter "kiwi" as "login"
  And I enter "kiwi" as "desiredpassword"
  And I enter "kiwi" as "desiredpasswordConfirm"
  And I select "Mr." from "prefix"
  And I enter "TestKiwi" as "firstNames"
  And I enter "TestKiwi" as "lastName"
  And I enter "kiwi@susy.de" as "email"
  And I click on "Create Login"
  Then I should see a "Account kiwi created" text
  And I should see a "kiwi" link
  And I should see a "kiwi user" text
  And I follow "kiwi"
  And I check "role_image_admin"
  And I click on "Update"

  Scenario: Create Kiwi activation key
  Given I am on the Systems page
  And I follow "Activation Keys" in the left menu
  And I follow "Create Key"
  When I enter "Kiwi testing" as "description"
  And I enter "KIWI-TEST" as "key"
  And I enter "20" as "usageLimit"
  And I select "Test-Channel-x86_64" from "selectedChannel"
  And I click on "Create Activation Key"
  Then I should see a "Activation key Kiwi testing has been modified" text

  Scenario: Turn the SLES minion into a OS Image build host and check output
  Given I am on the Systems overview page of this "sle-minion"
  When I follow "Details" in the content area
  And I follow "Properties" in the content area
  And I check "osimage_build_host"
  And I click on "Update Properties"
  Then I should see a "OS Image Build Host type has been applied." text
  And I should see a "Note: This action will not result in state application" text
  And I should see a "To apply the state, either use the states page or run `state.highstate` from the command line." text
  And I should see a "System properties changed" text

  Scenario: Apply the highstate to container build host
  Given I am on the Systems overview page of this "sle-minion"
  Then I should see a "[OS Image Build Host]" text
  And I wait until no Salt job is running on "sle-minion"

  Scenario: Create an image profile with activation key
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Profiles" in the left menu
  And I follow "Create"
  And I enter "suse_osimage_real_simple" as "label"
  And I select "1-KIWI-TEST" from "activationKey"
  And I enter "https://gitlab.suse.de/mbologna/SUMA_Retail.git#master:POS_Image-JeOS6/jeos-6.0.0" as "path" # TODO simple repo with packages from Test-Channel-x86_64
  And I click on "create-btn"

  Scenario: Build some images via GUI
  Given I am authorized as "admin" with password "admin"
  And I navigate to images build webpage
  And I select "suse_osimage_real_simple" from "profileId"
  And I select sle-minion hostname in Build Host
  And I click on "submit-btn"
  And I wait for "5" seconds
  Then I should see a "Building the image has been scheduled" text

  Scenario: Login as Kiwi image administrator and build an image
  Given I am authorized as "kiwi" with password "kiwi"
  And I navigate to images build webpage
  And I select "suse_osimage_real_simple" from "profileId"
  And I select sle-minion hostname in Build Host
  And I click on "submit-btn"
  And I wait for "5" seconds
  Then I should see a "Building the image has been scheduled" text
