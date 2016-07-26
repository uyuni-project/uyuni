Feature: Basic web security measures and recommendations
  In order to be secure
  As an authorized user
  I want to avoid session and other attacks

  Scenario: Caching should be disabled for non-static content
    Given I navigate to any non-static page
    Then the response header "Cache-Control" should be "no-cache,no-store,must-revalidate,private"
    And the response header "Pragma" should be "no-cache"
    And the response header "Expires" should be "0"
    And the response header "Set-Cookie" should contain ";HttpOnly;Secure"
    And the response header "X-Frame-Options" should contain "SAMEORIGIN"
    And the response header "X-XSS-Protection" should be "1; mode=block"
    And the response header "X-Content-Type-Options" should be "nosniff"
    And the response header "X-Permitted-Cross-Domain-Policies" should be "master-only"

  Scenario: Obsolete and problematic headers for non-static content
    Given I navigate to any non-static page
    Then the response header "X-WebKit-CSP" should not be present

  Scenario: Caching should be enabled for static content
    Given I retrieve any static resource
    Then the response header "Cache-Control" should be "max-age=86400, public"
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
