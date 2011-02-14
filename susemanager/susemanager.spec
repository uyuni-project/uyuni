Name:           susemanager
Version:        1.2.0
Release:        1%{?dist}
Summary:        SUSE Manager specific scripts
Group:          Applications/System
License:        GPLv2
URL:            http://www.novell.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
#BuildArch:      noarch
BuildRequires:  python-devel
PreReq:         %fillup_prereq %insserv_prereq
Requires:       dialog
Requires:       spacewalk-setup spacewalk-admin cobbler spacewalk-schema
Requires:       rsync less
Requires:       susemanager-tools
# needed for sqlplus
Requires:       oracle-xe-univ
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
make -C sm-register all
make -C ncc-sync all

%install
mkdir -p %{buildroot}/%{_prefix}/lib/susemanager/bin/
install -m 0755 bin/*.sh %{buildroot}/%{_prefix}/lib/susemanager/bin/

mkdir -p %{buildroot}/%{_sysconfdir}/rhn/default/
mkdir -p %{buildroot}/%{_sysconfdir}/sysconfig/SuSEfirewall2.d/services
mkdir -p %{buildroot}/%{_sysconfdir}/init.d
install -m 0644 rhn-conf/rhn_server_susemanager.conf %{buildroot}/%{_sysconfdir}/rhn/default/
install -m 0644 etc/sysconfig/SuSEfirewall2.d/services/suse-manager-server %{buildroot}/%{_sysconfdir}/sysconfig/SuSEfirewall2.d/services/
install -m 755 etc/init.d/susemanager %{buildroot}/%{_sysconfdir}/init.d
make -C sm-register install PREFIX=$RPM_BUILD_ROOT
make -C ncc-sync install PREFIX=$RPM_BUILD_ROOT
mkdir -p %{buildroot}/%{_sbindir}/
install -m 0755 sm-register/mgr-register.py %{buildroot}/%{_sbindir}/mgr-register
install -m 0755 ncc-sync/mgr-ncc-sync.py %{buildroot}/%{_sbindir}/mgr-ncc-sync

# YaST configuration
mkdir -p %{buildroot}%{_datadir}/YaST2/clients
install -m 0644 yast/*.ycp %{buildroot}%{_datadir}/YaST2/clients

%clean
rm -rf %{buildroot}

%post
%{fillup_and_insserv susemanager}
if [ -f /etc/sysconfig/atftpd ]; then
  . /etc/sysconfig/atftpd
  if [ $ATFTPD_DIRECTORY = "/tftpboot" ]; then
    sysconf_addword -r /etc/sysconfig/atftpd ATFTPD_DIRECTORY "/tftpboot"
    sysconf_addword /etc/sysconfig/atftpd ATFTPD_DIRECTORY "/srv/tftpboot"
    mkdir -p /srv/tftpboot
  fi
fi

%postun
%{insserv_cleanup}

%files
%defattr(-,root,root,-)
%doc doc/* Changes license.txt congratulate.txt
%dir %{_prefix}/lib/susemanager
%dir %{_prefix}/lib/susemanager/bin/
%dir %{_datadir}/YaST2
%dir %{_datadir}/YaST2/clients
%{_prefix}/lib/susemanager/bin/*
%{_datadir}/YaST2/clients/*.ycp
%config %{_sysconfdir}/sysconfig/SuSEfirewall2.d/services/suse-manager-server
%{_sysconfdir}/init.d/susemanager

%files tools
%defattr(-,root,root,-)
%dir %{pythonsmroot}
%dir %{pythonsmroot}/susemanager
%dir %{_sysconfdir}/rhn
%dir %{_sysconfdir}/rhn/default
%config %{_sysconfdir}/rhn/default/rhn_*.conf
%attr(0755,root,root) %{_sbindir}/mgr-register
%attr(0755,root,root) %{_sbindir}/mgr-ncc-sync
%{pythonsmroot}/susemanager/__init__.py*
%{pythonsmroot}/susemanager/suseLib.py*
%{pythonsmroot}/susemanager/mgr_register.py*
%{pythonsmroot}/susemanager/mgr_ncc_sync_lib.py*


%changelog

