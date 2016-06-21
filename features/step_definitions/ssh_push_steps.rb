# Copyright (c) 2013-2016 Novell, Inc, SUSE-LINUX
# Licensed under the terms of the MIT license.

When(/^I register this client for SSH push via tunnel$/) do
  # Create backups of /etc/hosts and up2date config
  run_cmd($client, "cp /etc/hosts /etc/hosts.BACKUP", 600)
  run_cmd($client, "/etc/sysconfig/rhn/up2date /etc/sysconfig/rhn/up2date.BACKUP", 600)

  # Generate expect file
  bootstrap = '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'
  expectFile = ExpectFileGenerator.new("#{$client_hostname}", bootstrap)
  step "I copy to server \"" + expectFile.path + "\""

  # Perform the registration
  filename = expectFile.filename
  command = "echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST expect #{filename} 2>&1"
  code = run_cmd($client, command, 600)
  if code != 0
    raise "Execute command failed: #{$!}: #{$sshout}"
  end

  # Restore files from backups
  run_cmd($client, "mv /etc/hosts.BACKUP /etc/hosts", 500)
  run_cmd($client, "mv /etc/sysconfig/rhn/up2date.BACKUP /etc/sysconfig/rhn/up2date", 500)
end
