#
# spec file for package spacewalk-ssl-cert-check
#
# Copyright (c) 2018 SUSE LINUX GmbH, Nuernberg, Germany.
# Copyright (c) 2008-2018 Red Hat, Inc.
#
# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon. The license for this file, and modifications and additions to the
# file, is the same license as for the pristine package itself (unless the
# license for the pristine package is not an Open Source License, in which
# case the license is the MIT License). An "Open Source License" is a
# license that conforms to the Open Source Definition (Version 1.9)
# published by the Open Source Initiative.

# Please submit bugfixes or comments via http://bugs.opensuse.org/
#


Name:           spacewalk-ssl-cert-check
Version:        4.0.1
Release:        1%{?dist}
Summary:        Check ssl certs for impending expiration
License:        GPL-2.0-only
Group:          Applications/System
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
Source1:        %{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Obsoletes:      rhn-ssl-cert-check < %{version}
Provides:       rhn-ssl-cert-check = %{version}
Requires:       cron
Requires:       python-cryptography
Requires:       python-setuptools

%description
Runs a check once a day to see if the ssl certificates installed on this
server are expected to expire in the next 30 days, and if so, send a
notification

%prep
%setup -q

%build
# Nothing to do

%install

install -d $RPM_BUILD_ROOT/etc/cron.daily
install -d $RPM_BUILD_ROOT/etc/sysconfig/rhn
install -d $RPM_BUILD_ROOT/%{_bindir}
install -d $RPM_BUILD_ROOT%{_mandir}/man8/

install -m755 cron.ssl-cert-check $RPM_BUILD_ROOT/%{_sysconfdir}/cron.daily/suse.de-ssl-cert-check
install -m644 sysconfig.ssl-cert-check $RPM_BUILD_ROOT/%{_sysconfdir}/sysconfig/rhn/ssl-cert-check
install -m755 ssl-cert-check $RPM_BUILD_ROOT/%{_bindir}/ssl-cert-check
install -m644 ssl-cert-check.8 $RPM_BUILD_ROOT%{_mandir}/man8/

%files
%defattr(-,root,root,-)
%dir %{_sysconfdir}/sysconfig/rhn
%attr(0755,root,root) %{_sysconfdir}/cron.daily/suse.de-ssl-cert-check
%attr(0755,root,root) %{_bindir}/ssl-cert-check
%{_mandir}/man8/ssl-cert-check.*
%config %{_sysconfdir}/sysconfig/rhn/ssl-cert-check
%doc LICENSE

%changelog
