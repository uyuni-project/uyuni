Feature: Endpoint to download packages
  In Order distribute software to the clients
  As an authorized user
  I want to download packages from the channels

  Scenario: user without token
    Given I try download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "sles11-sp3-updates-x86_64-channel"
    Then the download should get a 403 response

  Scenario: user with a valid token for the org
    Given I have a valid token for organization "1"
    Then I try download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "sles11-sp3-updates-x86_64-channel"
    And the download should get no error

  Scenario: user with an invalid token for the org
    Given I have an invalid token for organization "1"
    Then I try download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "sles11-sp3-updates-x86_64-channel"
    And the download should get a 403 response

  Scenario: user with an expired valid token for the org
    Given I have an expired valid token for organization "1"
    Then I try download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "sles11-sp3-updates-x86_64-channel"
    And the download should get a 403 response

  Scenario: user with an non expired valid token for the org
    Given I have a valid token expiring tomorrow for organization "1"
    Then I try download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "sles11-sp3-updates-x86_64-channel"
    And the download should get no error

  Scenario: user with a valid token that cant be used until tomorrow for the org
    Given I have a not yet usable valid token for organization "1"
    Then I try download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "sles11-sp3-updates-x86_64-channel"
    And the download should get a 403 response

  Scenario: user with a valid token for the org and specific channels
    Given I have a valid token for organization "1" and channel "foobar"
    Then I try download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "sles11-sp3-updates-x86_64-channel"
    And the download should get a 403 response

