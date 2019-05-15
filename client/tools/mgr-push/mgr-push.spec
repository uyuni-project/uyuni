#
# spec file for package mgr-push
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


# Old name and version+1 before renaming to mgr-push
%define oldname rhnpush
%define oldversion 5.5.114
%{!?pylint_check: %global pylint_check 0}

%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
%global build_py3   1
%global default_py3 1
%endif

%global build_py2   1

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

Name:           mgr-push
Summary:        Package uploader for the Spacewalk
License:        GPL-2.0-only
Group:          Applications/System
Url:            https://github.com/uyuni-project/uyuni
Version:        4.0.6
Provides:       %{oldname} = %{oldversion}
Obsoletes:      %{oldname} < %{oldversion}
Release:        1%{?dist}
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
Source1:        %{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif
Requires:       %{pythonX}-%{name} = %{version}-%{release}
BuildRequires:  docbook-utils
BuildRequires:  gettext
%if 0%{?pylint_check}
%if 0%{?build_py2}
BuildRequires:  spacewalk-python2-pylint
%endif
%if 0%{?build_py3}
BuildRequires:  spacewalk-python3-pylint
%endif
%endif

%description
rhnpush uploads package headers to the Spacewalk
servers into specified channels and allows for several other channel
management operations relevant to controlling what packages are available
per channel.

%if 0%{?build_py2}
%package -n python2-%{name}
Summary:        Package uploader for the Spacewalk or Red Hat Satellite Server
Group:          Applications/System
Provides:       python2-%{oldname} = %{oldversion}
Obsoletes:      python2-%{oldname} < %{oldversion}
Requires:       %{name} = %{version}-%{release}
%if 0%{?fedora} >= 28
Requires:       python2-rpm
BuildRequires:  python2-devel
%else
Requires:       rpm-python
BuildRequires:  python-devel
%endif
Requires:       python2-rhn-client-tools
Requires:       rhnlib >= 2.8.3
Requires:       spacewalk-backend-libs >= 1.7.17
Requires:       spacewalk-usix
BuildRequires:  python2-rhn-client-tools
BuildRequires:  spacewalk-backend-libs > 1.8.33

%description -n python2-%{name}
Python 2 specific files for rhnpush.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        Package uploader for the Spacewalk or Red Hat Satellite Server
Group:          Applications/System
Provides:       python3-%{oldname} = %{oldversion}
Obsoletes:      python3-%{oldname} < %{oldversion}
Requires:       %{name} = %{version}-%{release}
%if 0%{?suse_version}
Requires:       python3-rpm
%else
Requires:       rpm-python3
%endif
Requires:       python3-rhn-client-tools
Requires:       python3-rhnlib >= 2.8.3
Requires:       python3-spacewalk-backend-libs
Requires:       python3-spacewalk-usix
BuildRequires:  python3-devel
BuildRequires:  python3-rhn-client-tools
BuildRequires:  python3-rpm-macros
BuildRequires:  python3-spacewalk-backend-libs > 1.8.33

%description -n python3-%{name}
Python 3 specific files for rhnpush.
%endif

%prep
%setup -q

%build
make -f Makefile.rhnpush all

%install
install -d $RPM_BUILD_ROOT/%{python_sitelib}
%if 0%{?build_py2}
make -f Makefile.rhnpush install PREFIX=$RPM_BUILD_ROOT ROOT=%{python_sitelib} \
    MANDIR=%{_mandir} PYTHON_VERSION=%{python_version}
%endif

%if 0%{?build_py3}
sed -i 's|#!/usr/bin/python|#!/usr/bin/python3|' rhnpush
install -d $RPM_BUILD_ROOT/%{python3_sitelib}
make -f Makefile.rhnpush install PREFIX=$RPM_BUILD_ROOT ROOT=%{python3_sitelib} \
    MANDIR=%{_mandir} PYTHON_VERSION=%{python3_version}
%endif

%define default_suffix %{?default_py3:-%{python3_version}}%{!?default_py3:-%{python_version}}
ln -s rhnpush%{default_suffix} $RPM_BUILD_ROOT%{_bindir}/rhnpush
%if 0%{?suse_version}
ln -s rhnpush $RPM_BUILD_ROOT/%{_bindir}/mgrpush
%endif

%check
%if 0%{?pylint_check}
# check coding style
%if 0%{?build_py2}
export PYTHONPATH=$RPM_BUILD_ROOT%{python_sitelib}
spacewalk-python2-pylint $RPM_BUILD_ROOT%{_bindir} $RPM_BUILD_ROOT%{python_sitelib}
%endif
%if 0%{?build_py3}
export PYTHONPATH=$RPM_BUILD_ROOT%{python3_sitelib}
spacewalk-python3-pylint $RPM_BUILD_ROOT%{_bindir} $RPM_BUILD_ROOT%{python3_sitelib}
%endif
%endif

%files
%defattr(-,root,root)
%{_bindir}/rhnpush
%{_bindir}/rpm2mpm
%dir %{_sysconfdir}/sysconfig/rhn
%if 0%{?suse_version}
%{_bindir}/mgrpush
%endif
%config(noreplace) %attr(644,root,root) %{_sysconfdir}/sysconfig/rhn/rhnpushrc
%{_mandir}/man8/rhnpush.8*
%doc COPYING

%if 0%{?build_py2}
%files -n python2-%{name}
%defattr(-,root,root)
%attr(755,root,root) %{_bindir}/rhnpush-%{python_version}
%{python_sitelib}/rhnpush/
%endif

%if 0%{?build_py3}
%files -n python3-%{name}
%defattr(-,root,root)
%attr(755,root,root) %{_bindir}/rhnpush-%{python3_version}
%{python3_sitelib}/rhnpush/
%endif

%changelog
