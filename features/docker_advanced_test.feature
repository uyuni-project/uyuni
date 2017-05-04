# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature:  Test advanced functionality of content management

 Scenario: Create an Image Store as docker_admin
  Given I am authorized as "docker" with password "docker"
  And I follow "Images" in the left menu
  And I follow "Stores" in the left menu
  And I follow "Create"
  And I enter "docker_admin" as "label"
  And I enter "registry.mgr.suse.de" as "uri"
  And I click on "create-btn"

 Scenario: Create a profile  As docker admin
   Given I am authorized as "docker" with password "docker"
   And I follow "Images" in the left menu
   And I follow "Profiles" in the left menu
   And I follow "Create"
   And I enter "suse_docker_admin" as "label"
   And I select "galaxy-registry" from "imageStore"
   And I select "1-DOCKER-TEST" from "activationKey"
   And I enter "https://gitlab.suse.de/galaxy/suse-manager-containers.git#:test-profile/serverhost" as "path"
   And I click on "create-btn"

 Scenario: Create a user without rights/roles
  Given I am on the active Users page
  When I follow "Create User"
  And I enter "norole" as "login"
  And I enter "norole" as "desiredpassword"
  And I enter "norole" as "desiredpasswordConfirm"
  And I select "Mr." from "prefix"
  And I enter "norole" as "firstNames"
  And I enter "norole" as "lastName"
  And I enter "norole-ginae@susy.de" as "email"
  And I click on "Create Login"
  Then I should see a "Account norole created" text
  And I should see a "norole" link
  And I should see a "normal user" text

 Scenario: I can't access build image page with norole user
  Given I am authorized as "norole" with password "norole"
  And I navigate to images build webpage
  And I should not see a "Internal Server Error" text
  And I should not see a "Build Profile" text
  And I should not see a "Build Host" text
  And I should not see a "Build Image" text
