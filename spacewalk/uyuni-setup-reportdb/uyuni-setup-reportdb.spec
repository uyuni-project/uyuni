#
# spec file for package uyuni-setup-reportdb
#
# Copyright (c) 2022 SUSE LLC
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


Name:           uyuni-setup-reportdb
Version:        4.3.2
Release:        1
Summary:        Tools to setup PostgreSQL database as reporting DB for Uyuni and SUSE Manager
License:        GPL-2.0-only
Group:          Applications/System
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
%if 0%{?suse_version}
%if 0%{?sle_version} >= 150400
Requires:       postgresql14-contrib
Requires:       postgresql14-server
%else
Requires:       postgresql13-contrib
Requires:       postgresql13-server
%endif
%else
Requires:       postgresql-contrib >= 12
Requires:       postgresql-server > 12
%endif
Requires:       lsof
Requires:       susemanager-schema-utility
Requires:       uyuni-reportdb-schema

%description
Script, which will setup PostgreSQL database as reporting DB for Uyuni and SUSE Manager Server

%prep
%setup -q

%build

%install
install -d -m 755 %{buildroot}/%{_bindir}
install -m 0755 bin/* %{buildroot}/%{_bindir}

%files
%defattr(-,root,root,-)
%license LICENSE
%attr(755,root,root) %{_bindir}/uyuni-setup-reportdb
%attr(755,root,root) %{_bindir}/uyuni-setup-reportdb-user
#%{_mandir}/man1/*

%changelog
