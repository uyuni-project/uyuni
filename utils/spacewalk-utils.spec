#
# spec file for package spacewalk-utils
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

%if 0%{?fedora} || 0%{?rhel} >= 7
%{!?pylint_check: %global pylint_check 1}
%endif

%if 0%{?fedora} || 0%{?rhel}
%global apache_group apache
%endif
%if 0%{?suse_version}
%global apache_group www
%endif

Name:           spacewalk-utils
Version:        4.1.5
Release:        1%{?dist}
Summary:        Utilities that may be run against a Uyuni server
License:        GPL-2.0-only AND GPL-3.0-or-later
Group:          Productivity/Other

Url:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%if 0%{?pylint_check}
BuildRequires:  spacewalk-python3-pylint
%endif
BuildRequires:  /usr/bin/docbook2man
BuildRequires:  /usr/bin/pod2man
BuildRequires:  docbook-utils
BuildRequires:  python3
BuildRequires:  uyuni-base-common

Requires(pre):  uyuni-base-common
Requires:       bash
Requires:       cobbler
Requires:       coreutils
Requires:       /usr/bin/spacewalk-sql
Requires:       iproute
Requires:       net-tools
Requires:       perl-Satcon
%if 0%{?suse_version}
Requires:       perl = %{perl_version}
%else
Requires:       perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
%endif
Requires:       python3
Requires:       python3-rpm
Requires:       python3-rhnlib >= 2.5.20
Requires:       rpm
Requires:       salt
Requires:       spacewalk-admin
Requires:       spacewalk-backend
Requires:       python3-uyuni-common-libs
Requires:       spacewalk-backend-tools >= 2.2.27
Requires:       spacewalk-certs-tools
Requires:       spacewalk-config
Requires:       spacewalk-reports
Requires:       spacewalk-setup

Requires:       python3-curses
Requires:       python3-ldap
%if 0%{?suse_version} >= 1320
Requires:       python3-PyYAML
%else
Requires:       python3-yaml
%endif

%description
Generic utilities that may be run against a Uyuni server.


%prep
%setup -q

%build
make all %{?pod2man}

# Fixing shebang for Python 3
for i in $(find . -type f);
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done

%install
make install PREFIX=$RPM_BUILD_ROOT ROOT=%{python3_sitelib} \
    MANDIR=%{_mandir} %{?pod2man}
pushd %{buildroot}
%py3_compile -O %{buildroot}%{python3_sitelib}
popd

%check
%if 0%{?pylint_check}
# check coding style
spacewalk-python3-pylint $RPM_BUILD_ROOT%{python3_sitelib}
%endif

%files
%defattr(-,root,root)
%config %{_sysconfdir}/rhn/spacewalk-common-channels.ini
%config(noreplace) %{_sysconfdir}/rhn/sw-ldap-user-sync.conf
%attr(755,root,root) %{_bindir}/*
%dir %{python3_sitelib}/utils
%{python3_sitelib}/utils/__init__.py*
%{python3_sitelib}/utils/systemSnapshot.py*
%{python3_sitelib}/utils/migrateSystemProfile.py*
%{python3_sitelib}/utils/cloneByDate.py*
%{python3_sitelib}/utils/depsolver.py*
%{python3_sitelib}/utils/__pycache__
%{_mandir}/man8/*
%doc COPYING.GPLv2 COPYING.GPLv3

%changelog
