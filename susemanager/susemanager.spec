Name:           susemanager
Version:        1.7.6
Release:        1%{?dist}
Summary:        SUSE Manager specific scripts
Group:          Applications/System
License:        GPLv2
URL:            http://www.novell.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
#BuildArch:      noarch - not noarch because of *ycp files
BuildRequires:  python-devel

# check section
BuildRequires:  pylint
BuildRequires:  python-unittest2
BuildRequires:  python-mock
BuildRequires:  python-curl
BuildRequires:  spacewalk-backend
BuildRequires:  spacewalk-backend-server
BuildRequires:  spacewalk-backend-sql-oracle
BuildRequires:  suseRegisterInfo
BuildRequires:  pyxml

PreReq:         %fillup_prereq %insserv_prereq atftp
Requires:       spacewalk-setup spacewalk-admin cobbler spacewalk-schema
Requires:       rsync less
Requires:       susemanager-tools
# migration.sh need either sqlplus or psql
Requires:       spacewalk-db-virtual
Requires:       susemanager-branding
%{!?python_sitelib: %define python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib()")}
%global pythonsmroot %{python_sitelib}/spacewalk


%description
A collection of scripts for managing SUSE Manager's initial
setup tasks, re-installation, upgrades and managing.

%package tools
Summary:    SUSE Manager Tools
Group:      Productivity/Other
Requires:       suseRegisterInfo
Requires:       sm-ncc-sync-data
Requires:       spacewalk-backend spacewalk-backend-sql
Requires:       python

%description tools
This package contains SUSE Manager tools

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}/%{_prefix}/lib/susemanager/bin/
install -m 0755 bin/*.sh %{buildroot}/%{_prefix}/lib/susemanager/bin/

mkdir -p %{buildroot}/%{_prefix}/share/rhn/config-defaults
mkdir -p %{buildroot}/%{_sysconfdir}/sysconfig/SuSEfirewall2.d/services
mkdir -p %{buildroot}/%{_sysconfdir}/init.d
install -m 0644 rhn-conf/rhn_server_susemanager.conf %{buildroot}/%{_prefix}/share/rhn/config-defaults
install -m 0644 etc/sysconfig/SuSEfirewall2.d/services/suse-manager-server %{buildroot}/%{_sysconfdir}/sysconfig/SuSEfirewall2.d/services/
install -m 755 etc/init.d/susemanager %{buildroot}/%{_sysconfdir}/init.d
make -C src install PREFIX=$RPM_BUILD_ROOT

# YaST configuration
mkdir -p %{buildroot}%{_datadir}/YaST2/clients
mkdir -p %{buildroot}%{_datadir}/applications/YaST2
mkdir -p %{buildroot}/etc/YaST2
install -m 0644 yast/*.ycp %{buildroot}%{_datadir}/YaST2/clients
install -m 0644 yast/firstboot-susemanager.xml %{buildroot}/etc/YaST2
install -m 0644 yast/susemanager_setup.desktop %{buildroot}%{_datadir}/applications/YaST2/

%clean
rm -rf %{buildroot}

%check
# the pythonpath must have /spacewalk too because susemanager can't import
export PYTHONPATH=%{buildroot}%{python_sitelib}:%{buildroot}%{python_sitelib}/spacewalk:%{_datadir}/rhn
make -f Makefile.susemanager unittest
make -f Makefile.susemanager pylint
unset PYTHONPATH
pushd %{buildroot}
find -name '*.py' -print0 | xargs -0 python %py_libdir/py_compile.py
popd

%post
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
fi
# XE appliance overlay file created this with different user
chown root.root /etc/sysconfig

%postun
%{insserv_cleanup}

%files
%defattr(-,root,root,-)
#%doc doc/*
%dir %{_prefix}/lib/susemanager
%dir %{_prefix}/lib/susemanager/bin/
%dir /etc/YaST2
%dir %{_datadir}/YaST2
%dir %{_datadir}/YaST2/clients
%dir %{_datadir}/applications/YaST2
%{_prefix}/lib/susemanager/bin/*
%{_datadir}/YaST2/clients/*.ycp
%config /etc/YaST2/firstboot-susemanager.xml
%config %{_sysconfdir}/sysconfig/SuSEfirewall2.d/services/suse-manager-server
%{_sysconfdir}/init.d/susemanager
%{_datadir}/applications/YaST2/susemanager_setup.desktop

%files tools
%defattr(-,root,root,-)
%dir %{pythonsmroot}
%dir %{pythonsmroot}/susemanager
%dir %{_prefix}/share/rhn/
%dir %{_prefix}/share/rhn/config-defaults
%{_prefix}/share/rhn/config-defaults/rhn_*.conf
%attr(0755,root,root) %{_sbindir}/mgr-register
%attr(0755,root,root) %{_sbindir}/mgr-ncc-sync
%attr(0755,root,root) %{_sbindir}/mgr-clean-old-patchnames
%{pythonsmroot}/susemanager/__init__.py*
%{pythonsmroot}/susemanager/mgr_register.py*
%{pythonsmroot}/susemanager/mgr_ncc_sync_lib.py*
%{pythonsmroot}/susemanager/mgr_clean_old_patchnames.py*

%changelog

