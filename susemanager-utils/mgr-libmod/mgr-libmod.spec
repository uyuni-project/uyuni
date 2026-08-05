#
# spec file for package mgr-libmod
#
# Copyright (c) 2026 SUSE LLC
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

%{?!python_module:%define python_module() python-%{**} python3-%{**}}

Name:           mgr-libmod
Version:        5.3.1
Release:        0
Summary:        Modular dependency resolver for content lifecycle management
License:        MIT
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
URL:            https://github.com/uyuni-project/uyuni
#!CreateArchive: %{name}
Source:         %{name}-%{version}.tar.gz
BuildRequires:  %{python_module pytest}
BuildRequires:  fdupes
BuildRequires:  python-rpm-macros
Requires:       python3-libmodulemd
Requires(pre):  coreutils
BuildArch:      noarch
%if 0%{?rhel}
BuildRequires:  %{python_module rpm-generators}
%endif

%description
mgr-libmod

%prep
%setup -q

%build
%python_build

%install
%python_install
%python_expand %fdupes %{buildroot}%{$python_sitelib}
mkdir -p %{buildroot}%{_bindir}
cp -R scripts/* %{buildroot}%{_bindir}

%files
%{python_sitelib}/mgrlibmod
%{python_sitelib}/mgrlibmod-%{version}*-info
%{_bindir}/mgr-libmod
%license LICENSE

%changelog
