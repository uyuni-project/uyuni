# Copyright (c) 2017 SUSE LLC
# SPDX-License-Identifier: MIT

@scope_salt
Feature: Endpoint to download packages
  In order to distribute software to the clients
  As an authorized user
  I want to download packages from the channels

  Scenario: Download package, user without token
    Given I try to download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "fake-rpm-suse-channel"
    Then the download should get a 403 response

  Scenario: Download package, user with a valid token for the org
    Given I have a valid token for organization "1"
    When I try to download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "fake-rpm-suse-channel"
    Then the download should get no error

  Scenario: Download package, user with an invalid token for the org
    Given I have an invalid token for organization "1"
    When I try to download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "fake-rpm-suse-channel"
    Then the download should get a 403 response

  Scenario: Download package, user with an expired valid token for the org
    Given I have an expired valid token for organization "1"
    When I try to download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "fake-rpm-suse-channel"
    Then the download should get a 403 response

  Scenario: Download package, user with an non expired valid token for the org
    Given I have a valid token expiring tomorrow for organization "1"
    When I try to download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "fake-rpm-suse-channel"
    Then the download should get no error

  Scenario: Download package, user with a valid token that cant be used until tomorrow for the org
    Given I have a not yet usable valid token for organization "1"
    When I try to download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "fake-rpm-suse-channel"
    Then the download should get a 403 response

  Scenario: Download package, user with a valid token for the org and specific channels
    Given I have a valid token for organization "1" and channel "foobar"
    When I try to download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "fake-rpm-suse-channel"
    Then the download should get a 403 response

