#
# spec file for package uyuni-storage-setup
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


%global debug_package %{nil}
Name:           uyuni-storage-setup
Version:        5.1.0
Release:        0
Summary:        Tools for managing storage on Uyuni
License:        GPL-3.0-only
Group:          System/Management
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz

%description
Tools for managing storage on Uyuni Server and Proxy

%package server
Summary:        Storage setup scripts for Uyuni and SUSE Manager Server
Requires:       grep
Requires:       mgradm
Requires:       parted
Requires:       rsync
Requires:       util-linux
Requires:       xfsprogs
Conflicts:      %{name}-proxy

%description server
Scripts that help setting up Uyuni and SUSE Manager Server storage after deployment.

%package proxy
Summary:        Storage setup scripts for Uyuni and SUSE Manager Proxy
Requires:       grep
Requires:       mgrpxy
Requires:       parted
Requires:       rsync
Requires:       util-linux
Requires:       xfsprogs
Conflicts:      %{name}-server

%description proxy
Scripts that help setting up Uyuni and SUSE Manager Proxy storage after deployment.

%prep
%autosetup

%build

%install
install -m 0755 -vd %{buildroot}%{_bindir}
install -m 0755 -vd %{buildroot}%{_usr}/lib/susemanager/

install -m 755 scripts/mgr-storage-server %{buildroot}%{_bindir}/mgr-storage-server
install -m 755 scripts/mgr-storage-proxy %{buildroot}%{_bindir}/mgr-storage-proxy
install -m 755 scripts/susemanager-storage-setup-functions.sh %{buildroot}%{_usr}/lib/susemanager/

%files server
%defattr(-,root,root)
%doc README.md
%license LICENSE
%{_bindir}/mgr-storage-server
%{_usr}/lib/susemanager

%files proxy
%defattr(-,root,root)
%doc README.md
%license LICENSE
%{_bindir}/mgr-storage-proxy
%{_usr}/lib/susemanager

%changelog
