#
# spec file for package spacewalk-admin
#
# Copyright (c) 2019 SUSE LINUX GmbH, Nuernberg, Germany.
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

%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
%global build_py3   1
%endif

%define pythonX %{?build_py3: python3}%{!?build_py3: python}

%global rhnroot /%{_datadir}/rhn
Summary:        Various utility scripts and data files for Spacewalk installations
License:        GPL-2.0-only
Group:          Applications/Internet
Name:           spacewalk-admin
Url:            https://github.com/uyuni-project/uyuni
Version:        4.2.5
Release:        1%{?dist}
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
Requires:       lsof
Requires:       spacewalk-base
Requires:       perl(MIME::Base64)
Requires:       %{pythonX}
BuildRequires:  /usr/bin/pod2man
%if 0%{?rhel} >= 7 || 0%{?fedora} || 0%{?suse_version} >= 1210
BuildRequires:  systemd
%endif
Obsoletes:      satellite-utils < 5.3.0
Provides:       satellite-utils = 5.3.0
Obsoletes:      rhn-satellite-admin < 5.3.0
Provides:       rhn-satellite-admin = 5.3.0
BuildArch:      noarch
%if 0%{?suse_version}
BuildRequires:  spacewalk-config
%endif
BuildRequires:  uyuni-base-common
Requires(pre):  uyuni-base-common

%description
Various utility scripts and data files for Spacewalk installations.

%prep
%setup -q

%build

%install

%if 0%{?rhel} >= 7 || 0%{?fedora} || 0%{?suse_version} >= 1210
mv -f spacewalk-service.systemd spacewalk-service
make -f Makefile.admin install_systemd PREFIX=$RPM_BUILD_ROOT
%if 0%{?suse_version} >= 1210
install -m 644 spacewalk.target.SUSE $RPM_BUILD_ROOT%{_unitdir}/spacewalk.target
install -m 644 spacewalk-wait-for-tomcat.service.SUSE $RPM_BUILD_ROOT%{_unitdir}/spacewalk-wait-for-tomcat.service
%endif
%endif
make -f Makefile.admin install PREFIX=$RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT%{_mandir}/man8/
%{_bindir}/pod2man --section=8 rhn-config-schema.pl > $RPM_BUILD_ROOT%{_mandir}/man8/rhn-config-schema.pl.8
%{_bindir}/pod2man --section=8 man/spacewalk-service.pod > $RPM_BUILD_ROOT%{_mandir}/man8/spacewalk-service.8
%{_bindir}/pod2man --section=8 man/rhn-sat-restart-silent.pod > $RPM_BUILD_ROOT%{_mandir}/man8/rhn-sat-restart-silent.8
%{_bindir}/pod2man --section=8 rhn-config-satellite.pl > $RPM_BUILD_ROOT%{_mandir}/man8/rhn-config-satellite.pl.8
%{_bindir}/pod2man --section=8 man/rhn-generate-pem.pl.pod > $RPM_BUILD_ROOT%{_mandir}/man8/rhn-generate-pem.pl.8
%{_bindir}/pod2man --section=8 man/rhn-deploy-ca-cert.pl.pod > $RPM_BUILD_ROOT%{_mandir}/man8/rhn-deploy-ca-cert.pl.8
%{_bindir}/pod2man --section=8 man/rhn-install-ssl-cert.pl.pod > $RPM_BUILD_ROOT%{_mandir}/man8/rhn-install-ssl-cert.pl.8
install -p man/rhn-satellite.8 $RPM_BUILD_ROOT%{_mandir}/man8/
chmod 0644 $RPM_BUILD_ROOT%{_mandir}/man8/*.8*
ln -s spacewalk-service $RPM_BUILD_ROOT%{_sbindir}/rhn-satellite
%if 0%{?build_py3}
sed -i 's|#!/usr/bin/python|#!/usr/bin/python3|' $RPM_BUILD_ROOT/usr/bin/salt-secrets-config.py
%endif

%post
if [ -x /usr/bin/systemctl ]; then
    /usr/bin/systemctl daemon-reload || :
fi


%files
%license LICENSE
%dir %{rhnroot}
%{_sbindir}/spacewalk-startup-helper
%{_sbindir}/spacewalk-service
%{_sbindir}/rhn-satellite
%{_sbindir}/uyuni-update-config
%{_bindir}/rhn-config-satellite.pl
%{_bindir}/rhn-config-schema.pl
%{_bindir}/rhn-generate-pem.pl
%{_bindir}/rhn-deploy-ca-cert.pl
%{_bindir}/rhn-install-ssl-cert.pl
%{_bindir}/salt-secrets-config.py
%{_sbindir}/rhn-sat-restart-silent
%{_sbindir}/mgr-monitoring-ctl
%{rhnroot}/RHN-GPG-KEY
%{_mandir}/man8/rhn-satellite.8*
%{_mandir}/man8/rhn-config-schema.pl.8*
%{_mandir}/man8/spacewalk-service.8*
%{_mandir}/man8/rhn-sat-restart-silent.8*
%{_mandir}/man8/rhn-config-satellite.pl.8*
%{_mandir}/man8/rhn-generate-pem.pl.8*
%{_mandir}/man8/rhn-deploy-ca-cert.pl.8*
%{_mandir}/man8/rhn-install-ssl-cert.pl.8*
%config(noreplace) %{_sysconfdir}/rhn/service-list
%if 0%{?rhel} >= 7 || 0%{?fedora} || 0%{?suse_version} >= 1210
%{_unitdir}/spacewalk.target
%{_unitdir}/spacewalk-wait-for-tomcat.service
%{_unitdir}/spacewalk-wait-for-salt.service
%{_unitdir}/spacewalk-wait-for-jabberd.service
%{_unitdir}/spacewalk-wait-for-taskomatic.service
%{_unitdir}/salt-secrets-config.service
%{_unitdir}/mgr-websockify.service
%{_unitdir}/uyuni-check-database.service
%{_unitdir}/uyuni-update-config.service
%{_unitdir}/*.service.d
%endif

%changelog
