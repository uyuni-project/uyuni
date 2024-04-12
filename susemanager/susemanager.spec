#
# spec file for package susemanager
#
# Copyright (c) 2021 SUSE LLC
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


%if 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
# SLE15 and RHEL8 build on Python 3
%global build_py3   1
%endif
%define pythonX %{?build_py3:python3}%{!?build_py3:python2}

%if 0%{?rhel}
%global apache_user root
%global apache_group root
%global tftp_group root
%global salt_user root
%global salt_group root
%global serverdir %{_sharedstatedir}
%global wwwroot %{_localstatedir}/www
%global wwwdocroot %{wwwroot}/html
%endif

%if 0%{?suse_version}
%global apache_user wwwrun
%global apache_group www
%global tftp_group tftp
%global salt_user salt
%global salt_group salt
%global serverdir /srv
%global wwwroot %{serverdir}/www
%global wwwdocroot %{wwwroot}/htdocs
%endif

%global debug_package %{nil}

Name:           susemanager
Version:        4.3.35
Release:        0
Summary:        SUSE Manager specific scripts
License:        GPL-2.0-only
Group:          Applications/System
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#BuildArch:      noarch - not noarch because of ifarch usage!!!!

%if 0%{?rhel}
BuildRequires:  gettext
%endif

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
BuildRequires:  python2
%if !0%{?rhel}
BuildRequires:  pyxml
%endif
BuildRequires:  spacewalk-backend >= 1.7.38.20
BuildRequires:  spacewalk-backend-server
BuildRequires:  spacewalk-backend-sql-postgresql
BuildRequires:  suseRegisterInfo

%if 0%{?suse_version}
BuildRequires:  %fillup_prereq %insserv_prereq tftp postgresql-init
Requires(pre):  %fillup_prereq %insserv_prereq tftp postgresql-init
Requires(preun):%fillup_prereq %insserv_prereq tftp postgresql-init
Requires(post): user(%{apache_user})
Requires:       yast2-users
%endif
Requires(pre):  salt
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
Recommends:     susemanager-branding
BuildRequires:  uyuni-base-server
Requires(pre):  uyuni-base-server
# yast module dependency
%if 0%{?suse_version} > 1320
Requires:       firewalld
%endif
Requires:       postfix
Requires:       reprepro >= 5.4
# mgr-setup want to call mksubvolume for btrfs filesystems
Recommends:     snapper
# mgr-setup calls dig
Requires:       bind-utils
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
Requires:       python3-uyuni-common-libs
BuildRequires:  python3-configobj
%else
Requires:       createrepo
Requires:       python
Requires:       python-argparse
Requires:       python-configobj
Requires:       python2-uyuni-common-libs
BuildRequires:  python-configobj
Requires:       python-enum34
BuildRequires:  python-enum34
%endif
Requires:       spacewalk-backend >= 2.1.55.11
Requires:       spacewalk-backend-sql
Requires:       spacewalk-common
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
mkdir -p %{buildroot}/%{_sysconfdir}/slp.reg.d
mkdir -p %{buildroot}/%{_sysconfdir}/logrotate.d
install -m 0644 rhn-conf/rhn_server_susemanager.conf %{buildroot}/%{_prefix}/share/rhn/config-defaults
install -m 0644 etc/logrotate.d/susemanager-tools %{buildroot}/%{_sysconfdir}/logrotate.d
install -m 0644 etc/slp.reg.d/susemanager.reg %{buildroot}/%{_sysconfdir}/slp.reg.d
make -C src install PREFIX=$RPM_BUILD_ROOT PYTHON_BIN=%{pythonX} MANDIR=%{_mandir}
install -d -m 755 %{buildroot}/%{wwwroot}/os-images/

# empty repo for rhel base channels
mkdir -p %{buildroot}%{wwwdocroot}/pub/repositories/
cp -r pub/empty %{buildroot}%{wwwdocroot}/pub/repositories/

# empty repo for Ubuntu base fake channel
cp -r pub/empty-deb %{buildroot}%{wwwdocroot}/pub/repositories/

# YaST configuration
mkdir -p %{buildroot}%{_datadir}/YaST2/clients
mkdir -p %{buildroot}%{_datadir}/YaST2/scrconf
mkdir -p %{buildroot}%{_datadir}/applications/YaST2
mkdir -p %{buildroot}/etc/YaST2
install -m 0644 yast/*.rb %{buildroot}%{_datadir}/YaST2/clients
install -m 0644 yast/*.scr %{buildroot}%{_datadir}/YaST2/scrconf
%if 0%{?is_opensuse}
install -m 0644 yast/org.uyuni-project.yast2.Uyuni.desktop %{buildroot}%{_datadir}/applications/YaST2/org.uyuni-project.yast2.Uyuni.desktop
%else
install -m 0644 yast/com.suse.yast2.SUSEManager.desktop %{buildroot}%{_datadir}/applications/YaST2/com.suse.yast2.SUSEManager.desktop
%endif

%if 0%{?suse_version} > 1320
mkdir -p %{buildroot}/%{_prefix}/lib/firewalld/services
install -m 0644 etc/firewalld/services/suse-manager-server.xml %{buildroot}/%{_prefix}/lib/firewalld/services
%else
mkdir -p %{buildroot}/%{_sysconfdir}/firewalld/services
install -m 0644 etc/firewalld/services/suse-manager-server.xml %{buildroot}/%{_sysconfdir}/firewalld/services
%endif

%if 0%{?sle_version} && !0%{?is_opensuse}
# this script migrate the server to Uyuni. It should not be available on SUSE Manager
rm -f %{buildroot}/%{_prefix}/lib/susemanager/bin/server-migrator.sh
%endif

make -C po install PREFIX=$RPM_BUILD_ROOT

%find_lang susemanager

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

%post
POST_ARG=$1
if [ -f /etc/sysconfig/atftpd ]; then
  . /etc/sysconfig/atftpd
  if [ $ATFTPD_DIRECTORY = "/tftpboot" ]; then
    sysconf_addword -r /etc/sysconfig/atftpd ATFTPD_DIRECTORY "/tftpboot"
    sysconf_addword /etc/sysconfig/atftpd ATFTPD_DIRECTORY "%{serverdir}/tftpboot"
  fi
fi
if [ ! -d %{serverdir}/tftpboot ]; then
  mkdir -p %{serverdir}/tftpboot
  chmod 750 %{serverdir}/tftpboot
  chown %{apache_user}:%{tftp_group} %{serverdir}/tftpboot
fi
# XE appliance overlay file created this with different user
chown root.root /etc/sysconfig
if [ $POST_ARG -eq 2 ] ; then
    # when upgrading make sure /var/spacewalk/systems has the correct perms and owner
    MOUNT_POINT=$(grep -oP "^mount_point =\s*\K([^ ]+)" /etc/rhn/rhn.conf || echo "/var/spacewalk")
    SYSTEMS_DIR="$MOUNT_POINT/systems"
    if [[ -d "$MOUNT_POINT" && ! -d "$SYSTEMS_DIR" ]]; then
        mkdir $SYSTEMS_DIR
    fi
    if [ -d "$SYSTEMS_DIR" ]; then
        chmod 775 "$SYSTEMS_DIR"
        chown %{apache_user}:%{apache_group} "$SYSTEMS_DIR"
    fi
fi
# else new install and the systems dir should be created by spacewalk-setup
# Fix permissions for existing swapfiles (bsc#1131954, CVE-2019-3684)
if [[ -f /SWAPFILE && $(stat -c "%a" "/SWAPFILE") != "600" ]]; then
    chmod 600 /SWAPFILE
fi

%if !0%{?suse_version}
sed -i 's/su wwwrun www/su apache apache/' /etc/logrotate.d/susemanager-tools
%endif

%posttrans

%postun
%if 0%{?suse_version}
%{insserv_cleanup}
%endif
# Cleanup
sed -i '/You can access .* via https:\/\//d' /tmp/motd 2> /dev/null ||:


%files -f susemanager.lang
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
%config %{_sysconfdir}/slp.reg.d/susemanager.reg
%if 0%{?is_opensuse}
%{_datadir}/applications/YaST2/org.uyuni-project.yast2.Uyuni.desktop
%else
%{_datadir}/applications/YaST2/com.suse.yast2.SUSEManager.desktop
%endif
%attr(775,%{salt_user},susemanager) %dir %{wwwroot}/os-images/
%if 0%{?suse_version} > 1320
%{_prefix}/lib/firewalld/services/suse-manager-server.xml
%else
%{_sysconfdir}/firewalld/services/suse-manager-server.xml
%endif

%files tools
%defattr(-,root,root,-)
%dir %{pythonsmroot}
%dir %{pythonsmroot}/susemanager
%dir %{_prefix}/share/rhn/
%dir %{_datadir}/susemanager
%dir %{wwwdocroot}/pub
%dir %{wwwdocroot}/pub/repositories
%dir %{wwwdocroot}/pub/repositories/empty
%dir %{wwwdocroot}/pub/repositories/empty/repodata
%dir %{wwwdocroot}/pub/repositories/empty-deb
%config(noreplace) %{_sysconfdir}/logrotate.d/susemanager-tools
%{_prefix}/share/rhn/config-defaults/rhn_*.conf
%attr(0755,root,root) %{_bindir}/mgr-salt-ssh
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
%if 0%{?rhel} || 0%{?build_py3}
%{pythonsmroot}/susemanager/__pycache__/
%{pythonsmroot}/susemanager/mgr_sync/__pycache__/
%{_datadir}/susemanager/__pycache__/
%endif
%{_mandir}/man8/mgr-sync.8*
%{wwwdocroot}/pub/repositories/empty/repodata/*.xml*
%{wwwdocroot}/pub/repositories/empty-deb/Packages
%{wwwdocroot}/pub/repositories/empty-deb/Release

%changelog
