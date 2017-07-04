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
cuke 'Adding Test-Channel-x86_64 base channel$'
cuke 'Adding Test-Repository-x86_64 repository$'
cuke "Disable Metadata check for Test-Repository-x86_64 repository$"
cuke 'Add repository to the x86_64 channel$'
cuke 'Sync the repository in the x86_64 channel$'

cuke 'create an activation key with Channel and package list \(x64\)$'
cuke 'Create the bootstrap script - traditional$'
cuke 'Create activation key for SSH push$'
cuke 'Create activation key for SSH push via tunnel$'

cuke 'register this client using the bootstrap script$'
cuke 'bootstrap a sles minion$'
cuke 'bootstrap a centos minion$'
