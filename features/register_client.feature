Feature: Register a client
  In Order register a client to the spacewalk server
  As the root user
  I want to call rhnreg_ks

  Scenario: Register a client
    Given I am root
    When I register using an activation key
    Then I should see this client in spacewalk
  

