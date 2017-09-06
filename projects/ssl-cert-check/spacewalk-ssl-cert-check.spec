Name: spacewalk-ssl-cert-check
Epoch:	 1
Version: 2.6
Release: 1%{?dist}
Summary: Check ssl certs for impending expiration
Group:   Applications/System
License: GPLv2
URL:     https://github.com/spacewalkproject/spacewalk
Source0: https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch: noarch
Requires:  /etc/cron.daily/certwatch
Obsoletes: rhn-ssl-cert-check < %{epoch}:%{version}
Provides:  rhn-ssl-cert-check = %{epoch}:%{version}

%description 
Runs a check once a day to see if the ssl certificates installed on this
server are expected to expire in the next 30 days, and if so, email the 
administrator.

%prep
%setup -q

%build
# Nothing to do

%install
rm -rf $RPM_BUILD_ROOT

install -d $RPM_BUILD_ROOT/etc/cron.daily

install -m755 rhn-ssl-cert-check $RPM_BUILD_ROOT/%{_sysconfdir}/cron.daily/rhn-ssl-cert-check

%clean
rm -rf $RPM_BUILD_ROOT

%files
%attr(0755,root,root) %{_sysconfdir}/cron.daily/rhn-ssl-cert-check
%doc LICENSE

%changelog
* Mon Jul 31 2017 Michael Mraka <michael.mraka@redhat.com> 2.6-1
- move version and release before sources

* Mon Jul 17 2017 Jan Dobes 2.5-1
- Updated links to github in spec files
- Migrating Fedorahosted to GitHub

* Mon Jan 12 2015 Matej Kollar <mkollar@redhat.com> 2.4-1
- Getting rid of Tabs and trailing spaces in LICENSE, COPYING, and README files
- Purging %%changelog entries preceding Spacewalk 1.0, in active packages.

* Tue Oct 30 2012 Jan Pazdziora 2.3-1
- Update the copyright year.

* Thu Jun 07 2012 Jan Pazdziora 2.2-1
- 788972 - for multiple recipient email addresses, join them with comma.
- %%defattr is not needed since rpm 4.4

* Tue Nov 02 2010 Jan Pazdziora 2.1-1
- Update copyright years in the rest of the repo.

