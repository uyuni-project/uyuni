#
# spec file for package uyuni-cobbler-helper
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

%{!?python3_sitelib: %global python3_sitelib %(%{__python3} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")}


Name:           uyuni-cobbler-helper
Version:        0.1.0
Release:        0
Summary:        Python helper functions for Uyuni cobbler snippets
License:        Apache-2.0
Group:          System/Management
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if !0%{?suse_version} || 0%{?suse_version} >= 1120
BuildArch:      noarch
%endif

Requires:       python3
Requires:       python3-psycopg2 >= 2.8.4
BuildRequires:  python3-rpm
BuildRequires:  python3-rpm-macros

%description
This package provides utility functions to expose Uyuni data to cobbler snippets.

%prep
%autosetup

%install
install -m 644 uyuni_cobbler_helper.py %{python3_sitelib}/uyuni_cobbler_helper.py

%files
%{python3_sitelib}/uyuni_cobbler_helper.py

%changelog
