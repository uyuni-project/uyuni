# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: register a traditional client centos7

  Scenario: Setup the centos7 trad-client pkg
     Given I am authorized
     And  I run "sed s/enabled=0/enabled=1/g /etc/yum.repos.d/Devel_Galaxy_Manager_Head_RES-Manager-Tools-7-x86_64.repo  -i" on "ceos-minion" without error control
     And  I run " yum repolist" on "ceos-minion"
     And  I run "yum install -y rhn-client-tools rhn-check rhn-setup rhnsd hwdata m2crypto wget osad rhncfg-actions" on "ceos-minion"
     And  I register the centos7 as tradclient
