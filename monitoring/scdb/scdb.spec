Name:         scdb
Source0:      https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
Version:      2.2.0
Release:      1%{?dist}
Summary:      State Change Database
URL:          https://fedorahosted.org/spacewalk
BuildArch:    noarch
Group:        Applications/Databases
License:      GPLv2
BuildRoot:    %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Requires:     nocpulse-common
%if 0%{?suse_version}
BuildRequires: nocpulse-common
%define apache_user wwwrun
%define apache_group www
%else
%define apache_user apache
%define apache_group apache
%endif

%description
NOCpulse provides application, network, systems and transaction monitoring,
coupled with a comprehensive reporting system including availability,
historical and trending reports in an easy-to-use browser interface.

This package contains State Change Database.

%prep
%setup -q

%build
#Nothing to build

%install
rm -rf $RPM_BUILD_ROOT
# Make sure the 'bdb' directory exists
mkdir -p  $RPM_BUILD_ROOT/var/lib/nocpulse/scdb/bdb

# Copy the module
mkdir -p $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse
install -m 644 SCDB.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse

%{_fixperms} $RPM_BUILD_ROOT/*

%files
%defattr(-,root,root,-)
%dir %{_localstatedir}/lib/nocpulse
%dir %attr(-, nocpulse,nocpulse) %{_localstatedir}/lib/nocpulse
%dir %{_localstatedir}/lib/nocpulse/scdb
%attr(755,%{apache_user},%{apache_group}) %dir %{_localstatedir}/lib/nocpulse/scdb/bdb
%{perl_vendorlib}/NOCpulse/*

%clean
rm -rf $RPM_BUILD_ROOT

%changelog
* Tue Feb 07 2012 Jan Pazdziora 1.15.8-1
- scdb: use /var/lib/nocpulse directory (mzazrivec@redhat.com)

* Thu Feb 02 2012 Jan Pazdziora 1.15.7-1
- removing non-working debugging scripts (michael.mraka@redhat.com)

