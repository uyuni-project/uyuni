#
# spec file for package supportutils-plugin-susemanager
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


Name:           supportutils-plugin-susemanager
Version:        5.1.0
Release:        0
Summary:        Supportconfig Plugin for SUSE Manager
License:        GPL-2.0-only
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Documentation/SuSE
Source:         %{name}-%{version}.tar.gz
URL:            https://github.com/uyuni-project/uyuni
Requires:       supportconfig-plugin-resource
Requires:       supportconfig-plugin-tag
Requires:       susemanager
Requires:       perl(XML::Simple)
Supplements:    packageand(spacewalk-common:supportutils)
BuildArch:      noarch

%description
Extends supportconfig functionality to include system information about
SUSE Manager. The supportconfig saves the plugin output to plugin-susemanager.txt.

%prep
%setup -q

%build
gzip -9f susemanager-plugin.8

%install
pwd;ls -la
install -d %{buildroot}%{_prefix}/lib/supportconfig/plugins
install -d %{buildroot}%{_mandir}/man8
install -d %{buildroot}/sbin
install -d %{buildroot}%{_prefix}/lib/susemanager/bin/
install -m 0544 supportconfig-sumalog %{buildroot}/sbin
install -m 0544 susemanager-connection-check.pl %{buildroot}%{_prefix}/lib/susemanager/bin/susemanager-connection-check
install -m 0544 susemanager %{buildroot}%{_prefix}/lib/supportconfig/plugins
install -m 0644 susemanager-plugin.8.gz %{buildroot}%{_mandir}/man8/susemanager-plugin.8.gz

%files
%defattr(-,root,root)
%license COPYING.GPLv2
%dir %{_prefix}/lib/susemanager
%dir %{_prefix}/lib/susemanager/bin
/sbin/supportconfig-sumalog
%{_prefix}/lib/susemanager/bin/susemanager-connection-check
%{_prefix}/lib/supportconfig
%{_prefix}/lib/supportconfig/plugins
%{_prefix}/lib/supportconfig/plugins/susemanager
%{_mandir}/man8/susemanager-plugin.8%{?ext_man}

%changelog
