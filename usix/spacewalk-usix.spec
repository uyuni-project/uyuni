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

# ------------------------------- Python macros (mostly for debian) -------------------------------
%{!?__python2:%global __python2 /usr/bin/python2}
%{!?__python3:%global __python3 /usr/bin/python3}

%if %{undefined python2_version}
%global python2_version %(%{__python2} -Esc "import sys; sys.stdout.write('{0.major}.{0.minor}'.format(sys.version_info))")
%endif

%if %{undefined python3_version}
%global python3_version %(%{__python3} -Ic "import sys; sys.stdout.write(sys.version[:3])")
%endif

%if %{undefined python2_sitelib}
%global python2_sitelib %(%{__python2} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%endif

%if %{undefined python3_sitelib}
%global python3_sitelib %(%{__python3} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%endif
# --------------------------- End Python macros ---------------------------------------------------

%if %{_vendor} == "debbuild"
# Bash constructs in scriptlets don't play nice with Debian's default shell, dash
%global _buildshell /bin/bash
%endif

%if 0%{?build_py3}
%global python3rhnroot %{python3_sitelib}/spacewalk
%endif

%global pythonrhnroot %{python2_sitelib}/spacewalk

Name:           spacewalk-usix
Version:        4.0.4
Release:        1%{?dist}
Summary:        Spacewalk server and client nano six library
%if %{_vendor} == "debbuild"
Group:      admin
Packager:   Uyuni Project <uyuni-devel@opensuse.org>
%else
Group:          Applications/Internet
%endif
License:        GPL-2.0-only
URL:            https://github.com/uyuni-project/uyuni
Source0:   %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210 || 0%{?debian} || 0%{?ubuntu}
BuildArch:      noarch
%endif

Provides:       python2-spacewalk-usix = %{version}-%{release}
Provides:       spacewalk-backend-usix = %{version}-%{release}
Obsoletes:      spacewalk-backend-usix < 2.8
%if %{_vendor} == "debbuild"
BuildRequires: python-dev
Requires(preun): python-minimal
Requires(post): python-minimal
%else
BuildRequires:  python-devel
%endif

%description
Library for writing code that runs on Python 2 and 3

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        Spacewalk client micro six library
Group:          Applications/Internet
Provides:       python3-spacewalk-backend-usix = %{version}-%{release}
Obsoletes:      python3-spacewalk-backend-usix < 2.8
%if %{_vendor} == "debbuild"
BuildRequires: python3-dev
Requires(preun): python3-minimal
Requires(post): python3-minimal
%else
BuildRequires:  python3-devel
%endif

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
# These macros don't work on debbuild, but it doesn't matter because we don't do bytecompilation
# until after install anyway.
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
%if %{_vendor} != "debbuild"
%exclude %{python3rhnroot}/__pycache__/*
%exclude %{python3rhnroot}/common/__pycache__/__init__.*
%endif
%endif

%if %{_vendor} == "debbuild"
# Debian requires:
# post: Do bytecompilation after install
# preun: Remove any *.py[co] files

%post -n python2-%{name}
pycompile -p python2-%{name} -V -3.0

%preun -n python2-%{name}
pyclean -p python2-%{name}

%if 0%{?build_py3}
%post -n python3-%{name}
py3compile -p python3-%{name} -V -4.0

%preun -n python3-%{name}
py3clean -p python3-%{name}
%endif
%endif

%changelog
