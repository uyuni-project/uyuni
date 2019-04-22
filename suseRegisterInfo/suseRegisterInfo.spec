#
# spec file for package suseRegisterInfo
#
# Copyright (c) 2018 SUSE LINUX GmbH, Nuernberg, Germany.
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


%{!?python_sitelib: %global python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib()")}
%if 0%{?fedora} || 0%{?suse_version} > 1320
%global build_py3   1
%global default_py3 1
%endif

%define pythonX %{?default_py3:python3}%{!?default_py3:python2}

Name:           suseRegisterInfo
Version:        4.0.3
Release:        1%{?dist}
Summary:        Tool to get informations from the local system
License:        GPL-2.0-only
Group:          Productivity/Other
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif
Requires:       %{pythonX}-%{name} = %{version}-%{release}
Requires:       perl
%if  0%{?rhel} && 0%{?rhel} < 6
Requires:       e2fsprogs
%endif

%description
This tool read data from the local system required
for a registration

%package -n python2-%{name}
Summary:        Python 2 specific files for %{name}
Group:          Productivity/Other
Requires:       %{name} = %{version}-%{release}
Requires:       python
BuildRequires:  python-devel

%description -n python2-%{name}
Python 2 specific files for %{name}.

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        Python 3 specific files for %{name}
Group:          Productivity/Other
Requires:       %{name} = %{version}-%{release}
Requires:       python3
BuildRequires:  python3-devel

%description -n python3-%{name}
Python 2 specific files for %{name}.

%endif

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}/usr/lib/suseRegister/bin/
install -m 0755 suseRegister/parse_release_info %{buildroot}/usr/lib/suseRegister/bin/parse_release_info
make -C suseRegister install PREFIX=$RPM_BUILD_ROOT PYTHONPATH=%{python_sitelib} PYTHON_BIN=%{pythonX}

%if 0%{?build_py3}
make -C suseRegister install PREFIX=$RPM_BUILD_ROOT PYTHONPATH=%{python3_sitelib} PYTHON_BIN=%{pythonX}
%endif

%if 0%{?suse_version}
%py_compile -O %{buildroot}/%{python_sitelib}
%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python3_sitelib}
%endif
%endif

%files
%defattr(-,root,root,-)
%dir /usr/lib/suseRegister
%dir /usr/lib/suseRegister/bin
/usr/lib/suseRegister/bin/parse_release_info

%files -n python2-%{name}
%defattr(-,root,root)
%{python_sitelib}/suseRegister

%if 0%{?build_py3}
%files -n python3-%{name}
%defattr(-,root,root)
%{python3_sitelib}/suseRegister

%endif

%changelog
