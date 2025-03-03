#
# spec file for package mgr-health-check
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

%{?sle15_python_module_pythons}
Name:           mgr-health-check
Version:        0.1
Release:        0
Summary:        Show Uyuni server health metrics and logs
License:        Apache-2.0 
URL:            https://github.com/uyuni-project/uyuni
Source:         %{name}-%{version}.tar.gz
Group:          Applications/Internet
BuildRequires:  %{python_module pip}
BuildRequires:  %{python_module setuptools}
BuildRequires:  %{python_module wheel}
BuildRequires:  fdupes 
BuildRequires:  python-rpm-macros
Requires(post):   update-alternatives
Requires(postun):  update-alternatives
Requires:       podman
Requires:       python-Jinja2
Requires:       python-PyYAML
Requires:       python-click
Requires:       python-requests
Requires:       python-rich
Requires:       python-tomli
BuildArch:      noarch
Provides:       mgr-health-check

%python_subpackages

%description
Show Uyuni server health metrics and logs

%prep
%autosetup -p1

%build
%pyproject_wheel

%install
%pyproject_install
%python_expand %fdupes %{buildroot}/%{$python_sitelib}/health_check
%python_clone -a %{buildroot}/%{_bindir}/mgr-health-check

%post
%python_install_alternative mgr-health-check

%postun
%python_uninstall_alternative mgr-health-check

%files %{python_files}
%doc README.md
%{python_sitelib}/health_check
%{python_sitelib}/health_check-%{version}*-info
%python_alternative %{_bindir}/mgr-health-check

%changelog

