#
# spec file for package uyuni-cobbler-helper
#
# Copyright (c) 2025 SUSE LLC
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

%{!?python3_sitelib: %global python3_sitelib %(%{__python3} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")}

## The productprettyname macros is controlled in the prjconf. If not defined, we fallback here
%{!?productprettyname: %global productprettyname Uyuni}

# Keep in sync with salt/salt.spec
%if 0%{?suse_version} == 1500 && 0%{?sle_version} >= 150700
%global use_python python311
%else
%global use_python python3
%endif


Name:           uyuni-cobbler-helper
Version:        5.1.0
Release:        0
Summary:        Python helper functions for %{productprettyname} Cobbler snippets
License:        Apache-2.0
Group:          System/Management
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

Requires:       python3
Requires:       %{use_python}-psycopg2 >= 2.8.4
BuildRequires:  python3-rpm-macros

%description
This package provides utility functions to expose %{productprettyname} data to Cobbler snippets.

%prep
%autosetup

%build

%install
install -d %{buildroot}%{python3_sitelib}
install -m 644 uyuni_cobbler_helper.py %{buildroot}%{python3_sitelib}/uyuni_cobbler_helper.py

%files
%{python3_sitelib}/uyuni_cobbler_helper.py

%changelog
