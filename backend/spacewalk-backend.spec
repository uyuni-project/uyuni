#
# spec file for package spacewalk-backend
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


%global rhnroot %{_prefix}/share/rhn
%global rhnconfigdefaults %{rhnroot}/config-defaults
%global rhnconf %{_sysconfdir}/rhn
%global m2crypto m2crypto

%if 0%{?fedora} || 0%{?suse_version} >= 1500 || 0%{?rhel} >= 8
%{!?python3_sitelib: %global python3_sitelib %(%{__python3} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")}
%global python_sitelib %{python3_sitelib}
%global python3rhnroot %{python3_sitelib}/spacewalk
%global pythonrhnroot %{python3_sitelib}/spacewalk
%global build_py3 1
%endif

%define pythonX %{?build_py3:python3}%{!?build_py3:python}

%if 0%{?fedora} || 0%{?rhel} >= 7
%{!?pylint_check: %global pylint_check 0}
%endif

%if 0%{?fedora} || 0%{?rhel}
%global apacheconfd %{_sysconfdir}/httpd/conf.d
%global apache_user apache
%global apache_group apache
%global apache_pkg httpd
%endif

%if 0%{?suse_version}
%{!?pylint_check: %global pylint_check 0}
%global apacheconfd %{_sysconfdir}/apache2/conf.d
%global apache_user wwwrun
%global apache_group www
%global apache_pkg apache2
%global m2crypto %{pythonX}-M2Crypto
%if !0%{?is_opensuse}
%define with_oracle     1
%endif
%endif

%if 0%{?suse_version} >= 1500
%global python_prefix python3
%else
%if  0%{?fedora} >= 28  || 0%{?rhel} >= 8
%global python_prefix python2
%else
%global python_prefix python
%endif
%endif

%{!?python2_sitelib: %global python2_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")}
%global python2rhnroot %{python2_sitelib}/spacewalk
%if !0%{?build_py3}
%global pythonrhnroot %{python_sitelib}/spacewalk
%endif

Name:           spacewalk-backend
Summary:        Common programs needed to be installed on the Spacewalk servers/proxies
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.0.4
Release:        1%{?dist}
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if !0%{?suse_version} || 0%{?suse_version} >= 1120
BuildArch:      noarch
%endif

Requires:       %{pythonX}
# /etc/rhn is provided by spacewalk-proxy-common or by spacewalk-config
Requires:       /etc/rhn
%if 0%{?build_py3}
Requires:       python3-%{name}-libs >= %{version}
Requires:       python3-rhnlib >= 2.5.74
Requires:       python3-rpm
%else
Requires:       python2-rhnlib >= 2.5.74
Requires:       %{name}-libs >= %{version}
%if 0%{?suse_version} >= 1500
Requires:       python2-rpm
%else
Requires:       rpm-python
%endif
%if 0%{?suse_version}
Requires:       python-pyliblzma
%else
%if 0%{?rhel}
Requires:       pyliblzma
%endif # %if 0%{?rhel}
%endif # 0%{?suse_version}
%endif # 0%{?build_py3}
# for Debian support
Requires:       %{python_prefix}-debian
%if 0%{?pylint_check}
%if 0%{?build_py3}
BuildRequires:  spacewalk-python3-pylint
%else
BuildRequires:  spacewalk-python2-pylint
%endif
%endif
BuildRequires:  /usr/bin/docbook2man
BuildRequires:  /usr/bin/msgfmt
BuildRequires:  docbook-utils
%if 0%{?build_py3}
BuildRequires:  python3-spacewalk-usix
%else
BuildRequires:  python2-spacewalk-usix
%endif
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} > 1310
%if 0%{?build_py3}
BuildRequires:  python3-gzipstream
BuildRequires:  python3-rhn-client-tools
BuildRequires:  python3-rhnlib >= 2.5.74
BuildRequires:  python3-rpm
%else
BuildRequires:  python2-gzipstream
BuildRequires:  python2-rhn-client-tools
BuildRequires:  python2-rhnlib >= 2.5.74
%if 0%{?suse_version} >= 1500
Requires:       python2-rpm
%else
Requires:       rpm-python
%endif
%endif
BuildRequires:  %{python_prefix}-debian

BuildRequires:  %{m2crypto}
%endif
Requires(pre): %{apache_pkg}
Requires:       %{apache_pkg}
%if 0%{?build_py3}
Requires:       python3-spacewalk-usix
%else
Requires:       python2-spacewalk-usix
%endif

%if 0%{?suse_version}
Requires:       %{pythonX}-pycurl
%endif

# we don't really want to require this redhat-release, so we protect
# against installations on other releases using conflicts...
Obsoletes:      rhns < 5.3.0
Obsoletes:      rhns-common < 5.3.0
Provides:       rhns = 1:%{version}-%{release}
Provides:       rhns-common = 1:%{version}-%{release}
Obsoletes:      spacewalk-backend-upload-server < 1.2.28
Provides:       spacewalk-backend-upload-server = 1:%{version}-%{release}

%description
Generic program files needed by the Spacewalk server machines.
This package includes the common code required by all servers/proxies.

%package sql
Summary:        Core functions providing SQL connectivity for the Spacewalk backend modules
Group:          Applications/Internet
Requires(pre): %{name} = %{version}-%{release}
Requires:       %{name} = %{version}-%{release}
Obsoletes:      rhns-sql < 5.3.0
Provides:       rhns-sql = 1:%{version}-%{release}
Requires:       %{name}-sql-virtual = %{version}-%{release}
%if 0%{?build_py3}
Requires:       python3-spacewalk-usix
%else
Requires:       python2-spacewalk-usix
%endif

%description sql
This package contains the basic code that provides SQL connectivity for
the Spacewalk backend modules.

%if 0%{?with_oracle}
%package sql-oracle
Summary:        Oracle backend for Spacewalk
Group:          Applications/Internet
%if 0%{?build_py3}
Requires:       python3-spacewalk-usix
%else
Requires:       python2-spacewalk-usix
%endif
Requires:       python(:DBAPI:oracle)
Provides:       %{name}-sql-virtual = %{version}-%{release}

%description sql-oracle
This package contains provides Oracle connectivity for the Spacewalk backend
modules.
%endif

%package sql-postgresql
Summary:        Postgresql backend for Spacewalk
Group:          Applications/Internet
%if 0%{?build_py3}
Requires:       python3-psycopg2 >= 2.0.14-2
Requires:       python3-spacewalk-usix
%else
Requires:       python-psycopg2 >= 2.0.14-2
Requires:       python2-spacewalk-usix
%endif
Provides:       %{name}-sql-virtual = %{version}-%{release}

%description sql-postgresql
This package contains provides PostgreSQL connectivity for the Spacewalk
backend modules.

%package server
Summary:        Basic code that provides Spacewalk Server functionality
Group:          Applications/Internet
Requires(pre): %{name}-sql = %{version}-%{release}
Requires:       %{name}-sql = %{version}-%{release}
%if 0%{?build_py3}
Requires:       python3-spacewalk-usix
Requires:       python3-python-pam
%else
Requires:       python2-spacewalk-usix
Requires:       python-python-pam
%endif
Requires:       spacewalk-config
Obsoletes:      rhns-server < 5.3.0
Provides:       rhns-server = 1:%{version}-%{release}

#this exists only on rhel5 and rhel6
Conflicts:      python-sgmlop
# cobbler-web is known to break our configuration
Conflicts:      cobbler-web

%if 0%{?build_py3}
Requires:       apache2-mod_wsgi-python3
%else
Requires:       mod_wsgi
%endif

%description server
This package contains the basic code that provides server/backend
functionality for a variety of XML-RPC receivers. The architecture is
modular so that you can plug/install additional modules for XML-RPC
receivers and get them enabled automatically.

%package xmlrpc
Summary:        Handler for /XMLRPC
Group:          Applications/Internet
Requires:       %{name}-server = %{version}-%{release}
%if 0%{?build_py3}
Requires:       python3-spacewalk-usix
Requires:       python3-rpm
%else
Requires:       python2-spacewalk-usix
%if 0%{?suse_version} >= 1500
Requires:       python2-rpm
%else
Requires:       rpm-python
%endif
%endif
Obsoletes:      rhns-server-xmlrpc < 5.3.0
Obsoletes:      rhns-xmlrpc < 5.3.0
Provides:       rhns-server-xmlrpc = 1:%{version}-%{release}
Provides:       rhns-xmlrpc = 1:%{version}-%{release}

%description xmlrpc
These are the files required for running the /XMLRPC handler, which
provide the basic support for the registration client (rhn_register)
and the up2date clients.

%package applet
Summary:        Handler for /APPLET
Group:          Applications/Internet
Requires:       %{name}-server = %{version}-%{release}
%if 0%{?build_py3}
Requires:       python3-spacewalk-usix
%else
Requires:       python2-spacewalk-usix
%endif
Obsoletes:      rhns-applet < 5.3.0
Provides:       rhns-applet = 1:%{version}-%{release}

%description applet
These are the files required for running the /APPLET handler, which
provides the functions for the Spacewalk applet.

%package app
Summary:        Handler for /APP
Group:          Applications/Internet
Requires:       %{name}-server = %{version}-%{release}
%if 0%{?build_py3}
Requires:       python3-spacewalk-usix
%else
Requires:       python2-spacewalk-usix
%endif
Obsoletes:      rhns-app < 5.3.0
Obsoletes:      rhns-server-app < 5.3.0
Provides:       rhns-app = 1:%{version}-%{release}
Provides:       rhns-server-app = 1:%{version}-%{release}
Obsoletes:      spacewalk-backend-xp < 1.8.38
Provides:       spacewalk-backend-xp = %{version}-%{release}
Obsoletes:      rhns-server-xp < 5.3.0
Obsoletes:      rhns-xp < 5.3.0
Provides:       rhns-server-xp = 1:%{version}-%{release}
Provides:       rhns-xp = 1:%{version}-%{release}

%description app
These are the files required for running the /APP handler.
Calls to /APP are used by internal maintenance tools (rhnpush).

%package iss
Summary:        Handler for /SAT
Group:          Applications/Internet
Requires:       %{name}-server = %{version}-%{release}
Obsoletes:      rhns-sat < 5.3.0
Provides:       rhns-sat = 1:%{version}-%{release}

%description iss
%{name} contains the basic code that provides server/backend
functionality for a variety of XML-RPC receivers. The architecture is
modular so that you can plug/install additional modules for XML-RPC
receivers and get them enabled automatically.

This package contains /SAT handler, which provide Inter Spacewalk Sync
capability.

%package iss-export
Summary:        Listener for the Server XML dumper
Group:          Applications/Internet
Requires:       %{name}-xml-export-libs = %{version}-%{release}
%if 0%{?build_py3}
Requires:       python3-spacewalk-usix
Requires:       python3-rpm
%else
Requires:       python2-spacewalk-usix
%if 0%{?suse_version} >= 1500
Requires:       python2-rpm
%else
Requires:       rpm-python
%endif
%endif

%description iss-export
%{name} contains the basic code that provides server/backend
functionality for a variety of XML-RPC receivers. The architecture is
modular so that you can plug/install additional modules for XML-RPC
receivers and get them enabled automatically.

This package contains listener for the Server XML dumper.

%package libs
Summary:        Spacewalk server and client tools libraries
Group:          Applications/Internet
Requires:       python
%if 0%{?suse_version}
BuildRequires:  python-devel
%else
BuildRequires:  python2-devel
Conflicts:      %{name} < 1.7.0
%endif
Requires:       python2-spacewalk-usix

%description libs
Libraries required by both Spacewalk server and Spacewalk client tools.

%if 0%{?build_py3}
%package -n python3-%{name}-libs
Summary:        Spacewalk client tools libraries for python3
Group:          Applications/Internet
BuildRequires:  python3-devel
Conflicts:      %{name} < 1.7.0
%if 0%{?suse_version}
Requires:       python3-base
%else
Requires:       python3-libs
%endif
Requires:       python3-spacewalk-usix

%description -n python3-%{name}-libs
Libraries required by Spacewalk client tools on Fedora 23.

%endif

%package config-files-common
Summary:        Common files for the Configuration Management project
Group:          Applications/Internet
Requires:       %{name}-server = %{version}-%{release}
%if 0%{?build_py3}
Requires:       python3-spacewalk-usix
%else
Requires:       python2-spacewalk-usix
%endif
Obsoletes:      rhns-config-files-common < 5.3.0
Provides:       rhns-config-files-common = 1:%{version}-%{release}

%description config-files-common
Common files required by the Configuration Management project

%package config-files
Summary:        Handler for /CONFIG-MANAGEMENT
Group:          Applications/Internet
Requires:       %{name}-config-files-common = %{version}-%{release}
Obsoletes:      rhns-config-files < 5.3.0
Provides:       rhns-config-files = 1:%{version}-%{release}

%description config-files
This package contains the server-side code for configuration management.

%package config-files-tool
Summary:        Handler for /CONFIG-MANAGEMENT-TOOL
Group:          Applications/Internet
Requires:       %{name}-config-files-common = %{version}-%{release}
%if 0%{?build_py3}
Requires:       python3-spacewalk-usix
%else
Requires:       python2-spacewalk-usix
%endif
Obsoletes:      rhns-config-files-tool < 5.3.0
Provides:       rhns-config-files-tool = 1:%{version}-%{release}

%description config-files-tool
This package contains the server-side code for configuration management tool.

%package package-push-server
Summary:        Listener for rhnpush (non-XMLRPC version)
Group:          Applications/Internet
Requires:       %{name}-server = %{version}-%{release}
Obsoletes:      rhns-package-push-server < 5.3.0
Provides:       rhns-package-push-server = 1:%{version}-%{release}

%description package-push-server
Listener for rhnpush (non-XMLRPC version)

%package tools
Summary:        Spacewalk Services Tools
Group:          Applications/Internet
Requires:       %{name}
Requires:       %{name}-app = %{version}-%{release}
Requires:       %{name}-xmlrpc = %{version}-%{release}
%if 0%{?build_py3}
Requires:       python3-python-dateutil
Requires:       python3-gzipstream
Requires:       python3-rhn-client-tools
%else
Requires:       python-dateutil
Requires:       python2-gzipstream
Requires:       python2-rhn-client-tools
%if 0%{?suse_version}
Requires:       python-pyliblzma
%else
%if 0%{?fedora} || 0%{?rhel} > 6
Requires:       pyliblzma
%endif # 0%{?fedora} || 0%{?rhel} > 6
%endif # 0%{?suse_version}
%endif # 0%{?build_py3}
Requires:       spacewalk-admin >= 0.1.1-0
Requires:       spacewalk-certs-tools
%if 0%{?suse_version}
Requires:       apache2-prefork
Requires:       susemanager-tools
%endif
%if 0%{?fedora} || 0%{?rhel}
Requires:       mod_ssl
Requires:       python2-devel
%endif
Requires:       %{name}-xml-export-libs
Requires:       cobbler >= 2.0.0
%if 0%{?fedora} >= 22
Recommends:     cobbler20
%endif
Requires:       %{m2crypto}
Requires:       %{pythonX}-requests
%if 0%{?build_py3}
Requires:       python3-spacewalk-usix
Requires:       python3-rhnlib  >= 2.5.57
%else
Requires:       python2-spacewalk-usix
Requires:       python2-rhnlib  >= 2.5.57
%endif
%if 0%{?fedora} || 0%{?rhel}
BuildRequires:  python-requests
%endif
Obsoletes:      rhns-satellite-tools < 5.3.0
Obsoletes:      spacewalk-backend-satellite-tools <= 0.2.7
Provides:       rhns-satellite-tools = 1:%{version}-%{release}
Provides:       spacewalk-backend-satellite-tools = %{version}-%{release}

%description tools
Various utilities for the Spacewalk Server.

%package xml-export-libs
Summary:        Spacewalk XML data exporter
Group:          Applications/Internet
Requires:       %{name}-server = %{version}-%{release}
%if 0%{?build_py3}
Requires:       python3-spacewalk-usix
%else
Requires:       python2-spacewalk-usix
%endif
Obsoletes:      rhns-xml-export-libs < 5.3.0
Provides:       rhns-xml-export-libs = 1:%{version}-%{release}

%description xml-export-libs
Libraries required by various exporting tools

%package cdn
Summary:        CDN tools
Group:          Applications/Internet
Requires:       %{m2crypto}
Requires:       %{name}-server = %{version}-%{release}
%if 0%{?build_py3}
Requires:       python3-argparse
Requires:       python3-spacewalk-usix
%else
Requires:       python-argparse
Requires:       python2-spacewalk-usix
%endif
Requires:       subscription-manager

%description cdn
Tools for syncing content from Red Hat CDN

%prep
%setup -q

%build
make -f Makefile.backend all PYTHON_BIN=%{pythonX}

# Fixing shebang for Python 3
%if 0%{?build_py3}
for i in `find . -type f`;
do
	sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done
%endif

%install
install -d $RPM_BUILD_ROOT%{rhnroot}
install -d $RPM_BUILD_ROOT%{pythonrhnroot}
install -d $RPM_BUILD_ROOT%{pythonrhnroot}/common
install -d $RPM_BUILD_ROOT%{rhnconf}

make -f Makefile.backend install PREFIX=$RPM_BUILD_ROOT \
    MANDIR=%{_mandir} APACHECONFDIR=%{apacheconfd} PYTHON_BIN=%{pythonX}
%if !0%{?with_oracle}
rm -f $RPM_BUILD_ROOT%{pythonrhnroot}/server/rhnSQL/driver_cx_Oracle.py*
%endif

%if 0%{?build_py3}
install -d $RPM_BUILD_ROOT%{pythonrhnroot}/common
install -d $RPM_BUILD_ROOT%{python2rhnroot}/common
cp $RPM_BUILD_ROOT%{pythonrhnroot}/__init__.py \
    $RPM_BUILD_ROOT%{python2rhnroot}/
cp $RPM_BUILD_ROOT%{pythonrhnroot}/common/__init__.py \
    $RPM_BUILD_ROOT%{python2rhnroot}/common
cp $RPM_BUILD_ROOT%{pythonrhnroot}/common/{checksum.py,cli.py,rhn_deb.py,rhn_mpm.py,rhn_pkg.py,rhn_rpm.py,stringutils.py,fileutils.py,rhnLib.py,timezone_utils.py} \
    $RPM_BUILD_ROOT%{python2rhnroot}/common
%endif

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

%find_lang %{name}-server

%if 0%{?is_opensuse}
sed -i 's/^product_name.*/product_name = Uyuni/' $RPM_BUILD_ROOT%{rhnconfigdefaults}/rhn.conf
%endif
%if 0%{?fedora} || 0%{?rhel} > 6
sed -i 's/#LOGROTATE-3.8#//' $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/spacewalk-backend-*
sed -i 's/#DOCUMENTROOT#/\/var\/www\/html/' $RPM_BUILD_ROOT%{rhnconfigdefaults}/rhn.conf
%endif
%if 0%{?suse_version}
sed -i 's/#LOGROTATE-3.8#.*/    su root www/' $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/spacewalk-backend-*
sed -i 's/#DOCUMENTROOT#/\/srv\/www\/htdocs/' $RPM_BUILD_ROOT%{rhnconfigdefaults}/rhn.conf
%if !0%{?build_py3}
pushd $RPM_BUILD_ROOT
find -name '*.py' -print0 | xargs -0 python %py_libdir/py_compile.py
popd
%endif

%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{pythonrhnroot}
%py_compile -O %{buildroot}/%{python2rhnroot}
rm -f $RPM_BUILD_ROOT%{python2rhnroot}/__init__.py*
rm -f $RPM_BUILD_ROOT%{python2rhnroot}/common/__init__.py*
%endif
%endif

%check
# Copy spacewalk-usix python files to allow unit tests to run
cp %{pythonrhnroot}/common/usix* $RPM_BUILD_ROOT%{pythonrhnroot}/common/
make -f Makefile.backend PYTHONPATH=$RPM_BUILD_ROOT%{python_sitelib} PYTHON_BIN=%{pythonX} test

%if 0%{?pylint_check}
# check coding style
export PYTHONPATH=$RPM_BUILD_ROOT%{pythonrhnroot}:/usr/lib/rhn:/usr/share/rhn
spacewalk-%{pythonX} $RPM_BUILD_ROOT%{pythonrhnroot}/common \
                     $RPM_BUILD_ROOT%{pythonrhnroot}/satellite_exporter \
                     $RPM_BUILD_ROOT%{pythonrhnroot}/satellite_tools \
                     $RPM_BUILD_ROOT%{pythonrhnroot}/cdn_tools \
                     $RPM_BUILD_ROOT%{pythonrhnroot}/upload_server \
                     $RPM_BUILD_ROOT%{pythonrhnroot}/wsgi

%if 0%{?build_py3}
export PYTHONPATH=$RPM_BUILD_ROOT%{python2rhnroot}:/usr/lib/rhn:/usr/share/rhn
spacewalk-python2 $RPM_BUILD_ROOT%{python2rhnroot}/common \
                  $RPM_BUILD_ROOT%{python2rhnroot}/satellite_exporter \
                  $RPM_BUILD_ROOT%{python2rhnroot}/satellite_tools \
                  $RPM_BUILD_ROOT%{python2rhnroot}/cdn_tools \
                  $RPM_BUILD_ROOT%{python2rhnroot}/upload_server \
                  $RPM_BUILD_ROOT%{python2rhnroot}/wsgi
%endif
%endif

# prevent file conflict with spacewalk-usix
rm -f $RPM_BUILD_ROOT%{pythonrhnroot}/__init__.py*
rm -f $RPM_BUILD_ROOT%{pythonrhnroot}/common/__init__.py*

# Remove spacewalk-usix python files used for running unit-tests
rm -f $RPM_BUILD_ROOT%{pythonrhnroot}/common/usix.py*
%if 0%{?build_py3}
rm -f $RPM_BUILD_ROOT%{pythonrhnroot}/common/__pycache__/usix*
%endif

%if !0%{?build_py3}
if [ -x %py_libdir/py_compile.py ]; then
    pushd %{buildroot}
    find -name '*.py' -print0 | xargs -0 python %py_libdir/py_compile.py
    popd
fi
%endif

%pre server
OLD_SECRET_FILE=%{_var}/www/rhns/server/secret/rhnSecret.py
if [ -f $OLD_SECRET_FILE ]; then
    install -d -m 750 -o root -g %{apache_group} %{rhnconf}
    mv ${OLD_SECRET_FILE}*  %{rhnconf}
fi

%post server
%if 0%{?suse_version}
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES wsgi
%endif
if [ ! -e %{rhnconf}/rhn.conf ]; then
    exit 0
fi

# Is secret key in our config file?
regex="^[[:space:]]*(server\.|)secret_key[[:space:]]*=.*$"

if grep -E -i $regex %{rhnconf}/rhn.conf > /dev/null 2>&1 ; then
    # secret key already there
    rm -f %{rhnconf}/rhnSecret.py*
    exit 0
fi

# Generate a secret key if old one is not present
if [ -f %{rhnconf}/rhnSecret.py ]; then
    secret_key=$(PYTHONPATH=%{rhnconf} %{__python} -c \
        "from rhnSecret import SECRET_KEY; print SECRET_KEY")
else
    secret_key=$(dd if=/dev/urandom bs=1024 count=1 2>/dev/null | sha1sum - |
        awk '{print $1}')
fi

echo "server.secret_key = $secret_key" >> %{rhnconf}/rhn.conf
rm -f %{rhnconf}/rhnSecret.py*

%files
%defattr(-,root,root)
%doc LICENSE
%dir %{pythonrhnroot}
%{pythonrhnroot}/common/suseLib.py*
%{pythonrhnroot}/common/apache.py*
%{pythonrhnroot}/common/byterange.py*
%{pythonrhnroot}/common/rhnApache.py*
%{pythonrhnroot}/common/rhnCache.py*
%{pythonrhnroot}/common/rhnConfig.py*
%{pythonrhnroot}/common/rhnException.py*
%{pythonrhnroot}/common/rhnFlags.py*
%{pythonrhnroot}/common/rhnLog.py*
%{pythonrhnroot}/common/rhnMail.py*
%{pythonrhnroot}/common/rhnTB.py*
%{pythonrhnroot}/common/rhnRepository.py*
%{pythonrhnroot}/common/rhnTranslate.py*
%{pythonrhnroot}/common/RPC_Base.py*
%attr(770,root,%{apache_group}) %dir %{_var}/log/rhn
# Workaround for strict-whitespace-enforcement in httpd
%attr(644,root,%{apache_group}) %config %{apacheconfd}/aa-spacewalk-server.conf
# config files
%attr(755,root,%{apache_group}) %dir %{rhnconfigdefaults}
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn.conf
%attr(755,root,root) %{_bindir}/spacewalk-cfg-get
%{_mandir}/man8/spacewalk-cfg-get.8.gz
# wsgi stuff
%dir %{rhnroot}/wsgi
%{rhnroot}/wsgi/__init__.py*
%{rhnroot}/wsgi/wsgiHandler.py*
%{rhnroot}/wsgi/wsgiRequest.py*
%if 0%{?suse_version}
%dir %{rhnroot}
%endif
%if 0%{?build_py3}
%dir %{pythonrhnroot}/__pycache__/
%{pythonrhnroot}/__pycache__/*
%{pythonrhnroot}/common/__pycache__/*
%endif

%files sql
%defattr(-,root,root)
%doc LICENSE
%if 0%{?suse_version}
%dir %{rhnroot}/server
%endif
# Need __init__ = share it with rhns-server
%dir %{pythonrhnroot}/server
%{pythonrhnroot}/server/__init__.py*
%{rhnroot}/server/__init__.py*
%dir %{pythonrhnroot}/server/rhnSQL
%{pythonrhnroot}/server/rhnSQL/const.py*
%{pythonrhnroot}/server/rhnSQL/dbi.py*
%{pythonrhnroot}/server/rhnSQL/__init__.py*
%{pythonrhnroot}/server/rhnSQL/sql_*.py*
%if 0%{?build_py3}
%dir %{pythonrhnroot}/server/__pycache__/
%dir %{pythonrhnroot}/server/rhnSQL/__pycache__/
%{pythonrhnroot}/server/__pycache__/*
%{pythonrhnroot}/server/rhnSQL/__pycache__/*
%endif

%if 0%{?with_oracle}
%files sql-oracle
%defattr(-,root,root)
%doc LICENSE
%{pythonrhnroot}/server/rhnSQL/driver_cx_Oracle.py*
%endif

%files sql-postgresql
%defattr(-,root,root)
%doc LICENSE
%{pythonrhnroot}/server/rhnSQL/driver_postgresql.py*

%files server -f %{name}-server.lang
%defattr(-,root,root)
%doc LICENSE
%if 0%{?suse_version}
%dir %{rhnroot}/server
%endif
# modules
%{pythonrhnroot}/server/apacheAuth.py*
%{pythonrhnroot}/server/apacheHandler.py*
%{pythonrhnroot}/server/apacheRequest.py*
%{pythonrhnroot}/server/apacheServer.py*
%{pythonrhnroot}/server/apacheUploadServer.py*
%{pythonrhnroot}/server/rhnAction.py*
%{pythonrhnroot}/server/rhnAuthPAM.py*
%{pythonrhnroot}/server/rhnCapability.py*
%{pythonrhnroot}/server/rhnChannel.py*
%{pythonrhnroot}/server/rhnDependency.py*
%{pythonrhnroot}/server/rhnPackage.py*
%{pythonrhnroot}/server/rhnPackageUpload.py*
%{pythonrhnroot}/server/basePackageUpload.py*
%{pythonrhnroot}/server/rhnHandler.py*
%{pythonrhnroot}/server/rhnImport.py*
%{pythonrhnroot}/server/rhnLib.py*
%{pythonrhnroot}/server/rhnMapping.py*
%{pythonrhnroot}/server/rhnRepository.py*
%{pythonrhnroot}/server/rhnSession.py*
%{pythonrhnroot}/server/rhnUser.py*
%{pythonrhnroot}/server/rhnVirtualization.py*
%{pythonrhnroot}/server/taskomatic.py*
%{pythonrhnroot}/server/suseEula.py*
%dir %{pythonrhnroot}/server/rhnServer
%{pythonrhnroot}/server/rhnServer/*
%dir %{pythonrhnroot}/server/importlib
%{pythonrhnroot}/server/importlib/__init__.py*
%{pythonrhnroot}/server/importlib/archImport.py*
%{pythonrhnroot}/server/importlib/backend.py*
%{pythonrhnroot}/server/importlib/backendLib.py*
%{pythonrhnroot}/server/importlib/backendOracle.py*
%{pythonrhnroot}/server/importlib/backend_checker.py*
%{pythonrhnroot}/server/importlib/channelImport.py*
%{pythonrhnroot}/server/importlib/debPackage.py*
%{pythonrhnroot}/server/importlib/errataCache.py*
%{pythonrhnroot}/server/importlib/errataImport.py*
%{pythonrhnroot}/server/importlib/headerSource.py*
%{pythonrhnroot}/server/importlib/importLib.py*
%{pythonrhnroot}/server/importlib/kickstartImport.py*
%{pythonrhnroot}/server/importlib/mpmSource.py*
%{pythonrhnroot}/server/importlib/packageImport.py*
%{pythonrhnroot}/server/importlib/packageUpload.py*
%{pythonrhnroot}/server/importlib/productNamesImport.py*
%{pythonrhnroot}/server/importlib/userAuth.py*
%{pythonrhnroot}/server/importlib/orgImport.py*
%{pythonrhnroot}/server/importlib/contentSourcesImport.py*
%{pythonrhnroot}/server/importlib/supportInformationImport.py*
%{pythonrhnroot}/server/importlib/suseProductsImport.py*
%if 0%{?build_py3}
%dir %{pythonrhnroot}/server/importlib/__pycache__/
%{pythonrhnroot}/server/importlib/__pycache__/*
%endif
%{rhnroot}/server/handlers/__init__.py*

# Repomd stuff
%dir %{pythonrhnroot}/server/repomd
%{pythonrhnroot}/server/repomd/__init__.py*
%{pythonrhnroot}/server/repomd/domain.py*
%{pythonrhnroot}/server/repomd/mapper.py*
%{pythonrhnroot}/server/repomd/repository.py*
%{pythonrhnroot}/server/repomd/view.py*
%if 0%{?build_py3}
%dir %{pythonrhnroot}/server/repomd/__pycache__/
%{pythonrhnroot}/server/repomd/__pycache__/*
%endif

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

%if 0%{?suse_version}
%dir %{rhnroot}/server
%dir %{rhnroot}/server/handlers
%endif

%files xmlrpc
%defattr(-,root,root)
%doc LICENSE
%dir %{rhnroot}/server/handlers/xmlrpc
%{rhnroot}/server/handlers/xmlrpc/*
%dir %{pythonrhnroot}/server/action
%{pythonrhnroot}/server/action/*
%dir %{pythonrhnroot}/server/action_extra_data
%{pythonrhnroot}/server/action_extra_data/*
%{pythonrhnroot}/server/auditlog.py*
# config files
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_xmlrpc.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-xmlrpc
%if 0%{?suse_version}
%dir %{rhnroot}/server
%dir %{rhnroot}/server/handlers
%endif

%files applet
%defattr(-,root,root)
%doc LICENSE
%if 0%{?suse_version}
%dir %{rhnroot}/server
%endif
%dir %{rhnroot}/server/handlers/applet
%{rhnroot}/server/handlers/applet/*
# config files
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_applet.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-applet

%files app
%defattr(-,root,root)
%doc LICENSE
%if 0%{?suse_version}
%dir %{rhnroot}/server
%endif
%dir %{rhnroot}/server/handlers/app
%{rhnroot}/server/handlers/app/*
# config files
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_app.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-app

%files iss
%defattr(-,root,root)
%doc LICENSE
%if 0%{?suse_version}
%dir %{rhnroot}/server
%endif
%dir %{rhnroot}/server/handlers/sat
%{rhnroot}/server/handlers/sat/*
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-iss

%files iss-export
%defattr(-,root,root)
%doc LICENSE
%dir %{pythonrhnroot}/satellite_exporter
%{pythonrhnroot}/satellite_exporter/__init__.py*
%{pythonrhnroot}/satellite_exporter/satexport.py*

%dir %{rhnroot}/satellite_exporter
%dir %{rhnroot}/satellite_exporter/handlers
%{rhnroot}/satellite_exporter/__init__.py*
%{rhnroot}/satellite_exporter/handlers/__init__.py*
%{rhnroot}/satellite_exporter/handlers/non_auth_dumper.py*
%if 0%{?build_py3}
%dir %{pythonrhnroot}/satellite_exporter/__pycache__/
%{pythonrhnroot}/satellite_exporter/__pycache__/*
%endif
# config files
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-iss-export

%files libs
%defattr(-,root,root)
%doc LICENSE
%if 0%{?build_py3}
%{python2rhnroot}
%{python2rhnroot}/common
%endif
%{python2rhnroot}/common/checksum.py*
%{python2rhnroot}/common/cli.py*
%{python2rhnroot}/common/fileutils.py*
%{python2rhnroot}/common/rhn_deb.py*
%{python2rhnroot}/common/rhn_mpm.py*
%{python2rhnroot}/common/rhn_pkg.py*
%{python2rhnroot}/common/rhn_rpm.py*
%{python2rhnroot}/common/stringutils.py*
%{python2rhnroot}/common/rhnLib.py*
%{python2rhnroot}/common/timezone_utils.py*

%if 0%{?build_py3}
%files -n python3-%{name}-libs
%defattr(-,root,root)
%doc LICENSE
%dir %{python3rhnroot}/common/__pycache__
%{python3rhnroot}/common/checksum.py
%{python3rhnroot}/common/cli.py
%{python3rhnroot}/common/fileutils.py
%{python3rhnroot}/common/rhn_deb.py
%{python3rhnroot}/common/rhn_mpm.py
%{python3rhnroot}/common/rhn_pkg.py
%{python3rhnroot}/common/rhn_rpm.py
%{python3rhnroot}/common/stringutils.py
%{python3rhnroot}/common/rhnLib.py*
%{python3rhnroot}/common/timezone_utils.py*
%{python3rhnroot}/common
%endif

%files config-files-common
%defattr(-,root,root)
%doc LICENSE
%{pythonrhnroot}/server/configFilesHandler.py*
%dir %{pythonrhnroot}/server/config_common
%{pythonrhnroot}/server/config_common/*

%files config-files
%defattr(-,root,root)
%doc LICENSE
%if 0%{?suse_version}
%dir %{rhnroot}/server
%endif
%dir %{rhnroot}/server/handlers/config
%{rhnroot}/server/handlers/config/*
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_config-management.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-config-files

%files config-files-tool
%defattr(-,root,root)
%doc LICENSE
%if 0%{?suse_version}
%dir %{rhnroot}/server
%endif
%dir %{rhnroot}/server/handlers/config_mgmt
%{rhnroot}/server/handlers/config_mgmt/*
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_config-management-tool.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-config-files-tool

%files package-push-server
%defattr(-,root,root)
%doc LICENSE
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
%doc LICENSE
%doc README.ULN
%attr(0750,root,%{apache_group}) %dir %{rhnconf}
%attr(644,root,%{apache_group}) %{rhnconfigdefaults}/rhn_server_satellite.conf
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-tools
%config(noreplace) %{rhnconf}/signing.conf
%attr(755,root,root) %{_bindir}/rhn-charsets
%attr(755,root,root) %{_bindir}/rhn-satellite-activate
%attr(755,root,root) %{_bindir}/rhn-schema-version
%attr(755,root,root) %{_bindir}/rhn-ssl-dbstore
%attr(755,root,root) %{_bindir}/satellite-sync
%attr(755,root,root) %{_bindir}/mgr-inter-sync
%attr(755,root,root) %{_bindir}/mgr-exporter
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
%{pythonrhnroot}/satellite_tools/contentRemove.py*
%{pythonrhnroot}/satellite_tools/SequenceServer.py*
%{pythonrhnroot}/satellite_tools/messages.py*
%{pythonrhnroot}/satellite_tools/progress_bar.py*
%{pythonrhnroot}/satellite_tools/req_channels.py*
%{pythonrhnroot}/satellite_tools/satsync.py*
%{pythonrhnroot}/satellite_tools/satCerts.py*
%{pythonrhnroot}/satellite_tools/satComputePkgHeaders.py*
%{pythonrhnroot}/satellite_tools/syncCache.py*
%{pythonrhnroot}/satellite_tools/sync_handlers.py*
%{pythonrhnroot}/satellite_tools/rhn_satellite_activate.py*
%{pythonrhnroot}/satellite_tools/rhn_ssl_dbstore.py*
%{pythonrhnroot}/satellite_tools/xmlWireSource.py*
%{pythonrhnroot}/satellite_tools/updatePackages.py*
%{pythonrhnroot}/satellite_tools/reposync.py*
%{pythonrhnroot}/satellite_tools/constants.py*
%{pythonrhnroot}/satellite_tools/download.py*
%dir %{pythonrhnroot}/satellite_tools/disk_dumper
%{pythonrhnroot}/satellite_tools/disk_dumper/__init__.py*
%{pythonrhnroot}/satellite_tools/disk_dumper/iss.py*
%{pythonrhnroot}/satellite_tools/disk_dumper/iss_ui.py*
%{pythonrhnroot}/satellite_tools/disk_dumper/iss_isos.py*
%{pythonrhnroot}/satellite_tools/disk_dumper/iss_actions.py*
%{pythonrhnroot}/satellite_tools/disk_dumper/dumper.py*
%{pythonrhnroot}/satellite_tools/disk_dumper/string_buffer.py*
%dir %{pythonrhnroot}/satellite_tools/repo_plugins
%attr(755,root,%{apache_group}) %dir %{_var}/log/rhn/reposync
%{pythonrhnroot}/satellite_tools/repo_plugins/__init__.py*
%{pythonrhnroot}/satellite_tools/repo_plugins/yum_src.py*
%{pythonrhnroot}/satellite_tools/repo_plugins/uln_src.py*
%{pythonrhnroot}/satellite_tools/repo_plugins/deb_src.py*
%if 0%{?build_py3}
%dir %{pythonrhnroot}/satellite_tools/__pycache__/
%dir %{pythonrhnroot}/satellite_tools/disk_dumper/__pycache__/
%dir %{pythonrhnroot}/satellite_tools/repo_plugins/__pycache__/
%{pythonrhnroot}/satellite_tools/__pycache__/*
%{pythonrhnroot}/satellite_tools/disk_dumper/__pycache__/*
%{pythonrhnroot}/satellite_tools/repo_plugins/__pycache__/*
%endif
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

%files xml-export-libs
%defattr(-,root,root)
%doc LICENSE
%dir %{pythonrhnroot}/satellite_tools
%{pythonrhnroot}/satellite_tools/__init__.py*
%{pythonrhnroot}/satellite_tools/geniso.py*
# A bunch of modules shared with satellite-tools
%{pythonrhnroot}/satellite_tools/connection.py*
%{pythonrhnroot}/satellite_tools/diskImportLib.py*
%{pythonrhnroot}/satellite_tools/syncLib.py*
%{pythonrhnroot}/satellite_tools/xmlDiskSource.py*
%{pythonrhnroot}/satellite_tools/xmlSource.py*
%dir %{pythonrhnroot}/satellite_tools/exporter
%{pythonrhnroot}/satellite_tools/exporter/__init__.py*
%{pythonrhnroot}/satellite_tools/exporter/exportLib.py*
%{pythonrhnroot}/satellite_tools/exporter/xmlWriter.py*
%if 0%{?build_py3}
%dir %{pythonrhnroot}/satellite_tools/exporter/__pycache__/
%{pythonrhnroot}/satellite_tools/exporter/__pycache__/*
%endif

%files cdn
%defattr(-,root,root)
%attr(755,root,root) %{_bindir}/cdn-sync
%dir %{pythonrhnroot}/cdn_tools
%{pythonrhnroot}/cdn_tools/*.py*
%attr(755,root,%{apache_group}) %dir %{_var}/log/rhn/cdnsync
%config(noreplace) %{_sysconfdir}/logrotate.d/spacewalk-backend-cdn
%{_mandir}/man8/cdn-sync.8*
%if 0%{?suse_version}
%dir %{pythonrhnroot}/cdn_tools
%if 0%{?build_py3}
%dir %{pythonrhnroot}/cdn_tools/__pycache__/
%{pythonrhnroot}/cdn_tools/__pycache__/*
%endif
%endif

%changelog
