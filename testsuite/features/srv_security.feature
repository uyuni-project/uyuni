# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Basic web security measures and recommendations
  In order to be secure
  As an authorized user
  I want to avoid session and other attacks

  Scenario: Caching should be enabled for static content
    Given I retrieve any static resource
    Then the response header "ETag" should not be present
    And the response header "Pragma" should not be present
    And the response header "Expires" should not be "0"
    And the response header "Set-Cookie" should not be present
    And the response header "X-Frame-Options" should contain "SAMEORIGIN"
    And the response header "X-XSS-Protection" should be "1; mode=block"
    And the response header "X-Content-Type-Options" should be "nosniff"
    And the response header "X-Permitted-Cross-Domain-Policies" should be "master-only"

  Scenario: Obsolete and problematic headers for static content
    Given I retrieve any static resource
    Then the response header "X-WebKit-CSP" should not be present

  Scenario: Do not use jsession id
    Given I am not authorized
    Then the login form does not contain a jsessionid

