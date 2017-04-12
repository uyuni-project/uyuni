# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature:  Build Container images with SUSE Manager. Basic image
          Images are not with zypper and doesn't contains the name
          of the server. So the inspect functionality is not tested here.

  Scenario: Check prerequisites of content manag feature
  Then I check that sles-minion exists otherwise bootstrap it

  Scenario: Create Activation-key docker
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
  And I click on "Update Key"
  Then I should see a "Activation key Minion testing has been modified" text

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
  And I run "zypper mr -e SLE-Module-Containers-SLE-12-x86_64-Pool" on "sle-minion"
  And I run "zypper mr -e SLE-Module-Containers-SLE-12-x86_64-Update" on "sle-minion"
  And I run "zypper mr -e SLE-12-SP2-x86_64-Pool" on "sle-minion"
  And I run "zypper mr -e SLE-12-SP2-x86_64-Update" on "sle-minion"
  And I run "zypper -n --gpg-auto-import-keys ref" on "sle-minion"
  And I apply highstate on Sles minion
  Then I wait until "docker" service is up and running on "sle-minion"
  # FIXME: We need a test for image store with credentials
  Scenario: Create an Image Store without credentials
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Stores" in the left menu
  And I follow "Create"
  And I enter "galaxy-registry" as "label"
  And I enter "registry.mgr.suse.de" as "uri"
  And I click on "create-btn"

  Scenario: Create a simple Image profile without act-key
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Profiles" in the left menu
  And I follow "Create"
  And I enter "suse_simply" as "label"
  And I select "galaxy-registry" from "imageStore"
  And I enter "https://gitlab.suse.de/galaxy/suse-manager-containers.git#:test-profile" as "path"
  And I click on "create-btn"

  Scenario: Create a simple Real Image profile without act-key
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Profiles" in the left menu
  And I follow "Create"
  And I enter "suse_real_simply" as "label"
  And I select "galaxy-registry" from "imageStore"
  And I enter "https://gitlab.suse.de/galaxy/suse-manager-containers.git#:test-profile/serverhost" as "path"
  And I click on "create-btn"

  Scenario: Create an Image profile with activation-key
  Given I am authorized as "admin" with password "admin"
  And I follow "Images" in the left menu
  And I follow "Profiles" in the left menu
  And I follow "Create"
  And I enter "suse_key" as "label"
  And I select "galaxy-registry" from "imageStore"
  And I select "1-DOCKER-TEST" from "activationKey"
  And I enter "https://gitlab.suse.de/galaxy/suse-manager-containers.git#:test-profile" as "path"
  And I click on "create-btn"

  Scenario: Create a simple Real Image profile with act-key
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
  # At moment phantomjs has problemes with datapickler so we use xmlrpc-api
  And I schedule the build of image "suse_key" via xmlrpc-call
  And I schedule the build of image "suse_simply" via xmlrpc-call
  And I schedule the build of image "suse_real_key" via xmlrpc-call

  Scenario: Build same images with different versions
  Given I am authorized as "admin" with password "admin"
  And I schedule the build of image "suse_key" with version "Latest_key-activation1" via xmlrpc-call 
  And I schedule the build of image "suse_simply" with version "Latest_simply" via xmlrpc-call 
  # then we can remove the sleep.
  And I verify that all "5" container images were built correctly in the gui

  Scenario: Delete image via xmlrpc calls
  Given I am authorized as "admin" with password "admin"
  And I delete the image "suse_key" with version "Latest_key-activation1" via xmlrpc-call
  And I delete the image "suse_simply" with version "Latest_simply" via xmlrpc-call
  And The image "suse_simply" with version "Latest_key-activation1" doesn't exist via xmlrpc-call
  And The image "suse_simply" with version "Latest_simply" doesn't exist via xmlrpc-call
  And I schedule the build of image "suse_simply" with version "Latest_simply" via xmlrpc-call
  And I schedule the build of image "suse_key" with version "Latest_key-activation1" via xmlrpc-call 
  And I wait for "60" seconds

  Scenario: Verify the status of images.
  Given I am authorized as "admin" with password "admin"
  And I navigate to images webpage
  Then I verify that all "5" container images were built correctly in the gui

  Scenario: Build some images via gui
  Given I am authorized as "admin" with password "admin"
  And I navigate to images build webpage
  When I enter "GUI_BUILDED_IMAGE" as "version"
  And I select "suse_real_key" from "profileId"
  And I select sle-minion hostname in Build Host
  And I click on "submit-btn"
  And I wait for "5" seconds
  Then I should see a "GUI_BUILDED_IMAGE" text 
  And I should see a "a few seconds ago" text
