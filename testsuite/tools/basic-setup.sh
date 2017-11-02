#! /bin/bash

function cuke
{
  ruby -S bundle exec cucumber -n "$1"
}

cd /root/spacewalk-testsuite-base

cuke 'Create Admin users and first org$'
cuke 'Create Testing username$'
cuke 'Grant Testing user admin priviledges$'

cuke 'Adding a base channel$'
cuke 'Adding a child channel$'
cuke 'Adding SLES11-SP3-Updates i586 base channel'
cuke 'Adding a child channel to SLES11-SP3-Updates i586$'
cuke 'Adding SLES11-SP3-Updates x86_64 base channel$'
cuke 'Adding a child channel to SLES11-SP3-Updates x86_64$'
cuke 'Adding Fedora x86_64 base channel$'

cuke 'Adding SLES11-SP3-Updates-x86_64 repository$'
cuke 'Disable Metadata check for SLES11-SP3-Updates-x86_64 repository$'
cuke 'Add repository to the x86_64 channel$'
cuke 'Sync the repository in the x86_64 channel$'
cuke 'Adding SLES11-SP3-Updates-i586 repository$'
cuke 'Add repository to the i586 channel$'
cuke 'Sync the repository in the i586 channel$'

cuke 'create an activation key with Channel and package list$'
cuke 'Create bootstrap-repo for sle12sp2$'
cuke 'Create the bootstrap script$'
cuke 'create minion activation key with Channel and package list$'
cuke 'Create activation key for SSH push$'
cuke 'Create activation key for SSH push via tunnel$'

cuke 'register this client using the bootstrap script$'
cuke 'bootstrap a sles minion with an activation-key$'
cuke 'bootstrap a centos minion$'
