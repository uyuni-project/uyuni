#
# spec file for package mgr-libmod
#
# Copyright (c) 2020 SUSE LINUX GmbH, Nuernberg, Germany.
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
Version:        0.2
Release:        1
Summary:        libmod app
License:        MIT
Group:          Applications/Internet
Source:         %{name}-%{version}.tar.gz
Requires(pre):  coreutils
BuildRequires:  python3-pytest
BuildRequires:  python3-mock
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%description
mgr-libmod

%prep
%setup -q

%build
%{__python} setup.py build

%install
%{__python} setup.py install --skip-build --root $RPM_BUILD_ROOT

%check

%post

%files
%defattr(-,root,root)

%changelog
