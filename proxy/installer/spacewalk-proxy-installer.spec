#
# spec file for package spacewalk-proxy-installer
#
# Copyright (c) 2024 SUSE LLC
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


#!BuildIgnore:  udev-mini libudev-mini1
%if 0%{?fedora} || 0%{?rhel}
%define apacheconfdir %{_sysconfdir}/httpd
%else
%define apacheconfdir %{_sysconfdir}/apache2
%endif

%define rhnroot %{_usr}/share/rhn
%define pythondir %{rhnroot}/proxy-installer

Name:           spacewalk-proxy-installer
Version:        5.1.0
Release:        0
Summary:        Spacewalk Proxy Server Installer
License:        GPL-2.0-only
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildArch:      noarch

Requires:       firewalld
Requires(pre):  spacewalk-proxy-common
Requires:       spacewalk-proxy-salt
%if 0%{?suse_version}
Requires:       aaa_base
Requires:       apache2
Requires:       glibc
%else
Requires:       chkconfig
Requires:       glibc-common
Requires:       hostname
Requires:       httpd
Requires:       net-tools
Requires:       rhn-client-tools > 2.8.4
Requires:       rhnlib
%endif
Requires:       libxslt
Requires:       salt
Requires:       spacewalk-certs-tools >= 1.6.4
BuildRequires:  /usr/bin/docbook2man

# weakremover used on SUSE to get rid of orphan packages which are
# unsupported and do not have a dependency anymore
Provides:       weakremover(mgr-cfg)
Provides:       weakremover(mgr-cfg-actions)
Provides:       weakremover(mgr-cfg-client)
Provides:       weakremover(mgr-cfg-management)

%define defaultdir %{_usr}/share/rhn/proxy-template

%description
The Spacewalk Proxy Server allows package proxying/caching
and local package delivery services for groups of local servers from
Spacewalk Server. This service adds flexibility and economy of
resources to package update and deployment.

This package includes command line installer of Spacewalk Proxy Server.
Run configure-proxy.sh after installation to configure proxy.

%prep
%setup -q

%build
/usr/bin/docbook2man rhn-proxy-activate.sgml
/usr/bin/gzip rhn-proxy-activate.8
/usr/bin/docbook2man configure-proxy.sh.sgml
/usr/bin/gzip configure-proxy.sh.8

%install
mkdir -p %{buildroot}%{_bindir}
mkdir -p %{buildroot}%{_mandir}/man8
mkdir -p %{buildroot}%{_usr}/sbin
mkdir -p %{buildroot}%{pythondir}
mkdir -p %{buildroot}%{_prefix}/lib/firewalld/services

install -m 755 -d %{buildroot}%{defaultdir}
install -m 644 squid.conf %{buildroot}%{defaultdir}
install -m 644 rhn.conf %{buildroot}%{defaultdir}
install -m 644 cobbler-proxy.conf %{buildroot}%{defaultdir}
install -m 644 insights-proxy.conf %{buildroot}%{defaultdir}
install -m 755 configure-proxy.sh %{buildroot}%{_usr}/sbin
install -m 644 fetch-certificate.py  %{buildroot}%{pythondir}
install -m 755 spacewalk-setup-httpd %{buildroot}%{_bindir}
install -m 644 get_system_id.xslt %{buildroot}%{_usr}/share/rhn/
install -m 644 rhn-proxy-activate.8.gz %{buildroot}%{_mandir}/man8/
install -m 644 configure-proxy.sh.8.gz %{buildroot}%{_mandir}/man8/
install -m 0644 suse-manager-proxy.xml %{buildroot}%{_prefix}/lib/firewalld/services

# Fixing shebang for Python 3
for i in $(find . -type f);
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done
install -m 755 rhn-proxy-activate.py %{buildroot}%{_usr}/sbin/rhn-proxy-activate

%check

%post
%if 0%{?suse_version}
if [ -f %{_sysconfdir}/sysconfig/apache2 ]; then
    sysconf_addword %{_sysconfdir}/sysconfig/apache2 APACHE_MODULES proxy_http
    sysconf_addword %{_sysconfdir}/sysconfig/apache2 APACHE_MODULES headers
fi
%endif

%files
%defattr(-,root,root,-)
%dir %{defaultdir}
%{defaultdir}/squid.conf
%{defaultdir}/rhn.conf
%{defaultdir}/cobbler-proxy.conf
%{defaultdir}/insights-proxy.conf
%{_usr}/sbin/configure-proxy.sh
%{_mandir}/man8/*
%{_usr}/share/rhn/get_system_id.xslt
%{_usr}/sbin/rhn-proxy-activate
%dir %{pythondir}
%{pythondir}/fetch-certificate.py
%{_bindir}/spacewalk-setup-httpd
%doc answers.txt
%license LICENSE
%dir %{_usr}/share/rhn/proxy-template
%dir %{_usr}/share/rhn
%{_prefix}/lib/firewalld/services/suse-manager-proxy.xml

%changelog
