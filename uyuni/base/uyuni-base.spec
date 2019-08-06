#
# spec file for package uyuni-base
#
# Copyright (c) 2019 SUSE LINUX GmbH, Nuernberg, Germany.
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

#Compat macro for new _fillupdir macro introduced in Nov 2017
%if ! %{defined _fillupdir}
  %define _fillupdir /var/adm/fillup-templates
%endif

Name:           uyuni-base
Version:        1.0.0
Release:        0
Url:            https://github.com/uyuni-project/uyuni
Summary:        Uyuni Base Package
License:        GPL-2.0-only
Group:          System/Fhs
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

%description
Uyuni is a systems management application that will
inventory, provision, update and control your Linux machines.

%package common
Summary:        Base structure for Uyuni server and proxy
Group:          System/Fhs

%description common
Basic filesystem hierarchy for Uyuni server and proxy.

%package server
Summary:        Base structure for Uyuni server
Group:          System/Fhs
BuildRequires:  uyuni-base-common
Requires(pre):  uyuni-base-common

%description server
Basic filesystem hierarchy for Uyuni server.

%package proxy
Summary:        Base structure for Uyuni proxy
Group:          System/Fhs
BuildRequires:  uyuni-base-common
Requires(pre):  uyuni-base-common

%description proxy
Basic filesystem hierarchy for Uyuni proxy.

%prep
# nothing to do here

%build
# nothing to do here

%install
mkdir -p %{buildroot}/etc/rhn
mkdir -p %{buildroot}/usr/share/rhn/proxy
mkdir -p %{buildroot}/var/spacewalk

%files common
%defattr(-,root,root)
%dir %attr(750,root,www) /etc/rhn
%dir /usr/share/rhn

%files server
%defattr(-,root,root)
%dir %attr(755,wwwrun,root) /var/spacewalk

%files proxy
%defattr(-,root,root)
%dir /usr/share/rhn/proxy

%changelog
