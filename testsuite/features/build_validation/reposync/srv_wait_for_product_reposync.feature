# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Wait for reposync activity to finish after adding products

  Scenario: Wait for running reposyncs to finish after adding products
    When I wait until all spacewalk-repo-sync finished
