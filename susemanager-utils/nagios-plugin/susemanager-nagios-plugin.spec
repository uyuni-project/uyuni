#
# spec file for package susemanager-nagios-plugin
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


Name:           susemanager-nagios-plugin
Version:        4.0.5
Release:        1%{?dist}
Summary:        Nagios plugins for SUSE Manager
License:        GPL-2.0-only
Group:          System/Monitoring
Url:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%description
Nagios plugins specific for SUSE Manager.

%prep
%setup -q

%build

%install
install -D -m 0755 check_suma_patches %{buildroot}/usr/lib/nagios/plugins/check_suma_patches
install -D -m 0755 check_suma_lastevent %{buildroot}/usr/lib/nagios/plugins/check_suma_lastevent
install -D -m 0755 check_suma_common.py %{buildroot}/usr/lib/nagios/plugins/check_suma_common.py

%files
%defattr(-,root,root,-)
%dir /usr/lib/nagios
%dir /usr/lib/nagios/plugins
/usr/lib/nagios/plugins/check_suma_patches
/usr/lib/nagios/plugins/check_suma_lastevent
/usr/lib/nagios/plugins/check_suma_common.py

%changelog
