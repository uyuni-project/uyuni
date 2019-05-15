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

%if 0%{?suse_version} > 1320
# SLE15 builds on Python 3
%global build_py3   1
%endif
%define pythonX %{?build_py3:python3}%{!?build_py3:python2}

%define rhnroot %{_prefix}/share/rhn
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
Version:        4.0.9
Release:        1%{?dist}
Summary:        Utilities that may be run against a Spacewalk server.
License:        GPL-2.0-only AND GPL-3.0-or-later
Group:          Applications/Internet

Url:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%if 0%{?pylint_check}
BuildRequires:  spacewalk-python2-pylint
%endif
BuildRequires:  /usr/bin/docbook2man
BuildRequires:  /usr/bin/pod2man
BuildRequires:  docbook-utils
BuildRequires:  python
%if 0%{?fedora} || 0%{?rhel} > 5
BuildRequires:  spacewalk-backend >= 1.7.24
BuildRequires:  spacewalk-backend-libs >= 1.7.24
BuildRequires:  spacewalk-backend-tools >= 1.7.24
BuildRequires:  spacewalk-config
BuildRequires:  yum
%endif

Requires:       bash
Requires:       cobbler
%if 0%{?fedora} >= 22
Recommends:     cobbler20
%endif
Requires:       coreutils
%if ! 0%{?suse_version}
Requires:       initscripts
%endif
Requires:       /usr/bin/spacewalk-sql
Requires:       iproute
Requires:       net-tools
Requires:       perl-Satcon
%if 0%{?suse_version}
Requires:       perl = %{perl_version}
%else
Requires:       perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
%endif
Requires:       python
Requires:       rpm-python
%if 0%{?rhel} == 6
Requires:       %{pythonX}-argparse
%endif
Requires:       %{pythonX}-rhnlib >= 2.5.20
Requires:       rpm
%if ! 0%{?suse_version}
Requires:       setup
%endif
Requires:       salt
Requires:       spacewalk-admin
Requires:       spacewalk-backend
%if 0%{?suse_version} >= 1320
Requires:       python3-spacewalk-backend-libs
%else
Requires:       spacewalk-backend-libs
%endif
Requires:       spacewalk-backend-tools >= 2.2.27
Requires:       spacewalk-certs-tools
Requires:       spacewalk-config
Requires:       spacewalk-reports
Requires:       spacewalk-setup

Requires:       %{pythonX}-curses
Requires:       %{pythonX}-ldap
%if 0%{?suse_version} >= 1320
Requires:       python3-PyYAML
%else
Requires:       %{pythonX}-yaml
%endif

%description
Generic utilities that may be run against a Spacewalk server.


%prep
%setup -q

%if  0%{?suse_version} && 0%{?suse_version} < 1200
%define pod2man POD2MAN=pod2man
%endif
%build
make all %{?pod2man}

# Fixing shebang for Python 3
%if 0%{?build_py3}
for i in $(find . -type f);
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done
%endif

%install
install -d $RPM_BUILD_ROOT/%{rhnroot}
make install PREFIX=$RPM_BUILD_ROOT ROOT=%{rhnroot} \
    MANDIR=%{_mandir} %{?pod2man}
pushd %{buildroot}
find -name '*.py' -print0 | xargs -0 python %py_libdir/py_compile.py
popd

%check
%if 0%{?pylint_check}
# check coding style
spacewalk-python2-pylint $RPM_BUILD_ROOT%{rhnroot}
%endif

%files
%defattr(-,root,root)
%config %{_sysconfdir}/rhn/spacewalk-common-channels.ini
%config(noreplace) %{_sysconfdir}/rhn/sw-ldap-user-sync.conf
%attr(755,root,root) %{_bindir}/*
%dir %{rhnroot}/utils
%{rhnroot}/utils/__init__.py*
%{rhnroot}/utils/systemSnapshot.py*
%{rhnroot}/utils/migrateSystemProfile.py*
%{rhnroot}/utils/cloneByDate.py*
%{rhnroot}/utils/depsolver.py*
%{_mandir}/man8/*
%attr(0750,root,%{apache_group}) %dir /etc/rhn
%dir %{_datadir}/rhn
%doc COPYING.GPLv2 COPYING.GPLv3

%changelog
