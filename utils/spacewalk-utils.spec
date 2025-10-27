#
# spec file for package spacewalk-utils
#
# Copyright (c) 2025 SUSE LLC
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

# The productprettyname macros is controlled in the prjconf. If not defined, we fallback here
%{!?productprettyname: %global productprettyname Uyuni}

Name:           spacewalk-utils
Version:        5.2.1
Release:        0
Summary:        Utilities that may be run against a %{productprettyname} server
License:        GPL-2.0-only AND GPL-3.0-or-later
Group:          Productivity/Other
URL:            https://github.com/uyuni-project/uyuni
#!CreateArchive: %{name}
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRequires:  docbook-utils
BuildRequires:  fdupes
BuildRequires:  make
BuildRequires:  python3
BuildRequires:  python3-rpm-macros
BuildRequires:  uyuni-base-common
# Required by depsolver.py
Requires:       (python3-PyYAML or python3-pyyaml)
# Required by spacewalk-watch-channel-sync.sh
Requires:       bash
# Required by depsolver.py
Requires:       python3-solv
# Required by depsolver.py, cloneByDate.py, spacewalk-common-channels
Requires:       python3-uyuni-common-libs
# Required by cloneByDate.py, spacewalk-clone-by-date, spacewalk-common-channels
Requires:       spacewalk-backend
# Required by cloneByDate.py
Requires:       spacewalk-backend-sql
# Required by cloneByDate.py, depsolver.py
Requires:       spacewalk-backend-tools >= 2.2.27
# Required by cloneByDate.py, depsolver.py,spacewalk-clone-by-date
Requires(pre):  uyuni-base-common
# Required by taskotop
Requires:       python3-curses
# Required by taskotop
Requires:       spacewalk-backend-sql
BuildArch:      noarch

%if 0%{?suse_version}
Requires:       perl = %{perl_version}
%else
Requires:       perl(:MODULE_COMPAT_%(eval "`perl -V:version`"; echo $version))
%endif

%description
Utilities that may be run against a %{productprettyname} server

%prep
%setup -q

%build
make all

%install
make install PREFIX=%{buildroot} ROOT=%{python3_sitelib} \
    MANDIR=%{_mandir}
pushd %{buildroot}
%if 0%{?suse_version}
%py3_compile -O %{buildroot}%{python3_sitelib}
%fdupes %{buildroot}%{python3_sitelib}
%else
%py_byte_compile %{__python3} %{buildroot}%{python3_sitelib}
%endif
popd

%check

%files
%defattr(-,root,root)
%license COPYING.GPLv2 COPYING.GPLv3
%attr(755,root,root) %{_bindir}/spacewalk-common-channels
%attr(755,root,root) %{_bindir}/spacewalk-clone-by-date
%attr(755,root,root) %{_bindir}/spacewalk-manage-channel-lifecycle
%attr(755,root,root) %{_bindir}/taskotop
%attr(755,root,root) %{_bindir}/spacewalk-watch-channel-sync.sh
%config %{_sysconfdir}/rhn/spacewalk-common-channels.ini
%dir %{python3_sitelib}/utils
%{python3_sitelib}/utils/__init__.py*
%{python3_sitelib}/utils/cloneByDate.py*
%{python3_sitelib}/utils/depsolver.py*
%dir %{python3_sitelib}/utils/__pycache__
%{python3_sitelib}/utils/__pycache__/__init__.*
%{python3_sitelib}/utils/__pycache__/cloneByDate.*
%{python3_sitelib}/utils/__pycache__/depsolver.*
%{_mandir}/man8/spacewalk-clone-by-date.8%{?ext_man}
%{_mandir}/man8/taskotop.8%{?ext_man}


%changelog
