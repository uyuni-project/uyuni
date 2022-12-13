#!/bin/bash

# Dependencies for reposync tests
zypper install --no-recommends -y dnf python3-librepo dnf dnf-plugins-core wget \
       perl-LockFile-Simple perl-Net-INET6Glue perl-LWP-Protocol-https ed
dnf install -y  http://download.fedoraproject.org/pub/fedora/linux/releases/37/Everything/x86_64/os/Packages/d/debmirror-2.36-4.fc37.noarch.rpm
