#
# spec file for package uyuni-common-libs
#
# Copyright (c) 2024 SUSE LLC
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


%global debug_package %{nil}
%define __python /usr/bin/python2

%if 0%{?fedora} || 0%{?suse_version} >= 1500 || 0%{?rhel} >= 8
%{!?python3_sitelib: %global python3_sitelib %(%{__python3} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")}
%global python3root %{python3_sitelib}/uyuni
%global build_py3 1
%endif

%if ( 0%{?rhel} && 0%{?rhel} < 8 ) || 0%{?suse_version}
%global build_py2   1
%endif

%define pythonX %{?build_py3:python3}%{!?build_py3:python}

%if 0%{?suse_version} >= 1500
%global python_prefix python3
%else
%if  0%{?fedora} >= 28  || 0%{?rhel} >= 8
%global python_prefix python2
%else
%global python_prefix python
%endif
%endif

%{!?python2_sitelib: %global python2_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")}
%global python2root %{python2_sitelib}/uyuni

Name:           uyuni-common-libs
Summary:        Uyuni server and client libs
License:        GPL-2.0-only
Group:          Development/Languages/Python
Version:        5.0.4
Release:        0
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRequires:  fdupes
BuildRequires:  make
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

%description
Uyuni server and client libs

%if 0%{?build_py2}
%package -n python2-%{name}
Summary:        Uyuni server and client tools libraries for python2
Group:          Development/Languages/Python
Requires:       python
%if 0%{?suse_version}
BuildRequires:  python-devel
%else
BuildRequires:  python2-devel
%endif
%if 0%{?suse_version} || 0%{?rhel} >= 8
Recommends:     zchunk
Recommends:     zstd
%endif

%description -n python2-%{name}
Python 2 libraries required by both Uyuni server and client tools.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        Uyuni server and client tools libraries for python3
Group:          Development/Languages/Python
BuildRequires:  python3-devel
BuildRequires:  python3-rpm-macros
Conflicts:      %{name} < 1.7.0
%if 0%{?suse_version}
Requires:       python3-base
%else
Requires:       python3-libs
%endif
%if 0%{?suse_version} || 0%{?rhel} >= 8
Recommends:     zchunk
Recommends:     zstd
%endif

Obsoletes:      python3-spacewalk-backend-libs
Obsoletes:      python3-spacewalk-usix

%description -n python3-%{name}
Python 3 libraries required by both Uyuni server and client tools.
%endif

%prep
%setup -q

%build
make -f Makefile.common-libs all PYTHON_BIN=%{pythonX}

%install
make -f Makefile.common-libs install PREFIX=$RPM_BUILD_ROOT \
    MANDIR=%{_mandir} PYTHON_BIN=%{pythonX}

%if 0%{?build_py3}
install -d $RPM_BUILD_ROOT%{python2root}/common
cp $RPM_BUILD_ROOT%{python3root}/__init__.py \
    $RPM_BUILD_ROOT%{python2root}/
cp $RPM_BUILD_ROOT%{python3root}/common/*.py \
    $RPM_BUILD_ROOT%{python2root}/common
%endif

%if 0%{?suse_version}
%if 0%{?build_py2}
%py_compile -O %{buildroot}/%{python2root}
%fdupes %{buildroot}/%{python2root}
%endif
%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python3root}
%fdupes %{buildroot}/%{python3root}
%endif
%endif

%if !(0%{?build_py2})
rm -Rf $RPM_BUILD_ROOT%{python2root}
%endif

%if 0%{?build_py2}
%files -n python2-%{name}
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%{python2root}
%endif

%if 0%{?build_py3}
%files -n python3-%{name}
%defattr(-,root,root)
%license LICENSE
%{python3root}
%endif

%changelog
