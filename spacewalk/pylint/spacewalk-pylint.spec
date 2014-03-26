Name:		spacewalk-pylint
Version:	0.12.1
Release:	1%{?dist}
Summary:	Pylint configuration for spacewalk python packages

Group:		Development/Debuggers
License:	GPLv2
URL:		https://fedorahosted.org/spacewalk
Source0:	https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
BuildRoot:	%(mktemp -ud %{_tmppath}/%{name}-%{version}-%{release}-XXXXXX)
BuildArch:	noarch

%if 0%{?fedora} >= 19
Requires:	pylint > 1.0
%else
Requires:	pylint < 1.0
%endif
%if 0%{?suse_version} != 1010
BuildRequires:	asciidoc
%endif
BuildRequires:	libxslt
%if 0%{?rhel} && 0%{?rhel} < 6
BuildRequires:	docbook-style-xsl
%endif


%description
Pylint configuration fine tuned to check coding style of spacewalk python
packages.

%prep
%setup -q

%build
%if 0%{?suse_version} != 1010
a2x -d manpage -f manpage spacewalk-pylint.8.asciidoc
%endif

%install
rm -rf %{buildroot}
install -d -m 755 %{buildroot}/%{_bindir}
install -p -m 755 spacewalk-pylint %{buildroot}/%{_bindir}/
install -d -m 755 %{buildroot}/%{_sysconfdir}
install -p -m 644 spacewalk-pylint.rc %{buildroot}/%{_sysconfdir}/
%if 0%{?fedora} < 20
# new checks in pylint 1.1
sed -i '/disable=/ s/,bad-whitespace,unpacking-non-sequence//g;' \
        %{buildroot}%{_sysconfdir}/spacewalk-pylint.rc
%endif
%if 0%{?suse_version} != 1010
mkdir -p %{buildroot}/%{_mandir}/man8
install -m 644 spacewalk-pylint.8 %{buildroot}/%{_mandir}/man8
%endif

%clean
rm -rf %{buildroot}


%files
%defattr(-,root,root,-)
%{_bindir}/spacewalk-pylint
%config(noreplace)  %{_sysconfdir}/spacewalk-pylint.rc
%if 0%{?suse_version} != 1010
%doc %{_mandir}/man8/spacewalk-pylint.8*
%endif
%doc LICENSE

%changelog
* Mon Sep 30 2013 Michael Mraka <michael.mraka@redhat.com> 0.12-1
- ignore old-style-* pylint warnings for pylint-1.0

* Mon Jan 28 2013 Michael Mraka <michael.mraka@redhat.com> 0.11-1
- Revert "ignore Container implementation related warnings"

* Fri Jan 25 2013 Michael Mraka <michael.mraka@redhat.com> 0.10-1
- ignore Container implementation related warnings

* Sun Nov 11 2012 Michael Calmer <mc@suse.de> 0.9-1
- BuildRequire docbook-style-xsl only on redhat

* Wed Oct 24 2012 Michael Mraka <michael.mraka@redhat.com> 0.8-1
- Revert "put W1201 on list of ignored pylint warnings"

* Fri Aug 24 2012 Miroslav Suchý <msuchy@redhat.com> 0.7-1
- put W1201 on list of ignored pylint warnings

* Fri Aug 24 2012 Michael Mraka <michael.mraka@redhat.com> 0.6-1
- let's silence pylint on our large modules and objects

* Mon Jun 04 2012 Miroslav Suchý <msuchy@redhat.com> 0.5-1
- %%defattr is not needed since rpm 4.4 (msuchy@redhat.com)

* Wed May 16 2012 Miroslav Suchý <msuchy@redhat.com> 0.4-1
- 800899 - consistently use macros
- 800899 - include license file
- Spacewalk is released under GPLv2, lets stick to it

* Wed Mar 07 2012 Miroslav Suchý 0.3-1
- add man page
- Description lines must not exceed 80 characters
- Summary must begin with capital letter

* Wed Feb 15 2012 Michael Mraka <michael.mraka@redhat.com> 0.2-1
- made it noarch package

* Wed Feb 15 2012 Michael Mraka <michael.mraka@redhat.com> 0.1-1
- new package built with tito

