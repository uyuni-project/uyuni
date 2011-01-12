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

%package -n sm-ncc-sync-data
Summary:    data files for NCC data configuration
Group:      Productivity/Other

%description -n sm-ncc-sync-data
This package contains data files with NCC information

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

%install
mkdir -p %{buildroot}/%{_prefix}/lib/susemanager/bin/
install -m 0755 bin/*.sh %{buildroot}/%{_prefix}/lib/susemanager/bin/

mkdir -p %{buildroot}/%{_sysconfdir}/rhn/default/
install -m 0644 rhn-conf/rhn_server_susemanager.conf %{buildroot}/%{_sysconfdir}/rhn/default/
make -C sm-register install PREFIX=$RPM_BUILD_ROOT
mkdir -p %{buildroot}/%{_sbindir}/
install -m 0755 sm-register/sm-register.py %{buildroot}/%{_sbindir}/sm-register
install -m 0755 ncc-sync/sm-ncc-sync.py %{buildroot}/%{_sbindir}/sm-ncc-sync

mkdir -p %{buildroot}/usr/share/susemanager
install -m 0644 ncc-sync/channel_families.xml %{buildroot}/usr/share/susemanager/channel_families.xml
install -m 0644 ncc-sync/channels.xml         %{buildroot}/usr/share/susemanager/channels.xml

%clean
rm -rf %{buildroot}

%post

if [ -f /etc/sysconfig/atftpd ]; then
  . /etc/sysconfig/atftpd
  if [ $ATFTPD_DIRECTORY = "/tftpboot" ]; then
    sysconf_addword -r /etc/sysconfig/atftpd ATFTPD_DIRECTORY "/tftpboot"
    sysconf_addword /etc/sysconfig/atftpd ATFTPD_DIRECTORY "/srv/tftpboot"
    mkdir -p /srv/tftpboot
  fi
fi

%files
%defattr(-,root,root,-)
%doc doc/* Changes license.txt
%dir %{_prefix}/lib/susemanager
%dir %{_prefix}/lib/susemanager/bin/
%{_prefix}/lib/susemanager/bin/*

%files -n sm-ncc-sync-data
%defattr(-,root,root,-)
%dir /usr/share/susemanager
/usr/share/susemanager/channel_families.xml
/usr/share/susemanager/channels.xml

%files tools
%defattr(-,root,root,-)
%dir %{pythonsmroot}
%dir %{pythonsmroot}/susemanager
%dir %{_sysconfdir}/rhn
%dir %{_sysconfdir}/rhn/default
%config %{_sysconfdir}/rhn/default/rhn_*.conf
%attr(0755,root,root) %{_sbindir}/sm-register
%attr(0755,root,root) %{_sbindir}/sm-ncc-sync
%{pythonsmroot}/susemanager/__init__.py*
%{pythonsmroot}/susemanager/suseLib.py*
%{pythonsmroot}/susemanager/smregister.py*


%changelog

