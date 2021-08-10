#
# spec file for package caasp-management-node-formula
#
# Copyright (c) 2021 SUSE LLC
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


%define fname caasp-management-node
%define fdir  %{_datadir}/susemanager/formulas
Name:           caasp-management-node-formula
Version:        4.3.1
Release:        1
Summary:        Salt formula for configuring a CaaSP management node
License:        GPL-2.0-only
Group:          Applications/System
Source:         %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%description
Salt formula for configuring a CaaS Platform management node.

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}%{fdir}/states/%{fname}
mkdir -p %{buildroot}%{fdir}/metadata/%{fname}
cp -R caasp-management-node/* %{buildroot}%{fdir}/states/%{fname}
cp -R metadata/* %{buildroot}%{fdir}/metadata/%{fname}

%files
%defattr(-,root,root)
%dir %{_datadir}/susemanager
%dir %{fdir}
%dir %{fdir}/states
%dir %{fdir}/metadata
%{fdir}/states/%{fname}
%{fdir}/metadata/%{fname}

%changelog
