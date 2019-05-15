#
# spec file for package supportutils-plugin-susemanager
#
# Copyright (c) 2019 SUSE LINUX GmbH, Nuernberg, Germany.
#
# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon. The license for this file, and modifications and additions to the
# file, is the same license as for the pristine package itself (unless the
# license for the pristine package is not an Open Source License, in which
# case the license is the MIT License). An "Open Source License" is a
# license that conforms to the Open Source Definition (Version 1.9)
# published by the Open Source Initiative.

# Please submit bugfixes or comments via https://bugs.opensuse.org/
#


Name:           supportutils-plugin-susemanager
Version:        4.0.2
Release:        1%{?dist}
Source:         %{name}-%{version}.tar.gz
Summary:        Supportconfig Plugin for SUSE Manager
License:        GPL-2.0-only
Group:          Documentation/SuSE
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Requires:       supportconfig-plugin-resource
Requires:       supportconfig-plugin-tag
Requires:       susemanager
Supplements:    packageand(spacewalk-common:supportutils)

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
install -d $RPM_BUILD_ROOT/usr/lib/susemanager/bin/
install -m 0544 supportconfig-sumalog $RPM_BUILD_ROOT/sbin
install -m 0544 susemanager-connection-check.pl $RPM_BUILD_ROOT/usr/lib/susemanager/bin/susemanager-connection-check
install -m 0544 susemanager $RPM_BUILD_ROOT/usr/lib/supportconfig/plugins
install -m 0644 susemanager-plugin.8.gz $RPM_BUILD_ROOT/usr/share/man/man8/susemanager-plugin.8.gz

%files
%defattr(-,root,root)
%doc COPYING.GPLv2
%dir /usr/lib/susemanager
%dir /usr/lib/susemanager/bin
/sbin/supportconfig-sumalog
/usr/lib/susemanager/bin/susemanager-connection-check
/usr/lib/supportconfig
/usr/lib/supportconfig/plugins
/usr/lib/supportconfig/plugins/susemanager
/usr/share/man/man8/susemanager-plugin.8.gz

%clean
rm -rf $RPM_BUILD_ROOT

%changelog
