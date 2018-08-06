#
# spec file for package spacewalk-usix
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

%if 0%{?rhel} && 0%{?rhel} < 6
%{!?python_sitelib: %global python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib()")}
%endif

%if 0%{?build_py3}
%{!?python3_sitelib: %global python3_sitelib %(%{__python3} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")}
%global python3rhnroot %{python3_sitelib}/spacewalk
%endif

%global pythonrhnroot %{python_sitelib}/spacewalk

Name:           spacewalk-usix
Version:        2.8.3.1
Release:        1%{?dist}
Summary:        Spacewalk server and client nano six library
License:        GPL-2.0-only
Group:          Applications/Internet

URL:            https://github.com/spacewalkproject/spacewalk
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif

Provides:       python2-spacewalk-usix = %{version}-%{release}
Provides:       spacewalk-backend-usix = %{version}-%{release}
Obsoletes:      spacewalk-backend-usix < 2.8
BuildRequires:  python-devel

%description
Library for writing code that runs on Python 2 and 3

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        Spacewalk client micro six library
Group:          Applications/Internet
Provides:       python3-spacewalk-backend-usix = %{version}-%{release}
Obsoletes:      python3-spacewalk-backend-usix < 2.8
BuildRequires:  python3-devel

%description -n python3-%{name}
Library for writing code that runs on Python 2 and 3

%endif

%prep
%setup -q

%build
%define debug_package %{nil}

%install
install -m 0755 -d $RPM_BUILD_ROOT%{pythonrhnroot}/common
install -m 0644 __init__.py $RPM_BUILD_ROOT%{pythonrhnroot}/__init__.py
install -m 0644 common/__init__.py $RPM_BUILD_ROOT%{pythonrhnroot}/common/__init__.py
install -m 0644 common/usix.py* $RPM_BUILD_ROOT%{pythonrhnroot}/common/usix.py

%if 0%{?build_py3}
install -d $RPM_BUILD_ROOT%{python3rhnroot}/common
cp $RPM_BUILD_ROOT%{pythonrhnroot}/__init__.py $RPM_BUILD_ROOT%{python3rhnroot}
cp $RPM_BUILD_ROOT%{pythonrhnroot}/common/__init__.py $RPM_BUILD_ROOT%{python3rhnroot}/common
cp $RPM_BUILD_ROOT%{pythonrhnroot}/common/usix.py $RPM_BUILD_ROOT%{python3rhnroot}/common
%endif

%if 0%{?suse_version} > 1140
%py_compile -O %{buildroot}/%{pythonrhnroot}
%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python3rhnroot}
%endif
%endif

%files
%defattr(-,root,root)
%dir %{pythonrhnroot}
%dir %{pythonrhnroot}/common
%{pythonrhnroot}/__init__.py
%{pythonrhnroot}/common/__init__.py
%{pythonrhnroot}/common/usix.py*
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1200
%exclude %{pythonrhnroot}/__init__.pyc
%exclude %{pythonrhnroot}/__init__.pyo
%exclude %{pythonrhnroot}/common/__init__.pyc
%exclude %{pythonrhnroot}/common/__init__.pyo
%endif

%if 0%{?build_py3}

%files -n python3-%{name}
%dir %{python3rhnroot}
%dir %{python3rhnroot}/common
%dir %{python3rhnroot}/common/__pycache__
%{python3rhnroot}/__init__.py
%{python3rhnroot}/common/__init__.py
%{python3rhnroot}/common/usix.py*
%{python3rhnroot}/common/__pycache__/*
%exclude %{python3rhnroot}/__pycache__/*
%exclude %{python3rhnroot}/common/__pycache__/__init__.*
%endif

%changelog
