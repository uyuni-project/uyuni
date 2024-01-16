#
# spec file for package dracut-mgrbootstrap
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


Name:           dracut-mgrbootstrap
Version:        5.0.1
Release:        1
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
Source:         dracut-mgrbootstrap-%{version}.tar.bz2
Summary:        PXE network boot dracut module
License:        GPL-2.0-only
Group:          System/Packages
BuildArch:      noarch
BuildRequires:  dracut
Requires:       dracut

%description
dracut module for booting PXE images.

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}/usr/lib/dracut/modules.d/50mgrbootstrap
cp -R mgrbootstrap/* %{buildroot}/usr/lib/dracut/modules.d/50mgrbootstrap
chmod 755 %{buildroot}/usr/lib/dracut/modules.d/50mgrbootstrap/*

%files
%defattr(-,root,root,-)
/usr/lib/dracut/modules.d/50mgrbootstrap

%changelog
