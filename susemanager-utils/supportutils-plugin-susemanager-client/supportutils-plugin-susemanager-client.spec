#
# spec file for package supportutils-plugin-susemanager-client (Version 1.0.0-1)
#
# Copyright (C) 2013 SUSE
# This file and all modifications and additions to the pristine
# package are under the same license as the package itself.
#

# norootforbuild
# neededforbuild

Name:         supportutils-plugin-susemanager-client
License:      GPLv2
Group:        Documentation/SuSE
Autoreqprov:  on
Version:      3.0.1
Release:      1%{?dist}
Source:       %{name}-%{version}.tar.gz
Summary:      Supportconfig Plugin for SUSE Manager Client
BuildRoot:    %{_tmppath}/%{name}-%{version}-build
BuildArch:    noarch
Distribution: Novell NTS
Vendor:       Novell Technical Services
Requires:     supportconfig-plugin-resource
Requires:     supportconfig-plugin-tag
Supplements:  packageand(spacewalk-check:supportutils)

%description
Extends supportconfig functionality to include system information for
a SUSE Manager Client. The supportconfig saves the plugin output to
plugin-susemanagerclient.txt.

%prep
%setup -q
%build
gzip -9f susemanagerclient-plugin.8

%install
pwd;ls -la
rm -rf $RPM_BUILD_ROOT
install -d $RPM_BUILD_ROOT/usr/lib/supportconfig/plugins
install -d $RPM_BUILD_ROOT/usr/share/man/man8
install -d $RPM_BUILD_ROOT/sbin
install -m 0544 susemanagerclient $RPM_BUILD_ROOT/usr/lib/supportconfig/plugins
install -m 0644 susemanagerclient-plugin.8.gz $RPM_BUILD_ROOT/usr/share/man/man8/susemanagerclient-plugin.8.gz

%files
%defattr(-,root,root)
%doc COPYING.GPLv2
/usr/lib/supportconfig
/usr/lib/supportconfig/plugins
/usr/lib/supportconfig/plugins/susemanagerclient
/usr/share/man/man8/susemanagerclient-plugin.8.gz

%clean
rm -rf $RPM_BUILD_ROOT

%changelog

