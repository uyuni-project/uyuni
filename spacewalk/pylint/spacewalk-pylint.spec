Name:		spacewalk-pylint
Version:	0.2.2
Release:	1%{?dist}
Summary:	Pylint configuration for spacewalk python packages

Group:		Development/Debuggers
License:	GPLv2+
URL:		https://fedorahosted.org/spacewalk
Source0:	https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
BuildRoot:	%(mktemp -ud %{_tmppath}/%{name}-%{version}-%{release}-XXXXXX)
BuildArch:	noarch

Requires:	pylint
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
rm -rf $RPM_BUILD_ROOT
install -d -m 755 %{buildroot}/%{_bindir}
install -p -m 755 spacewalk-pylint %{buildroot}/%{_bindir}/
install -d -m 755 %{buildroot}/%{_sysconfdir}
install -p -m 644 spacewalk-pylint.rc %{buildroot}/%{_sysconfdir}/
%if 0%{?suse_version} != 1010
mkdir -p %{buildroot}/%{_mandir}/man8
install -m 644 spacewalk-pylint.8 %{buildroot}/%{_mandir}/man8
%endif

%clean
rm -rf $RPM_BUILD_ROOT


%files
%defattr(-,root,root,-)
%{_bindir}/spacewalk-pylint
%config(noreplace)  %{_sysconfdir}/spacewalk-pylint.rc
%if 0%{?suse_version} != 1010
%doc %{_mandir}/man8/spacewalk-pylint.8*
%endif

%changelog
* Wed Feb 15 2012 Michael Mraka <michael.mraka@redhat.com> 0.2-1
- made it noarch package

* Wed Feb 15 2012 Michael Mraka <michael.mraka@redhat.com> 0.1-1
- new package built with tito

