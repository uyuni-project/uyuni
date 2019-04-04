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

%{!?__python2:%global __python2 /usr/bin/python2}
%{!?__python3:%global __python3 /usr/bin/python3}

%if %{undefined python2_sitelib}
%global python2_sitelib %(%{__python2} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%endif

%if %{undefined python3_sitelib}
%global python3_sitelib %(%{__python3} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%endif

%if %{_vendor} == "debbuild"
# For making sure we can set the right args for deb distros
%global is_deb 1
%endif


Summary:        Python libraries for the Spacewalk project
License:        GPL-2.0-only
Name:           rhnlib
Version:        4.0.6
Release:        1%{?dist}
%if %{_vendor} == "debbuild"
Group:      python
Packager:   Uyuni Project <uyuni-devel@opensuse.org>
%else
Group:      Development/Libraries
%endif
URL:            https://github.com/uyuni-project/uyuni
Source0: %{name}-%{version}.tar.gz

BuildRoot:      %{_tmppath}/%{name}-%{version}-build

%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif

%description
rhnlib is a collection of python modules used by the Spacewalk (http://spacewalk.redhat.com) software.

%package -n python2-rhnlib
Summary:        Python libraries for the Spacewalk project
Group:          Development/Libraries

%if %{_vendor} != "debbuild"
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
%endif # 0%{?suse_version} > 1200
%else
Requires:       pyOpenSSL
%endif # 0%{?suse_version}
%endif # 0%{?fedora} >= 28 || 0%{?rhel} >= 8
%endif # %{_vendor} != "debbuild"

%if %{_vendor} == "debbuild"
BuildRequires: python-dev
BuildRequires: rpm
Requires(preun): python-minimal
Requires(post): python-minimal
Requires: python-openssl
Obsoletes: python-rhn
Conflicts: python-rhn
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

%if %{_vendor} != "debbuild"
BuildRequires:  python3-devel
%if 0%{?suse_version}
BuildRequires:  python-rpm-macros
%endif
%endif
Requires:       python3-pyOpenSSL

%if %{_vendor} == "debbuild"
BuildRequires: python3-dev
BuildRequires: rpm
Requires(preun): python3-minimal
Requires(post): python3-minimal
Requires: python3-openssl
%endif

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
make -f Makefile.rhnlib PYTHON=%{__python2}
%if 0%{?build_py3}
make -f Makefile.rhnlib PYTHON=%{__python3}
%endif


%install
%{__python2} setup.py install %{!?is_deb:-O1}%{?is_deb:--no-compile -O0} --skip-build --root $RPM_BUILD_ROOT %{?is_deb:--install-layout=deb} --prefix=%{_prefix}
%if 0%{?build_py3}
%{__python3} setup.py install %{!?is_deb:-O1}%{?is_deb:--no-compile -O0} --skip-build --root $RPM_BUILD_ROOT %{?is_deb:--install-layout=deb} --prefix=%{_prefix}
%endif

%files -n python2-rhnlib
%defattr(-,root,root)
%doc ChangeLog COPYING README TODO
%{python2_sitelib}/*

%if 0%{?build_py3}
%files -n python3-rhnlib
%doc ChangeLog COPYING README TODO
%{python3_sitelib}/*
%endif

%if %{_vendor} == "debbuild"

%post -n python2-rhnlib
# Do late-stage bytecompilation, per debian policy
pycompile -p python2-rhnlib -V -3.0

%preun -n python2-rhnlib
# Ensure all *.py[co] files are deleted, per debian policy
pyclean -p python2-rhnlib

%if 0%{?build_py3}
%post -n python3-rhnlib
# Do late-stage bytecompilation, per debian policy
py3compile -p python3-rhnlib -V -4.0

%preun -n python3-rhnlib
# Ensure all *.py[co] files are deleted, per debian policy
py3clean -p python3-rhnlib
%endif
%endif

%changelog
