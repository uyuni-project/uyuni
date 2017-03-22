# Copyright (c) 2015-16 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test Salt master. We test that top.sls files
	 are not present in certains folder, because we don't 
	 support this

   Scenario: There are no top.sls file in certain folders
   When  I run "ls /srv/susemanager/salt/top.sls" on "server" without error control
   Then the command should fail
   
   When  I run "ls /srv/susemanager/salt/top.sls" on "server" without error control
   Then the command should fail

   When  I run "ls /srv/susemanager/pillar/top.sls" on "server" without error control
   Then the command should fail

   When  I run "ls /usr/share/susemanager/salt/top.sls" on "server" without error control
   Then the command should fail

   When  I run "ls /usr/share/susemanager/pillar/top.sls" on "server" without error control
   Then the command should fail
