#
# spec file for package mgr-libmod
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


Name:           mgr-libmod
Version:        5.1.0
Release:        0
Summary:        Modular dependency resolver for content lifecycle management
License:        MIT
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
URL:            https://github.com/uyuni-project/uyuni
Source:         %{name}-%{version}.tar.gz
BuildRequires:  python3-pytest
BuildRequires:  python3-rpm-macros
Requires:       python3-libmodulemd
Requires(pre):  coreutils
BuildArch:      noarch
%if 0%{?rhel}
BuildRequires:  python3-rpm-generators
%endif

%description
mgr-libmod

%prep
%setup -q

%build
%{__python3} setup.py build

%install
%{__python3} setup.py install --skip-build --root %{buildroot}
mkdir -p %{buildroot}%{_bindir}
cp -R scripts/* %{buildroot}%{_bindir}

%files
%defattr(-,root,root)
%{python3_sitelib}/*
%{_bindir}/mgr-libmod
%license LICENSE

%changelog
