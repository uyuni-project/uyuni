#
# spec file for package uyuni-base
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
#Compat macro for new _fillupdir macro introduced in Nov 2017
%if ! %{defined _fillupdir}
  %define _fillupdir %{_localstatedir}/adm/fillup-templates
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
Version:        5.1.1
Release:        0
Summary:        Uyuni Base Package
License:        GPL-2.0-only
Group:          System/Fhs
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz

%description
Uyuni is a systems management application that will
inventory, provision, update and control your Linux machines.

%package common
Summary:        Base structure for Uyuni server and proxy
Group:          System/Fhs
%if 0%{?suse_version} >= 1500
Requires(pre):  group(www)
%endif
%if 0%{?rhel} || 0%{?fedora}
Requires(pre):  httpd
%endif

%description common
Basic filesystem hierarchy for Uyuni server and proxy.

%if 0%{?suse_version} >= 1500 || 0%{?rhel} >= 9
%package server
Summary:        Base structure for Uyuni server
Group:          System/Fhs
Requires(pre):  %{_sbindir}/groupadd
Requires(pre):  %{_sbindir}/usermod
Requires(pre):  salt
Requires(pre):  tomcat
Requires(pre):  uyuni-base-common
Provides:       group(susemanager)
%if 0%{?suse_version} >= 1500
Requires(pre):  user(wwwrun)
%endif
%if 0%{?rhel} || 0%{?fedora}
Requires(pre):  httpd
%endif

%description server
Basic filesystem hierarchy for Uyuni server.
%endif

%package proxy
Summary:        Base structure for Uyuni proxy
Group:          System/Fhs
Requires(pre):  uyuni-base-common

%description proxy
Basic filesystem hierarchy for Uyuni proxy.

%prep
%setup -q

%build
# nothing to do here

%install
mkdir -p %{buildroot}%{_sysconfdir}/rhn
mkdir -p %{buildroot}%{_datadir}/rhn/proxy
%if 0%{?suse_version} >= 1500 || 0%{?rhel} >= 9
mkdir -p %{buildroot}%{_localstatedir}/spacewalk
%endif
mkdir -p %{buildroot}%{_datadir}/rhn/config-defaults
mkdir -p %{buildroot}/srv/www/distributions

%if 0%{?suse_version} >= 1500 || 0%{?rhel} >= 9
%pre server
getent group susemanager >/dev/null || %{_sbindir}/groupadd -r susemanager
getent passwd salt >/dev/null && %{_sbindir}/usermod -a -G susemanager salt
getent passwd tomcat >/dev/null && %{_sbindir}/usermod -a -G susemanager,%{apache_group} tomcat
getent passwd %{apache_user} >/dev/null && %{_sbindir}/usermod -a -G susemanager %{apache_user}
%endif

%files common
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %attr(750,root,%{apache_group}) %{_sysconfdir}/rhn
%dir %{_datadir}/rhn
%dir %attr(755,root,%{apache_group}) %{_datadir}/rhn/config-defaults

%if 0%{?suse_version} >= 1500 || 0%{?rhel} >= 9
%files server
%defattr(-,root,root)
%dir %attr(755,%{apache_user}, root) %{_localstatedir}/spacewalk
%dir %attr(755,root,root) /srv/www/distributions
%endif

%files proxy
%defattr(-,root,root)
%dir %{_datadir}/rhn/proxy

%changelog
