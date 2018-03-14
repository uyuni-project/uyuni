# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Basic images do not contain zypper nor the name of the server,
# so the inspect functionality is not tested here.

Feature: Build container images

  Scenario: Create a Docker user with image administrators rights
  Given I am on the active Users page
  When I follow "Create User"
  And I enter "docker" as "login"
  And I enter "docker" as "desiredpassword"
  And I enter "docker" as "desiredpasswordConfirm"
  And I select "Mr." from "prefix"
  And I enter "TestDocky" as "firstNames"
  And I enter "TestDocky" as "lastName"
  And I enter "gino-ginae@susy.de" as "email"
  And I click on "Create Login"
  Then I should see a "Account docker created" text
  And I should see a "docker" link
  And I should see a "normal user" text
  And I follow "docker"
  And I check "role_image_admin"
  And I click on "Update"

  Scenario: Create Docker activation key
  Given I am on the Systems page
  And I follow "Activation Keys" in the left menu
  And I follow "Create Key"
  When I enter "Docker testing" as "description"
  And I enter "DOCKER-TEST" as "key"
  And I enter "20" as "usageLimit"
  And I select "Test-Channel-x86_64" from "selectedChannel"
  And I click on "Create Activation Key"
  And I follow "Packages"
  And I enter "orion-dummy perseus-dummy" as "packages"
  And I click on "Update Activation Key"
  Then I should see a "Activation key Docker testing has been modified" text

  Scenario: Turn the SLES minion into a container build host and check output
  Given I am on the Systems overview page of this "sle-minion"
  When I follow "Details" in the content area
  And I follow "Properties" in the content area
  And I check "container_build_host"
  And I click on "Update Properties"
  Then I should see a "Container Build Host type has been applied." text
  And I should see a "Note: This action will not result in state application" text
  And I should see a "To apply the state, either use the states page or run `state.highstate` from the command line." text
  And I should see a "System properties changed" text

  Scenario: Apply the highstate to container build host
  Given I am on the Systems overview page of this "sle-minion"
  Then I should see a "[Container Build Host]" text
  And I wait until no Salt job is running on "sle-minion"
  And I enable SUSE container repository, but not for SLES11 systems
  And I enable SLES pool and update repository on "sle-minion", but not for SLES11

  Scenario: Create an image store without credentials
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Stores" in the left menu
  And I follow "Create"
  And I enter "galaxy-registry" as "label"
  And I enter "registry.mgr.suse.de" as "uri"
  And I click on "create-btn"

  Scenario: Create a simple image profile without activation key
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Profiles" in the left menu
  And I follow "Create"
  And I enter "suse_simple" as "label"
  And I select "galaxy-registry" from "imageStore"
  And I enter "https://gitlab.suse.de/galaxy/suse-manager-containers.git#:test-profile" as "path"
  And I click on "create-btn"

  Scenario: Create a simple real image profile without activation key
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Profiles" in the left menu
  And I follow "Create"
  And I enter "suse_real_simple" as "label"
  And I select "galaxy-registry" from "imageStore"
  And I enter "https://gitlab.suse.de/galaxy/suse-manager-containers.git#:test-profile/serverhost" as "path"
  And I click on "create-btn"

  Scenario: Create an image profile with activation key
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Profiles" in the left menu
  And I follow "Create"
  And I enter "suse_key" as "label"
  And I select "galaxy-registry" from "imageStore"
  And I select "1-DOCKER-TEST" from "activationKey"
  And I enter "https://gitlab.suse.de/galaxy/suse-manager-containers.git#:test-profile" as "path"
  And I click on "create-btn"

  Scenario: Create a simple real image profile with activation key
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Profiles" in the left menu
  And I follow "Create"
  And I enter "suse_real_key" as "label"
  And I select "galaxy-registry" from "imageStore"
  And I select "1-DOCKER-TEST" from "activationKey"
  And I enter "https://gitlab.suse.de/galaxy/suse-manager-containers.git#:test-profile/serverhost" as "path"
  And I click on "create-btn"

  Scenario: Build the images with and without activation key
  Given I am authorized as "admin" with password "admin"
  When I schedule the build of image "suse_key" via XML-RPC calls
  And I schedule the build of image "suse_simple" via XML-RPC calls
  And I schedule the build of image "suse_real_key" via XML-RPC calls

  Scenario: Build same images with different versions
  Given I am authorized as "admin" with password "admin"
  And I schedule the build of image "suse_key" with version "Latest_key-activation1" via XML-RPC calls
  And I schedule the build of image "suse_simple" with version "Latest_simple" via XML-RPC calls
  Then all "5" container images should be built correctly in the GUI

  Scenario: Delete image via XML-RPC calls
  Given I am authorized as "admin" with password "admin"
  And I delete the image "suse_key" with version "Latest_key-activation1" via XML-RPC calls
  And I delete the image "suse_simple" with version "Latest_simple" via XML-RPC calls
  And the image "suse_simple" with version "Latest_key-activation1" doesn't exist via XML-RPC calls
  And the image "suse_simple" with version "Latest_simple" doesn't exist via XML-RPC calls
  And I schedule the build of image "suse_simple" with version "Latest_simple" via XML-RPC calls
  And I schedule the build of image "suse_key" with version "Latest_key-activation1" via XML-RPC calls
  And I wait for "60" seconds

  Scenario: Verify the status of images
  Given I am authorized as "admin" with password "admin"
  And I navigate to images webpage
  Then all "5" container images should be built correctly in the GUI

  Scenario: Build some images via GUI
  Given I am authorized as "admin" with password "admin"
  And I navigate to images build webpage
  When I enter "GUI_BUILT_IMAGE" as "version"
  And I select "suse_real_key" from "profileId"
  And I select sle-minion hostname in Build Host
  And I click on "submit-btn"
  And I wait for "5" seconds
  Then I should see a "GUI_BUILT_IMAGE" text

  Scenario: Login as docker image administrator and build an image
  Given I am authorized as "docker" with password "docker"
  And I navigate to images build webpage
  When I enter "GUI_DOCKERADMIN" as "version"
  And I select "suse_real_key" from "profileId"
  And I select sle-minion hostname in Build Host
  And I click on "submit-btn"
  And I wait for "5" seconds
  Then I should see a "GUI_DOCKERADMIN" text

  Scenario: Cleanup: reset channels on the SLES minion
  Given I am authorized as "admin" with password "admin"
  And I disable SUSE container repository, but not for SLES11 systems
  And I disable SLES pool and update repository on "sle-minion"
