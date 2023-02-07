#
# spec file for package spacewalk-ssl-cert-check
#
# Copyright (c) 2022 SUSE LLC
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

# Please submit bugfixes or comments via https://bugs.opensuse.org/
#


%if 0%{?suse_version} > 1320 || 0%{?rhel}
%global build_py3 1
%endif

Name:           spacewalk-ssl-cert-check
Version:        4.4.1
Release:        1
Summary:        Check ssl certs for impending expiration
License:        GPL-2.0-only
Group:          Applications/System
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}-1.tar.gz
Source1:        https://raw.githubusercontent.com/uyuni-project/uyuni/%{name}-%{version}-1/projects/ssl-cert-check/%{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Obsoletes:      rhn-ssl-cert-check < %{version}
Provides:       rhn-ssl-cert-check = %{version}
%if 0%{?build_py3}
Requires:       python3-cryptography
Requires:       python3-setuptools
%else
Requires:       python-cryptography
Requires:       python-setuptools
%endif
BuildRequires:  pkgconfig(systemd)
%{?systemd_requires}

%description
Runs a check once a day to see if the ssl certificates installed on this
server are expected to expire in the next 30 days, and if so, send a
notification

%prep
%setup -q

%build
%if 0%{?build_py3}
sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' ssl-cert-check
%endif

%install

install -d $RPM_BUILD_ROOT/etc/sysconfig/rhn
install -d $RPM_BUILD_ROOT/%{_bindir}
install -d $RPM_BUILD_ROOT%{_mandir}/man8/
install -D -m 0644 %{name}.service %{buildroot}%{_unitdir}/%{name}.service
install -D -m 0644 %{name}.timer %{buildroot}%{_unitdir}/%{name}.timer
install -m644 sysconfig.ssl-cert-check $RPM_BUILD_ROOT/%{_sysconfdir}/sysconfig/rhn/ssl-cert-check
install -m755 timerscript.ssl-cert-check $RPM_BUILD_ROOT/%{_bindir}/ssl-cert-check-timerscript
install -m755 ssl-cert-check $RPM_BUILD_ROOT/%{_bindir}/ssl-cert-check
install -m644 ssl-cert-check.8 $RPM_BUILD_ROOT%{_mandir}/man8/

%pre
%if !0%{?rhel}
%service_add_pre %{name}.timer
%endif

%post
%if 0%{?rhel}
%systemd_post %{name}.timer
%else
%service_add_post %{name}.timer
%endif

%preun
%if 0%{?rhel}
%systemd_preun %{name}.timer
%else
%service_del_preun %{name}.timer
%endif

%postun
%if 0%{?rhel}
%systemd_preun %{name}.timer
%else
%service_del_postun %{name}.timer
%endif

%files
%defattr(-,root,root,-)
%dir %{_sysconfdir}/sysconfig/rhn
%{_unitdir}/%{name}.service
%{_unitdir}/%{name}.timer
%attr(0755,root,root) %{_bindir}/ssl-cert-check-timerscript
%attr(0755,root,root) %{_bindir}/ssl-cert-check
%{_mandir}/man8/ssl-cert-check.*
%config %{_sysconfdir}/sysconfig/rhn/ssl-cert-check
%{!?_licensedir:%global license %doc}
%license LICENSE

%changelog
