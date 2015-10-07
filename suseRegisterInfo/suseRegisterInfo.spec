#
# spec file for package suseRegisterInfo
#
# Copyright (c) 2014 SUSE LINUX Products GmbH, Nuernberg, Germany.
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


Name:           suseRegisterInfo
Version:        3.0.1
Release:        1%{?dist}
Summary:        Tool to get informations from the local system
License:        GPL-2.0
Group:          Productivity/Other
Url:            http://www.suse.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#BuildArch:      noarch
BuildRequires:  python-devel

Requires:       perl
Requires:       python
%if  0%{?rhel} && 0%{?rhel} < 6
Requires:       e2fsprogs
%endif
%{!?python_sitelib: %global python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib()")}

%description
This tool read data from the local system required
for a registration

%prep
%setup -q

%build

%install
make -C suseRegister install PREFIX=$RPM_BUILD_ROOT
mkdir -p %{buildroot}/usr/lib/suseRegister/bin/
install -m 0755 suseRegister/parse_release_info %{buildroot}/usr/lib/suseRegister/bin/parse_release_info

%if 0%{?suse_version}
%py_compile %{buildroot}/
%py_compile -O %{buildroot}/
%endif

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%dir /usr/lib/suseRegister
%dir /usr/lib/suseRegister/bin
/usr/lib/suseRegister/bin/parse_release_info
%{python_sitelib}/suseRegister

%changelog
