# Copyright (c) 2013-2016 Novell, Inc, SUSE-LINUX
# Licensed under the terms of the MIT license.

When(/^I register this client for SSH push via tunnel$/) do
  # Create backups of /etc/hosts and up2date config
  run_cmd($client, "cp /etc/hosts /etc/hosts.BACKUP", 600)
  run_cmd($client, "/etc/sysconfig/rhn/up2date /etc/sysconfig/rhn/up2date.BACKUP", 600)

  # Generate expect file
  bootstrap = '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'
  expect_file = ExpectFileGenerator.new("#{$client_hostname}", bootstrap)
  step "I copy to server \"" + expect_file.path + "\""

  # Perform the registration
  filename = expect_file.path
  command = "expect #{filename}"
  out, local, remote, code = $client.test_and_store_results_together(command, "root", 600)
  if code != 0
    raise "Execute command failed: #{out}"
  end

  # Restore files from backups
  run_cmd($client, "mv /etc/hosts.BACKUP /etc/hosts", 500)
  run_cmd($client, "mv /etc/sysconfig/rhn/up2date.BACKUP /etc/sysconfig/rhn/up2date", 500)
end
