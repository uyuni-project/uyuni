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

%if 0%{?suse_version}
%define www_path /srv/
%define apache_user wwwrun
%define apache_group www
%else
%define www_path %{_var}
%define apache_user apache
%define apache_group apache
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
Provides:       group(susemanager)
Requires(pre):  uyuni-base-common
Requires(pre):  %{_sbindir}/groupadd
Requires(pre):  %{_sbindir}/usermod
%if ! (0%{?rhel} == 6 || 0%{?suse_version} == 1110)
Requires(pre):  tomcat
%endif
Requires(pre):  salt
%if 0%{?suse_version} >= 1500
Requires(pre):  user(wwwrun)
%endif

%description server
Basic filesystem hierarchy for Uyuni server.

%package proxy
Summary:        Base structure for Uyuni proxy
Group:          System/Fhs
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
mkdir -p %{buildroot}/%{_prefix}/share/rhn/config-defaults

%pre server
%if ! 0%{?suse_version} == 1110
getent group susemanager >/dev/null || %{_sbindir}/groupadd -r susemanager
getent passwd salt >/dev/null && %{_sbindir}/usermod -a -G susemanager salt
getent passwd tomcat >/dev/null && %{_sbindir}/usermod -a -G susemanager tomcat
getent passwd wwwrun >/dev/null && %{_sbindir}/usermod -a -G susemanager wwwrun
%endif

%files common
%defattr(-,root,root)
%dir %attr(750,root,%{apache_group}) /etc/rhn
%dir %{_prefix}/share/rhn
%dir %attr(755,root,%{apache_group}) %{_prefix}/share/rhn/config-defaults

%files server
%defattr(-,root,root)
%dir %attr(755,%{apache_user},root) /var/spacewalk

%files proxy
%defattr(-,root,root)
%dir /usr/share/rhn/proxy

%changelog
