#
# spec file for package susemanager
#
# Copyright (c) 2018 SUSE LINUX GmbH, Nuernberg, Germany.
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

%if 0%{?suse_version} > 1320
# SLE15 builds on Python 3
%global build_py3   1
%endif
%define pythonX %{?build_py3:python3}%{!?build_py3:python2}

Name:           susemanager
Version:        4.0.7
Release:        1%{?dist}
Summary:        SUSE Manager specific scripts
License:        GPL-2.0-only
Group:          Applications/System
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#BuildArch:      noarch - not noarch because of ifarch usage!!!!

%if 0%{?build_py3}
BuildRequires:  python3-devel
%else
BuildRequires:  python-devel
%endif

# check section
%if 0%{?build_py3}
BuildRequires:  python3-pycurl
%else
BuildRequires:  python-curl
BuildRequires:  python-mock
%endif
BuildRequires:  pyxml
BuildRequires:  spacewalk-backend >= 1.7.38.20
BuildRequires:  spacewalk-backend-server
BuildRequires:  spacewalk-backend-sql-postgresql
BuildRequires:  suseRegisterInfo

PreReq:         %fillup_prereq %insserv_prereq tftp(server) postgresql-init
Requires(pre):  tomcat salt
Requires:       cobbler
Requires:       openslp-server
Requires:       spacewalk-admin
Requires:       spacewalk-setup
%ifarch %ix86 x86_64
Requires:       syslinux
%endif
%ifarch s390x ppc64le
Requires:       syslinux-x86_64
%endif
Requires:       less
Requires:       rsync
Requires:       spacewalk-schema
Requires:       susemanager-tools
# migration.sh need either sqlplus or psql
Requires:       spacewalk-db-virtual
Requires:       susemanager-branding
# yast module dependency
%if 0%{?suse_version} > 1320
Requires:       firewalld
%else
Requires:       SuSEfirewall2
%endif
Requires:       postfix
Requires:       yast2-users
# mgr-setup want to call mksubvolume
Requires:       snapper
%define python_sitelib %(%{pythonX} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%global pythonsmroot %{python_sitelib}/spacewalk

%description
A collection of scripts for managing SUSE Manager's initial
setup tasks, re-installation, upgrades and managing.

%package tools
Summary:        SUSE Manager Tools
Group:          Productivity/Other

%if 0%{?build_py3}
Requires:       createrepo_c
Requires:       python3
Requires:       python3-configobj
BuildRequires:  python3-configobj
%else
Requires:       createrepo
Requires:       python
Requires:       python-argparse
Requires:       python-configobj
BuildRequires:  python-configobj
Requires:       python-enum34
BuildRequires:  python-enum34
%endif
Requires:       spacewalk-backend >= 2.1.55.11
Requires:       spacewalk-backend-sql
Requires:       suseRegisterInfo
Requires:       susemanager-build-keys
Requires:       susemanager-sync-data
BuildRequires:  docbook-utils


%description tools
This package contains SUSE Manager tools

%prep
%setup -q

%build

# Fixing shebang for Python 3
%if 0%{?build_py3}
for i in `find . -type f`;
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done
%endif

%install
mkdir -p %{buildroot}/%{_prefix}/lib/susemanager/bin/
mkdir -p %{buildroot}/%{_prefix}/lib/susemanager/hooks/
install -m 0755 bin/* %{buildroot}/%{_prefix}/lib/susemanager/bin/
ln -s mgr-setup %{buildroot}/%{_prefix}/lib/susemanager/bin/migration.sh
ln -s pg-migrate-94-to-96.sh %{buildroot}/%{_prefix}/lib/susemanager/bin/pg-migrate.sh

mkdir -p %{buildroot}/%{_prefix}/share/rhn/config-defaults
mkdir -p %{buildroot}/%{_sysconfdir}/init.d
mkdir -p %{buildroot}/%{_sysconfdir}/slp.reg.d
mkdir -p %{buildroot}/%{_sysconfdir}/logrotate.d
install -m 0644 rhn-conf/rhn_server_susemanager.conf %{buildroot}/%{_prefix}/share/rhn/config-defaults
install -m 0644 etc/logrotate.d/susemanager-tools %{buildroot}/%{_sysconfdir}/logrotate.d
install -m 0644 etc/slp.reg.d/susemanager.reg %{buildroot}/%{_sysconfdir}/slp.reg.d
install -m 755 etc/init.d/susemanager %{buildroot}/%{_sysconfdir}/init.d
make -C src install PREFIX=$RPM_BUILD_ROOT PYTHON_BIN=%{pythonX} MANDIR=%{_mandir}
install -d -m 755 %{buildroot}/srv/www/os-images/

# empty repo for rhel base channels
mkdir -p %{buildroot}/srv/www/htdocs/pub/repositories/
cp -r pub/empty %{buildroot}/srv/www/htdocs/pub/repositories/

# YaST configuration
mkdir -p %{buildroot}%{_datadir}/YaST2/clients
mkdir -p %{buildroot}%{_datadir}/YaST2/scrconf
mkdir -p %{buildroot}%{_datadir}/applications/YaST2
mkdir -p %{buildroot}/etc/YaST2
install -m 0644 yast/*.rb %{buildroot}%{_datadir}/YaST2/clients
install -m 0644 yast/firstboot-susemanager.xml %{buildroot}/etc/YaST2
install -m 0644 yast/*.scr %{buildroot}%{_datadir}/YaST2/scrconf
%if 0%{?is_opensuse}
install -m 0644 yast/susemanager_setup_uyuni.desktop %{buildroot}%{_datadir}/applications/YaST2/susemanager_setup.desktop
%else
install -m 0644 yast/susemanager_setup.desktop %{buildroot}%{_datadir}/applications/YaST2/susemanager_setup.desktop
%endif

%if 0%{?suse_version} > 1320
mkdir -p %{buildroot}/%{_prefix}/lib/firewalld/services
install -m 0644 etc/firewalld/services/suse-manager-server.xml %{buildroot}/%{_prefix}/lib/firewalld/services
%else
mkdir -p %{buildroot}/%{_sysconfdir}/sysconfig/SuSEfirewall2.d/services
install -m 0644 etc/sysconfig/SuSEfirewall2.d/services/suse-manager-server %{buildroot}/%{_sysconfdir}/sysconfig/SuSEfirewall2.d/services/
%endif

%check
# we need to build a fake python dir. python did not work with
# two site-package/spacewalk dirs having different content
mkdir -p /var/tmp/fakepython/spacewalk
cp -a %{python_sitelib}/spacewalk/* /var/tmp/fakepython/spacewalk/
cp -a %{buildroot}%{python_sitelib}/spacewalk/* /var/tmp/fakepython/spacewalk/
export PYTHONPATH=/var/tmp/fakepython/:%{_datadir}/rhn
make -f Makefile.susemanager PYTHON_BIN=%{pythonX} unittest
unset PYTHONPATH
rm -rf /var/tmp/fakepython
%if !0%{?build_py3}
pushd %{buildroot}
find -name '*.py' -print0 | xargs -0 python %py_libdir/py_compile.py
popd
%endif

%pre
getent group susemanager >/dev/null || %{_sbindir}/groupadd -r susemanager

%post
POST_ARG=$1
%{fillup_and_insserv susemanager}
if [ -f /etc/sysconfig/atftpd ]; then
  . /etc/sysconfig/atftpd
  if [ $ATFTPD_DIRECTORY = "/tftpboot" ]; then
    sysconf_addword -r /etc/sysconfig/atftpd ATFTPD_DIRECTORY "/tftpboot"
    sysconf_addword /etc/sysconfig/atftpd ATFTPD_DIRECTORY "/srv/tftpboot"
  fi
fi
if [ ! -d /srv/tftpboot ]; then
  mkdir -p /srv/tftpboot
  chmod 750 /srv/tftpboot
  chown wwwrun:tftp /srv/tftpboot
fi
# XE appliance overlay file created this with different user
chown root.root /etc/sysconfig
# ensure susemanager group can write in all subdirs under /var/spacewalk/systems
getent passwd salt >/dev/null && usermod -a -G susemanager salt
getent passwd tomcat >/dev/null && usermod -a -G susemanager tomcat
getent passwd wwwrun >/dev/null && usermod -a -G susemanager wwwrun
if [ $POST_ARG -eq 2 ] ; then
    # when upgrading make sure /var/spacewalk/systems has the correct perms and owner
    MOUNT_POINT=$(grep -oP "^mount_point =\s*\K([^ ]+)" /etc/rhn/rhn.conf || echo "/var/spacewalk")
    SYSTEMS_DIR="$MOUNT_POINT/systems"
    if [[ -d "$MOUNT_POINT" && ! -d "$SYSTEMS_DIR" ]]; then
        mkdir $SYSTEMS_DIR
    fi
    if [ -d "$SYSTEMS_DIR" ]; then
        chmod 775 "$SYSTEMS_DIR"
        chown wwwrun:www "$SYSTEMS_DIR"
    fi
fi
# else new install and the systems dir should be created by spacewalk-setup

%posttrans
# make sure our database will use correct encoding
. /etc/sysconfig/postgresql
if [ -z $POSTGRES_LANG ]; then
    grep "^POSTGRES_LANG" /etc/sysconfig/postgresql > /dev/null 2>&1
    if [ $? = 0 ]; then
        sed -i -e "s/^POSTGRES_LANG.*$/POSTGRES_LANG=\"en_US.UTF-8\"/" /etc/sysconfig/postgresql
    else
        echo "POSTGRES_LANG=\"en_US.UTF-8\"" >> /etc/sysconfig/postgresql
    fi
fi

%postun
%{insserv_cleanup}

%files
%defattr(-,root,root,-)
%doc COPYING
%dir %{_prefix}/lib/susemanager
%dir %{_prefix}/lib/susemanager/bin/
%dir %{_prefix}/lib/susemanager/hooks/
%dir /etc/YaST2
%dir %{_datadir}/YaST2
%dir %{_datadir}/YaST2/clients
%dir %{_datadir}/YaST2/scrconf
%dir %{_datadir}/applications/YaST2
%dir %{_sysconfdir}/slp.reg.d
%{_prefix}/lib/susemanager/bin/*
%{_datadir}/YaST2/clients/*.rb
%{_datadir}/YaST2/scrconf/*.scr
%config /etc/YaST2/firstboot-susemanager.xml
%config %{_sysconfdir}/slp.reg.d/susemanager.reg
%{_sysconfdir}/init.d/susemanager
%{_datadir}/applications/YaST2/susemanager_setup.desktop
%attr(775,salt,susemanager) %dir /srv/www/os-images/
%if 0%{?suse_version} > 1320
%{_prefix}/lib/firewalld/services/suse-manager-server.xml
%else
%config %{_sysconfdir}/sysconfig/SuSEfirewall2.d/services/suse-manager-server
%endif

%files tools
%defattr(-,root,root,-)
%dir %{pythonsmroot}
%dir %{pythonsmroot}/susemanager
%dir %{_prefix}/share/rhn/
%dir %{_datadir}/susemanager
%dir /srv/www/htdocs/pub
%dir /srv/www/htdocs/pub/repositories
%dir /srv/www/htdocs/pub/repositories/empty
%dir /srv/www/htdocs/pub/repositories/empty/repodata
%attr(0755,root,www) %dir %{_prefix}/share/rhn/config-defaults
%config(noreplace) %{_sysconfdir}/logrotate.d/susemanager-tools
%{_prefix}/share/rhn/config-defaults/rhn_*.conf
%attr(0755,root,root) %{_sbindir}/mgr-register
%attr(0755,root,root) %{_sbindir}/mgr-clean-old-patchnames
%attr(0755,root,root) %{_sbindir}/mgr-create-bootstrap-repo
%attr(0755,root,root) %{_sbindir}/mgr-delete-patch
%attr(0755,root,root) %{_sbindir}/mgr-sync
%{pythonsmroot}/susemanager/__init__.py*
%{pythonsmroot}/susemanager/mgr_clean_old_patchnames.py*
%{pythonsmroot}/susemanager/mgr_delete_patch.py*
%{pythonsmroot}/susemanager/authenticator.py*
%{pythonsmroot}/susemanager/errata_helper.py*
%{pythonsmroot}/susemanager/helpers.py*
%{pythonsmroot}/susemanager/package_helper.py*
%{pythonsmroot}/susemanager/mgr_sync
%{_datadir}/susemanager/mgr_bootstrap_data.py*
%{_mandir}/man8/mgr-sync.8*
/srv/www/htdocs/pub/repositories/empty/repodata/*.xml*

%changelog
