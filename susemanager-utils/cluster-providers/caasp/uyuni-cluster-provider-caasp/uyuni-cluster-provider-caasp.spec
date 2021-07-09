#
# spec file for package uyuni-cluster-provider-caasp
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


%if 0%{?sle_version} >= 15000 || 0%{?rhel}
# SLE15 builds on Python 3
%global build_py3   1
%endif

%define fname caasp
%define fdir %{_datadir}/susemanager/cluster-providers
Name:           uyuni-cluster-provider-caasp
Version:        4.3.0
Release:        0
Summary:        SUSE CaaS Platform cluster provider for SUSE Manager
License:        GPL-2.0-only
Group:          Applications/System
Source:         %{name}-%{version}.tar.gz
Requires(pre):  coreutils
Requires:       caasp-management-node-formula
Requires:       caasp-management-settings-formula
Requires:       susemanager
Requires:       susemanager-build-keys-web >= 12.0.1
%if 0%{?build_py3}
BuildRequires:  python3-mock
BuildRequires:  python3-pytest
BuildRequires:  python3-salt
%else
BuildRequires:  python-mock
BuildRequires:  python-pytest
BuildRequires:  python-salt
%endif
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%description
Salt files and metadata for intergrting with SUSE CaaS Platform.

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}%{fdir}/states/%{fname}
mkdir -p %{buildroot}%{fdir}/metadata/%{fname}
cp -r caasp/* %{buildroot}%{fdir}/states/%{fname}
cp -r metadata/* %{buildroot}%{fdir}/metadata/%{fname}

%files
%defattr(-,root,root)
%dir %{_datadir}/susemanager
%dir %{fdir}
%dir %{fdir}/states
%dir %{fdir}/metadata
%{fdir}/states/%{fname}
%{fdir}/metadata/%{fname}

%changelog
