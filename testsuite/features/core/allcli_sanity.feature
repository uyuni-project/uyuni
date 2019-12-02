# Copyright (c) 2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Sanity checks
  In order to use the product
  I want to be sure to use a sane environment

  Scenario: The server is healthy
    Then "server" should have a FQDN

  Scenario: The traditional client is healthy
    Then "sle_client" should have a FQDN
    And "sle_client" should communicate with the server

  Scenario: The minion is healthy
    Then "sle_minion" should have a FQDN
    And "sle_minion" should communicate with the server

@ssh_minion
  Scenario: The SSH minion is healthy
    Then "ssh_minion" should have a FQDN
    And "ssh_minion" should communicate with the server

@proxy
  Scenario: The proxy is healthy
    Then "proxy" should have a FQDN
    And "proxy" should communicate with the server

@centos_minion
  Scenario: The Centos minion is healthy
    Then "ceos_minion" should have a FQDN
    And "ceos_minion" should communicate with the server

@ubuntu_minion
  Scenario: The Ubuntu minion is healthy
    Then "ubuntu_minion" should have a FQDN
    And "ubuntu_minion" should communicate with the server

  Scenario: The external resources can be reached
    Then it should be possible to download the file "http://download.suse.de/ibs/SUSE/Products/SLE-SERVER/12-SP4/x86_64/product/media.1/products.key"
    And it should be possible to download the file "https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Test-Packages:/Updates/rpm/repodata/repomd.xml"
    And it should be possible to download the file "https://gitlab.suse.de/galaxy/suse-manager-containers/blob/master/test-profile/Dockerfile"
    And it should be possible to download the file "https://github.com/uyuni-project/uyuni/blob/master/README.md"
    And it should be possible to reach the portus registry
    And it should be possible to reach the other registry
