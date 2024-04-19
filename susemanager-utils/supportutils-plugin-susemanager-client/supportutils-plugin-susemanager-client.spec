#
# spec file for package supportutils-plugin-susemanager-client
#
# Copyright (c) 2024 SUSE LLC
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


Name:           supportutils-plugin-susemanager-client
Version:        5.0.3
Release:        0
Source:         %{name}-%{version}.tar.gz
Summary:        Supportconfig Plugin for SUSE Manager Client
License:        GPL-2.0-only
Group:          Documentation/SuSE
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
BuildRequires:  supportutils
Requires:       supportconfig-plugin-resource
Requires:       supportconfig-plugin-tag
Supplements:    packageand(salt-minion:supportutils)
Supplements:    packageand(spacewalk-check:supportutils)

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

# if the new style rc file is available install the new version, otherwise the old one
# Only SLE15 and newer support the new style rc file.
# SLE12 and older only the old variant
if [ -e /usr/lib/supportconfig/resources/supportconfig.rc ]; then
    install -m 0544 susemanagerclient $RPM_BUILD_ROOT/usr/lib/supportconfig/plugins/susemanagerclient
else
    install -m 0544 susemanagerclient-scplugin $RPM_BUILD_ROOT/usr/lib/supportconfig/plugins/susemanagerclient
fi
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
