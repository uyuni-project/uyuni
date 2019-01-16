#
# spec file for package spacewalk-proxy-installer
#
# Copyright (c) 2018 SUSE LINUX GmbH, Nuernberg, Germany.
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

# Please submit bugfixes or comments via http://bugs.opensuse.org/
#


#!BuildIgnore:  udev-mini libudev-mini1
%if 0%{?fedora} || 0%{?rhel} >= 7
%{!?pylint_check: %global pylint_check 1}
%endif

%if 0%{?suse_version} > 1320 || 0%{?fedora}
# SLE15 and Fedora builds on Python 3
%global build_py3   1
%endif
%define pythonX %{?build_py3:python3}%{!?build_py3:python2}

Name:           spacewalk-proxy-installer
Summary:        Spacewalk Proxy Server Installer
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.0.4
Release:        1%{?dist}
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

Requires:       mgr-cfg
Requires:       mgr-cfg-actions
Requires:       mgr-cfg-client
Requires:       mgr-cfg-management
%if 0%{?suse_version}
Requires:       aaa_base
Requires:       apache2
Requires:       glibc
Requires(pre): spacewalk-proxy-common
Requires:       spacewalk-proxy-salt
%if 0%{?suse_version} > 1320
Requires:       firewalld
%else
Requires:       SuSEfirewall2
%endif
%else
Requires:       glibc-common

%if 0%{?fedora}
Requires:       hostname
%endif
%if 0%{?rhel} > 5
Requires:       net-tools
%endif

Requires:       chkconfig
%endif
Requires:       libxslt
Requires:       spacewalk-certs-tools >= 1.6.4
%if 0%{?pylint_check}
BuildRequires:  %{pythonX}-rhn-client-tools
BuildRequires:  spacewalk-%{pythonX}-pylint
%endif
BuildRequires:  /usr/bin/docbook2man

%if 0%{?fedora} || 0%{?rhel} > 5
Requires:       rhn-client-tools > 2.8.4
Requires:       rhnlib
%endif

Obsoletes:      proxy-installer < 5.3.0
Provides:       proxy-installer = 5.3.0

%define defaultdir %{_usr}/share/doc/proxy/conf-template/

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

%post
%if 0%{?suse_version}
if [ -f /etc/sysconfig/apache2 ]; then
    sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES proxy_http
fi
sed -i -e"s/^range_offset_limit -1 KB/range_offset_limit none/" /etc/squid/squid.conf
%endif

%install
mkdir -p $RPM_BUILD_ROOT/%{_bindir}
mkdir -p $RPM_BUILD_ROOT/%{_mandir}/man8
mkdir -p $RPM_BUILD_ROOT/%{_usr}/sbin
mkdir -p $RPM_BUILD_ROOT/%{_usr}/share/rhn/installer/jabberd
install -m 755 -d $RPM_BUILD_ROOT%{defaultdir}
install -m 644 squid.conf $RPM_BUILD_ROOT%{defaultdir}
install -m 644 rhn.conf $RPM_BUILD_ROOT%{defaultdir}
install -m 644 cobbler-proxy.conf $RPM_BUILD_ROOT%{defaultdir}
install -m 644 insights-proxy.conf $RPM_BUILD_ROOT%{defaultdir}
install -m 755 configure-proxy.sh $RPM_BUILD_ROOT/%{_usr}/sbin
install -m 644 get_system_id.xslt $RPM_BUILD_ROOT%{_usr}/share/rhn/
install -m 644 rhn-proxy-activate.8.gz $RPM_BUILD_ROOT%{_mandir}/man8/
install -m 644 configure-proxy.sh.8.gz $RPM_BUILD_ROOT%{_mandir}/man8/
install -m 640 jabberd/sm.xml jabberd/c2s.xml $RPM_BUILD_ROOT%{_usr}/share/rhn/installer/jabberd

# Fixing shebang for Python 3
%if 0%{?build_py3}
for i in $(find . -type f);
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done
%endif
install -m 755 rhn-proxy-activate.py $RPM_BUILD_ROOT/%{_usr}/sbin/rhn-proxy-activate
install -m 755 fetch-certificate.py  $RPM_BUILD_ROOT/%{_usr}/sbin/fetch-certificate

%if 0%{?suse_version} > 1320
mkdir -p %{buildroot}/%{_prefix}/lib/firewalld/services
install -m 0644 suse-manager-proxy.xml %{buildroot}/%{_prefix}/lib/firewalld/services
%endif

%check
%if 0%{?pylint_check}
# check coding style
spacewalk-%{pythonX}-pylint .
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
%dir %{_usr}/share/rhn/installer
%{_usr}/share/rhn/installer/jabberd/*.xml
%{_usr}/share/rhn/get_system_id.xslt
%{_usr}/sbin/rhn-proxy-activate
%{_usr}/sbin/fetch-certificate
%doc LICENSE answers.txt
%dir %{_usr}/share/doc/proxy
%dir %{_usr}/share/rhn
%dir %{_usr}/share/rhn/installer/jabberd
%if 0%{?suse_version} > 1320
%{_prefix}/lib/firewalld/services/suse-manager-proxy.xml
%endif

%changelog
