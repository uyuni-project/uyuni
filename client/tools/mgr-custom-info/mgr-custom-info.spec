#
# spec file for package mgr-custom-info
#
# Copyright (c) 2019 SUSE LINUX GmbH, Nuernberg, Germany.
# Copyright (c) 2008-2018 Red Hat, Inc.
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


%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
%global build_py3   1
%endif

Name:           mgr-custom-info
Summary:        Set and list custom values for Spacewalk-enabled machines
License:        GPL-2.0-only
Group:          Applications/System
Version:        4.0.4
# 5.4.43.2 was the last version+1 before renaming to mgr-custom-info
Provides:       rhn-custom-info = 5.4.43.3
Obsoletes:      rhn-custom-info < 5.4.43.3
Release:        1%{?dist}
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
Url:            https://github.com/uyuni-project/uyuni
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?build_py3}
BuildRequires:  python3-devel
Requires:       python3-rhnlib
%else
BuildRequires:  python-devel
Requires:       rhnlib
%endif

%if 0%{?fedora} || 0%{?rhel} >= 8
Requires:       dnf-plugin-spacewalk
%else
%if 0%{?suse_version}
Requires:       zypp-plugin-spacewalk
# provide rhn directories for filelist check
BuildRequires:  spacewalk-client-tools
%else
Requires:       yum-rhn-plugin
%endif
%endif

%description
Allows for the setting and listing of custom key/value pairs for
an Spacewalk-enabled system.

%prep
%setup -q

%build
make -f Makefile.rhn-custom-info all
%if 0%{?build_py3}
    sed -i 's|#!/usr/bin/python|#!/usr/bin/python3|' *.py
%endif

%install
install -d $RPM_BUILD_ROOT
%global pypath %{?build_py3:%{python3_sitelib}}%{!?build_py3:%{python_sitelib}}
make -f Makefile.rhn-custom-info install PREFIX=$RPM_BUILD_ROOT ROOT=%{pypath}
install -d $RPM_BUILD_ROOT%{_mandir}/man8/
install -m 644 rhn-custom-info.8 $RPM_BUILD_ROOT%{_mandir}/man8/
%if 0%{?suse_version}
ln -s rhn-custom-info $RPM_BUILD_ROOT/%{_bindir}/mgr-custom-info
%endif

%files
%defattr(-,root,root,-)
%{pypath}/custominfo/
%{_bindir}/*-custom-info
%doc LICENSE
%{_mandir}/man8/rhn-custom-info.*

%changelog
