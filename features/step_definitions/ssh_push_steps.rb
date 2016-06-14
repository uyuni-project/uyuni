# Copyright (c) 2013-2016 Novell, Inc, SUSE-LINUX
# Licensed under the terms of the MIT license.

When(/^I register this client for SSH push via tunnel$/) do
  # Create backups of /etc/hosts and up2date config
  sshcmd("cp /etc/hosts /etc/hosts.BACKUP", ignore_err: true)
  sshcmd("/etc/sysconfig/rhn/up2date /etc/sysconfig/rhn/up2date.BACKUP", ignore_err: true)

  # Generate expect file
  bootstrap = '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'
  expectFile = ExpectFileGenerator.new("#{$myhostname}", bootstrap)
  step "I copy to server \"" + expectFile.path + "\""

  # Perform the registration
  filename = expectFile.filename
  command = "echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST expect #{filename} 2>&1"
  $sshout = ''
  $sshout = `#{command}`
  unless $?.success?
    raise "Execute command failed: #{$!}: #{$sshout}"
  end

  # Restore files from backups
  sshcmd("mv /etc/hosts.BACKUP /etc/hosts", ignore_err: true)
  sshcmd("mv /etc/sysconfig/rhn/up2date.BACKUP /etc/sysconfig/rhn/up2date'", ignore_err: true)
end
