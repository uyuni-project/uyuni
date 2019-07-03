#
# spec file for package spacewalk-client-cert
#
# Copyright (c) 2019 SUSE LINUX GmbH, Nuernberg, Germany.
# Copyright (c) 2008-2018 Red Hat, Inc.
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


%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
%global build_py3   1
%endif

Name:           spacewalk-client-cert
Version:        4.0.4
Release:        1%{?dist}
Summary:        Package allowing manipulation with Spacewalk client certificates
License:        GPL-2.0-only
Group:          Applications/System

Url:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
Source1:        %{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
%if 0%{?build_py3}
BuildRequires:  python3-devel
Requires:       python3-rhn-client-tools
Requires:       python3-rhn-setup
%else
BuildRequires:  python-devel
Requires:       python2-rhn-client-tools
Requires:       python2-rhn-setup
%endif
%description
spacewalk-client-cert contains client side functionality allowing manipulation
with Spacewalk client certificates (/etc/sysconfig/rhn/systemid)

%prep
%setup -q

%build
make -f Makefile.spacewalk-client-cert

%install
%global pypath %{?build_py3:%{python3_sitelib}}%{!?build_py3:%{python_sitelib}}
make -f Makefile.spacewalk-client-cert install PREFIX=$RPM_BUILD_ROOT \
        PYTHONPATH=%{pypath}

%files
%dir /etc/sysconfig/rhn
%dir /etc/sysconfig/rhn/clientCaps.d
%config  /etc/sysconfig/rhn/clientCaps.d/client-cert
%{pypath}/rhn/actions/*

%if 0%{?suse_version}
%dir /etc/sysconfig/rhn
%dir /etc/sysconfig/rhn/clientCaps.d
%dir %{pypath}/rhn
%dir %{pypath}/rhn/actions
%endif

%changelog
