#
# spec file for package spacewalk-proxy
#
# Copyright (c) 2025 SUSE LLC
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

%{!?python3_sitelib: %global python3_sitelib %(%{__python3} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")}

Name:           spacewalk-proxy
Version:        5.2.2
Release:        0
Summary:        Spacewalk Proxy Server
License:        GPL-2.0-only
URL:            https://github.com/uyuni-project/uyuni
#!CreateArchive: %{name}
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRequires:  python3
BuildRequires:  make
BuildRequires:  spacewalk-backend >= 1.7.24

%define rhnroot %{_usr}/share/rhn
%define destdir %{rhnroot}/proxy
%define rhnconf %{_sysconfdir}/rhn
%define python3rhnroot %{python3_sitelib}/spacewalk
%define httpdconf %{_sysconfdir}/apache2/conf.d
%define apache_user wwwrun
%define apache_group www
BuildArch:      noarch

%description
This package is never built.

%package broker
Summary:        The Broker component for the Spacewalk Proxy Server
Requires:       apache2
Requires:       apache2-mod_wsgi
Requires:       apache2-prefork
Requires(post): %{name}-common
Conflicts:      %{name}-redirect < %{version}-%{release}
Conflicts:      %{name}-redirect > %{version}-%{release}

%description broker
The Spacewalk Proxy Server allows package caching
and local package delivery services for groups of local servers from
Spacewalk Server. This service adds flexibility and economy of
resources to package update and deployment.

This package includes module, which request is cache-able and should
be sent to Squid and which should be sent directly to parent Spacewalk
server.

%package redirect
Summary:        The SSL Redirect component for the Spacewalk Proxy Server
Requires:       apache2
Requires:       spacewalk-proxy-broker = %{version}-%{release}

%description redirect
The Spacewalk Proxy Server allows package caching
and local package delivery services for groups of local servers from
Spacewalk Server. This service adds flexibility and economy of
resources to package update and deployment.

This package includes module, which handle request passed through squid
and assures a fully secure SSL connection is established and maintained
between an Spacewalk Proxy Server and parent Spacewalk server.

%package common
Summary:        Modules shared by Spacewalk Proxy components
Requires(pre):  uyuni-base-common
BuildRequires:  uyuni-base-common
BuildRequires:  apache2
Requires:       apache2-mod_wsgi
Requires:       %{name}-broker >= %{version}
Requires:       curl
Requires:       spacewalk-backend >= 1.7.24
Requires(pre):  policycoreutils

%description common
The Spacewalk Proxy Server allows package caching
and local package delivery services for groups of local servers from
Spacewalk Server. This service adds flexibility and economy of
resources to package update and deployment.

This package contains the files shared by various
Spacewalk Proxy components.

%package salt
Summary:        A ZeroMQ Proxy for Salt Minions
Requires(pre):  salt

%description salt
A ZeroMQ Proxy for Salt Minions

%prep
%setup -q

%build
make -f Makefile.proxy

# Fixing shebang for Python 3
for i in $(find . -type f);
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done

%install
install -d -m 755 %{buildroot}/%{_sysconfdir}/pki/tls/certs
install -d -m 755 %{buildroot}/%{_sysconfdir}/pki/tls/private

make -f Makefile.proxy install PREFIX=%{buildroot}
install -d -m 750 %{buildroot}/%{_var}/cache/rhn/proxy-auth

mkdir -p %{buildroot}/%{_var}/spool/rhn-proxy/list

mkdir -p %{buildroot}%{_sysconfdir}/apache2
mv %{buildroot}%{_sysconfdir}/httpd/conf.d %{buildroot}/%{httpdconf}
rm -rf %{buildroot}%{_sysconfdir}/httpd
touch %{buildroot}/%{httpdconf}/cobbler-proxy.conf

pushd %{buildroot}
%py3_compile -O %{buildroot}
popd

mkdir -p %{buildroot}/%{_bindir}/
install -m 0750 salt-broker/salt-broker %{buildroot}/%{_bindir}/
mkdir -p %{buildroot}/%{_sysconfdir}/salt/
install -m 0644 salt-broker/broker %{buildroot}/%{_sysconfdir}/salt/

install -m 0755 mgr-proxy-ssh-push-init %{buildroot}/%{_sbindir}/mgr-proxy-ssh-push-init
install -m 0755 mgr-proxy-ssh-force-cmd %{buildroot}/%{_sbindir}/mgr-proxy-ssh-force-cmd
install -d -m 0755 %{buildroot}/%{_var}/lib/spacewalk

%check

%post broker
if [ -f %{_sysconfdir}/sysconfig/rhn/systemid ]; then
    chown root.%{apache_group} %{_sysconfdir}/sysconfig/rhn/systemid
    chmod 0640 %{_sysconfdir}/sysconfig/rhn/systemid
fi

RHN_PKG_DIR=%{_var}/spool/rhn-proxy

rm -rf $RHN_PKG_DIR/list/*

# Make sure the scriptlet returns with success
exit 0

%post common
sysconf_addword %{_sysconfdir}/sysconfig/apache2 APACHE_MODULES wsgi
sysconf_addword %{_sysconfdir}/sysconfig/apache2 APACHE_MODULES proxy
sysconf_addword %{_sysconfdir}/sysconfig/apache2 APACHE_MODULES rewrite
sysconf_addword %{_sysconfdir}/sysconfig/apache2 APACHE_MODULES version
sysconf_addword %{_sysconfdir}/sysconfig/apache2 APACHE_SERVER_FLAGS SSL

%posttrans common
if [ -n "$1" ] ; then # anything but uninstall
    mkdir %{_localstatedir}/cache/rhn/proxy-auth 2>/dev/null
    chown %{apache_user}:root %{_localstatedir}/cache/rhn/proxy-auth
    restorecon %{_localstatedir}/cache/rhn/proxy-auth
fi

%files salt
%defattr(-,root,root)
%{_bindir}/salt-broker
%config(noreplace) %{_sysconfdir}/salt/broker

%files broker
%defattr(-,root,root)
%dir %{destdir}
%{destdir}/broker/__init__.py*
%{destdir}/broker/rhnBroker.py*
%{destdir}/broker/rhnRepository.py*
%attr(750,%{apache_user},%{apache_group}) %dir %{_var}/spool/rhn-proxy
%attr(750,%{apache_user},%{apache_group}) %dir %{_var}/spool/rhn-proxy/list
%attr(770,root,%{apache_group}) %dir %{_var}/log/rhn
%config(noreplace) %{_sysconfdir}/logrotate.d/rhn-proxy-broker
# config files
%attr(644,root,%{apache_group}) %{_datadir}/rhn/config-defaults/rhn_proxy_broker.conf
%dir %{destdir}/broker/__pycache__/
%{destdir}/broker/__pycache__/*

%files redirect
%defattr(-,root,root)
%dir %{destdir}
%{destdir}/redirect/__init__.py*
%{destdir}/redirect/rhnRedirect.py*
%attr(770,root,%{apache_group}) %dir %{_var}/log/rhn
%config(noreplace) %{_sysconfdir}/logrotate.d/rhn-proxy-redirect
# config files
%attr(644,root,%{apache_group}) %{_datadir}/rhn/config-defaults/rhn_proxy_redirect.conf
%dir %{destdir}/redirect
%dir %{destdir}/redirect/__pycache__/
%{destdir}/redirect/__pycache__/*

%files common
%defattr(-,root,root)
%dir %{destdir}
%{destdir}/__init__.py*
%{destdir}/apacheServer.py*
%{destdir}/apacheHandler.py*
%{destdir}/rhnShared.py*
%{destdir}/rhnConstants.py*
%{destdir}/responseContext.py*
%{destdir}/rhnAuthCacheClient.py*
%{destdir}/rhnProxyAuth.py*
%{destdir}/rhnAuthProtocol.py*
%attr(750,%{apache_user},%{apache_group}) %dir %{_var}/spool/rhn-proxy
%attr(750,%{apache_user},%{apache_group}) %dir %{_var}/spool/rhn-proxy/list
%attr(770,root,%{apache_group}) %dir %{_var}/log/rhn
# config files
%attr(640,root,%{apache_group}) %config(noreplace) %{rhnconf}/rhn.conf
%attr(644,root,%{apache_group}) %{_datadir}/rhn/config-defaults/rhn_proxy.conf
%attr(644,root,%{apache_group}) %config %{httpdconf}/spacewalk-proxy.conf
%attr(644,root,%{apache_group}) %config %{httpdconf}/smlm-proxy-forwards.conf
# this file is created by either cli or webui installer
%ghost %config %{httpdconf}/cobbler-proxy.conf
%attr(644,root,%{apache_group}) %config %{httpdconf}/spacewalk-proxy-wsgi.conf
%{rhnroot}/wsgi/xmlrpc.py*
%{rhnroot}/wsgi/xmlrpc_redirect.py*
# the cache
%attr(750,%{apache_user},root) %dir %{_var}/cache/rhn
%attr(750,%{apache_user},root) %dir %{_var}/cache/rhn/proxy-auth
%dir %{rhnroot}
%dir %{rhnroot}/wsgi
%{_sbindir}/mgr-proxy-ssh-push-init
%{_sbindir}/mgr-proxy-ssh-force-cmd
%attr(755,root,root) %dir %{_var}/lib/spacewalk
%dir %{rhnroot}/wsgi/__pycache__/
%{rhnroot}/wsgi/__pycache__/*
%dir %{destdir}/broker
%dir %{destdir}/__pycache__/
%{destdir}/__pycache__/*
%dir %{_sysconfdir}/pki/tls
%dir %{_sysconfdir}/pki/tls/certs
%dir %{_sysconfdir}/pki/tls/private

%changelog
