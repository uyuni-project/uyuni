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
Version:        5.0.2
Release:        1
Summary:        libmod app
License:        MIT
Group:          Applications/Internet
Source:         %{name}-%{version}.tar.gz
Requires(pre):  coreutils
Requires:       python3-libmodulemd
BuildRequires:  python3-pytest
BuildRequires:  python3-rpm-macros
%if 0%{?rhel}
BuildRequires:  python3-rpm-generators
%endif
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
URL:            https://github.com/uyuni-project/uyuni

%description
mgr-libmod

%prep
%setup -q

%build
%{__python3} setup.py build

%install
%{__python3} setup.py install --skip-build --root $RPM_BUILD_ROOT
mkdir -p %{buildroot}/usr/bin
cp -R scripts/* %{buildroot}/usr/bin

%files
%defattr(-,root,root)
%{python3_sitelib}/*
/usr/bin/mgr-libmod
%license LICENSE

%changelog
