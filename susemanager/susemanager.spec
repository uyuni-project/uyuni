Name:           susemanager
Version:        3.2.16
Release:        1%{?dist}
Summary:        SUSE Manager specific scripts
Group:          Applications/System
License:        GPLv2
URL:            http://www.suse.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
#BuildArch:      noarch - not noarch because of ifarch usage!!!!
BuildRequires:  python-devel

# check section
BuildRequires:  python-mock
BuildRequires:  python-curl
BuildRequires:  spacewalk-backend >= 1.7.38.20
BuildRequires:  spacewalk-backend-server
BuildRequires:  spacewalk-backend-sql-postgresql
BuildRequires:  suseRegisterInfo
BuildRequires:  pyxml

PreReq:         %fillup_prereq %insserv_prereq tftp(server) postgresql-init
Requires(pre):  tomcat salt
Requires:       openslp-server
Requires:       spacewalk-setup
Requires:       spacewalk-admin
Requires:       cobbler
%ifarch %ix86 x86_64
Requires:       syslinux
%endif
%ifarch s390x ppc64le
Requires:       syslinux-x86_64
%endif
Requires:       spacewalk-schema
Requires:       rsync less
Requires:       susemanager-tools
# migration.sh need either sqlplus or psql
Requires:       spacewalk-db-virtual
Requires:       susemanager-branding
# yast module dependency
Requires:       yast2-users
Requires:       SuSEfirewall2
Requires:       postfix
# mgr-setup want to call mksubvolume
Requires:       snapper
%{!?python_sitelib: %define python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib()")}
%global pythonsmroot %{python_sitelib}/spacewalk


%description
A collection of scripts for managing SUSE Manager's initial
setup tasks, re-installation, upgrades and managing.

%package tools
Summary:    SUSE Manager Tools
Group:      Productivity/Other
Requires:       suseRegisterInfo
Requires:       susemanager-build-keys
Requires:       susemanager-sync-data
Requires:       spacewalk-backend >= 2.1.55.11
Requires:       spacewalk-backend-sql
Requires:       python
Requires:       createrepo
Requires:       python-argparse
Requires:       python-configobj
BuildRequires:  python-configobj
Requires:       python-enum34
BuildRequires:  python-enum34
BuildRequires:  docbook-utils

%description tools
This package contains SUSE Manager tools

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}/%{_prefix}/lib/susemanager/bin/
mkdir -p %{buildroot}/%{_prefix}/lib/susemanager/hooks/
install -m 0755 bin/* %{buildroot}/%{_prefix}/lib/susemanager/bin/
ln -s mgr-setup %{buildroot}/%{_prefix}/lib/susemanager/bin/migration.sh
ln -s pg-migrate-94-to-96.sh %{buildroot}/%{_prefix}/lib/susemanager/bin/pg-migrate.sh

mkdir -p %{buildroot}/%{_prefix}/share/rhn/config-defaults
mkdir -p %{buildroot}/%{_sysconfdir}/sysconfig/SuSEfirewall2.d/services
mkdir -p %{buildroot}/%{_sysconfdir}/init.d
mkdir -p %{buildroot}/%{_sysconfdir}/slp.reg.d
mkdir -p %{buildroot}/%{_sysconfdir}/logrotate.d
install -m 0644 rhn-conf/rhn_server_susemanager.conf %{buildroot}/%{_prefix}/share/rhn/config-defaults
install -m 0644 etc/sysconfig/SuSEfirewall2.d/services/suse-manager-server %{buildroot}/%{_sysconfdir}/sysconfig/SuSEfirewall2.d/services/
install -m 0644 etc/logrotate.d/susemanager-tools %{buildroot}/%{_sysconfdir}/logrotate.d
install -m 0644 etc/slp.reg.d/susemanager.reg %{buildroot}/%{_sysconfdir}/slp.reg.d
install -m 755 etc/init.d/susemanager %{buildroot}/%{_sysconfdir}/init.d
make -C src install PREFIX=$RPM_BUILD_ROOT MANDIR=%{_mandir}
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

%check
# we need to build a fake python dir. python did not work with
# two site-package/spacewalk dirs having different content
mkdir -p /var/tmp/fakepython/spacewalk
cp -a %{python_sitelib}/spacewalk/* /var/tmp/fakepython/spacewalk/
cp -a %{buildroot}%{python_sitelib}/spacewalk/* /var/tmp/fakepython/spacewalk/
export PYTHONPATH=/var/tmp/fakepython/:%{_datadir}/rhn
make -f Makefile.susemanager unittest
unset PYTHONPATH
rm -rf /var/tmp/fakepython
pushd %{buildroot}
find -name '*.py' -print0 | xargs -0 python %py_libdir/py_compile.py
popd

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
%config %{_sysconfdir}/sysconfig/SuSEfirewall2.d/services/suse-manager-server
%config %{_sysconfdir}/slp.reg.d/susemanager.reg
%{_sysconfdir}/init.d/susemanager
%{_datadir}/applications/YaST2/susemanager_setup.desktop
%attr(775,salt,susemanager) %dir /srv/www/os-images/

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
