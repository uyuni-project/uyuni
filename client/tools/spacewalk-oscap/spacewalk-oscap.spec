#
# spec file for package spacewalk-oscap
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


%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
%global build_py3   1
%global default_py3 1
%endif

%if ( 0%{?fedora} && 0%{?fedora} < 28 ) || ( 0%{?rhel} && 0%{?rhel} < 8 ) || 0%{?suse_version}
%global build_py2   1
%endif

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

Name:           spacewalk-oscap
Version:        4.0.3
Release:        1%{?dist}
Summary:        OpenSCAP plug-in for rhn-check
License:        GPL-2.0-only
Group:          Applications/System

URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
Source1:        %{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif
BuildRequires:  libxslt
%if ( 0%{?rhel} && 0%{?rhel} < 8 ) || 0%{?suse_version}
Requires:       openscap-utils
%else
Requires:       openscap-scanner
%endif
Requires:       %{pythonX}-%{name} = %{version}-%{release}
Requires:       libxslt

%description
spacewalk-oscap is a plug-in for rhn-check. With this plugin, user is able
to run OpenSCAP scan from Spacewalk or Red Hat Satellite server.

%if 0%{?build_py2}
%package -n python2-%{name}
Summary:        OpenSCAP plug-in for rhn-check
Group:          Applications/System
Provides:       python-%{name} = %{version}-%{release}
Obsoletes:      python-%{name} < %{version}-%{release}
Requires:       %{name} = %{version}-%{release}
Requires:       python2-rhn-check >= 2.8.4
Requires:       rhnlib >= 2.8.3
BuildRequires:  python-devel
BuildRequires:  rhnlib >= 2.8.3

%description -n python2-%{name}
Python 2 specific files for %{name}.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        OpenSCAP plug-in for rhn-check
Group:          Applications/System
Requires:       %{name} = %{version}-%{release}
Requires:       python3-rhn-check >= 2.8.4
Requires:       python3-rhnlib >= 2.8.3
BuildRequires:  python3-devel
BuildRequires:  python3-rhnlib >= 2.8.3

%description -n python3-%{name}
Python 3 specific files for %{name}.
%endif

%prep
%setup -q

%build
make -f Makefile.spacewalk-oscap

%install
%if 0%{?build_py2}
make -f Makefile.spacewalk-oscap install PREFIX=$RPM_BUILD_ROOT PYTHONPATH=%{python_sitelib}
%endif
%if 0%{?build_py3}
make -f Makefile.spacewalk-oscap install PREFIX=$RPM_BUILD_ROOT PYTHONPATH=%{python3_sitelib}
%endif

%if 0%{?suse_version}
%if 0%{?build_py2}
%py_compile -O %{buildroot}/%{python_sitelib}
%endif
%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python3_sitelib}
%endif
%endif

%files
%defattr(-,root,root)
%doc COPYING
%config  /etc/sysconfig/rhn/clientCaps.d/scap
%{_datadir}/openscap/xsl/xccdf-resume.xslt
%if 0%{?suse_version}
%dir /etc/sysconfig/rhn
%dir /etc/sysconfig/rhn/clientCaps.d
%dir %{_datadir}/openscap
%dir %{_datadir}/openscap/xsl
%endif

%if 0%{?build_py2}
%files -n python2-%{name}
%defattr(-,root,root)
%dir %{python_sitelib}/rhn/actions
%{python_sitelib}/rhn/actions/scap.*
%if 0%{?suse_version}
%dir %{python_sitelib}/rhn/actions
%endif
%endif

%if 0%{?build_py3}
%files -n python3-%{name}
%defattr(-,root,root)
%{python3_sitelib}/rhn/actions/scap.*
%{python3_sitelib}/rhn/actions/__pycache__/scap.*
%if 0%{?suse_version}
%dir %{python3_sitelib}/rhn/actions
%dir %{python3_sitelib}/rhn/actions/__pycache__
%endif
%endif

%changelog
