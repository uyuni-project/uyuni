# Copyright (c) 2013 Novell, Inc.
# Licensed under the terms of the MIT license.

When /^I register this client for SSH push via tunnel$/ do
  # Create backups of /etc/hosts and up2date config
  FileUtils.cp('/etc/hosts', '/etc/hosts.BACKUP');
  FileUtils.cp('/etc/sysconfig/rhn/up2date', '/etc/sysconfig/rhn/up2date.BACKUP');

  # Generate expect file
  bootstrap = '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'
  expectFile = ExpectFileGenerator.new("#{$myhostname}", bootstrap)
  When "I copy to server \"" + expectFile.path + "\""

  # Perform the registration
  filename = expectFile.filename
  command = "echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST expect #{filename} 2>&1"
  $sshout = ''
  $sshout = `#{command}`
  unless $?.success?
    raise "Execute command failed: #{$!}: #{$sshout}"
  end

  # Restore files from backups
  FileUtils.mv('/etc/hosts.BACKUP', '/etc/hosts')
  FileUtils.mv('/etc/sysconfig/rhn/up2date.BACKUP', '/etc/sysconfig/rhn/up2date')
end

