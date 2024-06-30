# Copyright (c) 2017-2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Note: image building via API is not tested here
#       it is tested in buildhost_docker_build_image.feature and
#                       buildhost_docker_auth_registry.feature
#
# This feature is a dependency for:
# - features/secondary/buildhost_docker_build_image.feature
# - features/secondary/buildhost_docker_auth_registry.feature
#
# This feature can cause failures in the following features:
# - features/secondary/buildhost_docker_build_image.feature
# - features/secondary/buildhost_docker_auth_registry.feature

@skip_if_cloud
@scope_building_container_images
@no_auth_registry
Feature: API "image" namespace for containers and sub-namespaces

  Scenario: Test "image.store" namespace
    When I create and delete an image store via API
    And I list image store types and image stores via API
    And I set and get details of image store via API

  @scc_credentials
  Scenario: Test "image.profiles" namespace
    When I create and delete profiles via API
    And I create and delete profile custom values via API
    And I list image profiles via API
    And I set and get profile details via API

  @scc_credentials
  Scenario: Cleanup: remove custom system info
    Given I am authorized for the "Admin" section
    When I follow the left menu "Systems > Custom System Info"
    And I follow "arancio"
    And I follow "Delete Key"
    And I click on "Delete Key"
