#
# spec file for package rhnlib
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
%endif

%{!?python_sitelib: %define python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib()")}

Summary:        Python libraries for the Spacewalk project
License:        GPL-2.0-only
Group:          Development/Libraries
Name:           rhnlib
Version:        4.0.1
Release:        1%{?dist}
URL:            https://github.com/spacewalkproject/spacewalk
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz

BuildRoot:      %{_tmppath}/%{name}-%{version}-build

%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif

%description
rhnlib is a collection of python modules used by the Spacewalk (http://spacewalk.redhat.com) software.

%package -n python2-rhnlib
Summary:        Python libraries for the Spacewalk project
Group:          Development/Libraries
%if 0%{?fedora} >= 28 || 0%{?rhel} >= 8
BuildRequires:  python2-devel
Requires:       python2-pyOpenSSL
%else
BuildRequires:  python-devel
%if 0%{?suse_version}
%if 0%{?suse_version} > 1200
Requires:       python-pyOpenSSL
%else
Requires:       python-openssl
%endif
%else
Requires:       pyOpenSSL
%endif
%endif
Conflicts:      rhncfg < 5.10.45
Conflicts:      spacewalk-proxy-installer < 1.3.2
Conflicts:      rhn-client-tools < 1.3.3
Conflicts:      rhn-custom-info < 5.4.7
Conflicts:      rhnpush < 5.5.10
Conflicts:      rhnclient < 0.10
Conflicts:      spacewalk-proxy < 1.3.6

Provides:       rhnlib = %{version}-%{release}
Obsoletes:      rhnlib < %{version}-%{release}

%description -n python2-rhnlib
rhnlib is a collection of python modules used by the Spacewalk software.


%if 0%{?build_py3}
%package -n python3-rhnlib
Summary:        Python libraries for the Spacewalk project
Group:          Development/Libraries
BuildRequires:  python3-devel
%if 0%{?suse_version}
BuildRequires:  python-rpm-macros
%endif
Requires:       python3-pyOpenSSL
Conflicts:      rhncfg < 5.10.45
Conflicts:      spacewalk-proxy-installer < 1.3.2
Conflicts:      rhn-client-tools < 1.3.3
Conflicts:      rhn-custom-info < 5.4.7
Conflicts:      rhnpush < 5.5.10
Conflicts:      rhnclient < 0.10
Conflicts:      spacewalk-proxy < 1.3.6

%description -n python3-rhnlib
rhnlib is a collection of python modules used by the Spacewalk software.
%endif

%prep
%setup -q
if [ ! -e setup.py ]; then
    sed -e 's/@VERSION@/%{version}/' -e 's/@NAME@/%{name}/' setup.py.in > setup.py
fi
if [ ! -e setup.cfg ]; then
    sed 's/@RELEASE@/%{release}/' setup.cfg.in > setup.cfg
fi

%build
#%{__python} setup.py build
make -f Makefile.rhnlib

%install
%{__python} setup.py install -O1 --skip-build --root $RPM_BUILD_ROOT --prefix=%{_prefix}
%if 0%{?build_py3}
%{__python3} setup.py install -O1 --skip-build --root $RPM_BUILD_ROOT --prefix=%{_prefix}
%endif

%files -n python2-rhnlib
%defattr(-,root,root)
%doc ChangeLog COPYING README TODO
%{python_sitelib}/*

%if 0%{?build_py3}
%files -n python3-rhnlib
%doc ChangeLog COPYING README TODO
%{python3_sitelib}/*
%endif

%changelog
