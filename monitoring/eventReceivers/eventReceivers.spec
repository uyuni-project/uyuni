Name:         eventReceivers
Source0:      https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
Version:      2.20.18.1
Release:      1%{?dist}
Summary:      Command Center Event Receivers
URL:          https://fedorahosted.org/spacewalk
BuildArch:    noarch
Group:        Applications/Internet
License:      GPLv2
Buildroot:    %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
%if 0%{?suse_version}
Requires:     perl = %{perl_version}
%else
Requires:     perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
%endif

# smtpdaemon or mailx. I picked up smtpdaemon
%if 0%{?suse_version}
Requires:     smtp_daemon
%else
Requires:     smtpdaemon
%endif

%description
NOCpulse provides application, network, systems and transaction monitoring,
coupled with a comprehensive reporting system including availability,
historical and trending reports in an easy-to-use browser interface.

This package contains handler, which receive events from scouts.

%prep
%setup -q

%build
#Nothing to build

%install
rm -rf $RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse
install -m644 *.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse

%{_fixperms} $RPM_BUILD_ROOT/*

%files
%defattr(-,root,root)
%{perl_vendorlib}/*
%doc LICENSE

%clean
rm -rf $RPM_BUILD_ROOT

%changelog
* Thu Mar 21 2013 Jan Pazdziora 2.20.18-1
- 922250 - use $r->useragent_ip on Apache 2.4, $r->connection->remote_ip
  otherwise.
- %%defattr is not needed since rpm 4.4

* Fri Mar 02 2012 Jan Pazdziora 2.20.17-1
- Update the copyright year info.

* Thu Jan 26 2012 Jan Pazdziora 2.20.16-1
- Rollback the session to avoid IDLE in transaction in PerlAccessHandler
  NOCpulse::MonitoringAccessHandler.

* Wed Oct 13 2010 Jan Pazdziora 2.20.15-1
- 484612 - correctly set From using Mail::Send (msuchy@redhat.com)

