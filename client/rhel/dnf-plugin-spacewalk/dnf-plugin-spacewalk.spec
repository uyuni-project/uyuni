#
# spec file for package dnf-plugin-spacewalk
#
# Copyright (c) 2018 SUSE LINUX GmbH, Nuernberg, Germany.
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

# Please submit bugfixes or comments via http://bugs.opensuse.org/
#


%if 0%{?fedora} || 0%{?rhel} >= 8
%global build_py3   1
%global default_py3 1
%endif

%{!?python2_sitelib: %global python2_sitelib %(python -c "from distutils.sysconfig import get_python_lib; print get_python_lib()")}
%if ( 0%{?fedora} && 0%{?fedora} < 28 ) || ( 0%{?rhel} && 0%{?rhel} < 8 )
%global build_py2   1
%endif

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

Summary:        DNF plugin for Spacewalk
License:        GPL-2.0-only
Group:          System Environment/Base
Name:           dnf-plugin-spacewalk
Version:        4.0.3
Release:        1%{?dist}
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
URL:            https://github.com/uyuni-project/uyuni
BuildArch:      noarch

Requires:       %{pythonX}-%{name} = %{version}-%{release}
Requires:       dnf >= 2.0.0
Requires:       dnf-plugins-core
Requires:       librepo >= 1.7.15
%if 0%{?fedora}
Obsoletes:      yum-rhn-plugin < 2.7
%endif
%if 0%{?rhel} >= 8
Provides:       yum-rhn-plugin = %{version}
%endif

%description
This DNF plugin provides access to a Spacewalk server for software updates.

%if 0%{?build_py2}
%package -n python2-%{name}
Summary:        DNF plugin for Spacewalk
Group:          System Environment/Base
Provides:       python-%{name} = %{version}-%{release}
Obsoletes:      python-%{name} < %{version}-%{release}
BuildRequires:  python-devel
Requires:       %{name} = %{version}-%{release}
Requires:       python2-rhn-client-tools >= 2.8.4

%description -n python2-%{name}
Python 2 specific files for %{name}.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        DNF plugin for Spacewalk
Group:          System Environment/Base
BuildRequires:  python3-devel
Requires:       %{name} = %{version}-%{release}
Requires:       python3-rhn-client-tools >= 2.8.4
Requires:       python3-librepo

%description -n python3-%{name}
Python 3 specific files for %{name}.
%endif

%prep
%setup -q

%build

%install
install -d %{buildroot}%{_sysconfdir}/dnf/plugins/
install -d %{buildroot}/var/lib/up2date
install -d %{buildroot}%{_mandir}/man{5,8}
install -m 644 spacewalk.conf %{buildroot}%{_sysconfdir}/dnf/plugins/
install -m 644 man/spacewalk.conf.5 %{buildroot}%{_mandir}/man5/
install -m 644 man/dnf.plugin.spacewalk.8 %{buildroot}%{_mandir}/man8/
ln -sf dnf.plugin.spacewalk.8 $RPM_BUILD_ROOT%{_mandir}/man8/dnf-plugin-spacewalk.8
ln -sf dnf.plugin.spacewalk.8 $RPM_BUILD_ROOT%{_mandir}/man8/yum-rhn-plugin.8
install -d %{buildroot}%{_datadir}/licenses
install -d %{buildroot}%{_datadir}/licenses/%{name}
# python2
%if 0%{?build_py2}
install -d %{buildroot}%{python2_sitelib}/rhn/actions
install -d %{buildroot}%{python2_sitelib}/dnf-plugins/
install -m 644 spacewalk.py %{buildroot}%{python2_sitelib}/dnf-plugins/
install -m 644 actions/packages.py %{buildroot}%{python2_sitelib}/rhn/actions/
install -m 644 actions/errata.py %{buildroot}%{python2_sitelib}/rhn/actions/
%endif

%if 0%{?build_py3}
install -d %{buildroot}%{python3_sitelib}/rhn/actions
install -d %{buildroot}%{python3_sitelib}/dnf-plugins/
install -m 644 spacewalk.py %{buildroot}%{python3_sitelib}/dnf-plugins/
install -m 644 actions/packages.py %{buildroot}%{python3_sitelib}/rhn/actions/
install -m 644 actions/errata.py %{buildroot}%{python3_sitelib}/rhn/actions/
%endif

%files
%defattr(-,root,root,-)
%dir %{_sysconfdir}/dnf
%dir %{_sysconfdir}/dnf/plugins
%dir %{_datadir}/licenses
%verify(not md5 mtime size) %config(noreplace) %{_sysconfdir}/dnf/plugins/spacewalk.conf
%license LICENSE
%dir /var/lib/up2date
%{_mandir}/man*/*

%if 0%{?build_py2}
%files -n python2-%{name}
%dir %{python_sitelib}/dnf-plugins
%dir %{python_sitelib}/rhn
%dir %{python_sitelib}/rhn/actions
%{python_sitelib}/dnf-plugins/*
%{python_sitelib}/rhn/actions/*
%endif

%if 0%{?build_py3}
%files -n python3-%{name}
%dir %{python3_sitelib}/dnf-plugins
%dir %{python3_sitelib}/rhn
%dir %{python3_sitelib}/rhn/actions
%{python3_sitelib}/dnf-plugins/*
%{python3_sitelib}/rhn/actions/*
%endif

%changelog
