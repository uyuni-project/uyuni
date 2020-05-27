#
# spec file for package spacewalk-utils
#
# Copyright (c) 2020 SUSE LLC
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

Name:           spacewalk-utils
Version:        4.1.9
Release:        1%{?dist}
Summary:        Utilities that may be run against a SUSE Manager/Uyuni server
License:        GPL-2.0-only AND GPL-3.0-or-later
Group:          Productivity/Other
Url:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
BuildRequires:  fdupes
BuildRequires:  docbook-utils
BuildRequires:  python3
%if 0%{?pylint_check}
BuildRequires:  spacewalk-python3-pylint
%endif
BuildRequires:  uyuni-base-common

# Required by spacewalk-hostname-rename
Requires:       bash
# Required by spacewalk-hostname-rename
Requires:       cobbler
# Required by spacewalk-hostname-rename
Requires:       iproute
# Required by spacewalk-hostname-rename
%if 0%{?suse_version}
Requires:       perl = %{perl_version}
%else
Requires:       perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
%endif
# Required by spacewalk-hostname-rename
Requires:       perl-Satcon
# Required by debsolver.py
Requires:       python3-PyYAML
# Required by debsolver.py, cloneByDate.py, spacewalk-common-channels
Requires:       python3-uyuni-common-libs
# Required by spacewalk-clone-by-date, spacewalk-sync-setup
Requires:       python3-salt
# Required by spacewalk-hostname-rename
Requires:       rpm
# Required by spacewalk-hostname-rename
Requires:       spacewalk-admin
# Required by cloneByDate.py, spacewalk-clone-by-date, spacewalk-common-channels
Requires:       spacewalk-backend
# Required by cloneByDate.py
Requires:       spacewalk-backend-sql
# Required by cloneByDate.py, depsolver.py
Requires:       spacewalk-backend-tools >= 2.2.27
# Required by spacewalk-hostname-rename
Requires:       spacewalk-certs-tools
# Required by spacewalk-hostname-rename
Requires:       spacewalk-config
# Required by spacewalk-export
Requires:       spacewalk-reports
# Required by spacewalk-hostname-rename
Requires:       spacewalk-setup
# Required by spacewalk-hostname-rename (provides /usr/bin/spacewalk-sql)
Requires:       susemanager-schema
# Required by cloneByDate.py, depsolver.py,spacewalk-clone-by-date
Requires(pre):  uyuni-base-common


%description
Utilities that may be run against a SUSE Manager server (supported) or an Uyuni server

%package extras
Summary:        Extra utilities that may run against a SUSE Manager/Uyuni server
# Required by spacewalk-watch-channel-sync.sh
Requires:       bash
# Required by taskotop
Requires:       python3-curses
# Required by sw-ldap-user-sync
Requires:       python3-ldap
# Required by sw-ldap-user-sync
Requires:       python3-PyYAML
# Required by migrate-system-profile
Requires:       python3-rhnlib >= 2.5.20
# Required by migrateSystemProfile.py, systemSnapshot.py
Requires:       python3-uyuni-common-libs
# Required by spacewalk-manage-snapshots, systemSnapshot.py
Requires:       spacewalk-backend
# Required by taskotop
Requires:       spacewalk-backend-sql
# Required by spacewalk-final-archive, spacewalk-watch-channel-sync.sh
Requires:       spacewalk-backend-tools >= 2.2.27
# As spacewalk-utils owns {python3_sitelib}/utils
Requires:       spacewalk-utils
# Required by migrate-system-profile, migrateSystemProfile.py, spacewalk-export-channels, spacewalk-manage-snapshots, sw-system-snapshot, systemSnapshot.py
Requires(pre):  uyuni-base-common

%description extras
Extra utilities that may be run against a SUSE Manager server (unsupported) or an Uyuni server

%prep
%setup -q

%build
make all

%install
make install PREFIX=$RPM_BUILD_ROOT ROOT=%{python3_sitelib} \
    MANDIR=%{_mandir}
pushd %{buildroot}
%py3_compile -O %{buildroot}%{python3_sitelib}
%fdupes %{buildroot}%{python3_sitelib}
popd

%check
%if 0%{?pylint_check}
# check coding style
spacewalk-python3-pylint $RPM_BUILD_ROOT%{python3_sitelib}
%endif

%files
%defattr(-,root,root)
%license COPYING.GPLv2 COPYING.GPLv3
%attr(755,root,root) %{_bindir}/spacewalk-common-channels
%attr(755,root,root) %{_bindir}/spacewalk-clone-by-date
%attr(755,root,root) %{_bindir}/spacewalk-hostname-rename
%attr(755,root,root) %{_bindir}/spacewalk-manage-channel-lifecycle
%attr(755,root,root) %{_bindir}/spacewalk-sync-setup
%config %{_sysconfdir}/rhn/spacewalk-common-channels.ini
%dir %{python3_sitelib}/utils
%{python3_sitelib}/utils/__init__.py*
%{python3_sitelib}/utils/systemSnapshot.py*
%{python3_sitelib}/utils/cloneByDate.py*
%{python3_sitelib}/utils/depsolver.py*
%dir %{python3_sitelib}/utils/__pycache__
%{python3_sitelib}/utils/__pycache__/__init__.*
%{python3_sitelib}/utils/__pycache__/systemSnapshot.*
%{python3_sitelib}/utils/__pycache__/cloneByDate.*
%{python3_sitelib}/utils/__pycache__/depsolver.*
%{_mandir}/man8/spacewalk-clone-by-date.8.gz
%{_mandir}/man8/spacewalk-hostname-rename.8.gz
%{_mandir}/man8/spacewalk-sync-setup.8.gz

%files extras
%defattr(-,root,root)
%license COPYING.GPLv2 COPYING.GPLv3
%attr(755,root,root) %{_bindir}/apply_errata
%attr(755,root,root) %{_bindir}/delete-old-systems-interactive
%attr(755,root,root) %{_bindir}/migrate-system-profile
%attr(755,root,root) %{_bindir}/spacewalk-api
%attr(755,root,root) %{_bindir}/spacewalk-export
%attr(755,root,root) %{_bindir}/spacewalk-export-channels
%attr(755,root,root) %{_bindir}/spacewalk-final-archive
%attr(755,root,root) %{_bindir}/spacewalk-manage-snapshots
%attr(755,root,root) %{_bindir}/spacewalk-watch-channel-sync.sh
%attr(755,root,root) %{_bindir}/sw-ldap-user-sync
%attr(755,root,root) %{_bindir}/sw-system-snapshot
%attr(755,root,root) %{_bindir}/taskotop
%{python3_sitelib}/utils/migrateSystemProfile.py*
%{python3_sitelib}/utils/__pycache__/migrateSystemProfile.*
%config(noreplace) %{_sysconfdir}/rhn/sw-ldap-user-sync.conf
%{_mandir}/man8/delete-old-systems-interactive.8.gz
%{_mandir}/man8/migrate-system-profile.8.gz
%{_mandir}/man8/spacewalk-api.8.gz
%{_mandir}/man8/spacewalk-export-channels.8.gz
%{_mandir}/man8/spacewalk-export.8.gz
%{_mandir}/man8/spacewalk-final-archive.8.gz
%{_mandir}/man8/spacewalk-manage-snapshots.8.gz
%{_mandir}/man8/sw-system-snapshot.8.gz
%{_mandir}/man8/taskotop.8.gz

%changelog
