Name: spacewalk-proxy-docs
Summary: Spacewalk Proxy Server Documentation
Version: 2.8.2.2
Release: 1%{?dist}
Group: Applications/Internet
License: OPL-1.0
URL:     https://github.com/spacewalkproject/spacewalk
Source0: https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch: noarch
BuildRequires: susemanager-advanced-topics_en-pdf
BuildRequires: susemanager-reference_en-pdf
BuildRequires: xerces-j2
Obsoletes: rhns-proxy-docs < 5.3.0
Provides: rhns-proxy-docs = 5.3.0
ExcludeArch: aarch64

%description
This package includes the SUSE Manager Proxy Quick Start guide
Also included are the Client Configuration and Enterprise
User Reference guides.

%prep
%setup -q

%build
#nothing to do here

%install
install -m 755 -d $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT/%_defaultdocdir/%{name}

for book in reference advanced-topics; do
  pdf="%{_datadir}/doc/manual/susemanager-${book}_en-pdf/susemanager-${book}_en.pdf"
  test -f $pdf && \
    cp -av $pdf $RPM_BUILD_ROOT/%_defaultdocdir/%{name}/
done

install -m 644 LICENSE $RPM_BUILD_ROOT/%_defaultdocdir/%{name}/
install -m 644 squid.conf.sample $RPM_BUILD_ROOT/%_defaultdocdir/%{name}/

%files
%defattr(-,root,root)
%docdir %_defaultdocdir/%{name}
%dir %_defaultdocdir/%{name}
%_defaultdocdir/%{name}/*

%changelog
* Fri Feb 09 2018 Michael Mraka <michael.mraka@redhat.com> 2.8.2-1
- remove install/clean section initial cleanup
- removed Group from specfile
- removed BuildRoot from specfiles

* Wed Sep 06 2017 Michael Mraka <michael.mraka@redhat.com> 2.8.1-1
- purged changelog entries for Spacewalk 2.0 and older
- Bumping package versions for 2.8.

* Tue Jul 18 2017 Michael Mraka <michael.mraka@redhat.com> 2.7.2-1
- move version and release before sources

* Mon Jul 17 2017 Jan Dobes 2.7.1-1
- Updated links to github in spec files
- Migrating Fedorahosted to GitHub
- Bumping package versions for 2.7.
- Bumping package versions for 2.6.
- Bumping package versions for 2.5.
- Bumping package versions for 2.4.

* Wed Jan 14 2015 Matej Kollar <mkollar@redhat.com> 2.3.1-1
- Getting rid of Tabs and trailing spaces in LICENSE, COPYING, and README files
- Bumping package versions for 2.3.
- Bumping package versions for 2.2.
- Bumping package versions for 2.1.

