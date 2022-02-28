#
# spec file for package spacewalk-setup-postgresql
#
# Copyright (c) 2021 SUSE LLC
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


Name:           spacewalk-setup-postgresql
Version:        4.3.3
Release:        1
Summary:        Tools to setup embedded PostgreSQL database for Spacewalk
License:        GPL-2.0-only
Group:          Applications/System
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
%if 0%{?suse_version}
%if 0%{?sle_version} >= 150400
Requires:       postgresql14-server
Requires:       postgresql14-contrib
%else
Requires:       postgresql13-server
Requires:       postgresql13-contrib
%endif
%else
Requires:       postgresql-server > 12
Requires:       postgresql-contrib >= 12
%endif
Requires:       lsof
Requires:       spacewalk-dobby
Requires:       perl(DBD::Pg)
Obsoletes:      spacewalk-setup-embedded-postgresql

%description
Script, which will setup PostgreSQL database for Spacewalk.

%prep
%setup -q

%build

%install
install -d -m 755 %{buildroot}/%{_bindir}
install -m 0755 bin/* %{buildroot}/%{_bindir}
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/defaults.d
install -m 0644 setup/defaults.d/* %{buildroot}/%{_datadir}/spacewalk/setup/defaults.d/
install -m 0644 setup/*.conf %{buildroot}/%{_datadir}/spacewalk/setup/

%files
%defattr(-,root,root,-)
%license LICENSE
%dir %{_datadir}/spacewalk
%dir %{_datadir}/spacewalk/setup
%dir %{_datadir}/spacewalk/setup/defaults.d
%attr(755,root,root) %{_bindir}/spacewalk-setup-postgresql
#%{_mandir}/man1/*
%{_datadir}/spacewalk/setup/defaults.d/*
%{_datadir}/spacewalk/setup/*.conf
%if 0%{?suse_version}
%dir %{_datadir}/spacewalk
%dir %{_datadir}/spacewalk/setup
%dir %{_datadir}/spacewalk/setup/defaults.d
%endif

%changelog
