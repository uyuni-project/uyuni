# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create bootstrap repositories

# WORKAROUND: --flush option does not seem to work in case of hash mismatch with same version
  Scenario: Clean up all bootstrap repositories on the server
    When I clean up all bootstrap repositories on the server

@proxy
  Scenario: Create the bootstrap repository for the SUSE Manager proxy
    When I create the bootstrap repository for "proxy" on the server

@sle11sp4_client
  Scenario: Create the bootstrap repository for a SLES 11 SP4 traditional client
    When I create the bootstrap repository for "sle11sp4_client" on the server

@sle11sp4_minion
  Scenario: Create the bootstrap repository for a SLES 11 SP4 minion
    When I create the bootstrap repository for "sle11sp4_minion" on the server

@sle11sp4_ssh_minion
  Scenario: Create the bootstrap repository for a SLES 11 SP4 Salt SSH minion
    When I create the bootstrap repository for "sle11sp4_ssh_minion" on the server

@sle12sp4_client
  Scenario: Create the bootstrap repository for a SLES 12 SP4 traditional client
    When I create the bootstrap repository for "sle12sp4_client" on the server

@sle12sp4_minion
  Scenario: Create the bootstrap repository for a SLES 12 SP4 minion
    When I create the bootstrap repository for "sle12sp4_minion" on the server

@sle12sp4_ssh_minion
  Scenario: Create the bootstrap repository for a SLES 12 SP4 Salt SSH minion
    When I create the bootstrap repository for "sle12sp4_minion" on the server

@sle15_client
  Scenario: Create the bootstrap repository for a SLES 15 traditional client
    When I create the bootstrap repository for "sle15_client" on the server

@sle15_minion
  Scenario: Create the bootstrap repository for a SLES 15 minion
    When I create the bootstrap repository for "sle15_minion" on the server

@sle15_ssh_minion
  Scenario: Create the bootstrap repository for a SLES 15 Salt SSH minion
    When I create the bootstrap repository for "sle15_ssh_minion" on the server

@sle15sp1_client
  Scenario: Create the bootstrap repository for a SLES 15 SP1 traditional client
    When I create the bootstrap repository for "sle15sp1_client" on the server

@sle15sp1_minion
  Scenario: Create the bootstrap repository for a SLES 15 SP1 minion
    When I create the bootstrap repository for "sle15sp1_minion" on the server

@sle15sp1_ssh_minion
  Scenario: Create the bootstrap repository for a SLES 15 SP1 Salt SSH minion
    When I create the bootstrap repository for "sle15sp1_ssh_minion" on the server

@sle15sp2_client
  Scenario: Create the bootstrap repository for a SLES 15 SP2 traditional client
    When I create the bootstrap repository for "sle15sp2_client" on the server

@sle15sp2_minion
  Scenario: Create the bootstrap repository for a SLES 15 SP2 minion
    When I create the bootstrap repository for "sle15sp2_minion" on the server

@sle15sp2_ssh_minion
  Scenario: Create the bootstrap repository for a SLES 15 SP2 Salt SSH minion
    When I create the bootstrap repository for "sle15sp2_ssh_minion" on the server

@sle15sp3_client
  Scenario: Create the bootstrap repository for a SLES 15 SP3 traditional client
    When I create the bootstrap repository for "sle15sp3_client" on the server

@sle15sp3_minion
  Scenario: Create the bootstrap repository for a SLES 15 SP3 minion
    When I create the bootstrap repository for "sle15sp3_minion" on the server

@sle15sp3_ssh_minion
  Scenario: Create the bootstrap repository for a SLES 15 SP3 Salt SSH minion
    When I create the bootstrap repository for "sle15sp3_ssh_minion" on the server

@ceos7_client
  Scenario: Create the bootstrap repository for a CentOS 7 traditional client
    When I create the bootstrap repository for "ceos7_client" on the server

@ceos7_minion
  Scenario: Create the bootstrap repository for a CentOS 7 Salt minion
    When I create the bootstrap repository for "ceos7_minion" on the server

@ceos7_ssh_minion
  Scenario: Create the bootstrap repository for a CentOS 7 Salt SSH minion
    When I create the bootstrap repository for "ceos7_ssh_minion" on the server

@ceos8_minion
  Scenario: Create the bootstrap repository for a CentOS 8 Salt minion
    When I create the bootstrap repository for "ceos8_minion" on the server

@ceos8_ssh_minion
  Scenario: Create the bootstrap repository for a CentOS 8 Salt SSH minion
    When I create the bootstrap repository for "ceos8_ssh_minion" on the server

@ubuntu1804_minion
  Scenario: Create the bootstrap repository for a Ubuntu 18.04 Salt minion
    When I create the bootstrap repository for "ubuntu1804_minion" on the server

@ubuntu1804_ssh_minion
  Scenario: Create the bootstrap repository for a Ubuntu 18.04 Salt SSH minion
    When I create the bootstrap repository for "ubuntu1804_ssh_minion" on the server

@ubuntu2004_minion
  Scenario: Create the bootstrap repository for a Ubuntu 20.04 minion
    When I create the bootstrap repository for "ubuntu2004_minion" on the server

@ubuntu2004_ssh_minion
  Scenario: Create the bootstrap repository for a Ubuntu 20.04 Salt SSH minion
    When I create the bootstrap repository for "ubuntu2004_ssh_minion" on the server

@debian9_minion
  Scenario: Create the bootstrap repository for a Debian 9 minion
    When I create the bootstrap repository for "debian9_minion" on the server

@debian9_ssh_minion
  Scenario: Create the bootstrap repository for a Debian 9 Salt SSH minion
    When I create the bootstrap repository for "debian9_ssh_minion" on the server

@debian10_minion
  Scenario: Create the bootstrap repository for a Debian 10 minion
    When I create the bootstrap repository for "debian10_minion" on the server

@debian10_ssh_minion
  Scenario: Create the bootstrap repository for a Debian 10 Salt SSH minion
    When I create the bootstrap repository for "debian10_ssh_minion" on the server
