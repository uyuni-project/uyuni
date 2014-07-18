
Summary:      Meta-package that pulls in all of the Spacewalk monitoring packages
Name:         spacewalk-proxy-monitoring
Source0:      https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
Version:      2.3.0
Release:      1%{?dist}
URL:          https://fedorahosted.org/spacewalk
License:      GPLv2
Group:        Applications/System
BuildArch:    noarch
Obsoletes:    rhns-proxy-monitoring < 5.3.0
Provides:     rhns-proxy-monitoring

BuildRequires: SatConfig-general

#we need to be sure that nocpulse home is correct
Requires(pre): nocpulse-common

Requires: nocpulse-db-perl 
Requires: eventReceivers 
Requires: MessageQueue 
Requires: NOCpulsePlugins 
Requires: NPalert 
Requires: perl-NOCpulse-CLAC 
Requires: perl-NOCpulse-Debug 
Requires: perl-NOCpulse-Gritch 
Requires: perl-NOCpulse-Object 
Requires: perl-NOCpulse-OracleDB 
Requires: perl-NOCpulse-PersistentConnection 
Requires: perl-NOCpulse-Probe 
Requires: perl-NOCpulse-ProcessPool 
Requires: perl-NOCpulse-Scheduler 
Requires: perl-NOCpulse-SetID 
Requires: perl-NOCpulse-Utils 
Requires: ProgAGoGo 
Requires: SatConfig-bootstrap 
Requires: SatConfig-bootstrap-server 
Requires: SatConfig-cluster 
Requires: SatConfig-general 
Requires: SatConfig-generator 
Requires: SatConfig-installer 
Requires: SatConfig-spread 
Requires: scdb 
Requires: SNMPAlerts
Requires: SputLite-client 
Requires: SputLite-server 
Requires: ssl_bridge 
Requires: status_log_acceptor 
Requires: tsdb 
%if 0%{?suse_version}
Requires: apache2-mod_perl
%else
Requires: mod_perl
Requires: spacewalk-monitoring-selinux
%endif
Buildroot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Obsoletes: np-config < 2.111.0
Provides:  np-config = %{version}
Obsoletes: rhn-modperl < 1.30
Provides:  rhn-modperl = %{version}

%description
NOCpulse provides application, network, systems and transaction monitoring,
coupled with a comprehensive reporting system including availability,
historical and trending reports in an easy-to-use browser interface.

This package pulls in all of the Spacewalk Monitoring packages, including all 
MOC and Scout functionality.

%prep
%setup -q

%build
# nothing to do

%install
rm -Rf $RPM_BUILD_ROOT

#/etc/satname needs to be created on the proxy box, with the contents of '1'       
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}
mkdir -p $RPM_BUILD_ROOT/%{_sbindir}
mkdir -p $RPM_BUILD_ROOT/%{_initrddir}

ln -s /etc/rc.d/np.d/sysvStep $RPM_BUILD_ROOT/%{_sbindir}/MonitoringScout

install satname $RPM_BUILD_ROOT%{_sysconfdir}/satname
install MonitoringScout $RPM_BUILD_ROOT%{_initrddir}

ln -sf ../../etc/init.d/MonitoringScout $RPM_BUILD_ROOT/%{_sbindir}/rcMonitoringScout

%post
%if 0%{?suse_version}
%{fillup_and_insserv MonitoringScout}
%else
/sbin/chkconfig --add MonitoringScout
%endif

%preun
%if 0%{?suse_version}
%stop_on_removal MonitoringScout
%else
if [ $1 = 0 ] ; then
    /sbin/service MonitoringScout stop >/dev/null 2>&1
    /sbin/chkconfig --del MonitoringScout
fi
%endif

%postun
%if 0%{?suse_version}
%{insserv_cleanup}
%endif

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-, root,root,-)
%config %{_sysconfdir}/satname
%{_initrddir}/*
%{_sbindir}/*
%doc README

%changelog
* Wed Jul 17 2013 Tomas Kasparek <tkasparek@redhat.com> 2.0.1-1
- Bumping package versions for 2.0.
- Bumping package versions for 1.9
- Purging %%changelog entries preceding Spacewalk 1.0, in active packages.
- Bumping package versions for 1.9.
- %%defattr is not needed since rpm 4.4
- Bumping package versions for 1.8.
- Bumping package versions for 1.7.

* Fri Jul 22 2011 Jan Pazdziora 1.6.1-1
- We only support version 5 and newer of RHEL, removing conditions for old
  versions.

* Fri Oct 08 2010 Jan Pazdziora 1.2.1-1
- Since the package SatConfig-dbsynch is gone, remove dependencies that were
  requiring it.

* Mon Apr 19 2010 Michael Mraka <michael.mraka@redhat.com> 1.1.1-1
- bumping spec files to 1.1 packages

