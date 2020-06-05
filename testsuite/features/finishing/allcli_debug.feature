# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Debug the clients after the testsuite has run

@proxy
  Scenario: Get client logs for proxy
    When I get logfiles from "proxy"

@sle_client
  Scenario: Get client logs for traditional client
    When I get logfiles from "sle_client"

@sle_minion
  Scenario: Get client logs for minion
    When I get logfiles from "sle_minion"

@centos_minion
  Scenario: Get client logs for CentOS minion
    When I get logfiles from "ceos_minion"

@ubuntu_minion
  Scenario: Get client logs for Ubuntu minion
    When I get logfiles from "ubuntu_minion"

@ssh_minion
  Scenario: Get client logs for SSH minion
    When I get logfiles from "ssh_minion"

@virthost_kvm
  Scenario: Get client logs for KVM virtualization host
    When I get logfiles from "kvm_server"

@virthost_xen
  Scenario: Get client logs for Xen virtualization host
    When I get logfiles from "xen_server"

@buildhost
  Scenario: Get client logs for build host
    When I get logfiles from "build_host"
