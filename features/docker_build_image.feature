# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature:  Build Container images with SUSE Manager

  Scenario: Assign to the sles-minion the property container build host 
  Given I am on the Systems overview page of this "sle-minion"
  And I follow "Details" in the content area
  And I follow "Properties" in the content area
  And I check "container_build_host"
  When I click on "Update Properties"
  Then I should see a "Container Build Host type has been applied." text 
  And I should see a "Note: This action will not result in state application" text
  And I should see a "To apply the state, either use the states page or run `state.highstate` from the command line." text
  And I should see a "System properties changed" text

  Scenario: Apply the highstate to container buid host
  Given I am on the Systems overview page of this "sle-minion"
  Then I should see a "[Container Build Host]" text
  And I run "zypper mr -e Devel_Galaxy_Manager_Head_SLE-Manager-Tools-12-x86_64" on "sle-minion"
  And I run "zypper mr -e SUSE_Updates_SLE-Module-Containers_12_x86_64" on "sle-minion"
  And I run "zypper mr -e SUSE_Pool_SLE-Module-Containers_12_x86_64" on "sle-minion"
  And I run "zypper mr -e SLE-12-SP2-x86_64-Pool" on "sle-minion"
  And I run "zypper mr -e SLE-12-SP2-x86_64-Update" on "sle-minion"
  And I run "zypper -n --gpg-auto-import-keys ref" on "sle-minion"
  And I apply highstate on Sles minion
  Then I wait until "docker" service is up and running on "sle-minion"
  # FIXME: add certicates..  SUMAFORM
  Scenario: Create an Image Store without credentials
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Stores" in the left menu
  And I follow "Create"
  And I enter "galaxy-registry" as "label"
  And I enter "registry.suse.de" as "uri"
  And I click on "create-btn"

  Scenario: Create an Image Profile
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Profiles" in the left menu
  And I follow "Create"
  And I enter "opensuse" as "label"
  And I select "galaxy-registry" from "imageStore"
  And I enter "https://gitlab.suse.de/galaxy/suse-manager-containers.git#:test-profile" as "path"
  And I click on "create-btn"

  Scenario: Build a docker Image
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Build" in the left menu
  And I enter "opensuse" as "profileId"
  # FIXME/WIP: this need implmentation
  And I enter sle-minion hostname in Build Host
  And I click on "submit-btn"


  Scenario: Verify that the docker image was sucessefully created
  Given I am authorized as "admin" with password "admin"

  Scenario: Create an Image Store with authentication
  Given I am authorized as "admin" with password "admin"
