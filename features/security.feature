Feature: Basic web security measures and recommendations
  In order to be secure
  As an authorized user
  I want to avoid session and other attacks

  Scenario: Caching should be disabled for non-static content
    Given I navigate to any non-static page
    Then the response header "Cache-Control" should be "no-cache,no-store,must-revalidate,private"
    And the response header "Pragma" should be "no-cache"
    And the response header "Expires" should be "0"
    And the response header "Set-Cookie" should include ";HttpOnly;Secure"
    And the response header "X-Frame-Options" should include "SAMEORIGIN"
    And the response header "X-XSS-Protection" should be "1; mode=block"
    And the response header "X-Content-Type-Options" should be "nosniff"
    And the response header "X-Permitted-Cross-Domain-Policies" should be "master-only"




