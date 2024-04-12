#
# spec file for package spacewalk-backend
#
# Copyright (c) 2023 SUSE LLC
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


%{!?_unitdir: %global _unitdir /lib/systemd/system}

%{!?python3_sitelib: %global python3_sitelib %(%{__python3} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")}
%{!?pylint_check: %global pylint_check 0}
%global rhnroot %{_prefix}/share/rhn
%global rhnconfigdefaults %{rhnroot}/config-defaults
%global rhnconf %{_sysconfdir}/rhn
%global m2crypto m2crypto
%global python3rhnroot %{python3_sitelib}/spacewalk

%if 0%{?fedora} || 0%{?rhel}
%global apacheconfd %{_sysconfdir}/httpd/conf.d
%global apache_user root
%global apache_group root
%global apache_pkg httpd
%global documentroot %{_localstatedir}/www/html
%global m2crypto python3-m2crypto
%global sslrootcert %{_sysconfdir}/pki/ca-trust/source/anchors/
%endif

%if 0%{?suse_version}
%global apacheconfd %{_sysconfdir}/apache2/conf.d
%global apache_user wwwrun
%global apache_group www
%global apache_pkg apache2
%global documentroot /srv/www/htdocs
%global m2crypto python3-M2Crypto
%global sslrootcert %{_sysconfdir}/pki/trust/anchors/
%endif

Name:           spacewalk-backend
Summary:        Common programs needed to be installed on the Spacewalk servers/proxies
License:        GPL-2.0-only
Group:          System/Management
Version:        4.3.28
Release:        0
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}-0.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if !0%{?suse_version} || 0%{?suse_version} >= 1120
BuildArch:      noarch
%endif

Requires:       python3
# /etc/rhn is provided by uyuni-base-common
Requires(pre):  uyuni-base-common
BuildRequires:  uyuni-base-common
Requires:       python3-rhnlib >= 2.5.74
Requires:       python3-rpm
Requires:       python3-uyuni-common-libs
Requires(pre):  %{apache_pkg}
Requires:       %{apache_pkg}
Requires:       python3-pycurl
# for Debian support
Requires:       python3-debian >= 0.1.44
%if 0%{?pylint_check}
BuildRequires:  spacewalk-python3-pylint
%endif
BuildRequires:  %{m2crypto}
BuildRequires:  /usr/bin/docbook2man
BuildRequires:  /usr/bin/msgfmt
BuildRequires:  docbook-utils
BuildRequires:  fdupes
BuildRequires:  python3
BuildRequires:  python3-debian
BuildRequires:  python3-rhn-client-tools
BuildRequires:  python3-rhnlib >= 2.5.74
BuildRequires:  python3-rpm
BuildRequires:  python3-rpm-macros
BuildRequires:  python3-uyuni-common-libs

%description
Generic program files needed by the Spacewalk server machines.
This package includes the common code required by all servers/proxies.

%package sql
Summary:        Core functions providing SQL connectivity for the Spacewalk backend modules
Group:          System/Management
Requires(pre):  %{name} = %{version}-%{release}
Requires:       %{name} = %{version}-%{release}
Requires:       %{name}-sql-virtual = %{version}-%{release}

%description sql
This package contains the basic code that provides SQL connectivity for
the Spacewalk backend modules.

%package sql-postgresql
Summary:        Postgresql backend for Spacewalk
Group:          System/Management
Requires:       python3-psycopg2 >= 2.8.4
Provides:       %{name}-sql-virtual = %{version}-%{release}

%description sql-postgresql
This package contains provides PostgreSQL connectivity for the Spacewalk
backend modules.

%package server
Summary:        Basic code that provides Spacewalk Server functionality
Group:          System/Management
Requires(pre):  %{name}-sql = %{version}-%{release}
Requires:       %{name}-sql = %{version}-%{release}
Requires:       spacewalk-config
Requires:       (apache2-mod_wsgi or python3-mod_wsgi)
Requires:       (python3-pam or python3-python-pam)

# cobbler-web is known to break our configuration
Conflicts:      cobbler-web

%description server
This package contains the basic code that provides server/backend
functionality for a variety of XML-RPC receivers. The architecture is
modular so that you can plug/install additional modules for XML-RPC
receivers and get them enabled automatically.

%package xmlrpc
Summary:        Handler for /XMLRPC
Group:          System/Management
Requires:       %{name}-server = %{version}-%{release}
Requires:       python3-rpm

%description xmlrpc
These are the files required for running the /XMLRPC handler, which
provide the basic support for the registration client (rhn_register)
and the up2date clients.

%package applet
Summary:        Handler for /APPLET
Group:          System/Management
Requires:       %{name}-server = %{version}-%{release}

%description applet
These are the files required for running the /APPLET handler, which
provides the functions for the Spacewalk applet.

%package app
Summary:        Handler for /APP
Group:          System/Management
Requires:       %{name}-server = %{version}-%{release}

%description app
These are the files required for running the /APP handler.
Calls to /APP are used by internal maintenance tools (rhnpush).

%package iss
Summary:        Handler for /SAT
Group:          System/Management
Requires:       %{name}-server = %{version}-%{release}

%description iss
%{name} contains the basic code that provides server/backend
functionality for a variety of XML-RPC receivers. The architecture is
modular so that you can plug/install additional modules for XML-RPC
receivers and get them enabled automatically.

This package contains /SAT handler, which provide Inter Spacewalk Sync
capability.

%package iss-export
Summary:        Listener for the Server XML dumper
Group:          System/Management
Requires:       %{name}-xml-export-libs = %{version}-%{release}
Requires:       python3-rpm

%description iss-export
%{name} contains the basic code that provides server/backend
functionality for a variety of XML-RPC receivers. The architecture is
modular so that you can plug/install additional modules for XML-RPC
receivers and get them enabled automatically.

This package contains listener for the Server XML dumper.

%package config-files-common
Summary:        Common files for the Configuration Management project
Group:          System/Management
Requires:       %{name}-server = %{version}-%{release}

%description config-files-common
Common files required by the Configuration Management project

%package config-files
Summary:        Handler for /CONFIG-MANAGEMENT
Group:          System/Management
Requires:       %{name}-config-files-common = %{version}-%{release}

%description config-files
This package contains the server-side code for configuration management.

%package config-files-tool
Summary:        Handler for /CONFIG-MANAGEMENT-TOOL
Group:          System/Management
Requires:       %{name}-config-files-common = %{version}-%{release}

%description config-files-tool
This package contains the server-side code for configuration management tool.

%package package-push-server
Summary:        Listener for rhnpush (non-XMLRPC version)
Group:          System/Management
Requires:       %{name}-server = %{version}-%{release}

%description package-push-server
Listener for rhnpush (non-XMLRPC version)

%package tools
Summary:        Spacewalk Services Tools
Group:          System/Management
Requires:       %{name}
Requires:       %{name}-app = %{version}-%{release}
Requires:       %{name}-xmlrpc = %{version}-%{release}
Requires:       systemd
BuildRequires:  systemd
%if 0%{?rhel}
Requires:       python3-dnf
BuildRequires:  systemd-rpm-macros
%else
%{?systemd_requires}
%endif

Requires:       python3-rhn-client-tools
Requires:       python3-solv
Requires:       python3-urlgrabber >= 4
Requires:       spacewalk-admin >= 0.1.1-0
Requires:       spacewalk-certs-tools
Requires:       susemanager-tools
Requires:       (python3-dateutil or python3-python-dateutil)
%if 0%{?suse_version}
Requires(pre):  libzypp(plugin:system) >= 0
Requires:       apache2-prefork
Requires:       python3-zypp-plugin
%endif
%if 0%{?fedora} || 0%{?rhel}
Requires:       mod_ssl
%endif
Requires:       %{m2crypto}
Requires:       %{name}-xml-export-libs
Requires:       cobbler
Requires:       python3-requests
Requires:       python3-rhnlib  >= 2.5.57

%description tools
Various utilities for the Spacewalk Server.

%package xml-export-libs
Summary:        Spacewalk XML data exporter
Group:          System/Management
Requires:       %{name}-server = %{version}-%{release}

%description xml-export-libs
Libraries required by various exporting tools

%package cdn
Summary:        CDN tools
Group:          System/Management
Requires:       %{m2crypto}
Requires:       %{name}-server = %{version}-%{release}
Requires:       subscription-manager

%description cdn
Tools for syncing content from Red Hat CDN

%prep
%setup -q

%build
make -f Makefile.backend all PYTHON_BIN=python3

# Fixing shebang for Python 3
for i in `find . -type f`;
do
	sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done

%if !0%{?is_opensuse} && 0%{?sle_version}
sed -i 's/PRODUCT_NAME = "Uyuni"/PRODUCT_NAME = "SUSE Manager"/' common/rhnConfig.py
%endif

%install
install -d $RPM_BUILD_ROOT%{rhnroot}
install -d $RPM_BUILD_ROOT%{python3rhnroot}
install -d $RPM_BUILD_ROOT%{python3rhnroot}/common
install -d $RPM_BUILD_ROOT%{rhnconf}
install -d $RPM_BUILD_ROOT/%{_unitdir}
install -d $RPM_BUILD_ROOT/%{_prefix}/lib/susemanager/bin/

make -f Makefile.backend install PREFIX=$RPM_BUILD_ROOT \
    MANDIR=%{_mandir} APACHECONFDIR=%{apacheconfd} PYTHON_BIN=python3

export PYTHON_MODULE_NAME=%{name}
export PYTHON_MODULE_VERSION=%{version}

# remove all unsupported translations
cd $RPM_BUILD_ROOT
for d in usr/share/locale/*; do
  if [ ! -d "/$d" ]; then
    rm -rfv "./$d"
  fi
done
cd -
ln -s satellite-sync $RPM_BUILD_ROOT/usr/bin/mgr-inter-sync
ln -s satellite-sync.8.gz $RPM_BUILD_ROOT/usr/share/man/man8/mgr-inter-sync.8.gz
ln -s rhn-satellite-exporter $RPM_BUILD_ROOT/usr/bin/mgr-exporter

install -m 644 rhn-conf/signing.cnf $RPM_BUILD_ROOT%{rhnconf}/signing.conf

install -m 644 satellite_tools/spacewalk-diskcheck.service $RPM_BUILD_ROOT/%{_unitdir}
install -m 644 satellite_tools/spacewalk-diskcheck.timer $RPM_BUILD_ROOT/%{_unitdir}

install -m 644 satellite_tools/ulnauth.py $RPM_BUILD_ROOT/%{python3rhnroot}/satellite_tools

%find_lang %{name}-server

%if 0%{?is_opensuse} || 0%{?fedora} || 0%{?rhel}
sed -i 's/^product_name.*/product_name = Uyuni/' $RPM_BUILD_ROOT%{rhnconfigdefaults}/rhn.conf
%endif

sed -i 's|#DOCUMENTROOT#|%{documentroot}|' $RPM_BUILD_ROOT%{rhnconfigdefaults}/rhn.conf
sed -i 's|#HTTPD_CONFIG_DIR#|%{apacheconfd}|' $RPM_BUILD_ROOT%{rhnconfigdefaults}/rhn.conf
sed -i 's|#REPORT_DB_SSLROOTCERT#|%{sslrootcert}RHN-ORG-TRUSTED-SSL-CERT|' $RPM_BUILD_ROOT%{rhnconfigdefaults}/rhn.conf

%if 0%{?fedora} || 0%{?rhel} > 6
sed -i 's/#LOGROTATE-3.8#//' $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/spacewalk-backend-*
%endif
%if 0%{?suse_version}
sed -i 's/#LOGROTATE-3.8#.*/    su root www/' $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/spacewalk-backend-*

%py3_compile -O %{buildroot}/%{python3rhnroot}
%fdupes %{buildroot}/%{python3rhnroot}
%endif

install -m 755 satellite_tools/mgr-update-pkg-extra-tags $RPM_BUILD_ROOT%{_prefix}/lib/susemanager/bin/

## Install Zypper plugins only on SUSE machines
install -Dd -m 0750 % $RPM_BUILD_ROOT%{_prefix}/lib/zypp/plugins/urlresolver
%{__install} satellite_tools/spacewalk-uln-resolver $RPM_BUILD_ROOT%{_prefix}/lib/zypp/plugins/urlresolver/spacewalk-uln-resolver
%{__install} satellite_tools/spacewalk-extra-http-headers $RPM_BUILD_ROOT%{_prefix}/lib/zypp/plugins/urlresolver/spacewalk-extra-http-headers

%check

%if 0%{?pylint_check}
# check coding style
export PYTHONPATH=$RPM_BUILD_ROOT%{python3rhnroot}:/usr/lib/rhn:/usr/share/rhn:$RPM_BUILD_ROOT%{python3_sitelib}/uyuni/common-libs
spacewalk-python3 $RPM_BUILD_ROOT%{python3rhnroot}/common \
                     $RPM_BUILD_ROOT%{python3rhnroot}/satellite_exporter \
                     $RPM_BUILD_ROOT%{python3rhnroot}/satellite_tools \
                     $RPM_BUILD_ROOT%{python3rhnroot}/cdn_tools \
                     $RPM_BUILD_ROOT%{python3rhnroot}/upload_server \
                     $RPM_BUILD_ROOT%{python3rhnroot}/wsgi

%endif

%post server
%if 0%{?suse_version}
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES wsgi
%endif
if [ ! -e %{rhnconf}/rhn.conf ]; then
    exit 0
fi

%pre tools
%if !0%{?rhel}
%service_add_pre spacewalk-diskcheck.service spacewalk-diskcheck.timer
%endif

%post tools
%if 0%{?rhel}
%systemd_post spacewalk-diskcheck.service
%systemd_post spacewalk-diskcheck.timer
%else
%service_add_post spacewalk-diskcheck.service spacewalk-diskcheck.timer
%endif
if test -f /var/log/rhn/rhn_server_satellite.log; then 
    chown -f %{apache_user}:%{apache_group} /var/log/rhn/rhn_server_satellite.log
fi

%preun tools
%if 0%{?rhel}
%systemd_preun spacewalk-diskcheck.service
%systemd_preun spacewalk-diskcheck.timer
%else
%service_del_preun spacewalk-diskcheck.service spacewalk-diskcheck.timer
%endif

%postun tools
%if 0%{?rhel}
%systemd_postun spacewalk-diskcheck.service
%systemd_postun spacewalk-diskcheck.timer
%else
%service_del_postun spacewalk-diskcheck.service spacewalk-diskcheck.timer
%endif

%files
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %{python3rhnroot}
%dir %{_prefix}/lib/susemanager
%dir %{_prefix}/lib/susemanager/bin
%{python3rhnroot}/__init__.py*
%{python3rhnroot}/common
%if 0%{?rhel}
%dir %{_var}/log/rhn
%else
%attr(770,root,%{apache_group}) %dir %{_var}/log/rhn
%endif
# Workaround for strict-whitespace-enforcement in httpd
%attr(644,root,%{apache_group}) %config %{apacheconfd}/aa-spacewalk-server.conf
# config files
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn.conf
%attr(755,root,root) %{_bindir}/spacewalk-cfg-get
%{_mandir}/man8/spacewalk-cfg-get.8.gz
# wsgi stuff
%dir %{rhnroot}/wsgi
%{rhnroot}/wsgi/__init__.py*
%{rhnroot}/wsgi/wsgiHandler.py*
%{rhnroot}/wsgi/wsgiRequest.py*
%dir %{rhnroot}
%dir %{python3rhnroot}/__pycache__/
%{python3rhnroot}/__pycache__/*

%files sql
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %{rhnroot}/server
# Need __init__ = share it with rhns-server
%dir %{python3rhnroot}/server
%{python3rhnroot}/server/__init__.py*
%{rhnroot}/server/__init__.py*
%dir %{python3rhnroot}/server/rhnSQL
%{python3rhnroot}/server/rhnSQL/const.py*
%{python3rhnroot}/server/rhnSQL/dbi.py*
%{python3rhnroot}/server/rhnSQL/__init__.py*
%{python3rhnroot}/server/rhnSQL/sql_*.py*
%dir %{python3rhnroot}/server/__pycache__/
%dir %{python3rhnroot}/server/rhnSQL/__pycache__/
%{python3rhnroot}/server/__pycache__/__init__.*
%{python3rhnroot}/server/rhnSQL/__pycache__/*
%exclude %{python3rhnroot}/server/rhnSQL/__pycache__/driver_postgresql.*

%files sql-postgresql
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%{python3rhnroot}/server/rhnSQL/driver_postgresql.py*
%{python3rhnroot}/server/rhnSQL/__pycache__/driver_postgresql.*

%files server -f %{name}-server.lang
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %{rhnroot}/server
# modules
%{python3rhnroot}/server/apacheAuth.py*
%{python3rhnroot}/server/apacheHandler.py*
%{python3rhnroot}/server/apacheRequest.py*
%{python3rhnroot}/server/apacheServer.py*
%{python3rhnroot}/server/apacheUploadServer.py*
%{python3rhnroot}/server/rhnAction.py*
%{python3rhnroot}/server/rhnAuthPAM.py*
%{python3rhnroot}/server/rhnCapability.py*
%{python3rhnroot}/server/rhnChannel.py*
%{python3rhnroot}/server/rhnDependency.py*
%{python3rhnroot}/server/rhnPackage.py*
%{python3rhnroot}/server/rhnPackageUpload.py*
%{python3rhnroot}/server/basePackageUpload.py*
%{python3rhnroot}/server/rhnHandler.py*
%{python3rhnroot}/server/rhnImport.py*
%{python3rhnroot}/server/rhnLib.py*
%{python3rhnroot}/server/rhnMapping.py*
%{python3rhnroot}/server/rhnRepository.py*
%{python3rhnroot}/server/rhnSession.py*
%{python3rhnroot}/server/rhnUser.py*
%{python3rhnroot}/server/rhnVirtualization.py*
%{python3rhnroot}/server/taskomatic.py*
%{python3rhnroot}/server/suseEula.py*
%dir %{python3rhnroot}/server/rhnServer
%{python3rhnroot}/server/rhnServer/*
%dir %{python3rhnroot}/server/importlib
%{python3rhnroot}/server/importlib/__init__.py*
%{python3rhnroot}/server/importlib/archImport.py*
%{python3rhnroot}/server/importlib/backend.py*
%{python3rhnroot}/server/importlib/backendLib.py*
%{python3rhnroot}/server/importlib/backendOracle.py*
%{python3rhnroot}/server/importlib/backend_checker.py*
%{python3rhnroot}/server/importlib/channelImport.py*
%{python3rhnroot}/server/importlib/debPackage.py*
%{python3rhnroot}/server/importlib/errataCache.py*
%{python3rhnroot}/server/importlib/errataImport.py*
%{python3rhnroot}/server/importlib/headerSource.py*
%{python3rhnroot}/server/importlib/importLib.py*
%{python3rhnroot}/server/importlib/kickstartImport.py*
%{python3rhnroot}/server/importlib/mpmSource.py*
%{python3rhnroot}/server/importlib/packageImport.py*
%{python3rhnroot}/server/importlib/packageUpload.py*
%{python3rhnroot}/server/importlib/productNamesImport.py*
%{python3rhnroot}/server/importlib/userAuth.py*
%{python3rhnroot}/server/importlib/orgImport.py*
%{python3rhnroot}/server/importlib/contentSourcesImport.py*
%{python3rhnroot}/server/importlib/supportInformationImport.py*
%{python3rhnroot}/server/importlib/suseProductsImport.py*
%dir %{python3rhnroot}/server/importlib/__pycache__/
%{python3rhnroot}/server/importlib/__pycache__/*
%{python3rhnroot}/server/__pycache__/*
%exclude %{python3rhnroot}/server/__pycache__/__init__.*
%exclude %{python3rhnroot}/server/__pycache__/configFilesHandler.*
%{rhnroot}/server/handlers/__init__.py*

# Repomd stuff
%dir %{python3rhnroot}/server/repomd
%{python3rhnroot}/server/repomd/__init__.py*
%{python3rhnroot}/server/repomd/domain.py*
%{python3rhnroot}/server/repomd/mapper.py*
%{python3rhnroot}/server/repomd/repository.py*
%{python3rhnroot}/server/repomd/view.py*
%dir %{python3rhnroot}/server/repomd/__pycache__/
%{python3rhnroot}/server/repomd/__pycache__/*

# the cache
%attr(755,%{apache_user},%{apache_group}) %dir %{_var}/cache/rhn
%attr(755,root,root) %dir %{_var}/cache/rhn/satsync
# config files
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server.conf

# main httpd config
%attr(644,root,%{apache_group}) %config %{apacheconfd}/zz-spacewalk-server.conf

# wsgi stuff
%attr(644,root,%{apache_group}) %config %{apacheconfd}/zz-spacewalk-server-wsgi.conf
%{rhnroot}/wsgi/app.py*
%{rhnroot}/wsgi/applet.py*
%{rhnroot}/wsgi/config.py*
%{rhnroot}/wsgi/config_tool.py*
%{rhnroot}/wsgi/package_push.py*
%{rhnroot}/wsgi/sat.py*
%{rhnroot}/wsgi/sat_dump.py*
%{rhnroot}/wsgi/xmlrpc.py*

# logs and other stuff
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-server

%dir %{rhnroot}/server
%dir %{rhnroot}/server/handlers

%files xmlrpc
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %{rhnroot}/server/handlers/xmlrpc
%{rhnroot}/server/handlers/xmlrpc/*
%dir %{python3rhnroot}/server/action
%{python3rhnroot}/server/action/*
%dir %{python3rhnroot}/server/action_extra_data
%{python3rhnroot}/server/action_extra_data/*
# config files
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_xmlrpc.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-xmlrpc
%dir %{rhnroot}/server
%dir %{rhnroot}/server/handlers

%files applet
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %{rhnroot}/server
%dir %{rhnroot}/server/handlers/applet
%{rhnroot}/server/handlers/applet/*
# config files
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_applet.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-applet

%files app
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %{rhnroot}/server
%dir %{rhnroot}/server/handlers/app
%{rhnroot}/server/handlers/app/*
# config files
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_app.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-app

%files iss
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %{rhnroot}/server
%dir %{rhnroot}/server/handlers/sat
%{rhnroot}/server/handlers/sat/*
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-iss

%files iss-export
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %{python3rhnroot}/satellite_exporter
%{python3rhnroot}/satellite_exporter/__init__.py*
%{python3rhnroot}/satellite_exporter/satexport.py*

%dir %{rhnroot}/satellite_exporter
%dir %{rhnroot}/satellite_exporter/handlers
%{rhnroot}/satellite_exporter/__init__.py*
%{rhnroot}/satellite_exporter/handlers/__init__.py*
%{rhnroot}/satellite_exporter/handlers/non_auth_dumper.py*
%dir %{python3rhnroot}/satellite_exporter/__pycache__/
%{python3rhnroot}/satellite_exporter/__pycache__/*
# config files
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-iss-export

%files config-files-common
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%{python3rhnroot}/server/configFilesHandler.py*
%{python3rhnroot}/server/__pycache__/configFilesHandler.*
%dir %{python3rhnroot}/server/config_common
%{python3rhnroot}/server/config_common/*

%files config-files
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %{rhnroot}/server
%dir %{rhnroot}/server/handlers/config
%{rhnroot}/server/handlers/config/*
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_config-management.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-config-files

%files config-files-tool
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %{rhnroot}/server
%dir %{rhnroot}/server/handlers/config_mgmt
%{rhnroot}/server/handlers/config_mgmt/*
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_config-management-tool.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-config-files-tool

%files package-push-server
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %{rhnroot}/upload_server
%{rhnroot}/upload_server/__init__.py*
%dir %{rhnroot}/upload_server/handlers
%{rhnroot}/upload_server/handlers/__init__.py*
%{rhnroot}/upload_server/handlers/package_push
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_upload.conf
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_upload_package-push.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-package-push-server

%files tools
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%doc README.ULN
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_satellite.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-tools
%attr(600,root,root) %config(noreplace) %{rhnconf}/signing.conf
%attr(755,root,root) %{_bindir}/rhn-charsets
%attr(755,root,root) %{_bindir}/rhn-satellite-activate
%attr(755,root,root) %{_bindir}/rhn-schema-version
%attr(755,root,root) %{_bindir}/rhn-ssl-dbstore
%attr(755,root,root) %{_bindir}/satellite-sync
%{_bindir}/mgr-inter-sync
%{_bindir}/mgr-exporter
%attr(755,root,root) %{_bindir}/spacewalk-debug
%attr(755,root,root) %{_bindir}/rhn-satellite-exporter
%attr(755,root,root) %{_bindir}/update-packages
%attr(755,root,root) %{_bindir}/spacewalk-repo-sync
%attr(755,root,root) %{_bindir}/rhn-db-stats
%attr(755,root,root) %{_bindir}/rhn-schema-stats
%attr(755,root,root) %{_bindir}/satpasswd
%attr(755,root,root) %{_bindir}/satwho
%attr(755,root,root) %{_bindir}/spacewalk-remove-channel*
%attr(755,root,root) %{_bindir}/spacewalk-update-signatures
%attr(755,root,root) %{_bindir}/spacewalk-data-fsck
%attr(755,root,root) %{_bindir}/spacewalk-fips-tool
%attr(755,root,root) %{_bindir}/mgr-sign-metadata
%attr(755,root,root) %{_bindir}/mgr-sign-metadata-ctl
%attr(755,root,root) %{_bindir}/spacewalk-diskcheck
%attr(755,root,root) %{_prefix}/lib/susemanager/bin/mgr-update-pkg-extra-tags
%{_prefix}/lib/zypp/plugins/urlresolver/spacewalk-uln-resolver
%{_prefix}/lib/zypp/plugins/urlresolver/spacewalk-extra-http-headers
%{python3rhnroot}/satellite_tools/contentRemove.py*
%{python3rhnroot}/satellite_tools/SequenceServer.py*
%{python3rhnroot}/satellite_tools/messages.py*
%{python3rhnroot}/satellite_tools/progress_bar.py*
%{python3rhnroot}/satellite_tools/req_channels.py*
%{python3rhnroot}/satellite_tools/satsync.py*
%{python3rhnroot}/satellite_tools/satCerts.py*
%{python3rhnroot}/satellite_tools/satComputePkgHeaders.py*
%{python3rhnroot}/satellite_tools/syncCache.py*
%{python3rhnroot}/satellite_tools/sync_handlers.py*
%{python3rhnroot}/satellite_tools/rhn_satellite_activate.py*
%{python3rhnroot}/satellite_tools/rhn_ssl_dbstore.py*
%{python3rhnroot}/satellite_tools/xmlWireSource.py*
%{python3rhnroot}/satellite_tools/updatePackages.py*
%{python3rhnroot}/satellite_tools/reposync.py*
%{python3rhnroot}/satellite_tools/constants.py*
%{python3rhnroot}/satellite_tools/download.py*
%{python3rhnroot}/satellite_tools/ulnauth.py*
%dir %{python3rhnroot}/satellite_tools/disk_dumper
%{python3rhnroot}/satellite_tools/disk_dumper/__init__.py*
%{python3rhnroot}/satellite_tools/disk_dumper/iss.py*
%{python3rhnroot}/satellite_tools/disk_dumper/iss_ui.py*
%{python3rhnroot}/satellite_tools/disk_dumper/iss_isos.py*
%{python3rhnroot}/satellite_tools/disk_dumper/iss_actions.py*
%{python3rhnroot}/satellite_tools/disk_dumper/dumper.py*
%{python3rhnroot}/satellite_tools/disk_dumper/string_buffer.py*
%dir %{python3rhnroot}/satellite_tools/repo_plugins
%attr(755,root,%{apache_group}) %dir %{_var}/log/rhn/reposync
%{python3rhnroot}/satellite_tools/repo_plugins/__init__.py*
%{python3rhnroot}/satellite_tools/repo_plugins/yum_src.py*
%{python3rhnroot}/satellite_tools/repo_plugins/yum_dnf_src.py*
%{python3rhnroot}/satellite_tools/repo_plugins/uln_src.py*
%{python3rhnroot}/satellite_tools/repo_plugins/deb_src.py*
%dir %{python3rhnroot}/satellite_tools/__pycache__/
%dir %{python3rhnroot}/satellite_tools/disk_dumper/__pycache__/
%dir %{python3rhnroot}/satellite_tools/repo_plugins/__pycache__/
%{python3rhnroot}/satellite_tools/__pycache__/*
%exclude %{python3rhnroot}/satellite_tools/__pycache__/__init__.*
%exclude %{python3rhnroot}/satellite_tools/__pycache__/geniso.*
%exclude %{python3rhnroot}/satellite_tools/__pycache__/connection.*
%exclude %{python3rhnroot}/satellite_tools/__pycache__/diskImportLib.*
%exclude %{python3rhnroot}/satellite_tools/__pycache__/syncLib.*
%exclude %{python3rhnroot}/satellite_tools/__pycache__/xmlDiskSource.*
%exclude %{python3rhnroot}/satellite_tools/__pycache__/xmlSource.*
%{python3rhnroot}/satellite_tools/disk_dumper/__pycache__/*
%{python3rhnroot}/satellite_tools/repo_plugins/__pycache__/*
%config %attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_iss.conf
%{_mandir}/man8/rhn-satellite-exporter.8*
%{_mandir}/man8/rhn-charsets.8*
%{_mandir}/man8/rhn-satellite-activate.8*
%{_mandir}/man8/rhn-schema-version.8*
%{_mandir}/man8/rhn-ssl-dbstore.8*
%{_mandir}/man8/rhn-db-stats.8*
%{_mandir}/man8/rhn-schema-stats.8*
%{_mandir}/man8/satellite-sync.8*
%{_mandir}/man8/mgr-inter-sync.8*
%{_mandir}/man8/spacewalk-debug.8*
%{_mandir}/man8/satpasswd.8*
%{_mandir}/man8/satwho.8*
%{_mandir}/man8/spacewalk-fips-tool.8*
%{_mandir}/man8/spacewalk-remove-channel.8*
%{_mandir}/man8/spacewalk-repo-sync.8*
%{_mandir}/man8/spacewalk-data-fsck.8*
%{_mandir}/man8/spacewalk-update-signatures.8*
%{_mandir}/man8/update-packages.8*
%attr(644, root, root) %{_unitdir}/spacewalk-diskcheck.service
%attr(644, root, root) %{_unitdir}/spacewalk-diskcheck.timer

%files xml-export-libs
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%dir %{python3rhnroot}/satellite_tools
%{python3rhnroot}/satellite_tools/__init__.py*
%{python3rhnroot}/satellite_tools/geniso.py*
# A bunch of modules shared with satellite-tools
%{python3rhnroot}/satellite_tools/connection.py*
%{python3rhnroot}/satellite_tools/diskImportLib.py*
%{python3rhnroot}/satellite_tools/syncLib.py*
%{python3rhnroot}/satellite_tools/xmlDiskSource.py*
%{python3rhnroot}/satellite_tools/xmlSource.py*
%dir %{python3rhnroot}/satellite_tools/exporter
%{python3rhnroot}/satellite_tools/exporter/__init__.py*
%{python3rhnroot}/satellite_tools/exporter/exportLib.py*
%{python3rhnroot}/satellite_tools/exporter/xmlWriter.py*
%dir %{python3rhnroot}/satellite_tools/exporter/__pycache__/
%{python3rhnroot}/satellite_tools/__pycache__/__init__.*
%{python3rhnroot}/satellite_tools/__pycache__/geniso.*
%{python3rhnroot}/satellite_tools/__pycache__/connection.*
%{python3rhnroot}/satellite_tools/__pycache__/diskImportLib.*
%{python3rhnroot}/satellite_tools/__pycache__/syncLib.*
%{python3rhnroot}/satellite_tools/__pycache__/xmlDiskSource.*
%{python3rhnroot}/satellite_tools/__pycache__/xmlSource.*
%{python3rhnroot}/satellite_tools/exporter/__pycache__/*

%files cdn
%defattr(-,root,root)
%attr(755,root,root) %{_bindir}/cdn-sync
%dir %{python3rhnroot}/cdn_tools
%{python3rhnroot}/cdn_tools/*.py*
%attr(755,root,%{apache_group}) %dir %{_var}/log/rhn/cdnsync
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-cdn
%{_mandir}/man8/cdn-sync.8*
%dir %{python3rhnroot}/cdn_tools
%dir %{python3rhnroot}/cdn_tools/__pycache__/
%{python3rhnroot}/cdn_tools/__pycache__/*

%changelog
