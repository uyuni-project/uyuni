#
# spec file for package uyuni-reportdb-schema
#
# Copyright (c) 2024 SUSE LLC
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


%{!?fedora: %global sbinpath /sbin}%{?fedora: %global sbinpath %{_sbindir}}

Name:           uyuni-reportdb-schema
Version:        5.1.0
Release:        0
Summary:        Report DB SQL schema for Spacewalk server
License:        GPL-2.0-only
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
Source1:        https://raw.githubusercontent.com/uyuni-project/uyuni/%{name}-%{version}-0/schema/reportdb/%{name}-rpmlintrc
BuildArch:      noarch

%if 0%{?rhel}
BuildRequires:  perl-File-Find
%endif

BuildRequires:  make
BuildRequires:  susemanager-schema-sanity
%if 0%{?suse_version}
BuildRequires:  fdupes
%endif

Requires:       susemanager-schema-utility

%define rhnroot /usr/share/susemanager/db/

%define postgres %{rhnroot}/reportdb

%description
uyuni-reportdb-schema is the SQL schema for the SUSE Manager server.

%prep

%setup -q

%build
make -f Makefile.schema SCHEMA=%{name} VERSION=%{version} RELEASE=%{release}

%install
install -m 0755 -d %{buildroot}%{rhnroot}
install -m 0755 -d %{buildroot}%{postgres}
install -m 0644 postgres/main.sql %{buildroot}%{postgres}
install -m 0644 postgres/end.sql %{buildroot}%{postgres}/upgrade-end.sql

install -m 0755 -d %{buildroot}%{rhnroot}/reportdb-schema-upgrade
( cd upgrade && tar cf - --exclude='*.sql' . | ( cd %{buildroot}%{rhnroot}/reportdb-schema-upgrade && tar xf - ) )

%files
%defattr(-,root,root)
%dir %{_datadir}/susemanager
%dir %{rhnroot}
%{postgres}
%{rhnroot}/reportdb-schema-upgrade

%changelog
