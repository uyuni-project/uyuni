#
# spec file for package uyuni-setup-reportdb
#
# Copyright (c) 2025 SUSE LLC
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

# The productprettyname macros is controlled in the prjconf. If not defined, we fallback here
%{!?productprettyname: %global productprettyname Uyuni}

Name:           uyuni-setup-reportdb
Version:        5.2.0
Release:        0
Summary:        Tools to setup PostgreSQL database as reporting DB for %{productprettyname}
License:        GPL-2.0-only
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/System
URL:            https://github.com/uyuni-project/uyuni
#!CreateArchive: %{name}
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}.tar.gz
Requires:       lsof
Requires:       susemanager-schema-utility
Requires:       uyuni-reportdb-schema
BuildArch:      noarch

%description
Script, which will setup PostgreSQL database as reporting DB for %{productprettyname} Server

%prep
%setup -q

%build

%install
install -d -m 755 %{buildroot}%{_bindir}
install -m 0755 bin/* %{buildroot}%{_bindir}

%files
%license LICENSE
%attr(755,root,root) %{_bindir}/uyuni-setup-reportdb-user
%attr(755,root,root) %{_bindir}/uyuni-sort-pg_hba
#%{_mandir}/man1/*

%changelog
