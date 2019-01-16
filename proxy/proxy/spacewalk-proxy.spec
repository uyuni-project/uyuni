#
# spec file for package spacewalk-proxy
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


%if 0%{?fedora} || 0%{?rhel} >= 7
%{!?pylint_check: %global pylint_check 1}
%endif

%if 0%{?suse_version} > 1320
# SLE15 builds on Python 3
%global build_py3   1
%endif
%define pythonX %{?build_py3:python3}%{!?build_py3:python2}

Name:           spacewalk-proxy
Summary:        Spacewalk Proxy Server
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.0.4
Release:        1%{?dist}
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildRequires:  %{pythonX}
BuildArch:      noarch
Requires:       %{pythonX}-spacewalk-usix
Requires:       httpd
%if 0%{?pylint_check}
BuildRequires:  spacewalk-python2-pylint
%endif
BuildRequires:  mgr-push >= 4.0.0
BuildRequires:  %{pythonX}-mgr-push
BuildRequires:  spacewalk-backend >= 1.7.24

%define rhnroot %{_usr}/share/rhn
%define destdir %{rhnroot}/proxy
%define rhnconf %{_sysconfdir}/rhn
%if 0%{?suse_version}
%define httpdconf %{_sysconfdir}/apache2/conf.d
%define apache_user wwwrun
%define apache_group www
%else
%define httpdconf %{_sysconfdir}/httpd/conf.d
%define apache_user apache
%define apache_group apache
%endif

%define python_sitelib %(%{pythonX} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")

%description
This package is never built.

%package management
Summary:        Packages required by the Spacewalk Management Proxy
Group:          Applications/Internet
%if 0%{?suse_version}
Requires:       http_proxy
Requires:       openslp-server
%else
Requires:       squid
%endif
Requires:       %{name}-broker = %{version}
Requires:       %{name}-common >= %{version}
Requires:       %{name}-docs
Requires:       %{name}-html
Requires:       %{name}-redirect = %{version}
Requires:       httpd
Requires:       jabberd
Requires:       spacewalk-backend >= 1.7.24
Requires:       spacewalk-setup-jabberd
%if 0%{?fedora} || 0%{?rhel}
Requires:       sos
Requires:       spacewalk-proxy-selinux
Requires(preun): initscripts
%endif
Obsoletes:      rhns-proxy < 5.3.0
Obsoletes:      rhns-proxy-management < 5.3.0
BuildRequires:  /usr/bin/docbook2man
Obsoletes:      rhns-proxy-tools < 5.3.0
Provides:       rhns-proxy-tools = 5.3.0
Obsoletes:      spacewalk-proxy-tools < 0.5.3
Provides:       spacewalk-proxy-tools = %{version}
Obsoletes:      rhns-auth-daemon < 5.2.0
Provides:       rhns-auth-daemon = 1:%{version}
Obsoletes:      rhn-modssl < 2.9.0
Provides:       rhn-modssl = 1:%{version}
Obsoletes:      rhn-modpython < 2.8.0
Provides:       rhn-modpython = 1:%{version}
Obsoletes:      rhn-apache < 1.4.0
Provides:       rhn-apache = 1:%{version}

%description management
This package require all needed packages for Spacewalk Proxy Server.

%package broker
Summary:        The Broker component for the Spacewalk Proxy Server
Group:          Applications/Internet
Requires:       httpd
Requires:       spacewalk-certs-tools
Requires:       spacewalk-proxy-package-manager
Requires:       spacewalk-ssl-cert-check
%if 0%{?suse_version}
Requires:       apache2-prefork
Requires:       http_proxy
%else
Requires:       mod_ssl
Requires:       squid
%endif
%if 0%{?suse_version} > 1320
Requires:       apache2-mod_wsgi-python3
%else
Requires:       mod_wsgi
%endif
Requires(post): %{name}-common
Conflicts:      %{name}-redirect < %{version}-%{release}
Conflicts:      %{name}-redirect > %{version}-%{release}
# We don't want proxies and satellites on the same box
Conflicts:      rhns-satellite-tools
Obsoletes:      rhns-proxy-broker < 5.3.0

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
Group:          Applications/Internet
Requires:       httpd
Requires:       spacewalk-proxy-broker = %{version}-%{release}
Obsoletes:      rhns-proxy-redirect < 5.3.0

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
Group:          Applications/Internet
%if 0%{?suse_version}
BuildRequires:  apache2
%else
Requires:       mod_ssl
%endif
Requires:       curl
%if 0%{?suse_version} > 1320
Requires:       apache2-mod_wsgi-python3
%else
Requires:       mod_wsgi
%endif
Requires:       %{name}-broker >= %{version}
Requires:       spacewalk-backend >= 1.7.24
Requires(pre): policycoreutils
Obsoletes:      rhns-proxy-common < 5.3.0

%description common
The Spacewalk Proxy Server allows package caching
and local package delivery services for groups of local servers from
Spacewalk Server. This service adds flexibility and economy of
resources to package update and deployment.

This package contains the files shared by various
Spacewalk Proxy components.

%package package-manager
Summary:        Custom Channel Package Manager for the Spacewalk Proxy Server
Group:          Applications/Internet
Requires:       %{pythonX}
Requires:       %{pythonX}-rhnlib >= 2.5.56
Requires:       mgr-push >= 4.0.0
Requires:       spacewalk-backend >= 1.7.24
# proxy isn't Python 3 yet
Requires:       %{pythonX}-mgr-push
BuildRequires:  /usr/bin/docbook2man
BuildRequires:  %{pythonX}-devel
Obsoletes:      rhn_package_manager < 5.3.0
Obsoletes:      rhns-proxy-package-manager < 5.3.0

%description package-manager
The Spacewalk Proxy Server allows package caching
and local package delivery services for groups of local servers from
Spacewalk Server. This service adds flexibility and economy of
resources to package update and deployment.

This package contains the Command rhn_package_manager, which  manages
an Spacewalk Proxy Server\'s custom channel.

%package salt
Summary:        A ZeroMQ Proxy for Salt Minions
Group:          Applications/Internet
Requires:       systemd
Requires(pre):  salt
Requires(pre):  %{name}-common

%if 0%{?suse_version} >= 1210
BuildRequires:  systemd-rpm-macros
%endif

%{?systemd_requires}

# We don't want proxies and satellites on the same box
Conflicts:      rhns-satellite-tools

%description salt
A ZeroMQ Proxy for Salt Minions

%prep
%setup -q

%build
make -f Makefile.proxy

# Fixing shebang for Python 3
%if 0%{?build_py3}
for i in $(find . -type f);
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done
%endif

%install
make -f Makefile.proxy install PREFIX=$RPM_BUILD_ROOT
install -d -m 750 $RPM_BUILD_ROOT/%{_var}/cache/rhn/proxy-auth
mkdir -p %{buildroot}/%{_sysconfdir}/slp.reg.d
install -m 0644 etc/slp.reg.d/susemanagerproxy.reg %{buildroot}/%{_sysconfdir}/slp.reg.d

mkdir -p $RPM_BUILD_ROOT/%{_var}/spool/rhn-proxy/list

%if 0%{?suse_version}
mkdir -p $RPM_BUILD_ROOT/etc/apache2
mv $RPM_BUILD_ROOT/etc/httpd/conf.d $RPM_BUILD_ROOT/%{httpdconf}
rm -rf $RPM_BUILD_ROOT/etc/httpd
%endif
touch $RPM_BUILD_ROOT/%{httpdconf}/cobbler-proxy.conf

ln -sf rhn-proxy $RPM_BUILD_ROOT%{_sbindir}/spacewalk-proxy


pushd %{buildroot}
%if !0%{?build_py3}
find -name '*.py' -print0 | xargs -0 %{pythonX} %py_libdir/py_compile.py
%else
%py3_compile -O %{buildroot}
%endif
popd


install -m 0750 salt-broker/salt-broker %{buildroot}/%{_bindir}/
mkdir -p %{buildroot}/%{_sysconfdir}/salt/
install -m 0644 salt-broker/broker %{buildroot}/%{_sysconfdir}/salt/
install -d -m 755 %{buildroot}/%{_unitdir}/
%__install -D -m 444 salt-broker/salt-broker.service %{buildroot}/%{_unitdir}/salt-broker.service

ln -s %{_sbindir}/service %{buildroot}%{_sbindir}/rcsalt-broker

install -m 0755 mgr-proxy-ssh-push-init $RPM_BUILD_ROOT/%{_sbindir}/mgr-proxy-ssh-push-init
install -m 0755 mgr-proxy-ssh-force-cmd $RPM_BUILD_ROOT/%{_sbindir}/mgr-proxy-ssh-force-cmd
install -d -m 0755 $RPM_BUILD_ROOT/%{_var}/lib/spacewalk

%check
%if 0%{?pylint_check}
# check coding style
export PYTHONPATH=$RPM_BUILD_ROOT/usr/share/rhn:$RPM_BUILD_ROOT%{python_sitelib}:/usr/share/rhn
spacewalk-%{pythonX}-pylint $RPM_BUILD_ROOT/usr/share/rhn
%endif

%post broker
if [ -f %{_sysconfdir}/sysconfig/rhn/systemid ]; then
    chown root.%{apache_group} %{_sysconfdir}/sysconfig/rhn/systemid
    chmod 0640 %{_sysconfdir}/sysconfig/rhn/systemid
fi
%if 0%{?suse_version}
/sbin/service apache2 try-restart > /dev/null 2>&1 ||:
%else
/sbin/service httpd condrestart > /dev/null 2>&1
%endif

# In case of an upgrade, get the configured package list directory and clear it
# out.  Don't worry; it will be rebuilt by the proxy.

RHN_CONFIG_PY=%{rhnroot}/common/rhnConfig.py
RHN_PKG_DIR=%{_var}/spool/rhn-proxy

if [ -f $RHN_CONFIG_PY ] ; then

    # Check whether the config command supports the ability to retrieve a
    # config variable arbitrarily.  Versions of  < 4.0.6 (rhn) did not.

    %{pythonX} $RHN_CONFIG_PY proxy.broker > /dev/null 2>&1
    if [ $? -eq 1 ] ; then
        RHN_PKG_DIR=$(%{pythonX} $RHN_CONFIG_PY get proxy.broker pkg_dir)
    fi
fi

rm -rf $RHN_PKG_DIR/list/*

# Make sure the scriptlet returns with success
exit 0

%post common
%if 0%{?suse_version}
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES wsgi
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES access_compat
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES proxy
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES rewrite
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES version
sysconf_addword /etc/sysconfig/apache2 APACHE_SERVER_FLAGS SSL

# In case of an update, remove superfluous stuff
# from cobbler-proxy.conf (bnc#796581)

PROXY_CONF=/etc/apache2/conf.d/cobbler-proxy.conf
TMPFILE=`mktemp`

if grep "^ProxyPass /ks " $PROXY_CONF > /dev/null 2>&1 ; then
    grep -v "^ProxyPass /ks " $PROXY_CONF | \
    grep -v "^ProxyPassReverse /ks " | \
    grep -v "^ProxyPass /download " | \
    grep -v "^ProxyPassReverse /download " > $TMPFILE
    mv $TMPFILE $PROXY_CONF
fi

SSHUSER=mgrsshtunnel
if getent passwd $SSHUSER | grep ":/home/$SSHUSER:" > /dev/null ; then
  usermod -m -d %{_var}/lib/spacewalk/$SSHUSER $SSHUSER
fi
%endif

%post redirect
%if 0%{?suse_version}
/sbin/service apache2 try-restart > /dev/null 2>&1 ||:
%else
/sbin/service httpd condrestart > /dev/null 2>&1
%endif
# Make sure the scriptlet returns with success
exit 0

%post management
# The spacewalk-proxy-management package is also our "upgrades" package.
# We deploy new conf from configuration channel if needed
# we deploy new conf only if we install from webui and conf channel exist
if rhncfg-client verify %{_sysconfdir}/rhn/rhn.conf 2>&1|grep 'Not found'; then
     %{_bindir}/rhncfg-client get %{_sysconfdir}/rhn/rhn.conf
fi > /dev/null 2>&1
if rhncfg-client verify %{_sysconfdir}/squid/squid.conf | grep -E '(modified|missing)'; then
    rhncfg-client get %{_sysconfdir}/squid/squid.conf
    rm -rf %{_var}/spool/squid/*
    %{_usr}/sbin/squid -z
    /sbin/service squid condrestart
fi > /dev/null 2>&1

exit 0

%pre salt
%service_add_pre salt-broker.service

%post salt
%service_add_post salt-broker.service
systemctl enable salt-broker.service > /dev/null 2>&1 || :
systemctl start salt-broker.service > /dev/null 2>&1 || :

%preun salt
%service_del_preun salt-broker.service

%postun salt
%service_del_postun salt-broker.service

%preun broker
if [ $1 -eq 0 ] ; then
    # nuke the cache
    rm -rf %{_var}/cache/rhn/*
fi

%preun
if [ $1 = 0 ] ; then
%if 0%{?suse_version}
    /sbin/service apache2 try-restart > /dev/null 2>&1 ||:
%else
    /sbin/service httpd condrestart >/dev/null 2>&1
%endif
fi

%posttrans common
if [ -n "$1" ] ; then # anything but uninstall
    mkdir /var/cache/rhn/proxy-auth 2>/dev/null
    chown %{apache_user}:root /var/cache/rhn/proxy-auth
    restorecon /var/cache/rhn/proxy-auth
fi

%files salt
%defattr(-,root,root)
%{_bindir}/salt-broker
%{_unitdir}/salt-broker.service
%{_sbindir}/rcsalt-broker
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
%attr(644,root,%{apache_group}) %{_prefix}/share/rhn/config-defaults/rhn_proxy_broker.conf
%if 0%{?suse_version}
%dir %{destdir}/broker
%endif
%if 0%{?build_py3}
%dir %{destdir}/__pycache__/
%dir %{destdir}/broker/__pycache__/
%{destdir}/__pycache__/*
%{destdir}/broker/__pycache__/*
%endif

%files redirect
%defattr(-,root,root)
%dir %{destdir}
%{destdir}/redirect/__init__.py*
%{destdir}/redirect/rhnRedirect.py*
%attr(770,root,%{apache_group}) %dir %{_var}/log/rhn
%config(noreplace) %{_sysconfdir}/logrotate.d/rhn-proxy-redirect
# config files
%attr(644,root,%{apache_group}) %{_prefix}/share/rhn/config-defaults/rhn_proxy_redirect.conf
%if 0%{?suse_version}
%dir %{destdir}/redirect
%endif
%if 0%{?build_py3}
%dir %{destdir}/redirect/__pycache__/
%{destdir}/redirect/__pycache__/*
%endif

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
%attr(755,root,%{apache_group}) %dir %{rhnconf}
%attr(645,root,%{apache_group}) %config %{rhnconf}/rhn.conf
%attr(644,root,%{apache_group}) %{_prefix}/share/rhn/config-defaults/rhn_proxy.conf
%attr(644,root,%{apache_group}) %config %{httpdconf}/spacewalk-proxy.conf
# this file is created by either cli or webui installer
%ghost %config %{httpdconf}/cobbler-proxy.conf
%attr(644,root,%{apache_group}) %config %{httpdconf}/spacewalk-proxy-wsgi.conf
%{rhnroot}/wsgi/xmlrpc.py*
%{rhnroot}/wsgi/xmlrpc_redirect.py*
# the cache
%attr(750,%{apache_user},root) %dir %{_var}/cache/rhn
%attr(750,%{apache_user},root) %dir %{_var}/cache/rhn/proxy-auth
%if 0%{?suse_version}
%dir %{rhnroot}
%dir %{rhnroot}/wsgi
%{_sbindir}/mgr-proxy-ssh-push-init
%{_sbindir}/mgr-proxy-ssh-force-cmd
%attr(755,root,%{apache_group}) %dir %{rhnroot}/config-defaults
%attr(755,root,root) %dir %{_var}/lib/spacewalk
%endif
%if 0%{?build_py3}
%dir %{rhnroot}/wsgi/__pycache__/
%{rhnroot}/wsgi/__pycache__/*
%endif

%files package-manager
%defattr(-,root,root)
# config files
%attr(644,root,%{apache_group}) %{_prefix}/share/rhn/config-defaults/rhn_proxy_package_manager.conf
%{_bindir}/rhn_package_manager
%{rhnroot}/PackageManager/rhn_package_manager.py*
%{rhnroot}/PackageManager/__init__.py*
%{_mandir}/man8/rhn_package_manager.8.gz
%if 0%{?suse_version}
%dir %{rhnroot}/PackageManager
%endif
%if 0%{?build_py3}
%dir %{rhnroot}/PackageManager/__pycache__/
%{rhnroot}/PackageManager/__pycache__/*
%endif

%files management
%defattr(-,root,root)
# dirs
%dir %{destdir}
# start/stop script
%attr(755,root,root) %{_sbindir}/rhn-proxy
%{_sbindir}/spacewalk-proxy
# mans
%{_mandir}/man8/rhn-proxy.8*
%dir /usr/share/rhn
%dir %{_sysconfdir}/slp.reg.d
%config %{_sysconfdir}/slp.reg.d/susemanagerproxy.reg

%changelog
