#
# spec file for package supportutils-plugin-susemanager (Version 1.0.0-1)
#
# Copyright (C) 2013 SUSE
# This file and all modifications and additions to the pristine
# package are under the same license as the package itself.
#

# norootforbuild
# neededforbuild  

Name:         supportutils-plugin-susemanager
License:      GPLv2
Group:        Documentation/SuSE
Autoreqprov:  on
Version:      1.0.1
Release:      1%{?dist}
Source:       %{name}-%{version}.tar.gz
Summary:      Supportconfig Plugin for SUSE Manager
BuildRoot:    %{_tmppath}/%{name}-%{version}-build
BuildArch:    noarch
Distribution: Novell NTS
Vendor:       Novell Technical Services
Requires:     supportconfig-plugin-resource
Requires:     supportconfig-plugin-tag
Requires:     susemanager

%description
Extends supportconfig functionality to include system information about 
SUSE Manager. The supportconfig saves the plugin output to plugin-susemanager.txt.

%prep
%setup -q
%build
gzip -9f susemanager-plugin.8

%install
pwd;ls -la
rm -rf $RPM_BUILD_ROOT
install -d $RPM_BUILD_ROOT/usr/lib/supportconfig/plugins
install -d $RPM_BUILD_ROOT/usr/share/man/man8
install -d $RPM_BUILD_ROOT/sbin
install -m 0544 supportconfig-sumalog $RPM_BUILD_ROOT/sbin
install -m 0544 susemanager $RPM_BUILD_ROOT/usr/lib/supportconfig/plugins
install -m 0644 susemanager-plugin.8.gz $RPM_BUILD_ROOT/usr/share/man/man8/susemanager-plugin.8.gz

%files
%defattr(-,root,root)
/sbin/supportconfig-sumalog
/usr/lib/supportconfig
/usr/lib/supportconfig/plugins
/usr/lib/supportconfig/plugins/susemanager
/usr/share/man/man8/susemanager-plugin.8.gz

%clean
rm -rf $RPM_BUILD_ROOT

%changelog
* Thu Apr 04 2013 sbogner@suse.com
- initial release

