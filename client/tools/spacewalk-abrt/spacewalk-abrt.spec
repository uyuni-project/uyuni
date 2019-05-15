#
# spec file for package spacewalk-abrt
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


%if 0%{?fedora} || 0%{?rhel} >= 8
%global build_py3   1
%global default_py3 1
%endif

%if ( 0%{?fedora} && 0%{?fedora} < 28 ) || ( 0%{?rhel} && 0%{?rhel} < 8 ) || 0%{?suse_version}
%global build_py2   1
%endif

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

Name:           spacewalk-abrt
Version:        4.0.4
Release:        1%{?dist}
Summary:        ABRT plug-in for rhn-check
License:        GPL-2.0-only
Group:          Applications/System

Url:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
Source1:        %{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
BuildRequires:  gettext
BuildRequires:  python-devel
Requires:       %{pythonX}-%{name} = %{version}-%{release}
Requires:       abrt
Requires:       abrt-cli

%description
spacewalk-abrt - rhn-check plug-in for collecting information about crashes handled by ABRT.

%if 0%{?build_py2}
%package -n python2-%{name}
Summary:        ABRT plug-in for rhn-check
Group:          Applications/System
BuildRequires:  python
Requires:       python2-rhn-check
Requires:       python2-rhn-client-tools

%description -n python2-%{name}
Python 2 specific files for %{name}.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        ABRT plug-in for rhn-check
Group:          Applications/System
BuildRequires:  python3-rpm-macros
Requires:       python3-rhn-check
Requires:       python3-rhn-client-tools

%description -n python3-%{name}
Python 3 specific files for %{name}.
%endif

%prep
%setup -q

%build
make -f Makefile.spacewalk-abrt

%install
%if 0%{?build_py2}
make -f Makefile.spacewalk-abrt install PREFIX=$RPM_BUILD_ROOT \
                PYTHON_PATH=%{python_sitelib} PYTHON_VERSION=%{python_version}
%endif
%if 0%{?build_py3}
sed -i 's|#!/usr/bin/python|#!/usr/bin/python3|' src/bin/spacewalk-abrt
make -f Makefile.spacewalk-abrt install PREFIX=$RPM_BUILD_ROOT \
                PYTHON_PATH=%{python3_sitelib} PYTHON_VERSION=%{python3_version}
%endif

%define default_suffix %{?default_py3:-%{python3_version}}%{!?default_py3:-%{python_version}}
ln -s spacewalk-abrt%{default_suffix} $RPM_BUILD_ROOT%{_bindir}/spacewalk-abrt

%find_lang %{name}

%post
service abrtd restart ||:

%files -f %{name}.lang
%defattr(-,root,root)
%dir /etc/sysconfig/rhn/
%dir /etc/sysconfig/rhn/clientCaps.d
%dir /etc/libreport
%dir /etc/libreport/events.d
%config  /etc/sysconfig/rhn/clientCaps.d/abrt
%config  /etc/libreport/events.d/spacewalk.conf
%{_bindir}/spacewalk-abrt
%{_mandir}/man8/*

%if 0%{?build_py2}
%files -n python2-%{name}
%{_bindir}/spacewalk-abrt-%{python_version}
%{python_sitelib}/spacewalk_abrt/
%endif

%if 0%{?build_py3}
%files -n python3-%{name}
%{_bindir}/spacewalk-abrt-%{python3_version}
%{python3_sitelib}/spacewalk_abrt/
%endif

%changelog
