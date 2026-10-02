# Copyright (c) 2018-2023 SUSE LLC
# SPDX-License-Identifier: MIT
#
# This feature can cause failures in:
# If the "kiwikiwi" user fails to be created:
# - features/secondary/buildhost_osimage_build_image.feature


Feature: Prepare server for using Kiwi

  Scenario: Create a Kiwi user with image administrators rights
    Given I am authorized for the "Admin" section
    When I create a user with name "kiwikiwi" and password "kiwikiwi" with roles "image_admin"
