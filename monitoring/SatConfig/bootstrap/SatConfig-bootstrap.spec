Name:         SatConfig-bootstrap
Version:      2.3.0
Release:      1%{?dist}
Summary:      Satellite Configuration System - satellite id installer
URL:          https://fedorahosted.org/spacewalk
Source0:      https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
BuildArch:    noarch
Requires:     perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
Requires(pre): nocpulse-common
Group:        Development/Libraries
License:      GPLv2
Buildroot:    %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description
NOCpulse provides application, network, systems and transaction monitoring,
coupled with a comprehensive reporting system including availability,
historical and trending reports in an easy-to-use browser interface.

SatConfig-bootstrap queries NOCpulse for the contents of the netsaintId file.

%prep
%setup -q

%build
#Nothing to build

%install
rm -rf $RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -m 755 npBootstrap.pl $RPM_BUILD_ROOT%{_bindir}

%files
%{_bindir}/npBootstrap.pl

%clean
rm -rf $RPM_BUILD_ROOT

%changelog
* Wed Aug 05 2009 Jan Pazdziora 1.11.5-1
- 493060 - do not send email "RHN Monitoring Scout started" by default
  (msuchy@redhat.com)

* Mon May 11 2009 Miroslav Suchý <msuchy@redhat.com> 1.11.4-1
- utilize NOCpulse::Config for get scout_shared_key

* Thu Apr 23 2009 jesus m. rodriguez <jesusr@redhat.com> 1.11.3-1
- change Source0 to point to fedorahosted.org (msuchy@redhat.com)

* Mon Oct 20 2008 Miroslav Suchý <msuchy@redhat.com> 1.11.2-1
- 467441 - fix namespace

* Tue Sep 23 2008 Miroslav Suchý <msuchy@redhat.com> 1.11.1-1
- spec cleanup for Fedora

* Thu Jun 19 2008 Miroslav Suchy <msuchy@redhat.com>
- migrating nocpulse home dir (BZ 202614)

