Name: spacewalk-proxy-docs
Summary: Spacewalk Proxy Server Documentation
Group: Applications/Internet
License: Open Publication
URL:     https://fedorahosted.org/spacewalk
Source0: https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
Version: 2.2.0
Release: 1%{?dist}
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch: noarch
BuildRequires: susemanager-client-config_en-pdf
BuildRequires: susemanager-proxy-quick_en-pdf
BuildRequires: susemanager-reference_en-pdf
BuildRequires: xerces-j2
Obsoletes: rhns-proxy-docs < 5.3.0
Provides: rhns-proxy-docs = 5.3.0

%description
This package includes the SUSE Manager Proxy Quick Start guide
Also included are the Client Configuration and Enterprise
User Reference guides.

%prep
%setup -q

%build
#nothing to do here

%install
rm -rf $RPM_BUILD_ROOT
install -m 755 -d $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT/%_defaultdocdir/%{name}
if [ -e %{_datadir}/doc/manual/susemanager-client-config_en-pdf/susemanager-client-config_en.pdf ]; then
cp %{_datadir}/doc/manual/susemanager-client-config_en-pdf/susemanager-client-config_en.pdf $RPM_BUILD_ROOT/%_defaultdocdir/%{name}/
else
cp susemanager-client-config_en.pdf $RPM_BUILD_ROOT/%_defaultdocdir/%{name}/
fi
if [ -e %{_datadir}/doc/manual/susemanager-proxy-quick_en-pdf/susemanager-proxy-quick_en.pdf ]; then
cp %{_datadir}/doc/manual/susemanager-proxy-quick_en-pdf/susemanager-proxy-quick_en.pdf $RPM_BUILD_ROOT/%_defaultdocdir/%{name}/
else
cp susemanager-proxy-quick_en.pdf $RPM_BUILD_ROOT/%_defaultdocdir/%{name}/
fi
if [ -e %{_datadir}/doc/manual/susemanager-reference_en-pdf/susemanager-reference_en.pdf ]; then
cp %{_datadir}/doc/manual/susemanager-reference_en-pdf/susemanager-reference_en.pdf $RPM_BUILD_ROOT/%_defaultdocdir/%{name}/
else
cp susemanager-reference_en.pdf $RPM_BUILD_ROOT/%_defaultdocdir/%{name}/
fi

install -m 644 LICENSE $RPM_BUILD_ROOT/%_defaultdocdir/%{name}/
install -m 644 squid.conf.sample $RPM_BUILD_ROOT/%_defaultdocdir/%{name}/

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
%docdir %_defaultdocdir/%{name}
%dir %_defaultdocdir/%{name}
%_defaultdocdir/%{name}/*

%changelog
* Wed Jul 17 2013 Tomas Kasparek <tkasparek@redhat.com> 2.0.1-1
- Bumping package versions for 2.0.

* Mon Jun 17 2013 Michael Mraka <michael.mraka@redhat.com> 1.10.2-1
- removed old CVS/SVN version ids

* Wed Jun 12 2013 Tomas Kasparek <tkasparek@redhat.com> 1.10.1-1
- rebranding RHN Proxy to Red Hat Proxy
- Bumping package versions for 1.9
- Bumping package versions for 1.9.
- %%defattr is not needed since rpm 4.4
- Bumping package versions for 1.8.
- Bumping package versions for 1.7.
- Bumping package versions for 1.6.
- Bumping package versions for 1.5
- Bumping package versions for 1.4
- Bumping package versions for 1.3.

* Mon Apr 19 2010 Michael Mraka <michael.mraka@redhat.com> 1.1.1-1
- bumping spec files to 1.1 packages

* Fri Jan 15 2010 Michael Mraka <michael.mraka@redhat.com> 0.8.1-1
- rebuild for spacewalk 0.8

* Wed May 20 2009 Miroslav Suchy <msuchy@redhat.com> 0.6.2-1
- clarify the license. It is Open Publication instead of GPLv2

* Thu May 14 2009 Miroslav Suchy <msuchy@redhat.com> 0.6.1-1
- 497892 - create access.log on rhel5
- point source0 to fedorahosted.org
- provide versioned Provides: to Obsolete:
- make rpmlint happy
- change buildroot to recommended value
- marking documentation files as %%doc

* Tue Dec  9 2008 Michael Mraka <michael.mraka@redhat.com> 0.4.1-1
- fixed Obsoletes: rhns-* < 5.3.0

* Thu Aug  7 2008 Miroslav Suchy <msuchy@redhat.com> 0.1-2
- Rename to spacewalk-proxy-docs
- clean up spec

* Thu Apr 10 2008 Miroslav Suchy <msuchy@redhat.com>
- Isolate from rhns-proxy

