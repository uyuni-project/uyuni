#
# spec file for package rhnlib
#
# Copyright (c) 2024 SUSE LLC
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


%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
%global build_py3   1
%{!?__python3:%global __python3 /usr/bin/python3}

%if %{undefined python3_sitelib}
%global python3_sitelib %(%{__python3} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%endif
%endif

%if !(0%{?rhel} >= 8 || 0%{?sle_version} >= 150000 )
%global build_py2   1
%{!?__python2:%global __python2 /usr/bin/python2}
%if %{undefined python2_sitelib}
%global python2_sitelib %(%{__python2} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%endif
%endif

%if "%{_vendor}" == "debbuild"
# For making sure we can set the right args for deb distros
%global is_deb 1
%endif

Name:           rhnlib
Version:        5.1.0
Release:        0
Summary:        Python libraries for the Spacewalk project
License:        GPL-2.0-only
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
%if "%{_vendor}" == "debbuild"
Group:          python
Packager:       Uyuni Project <devel@lists.uyuni-project.org>
%else
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Development/Libraries
%endif
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif
BuildRequires:  make

%description
rhnlib is a collection of python modules used by the Spacewalk (http://spacewalk.redhat.com) software.

%if 0%{?build_py2}
%package -n python2-rhnlib
Summary:        Python libraries for the Spacewalk project
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Development/Libraries

%if "%{_vendor}" != "debbuild"
%if 0%{?fedora} >= 28 || 0%{?rhel} >= 8
BuildRequires:  python2-devel
Requires:       python2-pyOpenSSL
%else
BuildRequires:  python-devel
%if 0%{?suse_version}
%if 0%{?suse_version} > 1200
Requires:       python-pyOpenSSL
%else
Requires:       python-backports.ssl_match_hostname
Requires:       python-openssl
%endif # 0{?suse_version} > 1200
%else
Requires:       pyOpenSSL
%endif # 0{?suse_version}
%endif # 0{?fedora} >= 28 || 0{?rhel} >= 8
%endif # {_vendor} != "debbuild"

%if "%{_vendor}" == "debbuild"
BuildRequires:  python-dev
BuildRequires:  rpm
Requires(preun):python-minimal
Requires(post): python-minimal
Requires:       python-openssl
Obsoletes:      python-rhn
Conflicts:      python-rhn
%endif

Conflicts:      rhn-client-tools < 1.3.3
Conflicts:      rhn-custom-info < 5.4.7
Conflicts:      rhncfg < 5.10.45
Conflicts:      rhnclient < 0.10
Conflicts:      rhnpush < 5.5.10
Conflicts:      spacewalk-proxy < 1.3.6
Conflicts:      spacewalk-proxy-installer < 1.3.2

Provides:       rhnlib = %{version}-%{release}
Obsoletes:      rhnlib < %{version}-%{release}

%description -n python2-rhnlib
rhnlib is a collection of python modules used by the Spacewalk software.

%endif # 0%{?build_py2}

%if 0%{?build_py3}
%package -n python3-rhnlib
Summary:        Python libraries for the Spacewalk project
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          python

%if "%{_vendor}" != "debbuild"
BuildRequires:  python3-devel
%if 0%{?suse_version}
BuildRequires:  python-rpm-macros
%endif
%endif
Requires:       python3-pyOpenSSL

%if "%{_vendor}" == "debbuild"
BuildRequires:  python3-dev
BuildRequires:  rpm
Requires(preun): python3-minimal
Requires(post): python3-minimal
Requires:       python3-openssl
%endif

Conflicts:      rhn-client-tools < 1.3.3
Conflicts:      rhn-custom-info < 5.4.7
Conflicts:      rhncfg < 5.10.45
Conflicts:      rhnclient < 0.10
Conflicts:      rhnpush < 5.5.10
Conflicts:      spacewalk-proxy < 1.3.6
Conflicts:      spacewalk-proxy-installer < 1.3.2

%description -n python3-rhnlib
rhnlib is a collection of python modules used by the Spacewalk software.

%endif # 0%{?build_py2}

%prep
%setup -q

# Recreate the rhn module
mkdir rhn
pushd rhn
for pyfile in $(ls ../*.py)
do
  ln -s $pyfile
done
popd

if [ ! -e setup.py ]; then
    sed -e 's/@VERSION@/%{version}/' -e 's/@NAME@/%{name}/' setup.py.in > setup.py
fi
if [ ! -e setup.cfg ]; then
    sed 's/@RELEASE@/%{release}/' setup.cfg.in > setup.cfg
fi

%build
%if 0%{?build_py2}
make -f Makefile.rhnlib PYTHON=%{__python2}
%endif
%if 0%{?build_py3}
make -f Makefile.rhnlib PYTHON=%{__python3}
%endif

%install
%if 0%{?build_py2}
%{__python2} setup.py install %{!?is_deb:-O1}%{?is_deb:--no-compile -O0} --skip-build --root %{buildroot} %{?is_deb:--install-layout=deb} --prefix=%{_prefix}
%endif
%if 0%{?build_py3}
%{__python3} setup.py install %{!?is_deb:-O1}%{?is_deb:--no-compile -O0} --skip-build --root %{buildroot} %{?is_deb:--install-layout=deb} --prefix=%{_prefix}
%endif

%if 0%{?build_py2}
%files -n python2-rhnlib
%defattr(-,root,root)
%license COPYING
%doc ChangeLog README TODO
%{python2_sitelib}/*
%endif

%if 0%{?build_py3}
%files -n python3-rhnlib
%license COPYING
%doc ChangeLog README TODO
%{python3_sitelib}/*
%endif

%if "%{_vendor}" == "debbuild"

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
