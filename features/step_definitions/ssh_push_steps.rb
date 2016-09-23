# Copyright (c) 2013-2016 Novell, Inc, SUSE-LINUX
# Licensed under the terms of the MIT license.

When(/^I register this client for SSH push via tunnel$/) do
  # Create backups of /etc/hosts and up2date config
  $server.run("cp /etc/hosts /etc/hosts.BACKUP")
  $server.run("/etc/sysconfig/rhn/up2date /etc/sysconfig/rhn/up2date.BACKUP")

  # Generate expect file
  bootstrap = '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'
  expect_file = ExpectFileGenerator.new("#{$client_ip}", bootstrap)
  step "I copy to server \"" + expect_file.path + "\""
  filename = expect_file.filename
  # Perform the registration
  command = "expect #{filename}"
  $server.run(command, true, 600, 'root')
  # Restore files from backups
  $server.run("mv /etc/hosts.BACKUP /etc/hosts")
  $server.run("mv /etc/sysconfig/rhn/up2date.BACKUP /etc/sysconfig/rhn/up2date")
end
