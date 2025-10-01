#
# spec file for package uyuni-cobbler-helper
#
# Copyright (c) 2025 SUSE LLC and contributors
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

## The productprettyname macros is controlled in the prjconf. If not defined, we fallback here
%{!?productprettyname: %global productprettyname Uyuni}

%{?!python_module:%define python_module() python-%{**} python3-%{**}}
Name:           uyuni-cobbler-helper
Version:        5.2.0
Release:        0
Summary:        Python helper functions for %{productprettyname} Cobbler snippets
License:        Apache-2.0
Group:          System/Management
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
BuildRequires:  python-rpm-macros
BuildRequires:  %{python_module base}
Requires:       python
Requires:       python-psycopg2 >= 2.8.4
Provides:       uyuni-cobbler-helper
%python_subpackages

%description
This package provides utility functions to expose %{productprettyname} data to Cobbler snippets.

%prep
%autosetup

%build

%install
%{python_expand # expanded-body:
  install -d %{buildroot}%{$python_sitelib}
  install -m 644 uyuni_cobbler_helper.py %{buildroot}%{$python_sitelib}/uyuni_cobbler_helper.py
}


%files %{python_files}
%{python_sitelib}/uyuni_cobbler_helper.py

%changelog
