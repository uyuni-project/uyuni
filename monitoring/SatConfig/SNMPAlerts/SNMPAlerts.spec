%if 0%{?suse_version}
%define cgi_bin        /srv/www/cgi-bin
%define cgi_mod_perl   /srv/www/cgi-mod-perl
%else
%define cgi_bin        %{_datadir}/nocpulse/cgi-bin
%define cgi_mod_perl   %{_datadir}/nocpulse/cgi-mod-perl
%endif
Name:         SNMPAlerts
Version:      2.2.0
Release:      1%{?dist}
Summary:      Download and clear SNMP alerts from the database
URL:          https://fedorahosted.org/spacewalk
Source0:      https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
BuildArch:    noarch
Requires:     perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
Group:        Development/Libraries
License:      GPLv2
Buildroot:    %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description
NOCpulse provides application, network, systems and transaction monitoring,
coupled with a comprehensive reporting system including availability,
historical and trending reports in an easy-to-use browser interface.

This package provides ability to download and clear SNMP alerts from the 
database.

%prep
%setup -q

%build
#Nothing to build

%install
rm -rf $RPM_BUILD_ROOT

# CGI bin and mod-perl bin
mkdir -p $RPM_BUILD_ROOT%cgi_mod_perl
install -m 555 fetch_snmp_alerts.cgi $RPM_BUILD_ROOT%cgi_mod_perl

%{_fixperms} $RPM_BUILD_ROOT/*

%files
%cgi_mod_perl/*
%if 0%{?suse_version}
%dir %cgi_mod_perl
%endif

%clean
rm -rf $RPM_BUILD_ROOT

%changelog
* Fri Dec 09 2011 Jan Pazdziora 0.5.7-1
- replace synonyms with real table name (mc@suse.de)

* Sat Nov 20 2010 Miroslav Suchý <msuchy@redhat.com> 0.5.6-1
- 474591 - move web data to /usr/share/nocpulse (msuchy@redhat.com)

