
Feature: The mgr-create-bootstrap-repo command
  In Order to create repositories for bootstraping clients
  As an sysadmin logged into the server
  I want to be able to run a command to do it

  Scenario: mgr-create-bootstrap-repo of the stanard distributions
    Given the list of distributions
      | SLE-11-SP3-X86_64 |
      | SLE-12-SP1-x86_64 |
    Then calling mgr-create-bootstrap-repo -c should show no error
